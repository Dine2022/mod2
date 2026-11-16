package com.concertmod.client;

import com.concertmod.ConcertMod;
import com.concertmod.network.MessageDeviceSettings;
import com.concertmod.tile.TileAimable;
import com.concertmod.tile.TileConcertDevice;
import com.concertmod.tile.TileEffect;
import com.concertmod.tile.TileSmoke;
import com.concertmod.tile.TileSpotlight;
import com.concertmod.util.ConcertColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Control menu of a concert device: color palette + RGB sliders + rainbow, strobe, speed, and device specific
 * controls (spotlight motion and aim, effect machine mode / fire / fog type / aim).
 * Changes are sent to the server (MessageDeviceSettings) while you drag, so you see the result live.
 */
@SideOnly(Side.CLIENT)
public class GuiDevice extends GuiScreen {
    private static final int PW = 460;
    private static final int LX = 12;
    private static final int LW = 210;
    private static final int RX = 238;
    private static final int RW = 210;

    private static final int BG = 0xEE121220;
    private static final int BORDER = 0xFF3C3C66;
    private static final int ACCENT = 0xFF6D4DF0;
    private static final int BTN = 0xFF26263F;
    private static final int BTN_HOVER = 0xFF34345A;
    private static final int TRACK = 0xFF1A1A2E;
    private static final int TEXT = 0xFFEDEDF8;
    private static final int MUTED = 0xFFA4A4C4;

    private final TileConcertDevice dev;
    private final TileAimable aim;
    private final TileSpotlight spot;
    private final TileEffect eff;
    private final TileSmoke smoke;

    // local copy of the settings
    private int cr;
    private int cg;
    private int cb;
    private boolean rainbow;
    private boolean strobe;
    private boolean on;
    private boolean burst;
    private float speed;
    private float yaw;
    private float pitch;
    private int motion;
    private int fogType;

    private final List<Widget> widgets = new ArrayList<Widget>();
    private final List<Lbl> labels = new ArrayList<Lbl>();
    private Widget dragging;
    private int px;
    private int py;
    private int ph;
    private boolean dirty;

    public GuiDevice(TileConcertDevice dev) {
        this.dev = dev;
        this.aim = dev instanceof TileAimable ? (TileAimable) dev : null;
        this.spot = dev instanceof TileSpotlight ? (TileSpotlight) dev : null;
        this.eff = dev instanceof TileEffect ? (TileEffect) dev : null;
        this.smoke = dev instanceof TileSmoke ? (TileSmoke) dev : null;
        this.cr = (dev.color >> 16) & 255;
        this.cg = (dev.color >> 8) & 255;
        this.cb = dev.color & 255;
        this.rainbow = dev.rainbow;
        this.strobe = dev.strobe;
        this.on = dev.commandOn;
        this.speed = dev.speed;
        if (aim != null) {
            this.yaw = aim.yaw;
            this.pitch = aim.pitch;
        }
        if (spot != null) {
            this.motion = spot.mode.ordinal();
        }
        if (eff != null) {
            this.burst = eff.burstMode;
        }
        if (smoke != null) {
            this.fogType = smoke.fogType;
        }
    }

    // ------------------------------------------------------------------ helpers

    private FontRenderer font() {
        return mc.getRenderManager().getFontRenderer();
    }

    private static String t(String key) {
        return I18n.format("gui.concertmod." + key);
    }

    private void text(String s, int x, int y, int color) {
        font().drawStringWithShadow(s, x, y, color);
    }

    private void centered(String s, int cx, int y, int color) {
        text(s, cx - font().getStringWidth(s) / 2, y, color);
    }

    private void label(int rx, int ry, String s) {
        labels.add(new Lbl(rx, ry, s));
    }

    private Btn button(int rx, int ry, int w, int h, String s, BooleanSupplier active, Runnable action) {
        Btn b = new Btn(rx, ry, w, h, s, active, action, ACCENT);
        widgets.add(b);
        return b;
    }

    private Slider slider(int rx, int ry, int w, String name, float min, float max, Supplier<Float> get,
                          Consumer<Float> set, int fill, Function<Float, String> fmt) {
        Slider s = new Slider(rx, ry, w, 18, name, min, max, get, set, fill, fmt);
        widgets.add(s);
        return s;
    }

    private static String num1(float v) {
        return String.format(Locale.ROOT, "%.1f", v);
    }

