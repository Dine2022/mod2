package com.concertmod.radio;

import com.concertmod.ConcertMod;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Server-side wireless redstone / radio channel manager (channels 1..64 per dimension).
 */
@Mod.EventBusSubscriber(modid = ConcertMod.MODID)
public class RadioNetwork {
    public static final int MIN_CHANNEL = 1;
    public static final int MAX_CHANNEL = 64;

    private static final Map<Integer, DimNetwork> DIMS = new HashMap<Integer, DimNetwork>();

    private static class DimNetwork {
        final Map<Integer, Set<BlockPos>> transmitters = new HashMap<Integer, Set<BlockPos>>();
        final Set<Integer> activeRemotes = new HashSet<Integer>();
        final Map<Integer, Set<BlockPos>> listeners = new HashMap<Integer, Set<BlockPos>>();

        boolean isActive(int channel) {
            if (channel < MIN_CHANNEL || channel > MAX_CHANNEL) {
                return false;
            }
            if (activeRemotes.contains(channel)) {
                return true;
            }
            Set<BlockPos> tx = transmitters.get(channel);
            return tx != null && !tx.isEmpty();
        }
    }

    public static int clampChannel(int ch) {
        if (ch < MIN_CHANNEL) {
            return MIN_CHANNEL;
        }
        if (ch > MAX_CHANNEL) {
            return MAX_CHANNEL;
        }
        return ch;
    }

    private static DimNetwork get(World world) {
        int dim = world.provider.getDimension();
        DimNetwork net = DIMS.get(dim);
        if (net == null) {
            net = new DimNetwork();
            DIMS.put(dim, net);
        }
        return net;
    }

    public static boolean isChannelActive(World world, int channel) {
        if (world == null || world.isRemote || channel < MIN_CHANNEL || channel > MAX_CHANNEL) {
            return false;
        }
        return get(world).isActive(channel);
    }

    public static void setTransmitterState(World world, BlockPos pos, int channel, boolean powered) {
        if (world == null || world.isRemote || channel < MIN_CHANNEL || channel > MAX_CHANNEL) {
            return;
        }
        DimNetwork net = get(world);
        boolean wasActive = net.isActive(channel);
        Set<BlockPos> set = net.transmitters.get(channel);
        if (powered) {
            if (set == null) {
                set = new HashSet<BlockPos>();
                net.transmitters.put(channel, set);
            }
            set.add(pos.toImmutable());
        } else if (set != null) {
            set.remove(pos);
            if (set.isEmpty()) {
                net.transmitters.remove(channel);
            }
        }
        boolean nowActive = net.isActive(channel);
        if (wasActive != nowActive) {
            notifyListeners(world, net, channel, nowActive);
        }
    }

    public static boolean toggleRemote(World world, int channel) {
        if (world == null || world.isRemote || channel < MIN_CHANNEL || channel > MAX_CHANNEL) {
            return false;
        }
        DimNetwork net = get(world);
        boolean wasActive = net.isActive(channel);
        boolean remoteNowOn;
        if (net.activeRemotes.contains(channel)) {
            net.activeRemotes.remove(channel);
            remoteNowOn = false;
        } else {
            net.activeRemotes.add(channel);
            remoteNowOn = true;
        }
        boolean nowActive = net.isActive(channel);
        if (wasActive != nowActive) {
            notifyListeners(world, net, channel, nowActive);
        }
        return remoteNowOn;
    }

    public static void registerListener(World world, BlockPos pos, int channel) {
        if (world == null || world.isRemote || channel < MIN_CHANNEL || channel > MAX_CHANNEL) {
            return;
        }
        DimNetwork net = get(world);
        Set<BlockPos> set = net.listeners.get(channel);
        if (set == null) {
            set = new HashSet<BlockPos>();
            net.listeners.put(channel, set);
        }
        set.add(pos.toImmutable());
    }

    public static void unregisterListener(World world, BlockPos pos, int channel) {
        if (world == null || world.isRemote || channel < MIN_CHANNEL || channel > MAX_CHANNEL) {
            return;
        }
        DimNetwork net = DIMS.get(world.provider.getDimension());
        if (net == null) {
            return;
        }
        Set<BlockPos> set = net.listeners.get(channel);
        if (set != null) {
            set.remove(pos);
            if (set.isEmpty()) {
                net.listeners.remove(channel);
            }
        }
    }

    private static void notifyListeners(World world, DimNetwork net, int channel, boolean active) {
        Set<BlockPos> set = net.listeners.get(channel);
        if (set == null || set.isEmpty()) {
            return;
        }
        List<BlockPos> snapshot = new ArrayList<BlockPos>(set);
        for (BlockPos pos : snapshot) {
            if (!world.isBlockLoaded(pos)) {
                continue;
            }
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof IRadioListener) {
                ((IRadioListener) te).onRadioStateChanged(active);
            } else {
                set.remove(pos);
            }
        }
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        World world = event.getWorld();
        if (world != null && !world.isRemote) {
            DIMS.remove(world.provider.getDimension());
        }
    }
}
