package com.concertmod.command;

import com.concertmod.tile.TileAimable;
import com.concertmod.tile.TileConcertDevice;
import com.concertmod.tile.TileEffect;
import com.concertmod.tile.TileSpotlight;
import com.concertmod.util.ConcertColors;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * /concert on|off [radius]
 * /concert fire [radius]                 (one shot of confetti / smoke / flame machines)
 * /concert burst <on|off> [radius]       (machines: on = shots, off = continuous stream)
 * /concert color <name|#RRGGBB> [radius]
 * /concert rainbow <on|off> [radius]
 * /concert strobe <on|off> [radius]
 * /concert mode <static|sweep|circle|spin> [radius]
 * /concert speed <0.1-10> [radius]
 * /concert aim <yaw> <pitch> [radius]
 *
 * Works on all concert devices around the sender (player or command block), default radius 64.
 */
public class CommandConcert extends CommandBase {
    private static final List<String> SUBS = Arrays.asList("on", "off", "fire", "burst", "color", "rainbow", "strobe", "mode", "speed", "aim");

    @Override
    public String getName() {
        return "concert";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/concert <on|off|fire|burst|color|rainbow|strobe|mode|speed|aim> [value] [radius]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    private static boolean parseFlag(String s) throws CommandException {
        String a = s.toLowerCase(Locale.ROOT);
        if (a.equals("on") || a.equals("true")) {
            return true;
        }
        if (a.equals("off") || a.equals("false")) {
            return false;
        }
        throw new CommandException("Expected on/off but got: " + s);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            throw new WrongUsageException(getUsage(sender));
        }
        final String sub = args[0].toLowerCase(Locale.ROOT);
        int need;
        switch (sub) {
            case "on":
            case "off":
            case "fire":
                need = 0;
                break;
            case "color":
            case "rainbow":
            case "strobe":
            case "burst":
            case "mode":
            case "speed":
                need = 1;
                break;
            case "aim":
                need = 2;
                break;
            default:
                throw new WrongUsageException(getUsage(sender));
        }
        if (args.length < 1 + need) {
            throw new WrongUsageException(getUsage(sender));
        }
        double radius = 64.0D;
        if (args.length > 1 + need) {
            radius = parseDouble(args[1 + need], 1.0D, 512.0D);
        }

        int colorValue = 0;
        boolean flag = false;
        TileSpotlight.Motion motion = null;
        float num = 0F;
        float num2 = 0F;
        switch (sub) {
            case "color":
                try {
                    colorValue = ConcertColors.parse(args[1]);
                } catch (NumberFormatException e) {
                    throw new CommandException("Unknown color: " + args[1] + " (use a name or #RRGGBB)");
                }
                break;
            case "rainbow":
            case "strobe":
            case "burst":
                flag = parseFlag(args[1]);
                break;
            case "mode":
                motion = TileSpotlight.Motion.byName(args[1]);
                if (motion == null) {
                    throw new CommandException("Unknown mode: " + args[1] + " (static, sweep, circle, spin)");
                }
                break;
            case "speed":
                num = (float) parseDouble(args[1], 0.1D, 10.0D);
                break;
            case "aim":
                num = (float) parseDouble(args[1], -360.0D, 360.0D);
                num2 = (float) parseDouble(args[2], -90.0D, 90.0D);
                break;
            default:
                break;
        }

        World world = sender.getEntityWorld();
        Vec3d c = sender.getPositionVector();
        double r2 = radius * radius;
        int count = 0;

        for (TileEntity te : new ArrayList<TileEntity>(world.loadedTileEntityList)) {
            if (!(te instanceof TileConcertDevice)) {
                continue;
            }
            if (te.getDistanceSq(c.x, c.y, c.z) > r2) {
                continue;
            }
            TileConcertDevice dev = (TileConcertDevice) te;
            boolean applied = true;
            switch (sub) {
                case "on":
                    dev.commandOn = true;
                    break;
                case "off":
                    dev.commandOn = false;
                    break;
                case "color":
                    dev.color = colorValue;
                    dev.rainbow = false;
                    break;
                case "rainbow":
                    dev.rainbow = flag;
                    break;
                case "strobe":
                    if (dev instanceof TileEffect) {
                        applied = false;
                    } else {
                        dev.strobe = flag;
                    }
                    break;
                case "burst":
                    if (dev instanceof TileEffect) {
                        ((TileEffect) dev).burstMode = flag;
                    } else {
                        applied = false;
                    }
                    break;
                case "fire":
                    if (dev instanceof TileEffect) {
                        ((TileEffect) dev).fireShot();
                    } else {
                        applied = false;
                    }
                    break;
                case "speed":
                    dev.speed = num;
                    break;
                case "mode":
                    if (dev instanceof TileSpotlight) {
                        ((TileSpotlight) dev).mode = motion;
                    } else {
                        applied = false;
                    }
                    break;
                case "aim":
                    if (dev instanceof TileAimable) {
                        ((TileAimable) dev).yaw = num;
                        ((TileAimable) dev).pitch = num2;
                    } else {
                        applied = false;
                    }
                    break;
                default:
                    applied = false;
                    break;
            }
            if (applied) {
                dev.sync();
                count++;
            }
        }
        sender.sendMessage(new TextComponentString("Concert: " + count + " device(s) updated within " + (int) radius + " blocks"));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, SUBS);
        }
        if (args.length == 2) {
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "color":
                    return getListOfStringsMatchingLastWord(args, ConcertColors.NAMES);
                case "rainbow":
                case "strobe":
                case "burst":
                    return getListOfStringsMatchingLastWord(args, "on", "off");
                case "mode":
                    return getListOfStringsMatchingLastWord(args, "static", "sweep", "circle", "spin");
                default:
                    break;
            }
        }
        return Collections.<String>emptyList();
    }
}