    private static String deg(float v) {
        return Math.round(v) + "°";
    }

    // ------------------------------------------------------------------ layout

    @Override
    public void initGui() {
        super.initGui();
        widgets.clear();
        labels.clear();

        // header: power button
        widgets.add(new Btn(PW - 12 - 86, 6, 86, 18, "", () -> on, () -> on = !on, 0xFF2EA043) {
            @Override
            String caption() {
                return on ? t("power_on") : t("power_off");
            }
        });

        // left column: colors
        label(LX, 38, t("color"));
        for (int i = 0; i < ConcertColors.VALUES.length; i++) {
            widgets.add(new Swatch(LX + (i % 6) * 32, 50 + (i / 6) * 24, 28, 20, ConcertColors.VALUES[i]));
        }
        widgets.add(new RainbowSwatch(LX + 5 * 32, 74, 28, 20));
        slider(LX, 100, LW, "R", 0, 255, () -> (float) cr, v -> {
            cr = Math.round(v);
            rainbow = false;
        }, 0xFFE04848, v -> String.valueOf(Math.round(v)));
        slider(LX, 120, LW, "G", 0, 255, () -> (float) cg, v -> {
            cg = Math.round(v);
            rainbow = false;
        }, 0xFF48C860, v -> String.valueOf(Math.round(v)));
        slider(LX, 140, LW, "B", 0, 255, () -> (float) cb, v -> {
            cb = Math.round(v);
            rainbow = false;
        }, 0xFF4878E8, v -> String.valueOf(Math.round(v)));

        // right column
        int rightBottom;
        if (spot != null) {
            label(RX, 38, t("motion"));
            String[] keys = {"static", "sweep", "circle", "spin"};
            for (int i = 0; i < 4; i++) {
                final int idx = i;
                button(RX + (i % 2) * 107, 50 + (i / 2) * 22, 103, 18, I18n.format("mode.concertmod." + keys[i]),
                        () -> motion == idx, () -> motion = idx);
            }
            button(RX, 94, RW, 18, t("strobe"), () -> strobe, () -> strobe = !strobe);
            slider(RX, 118, RW, t("speed"), 0.1F, 10.0F, () -> speed, v -> speed = v, ACCENT, GuiDevice::num1);
            slider(RX, 138, RW, t("yaw"), -180.0F, 180.0F, () -> yaw, v -> yaw = v, ACCENT, GuiDevice::deg);
            slider(RX, 158, RW, t("pitch"), -90.0F, 90.0F, () -> pitch, v -> pitch = v, ACCENT, GuiDevice::deg);
            rightBottom = 176;
        } else if (eff != null) {
            label(RX, 38, t("mode"));
            button(RX, 50, 103, 18, t("shots"), () -> burst, () -> burst = true);
            button(RX + 107, 50, 103, 18, t("stream"), () -> !burst, () -> burst = false);
            widgets.add(new Btn(RX, 74, RW, 24, t("fire"), null, () -> send(true), 0xFFD2462E));
            int y = 106;
            if (smoke != null) {
                label(RX, 104, t("fogtype"));
                button(RX, 116, 66, 18, t("fog_jet"), () -> fogType == 0, () -> fogType = 0);
                button(RX + 72, 116, 66, 18, t("fog_low"), () -> fogType == 1, () -> fogType = 1);
                button(RX + 144, 116, 66, 18, t("fog_haze"), () -> fogType == 2, () -> fogType = 2);
                y = 140;
            }
            slider(RX, y, RW, t("freq"), 0.1F, 10.0F, () -> speed, v -> speed = v, ACCENT, GuiDevice::num1);
            slider(RX, y + 20, RW, t("yaw"), -180.0F, 180.0F, () -> yaw, v -> yaw = v, ACCENT, GuiDevice::deg);
            slider(RX, y + 40, RW, t("pitch"), -90.0F, 90.0F, () -> pitch, v -> pitch = v, ACCENT, GuiDevice::deg);
            button(RX, y + 62, 103, 18, t("up"), null, () -> pitch = -90.0F);
            button(RX + 107, y + 62, 103, 18, t("down"), null, () -> pitch = 90.0F);
            rightBottom = y + 80;
        } else {
            label(RX, 38, t("effects"));
            button(RX, 50, RW, 18, t("strobe"), () -> strobe, () -> strobe = !strobe);
            slider(RX, 76, RW, t("speed"), 0.1F, 10.0F, () -> speed, v -> speed = v, ACCENT, GuiDevice::num1);
            rightBottom = 94;
        }

        ph = Math.max(180, rightBottom) + 40;
        button(PW / 2 - 50, ph - 28, 100, 18, t("close"), null, () -> mc.displayGuiScreen(null));

        px = 8;
        py = Math.max(4, (height - ph) / 2);
    }

    // ------------------------------------------------------------------ sending

    private void send(boolean fire) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("Color", (cr << 16) | (cg << 8) | cb);
        tag.setBoolean("CommandOn", on);
        tag.setBoolean("Rainbow", rainbow);
        tag.setBoolean("Strobe", strobe);
        tag.setFloat("Speed", speed);
        if (aim != null) {
            tag.setFloat("Yaw", yaw);
            tag.setFloat("Pitch", pitch);
        }
        if (spot != null) {
            tag.setInteger("Mode", motion);
        }
        if (eff != null) {
            tag.setBoolean("BurstMode", burst);
            if (fire) {
                tag.setBoolean("Fire", true);
            }
        }
        if (smoke != null) {
            tag.setInteger("FogType", fogType);
        }
        ConcertMod.NET.sendToServer(new MessageDeviceSettings(dev.getPos(), tag));
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (dirty) {
            send(false);
            dirty = false;
        }
    }

    @Override
    public void onGuiClosed() {
        if (dirty) {
            send(false);
            dirty = false;
        }
        super.onGuiClosed();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    // ------------------------------------------------------------------ input / drawing

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == mc.gameSettings.keyBindInventory.getKeyCode()) {
            mc.displayGuiScreen(null);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton != 0) {
            return;
        }
        for (int i = widgets.size() - 1; i >= 0; i--) {
            Widget w = widgets.get(i);
            if (w.hit(mouseX, mouseY)) {
                if (w.click(mouseX, mouseY)) {
                    dragging = w;
                }
                return;
            }
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        if (dragging != null) {
            dragging.drag(mouseX, mouseY);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        dragging = null;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // panel (the world stays visible so you can watch the effect while adjusting)
        drawRect(px - 1, py - 1, px + PW + 1, py + ph + 1, BORDER);
        drawRect(px, py, px + PW, py + ph, BG);
        drawGradientRect(px, py, px + PW, py + 30, 0xFF3B2A94, 0xFF1D1748);

        String name = dev.getBlockType().getRegistryName().toString();
        name = name.substring(name.indexOf(':') + 1);
        text(I18n.format("tile.concertmod." + name + ".name"), px + 12, py + 11, 0xFFFFFFFF);
        if (dev.powered) {
            text(t("redstone"), px + 190, py + 11, 0xFFFFC857);
        }

        for (Lbl l : labels) {
            text(l.text, px + l.rx, py + l.ry, MUTED);
        }
        for (Widget w : widgets) {
            w.draw(mouseX, mouseY);
        }

        // color preview + hex
        int rgb = rainbow ? (MathHelper.hsvToRGB((Minecraft.getSystemTime() % 4000L) / 4000.0F, 1.0F, 1.0F) & 0xFFFFFF)
                : ((cr << 16) | (cg << 8) | cb);
        drawRect(px + LX - 1, py + 163, px + LX + 33, py + 181, BORDER);
        drawRect(px + LX, py + 164, px + LX + 32, py + 180, 0xFF000000 | rgb);
        text(rainbow ? t("rainbow") : ConcertColors.hex(rgb), px + LX + 42, py + 168, TEXT);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    // ------------------------------------------------------------------ widgets

    private static final class Lbl {
        final int rx;
        final int ry;
        final String text;

        Lbl(int rx, int ry, String text) {
            this.rx = rx;
            this.ry = ry;
            this.text = text;
        }
    }

    private abstract class Widget {
        final int rx;
        final int ry;
        final int w;
        final int h;

        Widget(int rx, int ry, int w, int h) {
            this.rx = rx;
            this.ry = ry;
            this.w = w;
            this.h = h;
        }

        int ax() {
            return px + rx;
        }

        int ay() {
            return py + ry;
        }

        boolean hit(int mx, int my) {
            return mx >= ax() && mx < ax() + w && my >= ay() && my < ay() + h;
        }

        abstract void draw(int mx, int my);

        /** Returns true if the widget wants to receive drag events. */
        boolean click(int mx, int my) {
            return false;
        }

        void drag(int mx, int my) {
        }
    }

    private class Btn extends Widget {
        private final String text;
        private final BooleanSupplier active;
        private final Runnable action;
        private final int activeColor;

        Btn(int rx, int ry, int w, int h, String text, BooleanSupplier active, Runnable action, int activeColor) {
            super(rx, ry, w, h);
            this.text = text;
            this.active = active;
            this.action = action;
            this.activeColor = activeColor;
        }

        String caption() {
            return text;
        }

        @Override
        void draw(int mx, int my) {
            int x = ax();
            int y = ay();
            boolean on = active != null && active.getAsBoolean();
            drawRect(x - 1, y - 1, x + w + 1, y + h + 1, on ? 0xFFB8A8FF : BORDER);
            drawRect(x, y, x + w, y + h, on ? activeColor : (hit(mx, my) ? BTN_HOVER : BTN));
            centered(caption(), x + w / 2, y + (h - 8) / 2, TEXT);
        }

        @Override
        boolean click(int mx, int my) {
            action.run();
            dirty = true;
            return false;
        }
    }

    private class Slider extends Widget {
        private final String name;
        private final float min;
        private final float max;
        private final Supplier<Float> get;
        private final Consumer<Float> set;
        private final int fill;
        private final Function<Float, String> fmt;

        Slider(int rx, int ry, int w, int h, String name, float min, float max, Supplier<Float> get,
               Consumer<Float> set, int fill, Function<Float, String> fmt) {
            super(rx, ry, w, h);
            this.name = name;
            this.min = min;
            this.max = max;
            this.get = get;
            this.set = set;
            this.fill = fill;
            this.fmt = fmt;
        }

        @Override
        void draw(int mx, int my) {
            int x = ax();
            int y = ay();
            float v = get.get();
            float f = MathHelper.clamp((v - min) / (max - min), 0.0F, 1.0F);
            text(name, x, y, MUTED);
            String s = fmt.apply(v);
            text(s, x + w - font().getStringWidth(s), y, TEXT);
            drawRect(x, y + 11, x + w, y + 15, TRACK);
            drawRect(x, y + 11, x + Math.round(f * w), y + 15, fill);
            int kx = x + Math.round(f * w);
            drawRect(kx - 3, y + 9, kx + 3, y + 17, 0xFFFFFFFF);
        }

        @Override
        boolean click(int mx, int my) {
            drag(mx, my);
            return true;
        }

        @Override
        void drag(int mx, int my) {
            float f = MathHelper.clamp((mx - ax()) / (float) w, 0.0F, 1.0F);
            set.accept(min + f * (max - min));
            dirty = true;
        }
    }

    private class Swatch extends Widget {
        private final int rgb;

        Swatch(int rx, int ry, int w, int h, int rgb) {
            super(rx, ry, w, h);
            this.rgb = rgb;
        }

        @Override
        void draw(int mx, int my) {
            int x = ax();
            int y = ay();
            boolean selected = !rainbow && rgb == ((cr << 16) | (cg << 8) | cb);
            drawRect(x - 1, y - 1, x + w + 1, y + h + 1, selected ? 0xFFFFFFFF : (hit(mx, my) ? 0xFFB0B0D0 : 0xFF14142A));
            drawRect(x, y, x + w, y + h, 0xFF000000 | rgb);
        }

        @Override
        boolean click(int mx, int my) {
            cr = (rgb >> 16) & 255;
            cg = (rgb >> 8) & 255;
            cb = rgb & 255;
            rainbow = false;
            dirty = true;
            return false;
        }
    }

    private class RainbowSwatch extends Widget {
        RainbowSwatch(int rx, int ry, int w, int h) {
            super(rx, ry, w, h);
        }

        @Override
        void draw(int mx, int my) {
            int x = ax();
            int y = ay();
            drawRect(x - 1, y - 1, x + w + 1, y + h + 1, rainbow ? 0xFFFFFFFF : (hit(mx, my) ? 0xFFB0B0D0 : 0xFF14142A));
            int n = 7;
            for (int i = 0; i < n; i++) {
                int c = MathHelper.hsvToRGB(i / (float) n, 1.0F, 1.0F) & 0xFFFFFF;
                drawRect(x + i * w / n, y, x + (i + 1) * w / n, y + h, 0xFF000000 | c);
            }
        }

        @Override
        boolean click(int mx, int my) {
            rainbow = !rainbow;
            dirty = true;
            return false;
        }
    }
}
