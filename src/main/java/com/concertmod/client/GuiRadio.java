package com.concertmod.client;

import com.concertmod.ConcertMod;
import com.concertmod.network.MessageRadioChannel;
import com.concertmod.radio.RadioNetwork;
import com.concertmod.tile.TileRadioBase;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Simple dark-themed channel selector screen for Radio Transmitter and Radio Receiver blocks. */
@SideOnly(Side.CLIENT)
public class GuiRadio extends GuiScreen {
    private static final int PW = 244;
    private static final int PH = 166;

    private static final int BG = 0xEE121220;
    private static final int BORDER = 0xFF3C3C66;
    private static final int ACCENT = 0xFF6D4DF0;
    private static final int BTN = 0xFF26263F;
    private static final int BTN_HOVER = 0xFF34345A;
    private static final int TEXT = 0xFFEDEDF8;
    private static final int MUTED = 0xFFA4A4C4;

    private final TileRadioBase tile;
    private int channel;
    private int px;
    private int py;
    private final List<Btn> buttons = new ArrayList<Btn>();

    public GuiRadio(TileRadioBase tile) {
        this.tile = tile;
        this.channel = RadioNetwork.clampChannel(tile.channel);
    }

    private FontRenderer font() {
        return mc.getRenderManager().getFontRenderer();
    }

    private void setChannel(int ch) {
        int clamped = RadioNetwork.clampChannel(ch);
        if (clamped != channel) {
            channel = clamped;
            ConcertMod.NET.sendToServer(new MessageRadioChannel(tile.getPos(), channel));
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        px = (width - PW) / 2;
        py = (height - PH) / 2;
        buttons.clear();

        buttons.add(new Btn(16, 62, 46, 20, "-10", false, () -> setChannel(channel - 10)));
        buttons.add(new Btn(68, 62, 46, 20, "-1", false, () -> setChannel(channel - 1)));
        buttons.add(new Btn(130, 62, 46, 20, "+1", false, () -> setChannel(channel + 1)));
        buttons.add(new Btn(182, 62, 46, 20, "+10", false, () -> setChannel(channel + 10)));

        for (int i = 0; i < 8; i++) {
            final int ch = i + 1;
            int bx = 16 + i * 27;
            buttons.add(new Btn(bx, 102, 23, 20, String.valueOf(ch), true, () -> setChannel(ch)) {
                @Override
                boolean isSelected() {
                    return channel == ch;
                }
            });
        }

        buttons.add(new Btn(PW / 2 - 48, PH - 28, 96, 18, I18n.format("gui.concertmod.close"), false,
                () -> mc.displayGuiScreen(null)));
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

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
        for (Btn b : buttons) {
            if (b.hit(mouseX, mouseY)) {
                b.action.run();
                return;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(px - 1, py - 1, px + PW + 1, py + PH + 1, BORDER);
        drawRect(px, py, px + PW, py + PH, BG);
        drawRect(px, py, px + PW, py + 26, 0xFF1A1A30);

        String titleKey = tile.isTransmitter() ? "tile.concertmod.radio_transmitter.name" : "tile.concertmod.radio_receiver.name";
        String title = I18n.format(titleKey);
        font().drawStringWithShadow(title, px + (PW - font().getStringWidth(title)) / 2, py + 9, TEXT);

        String chText = I18n.format("gui.concertmod.radio_channel_val", channel);
        font().drawStringWithShadow(chText, px + (PW - font().getStringWidth(chText)) / 2, py + 40, 0xFF7DF0FF);

        String quickLbl = I18n.format("gui.concertmod.radio_quick");
        font().drawStringWithShadow(quickLbl, px + 16, py + 90, MUTED);

        for (Btn b : buttons) {
            b.draw(mouseX, mouseY);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private class Btn {
        final int rx;
        final int ry;
        final int w;
        final int h;
        final String label;
        final boolean toggle;
        final Runnable action;

        Btn(int rx, int ry, int w, int h, String label, boolean toggle, Runnable action) {
            this.rx = rx;
            this.ry = ry;
            this.w = w;
            this.h = h;
            this.label = label;
            this.toggle = toggle;
            this.action = action;
        }

        boolean isSelected() {
            return false;
        }

        boolean hit(int mx, int my) {
            int x = px + rx;
            int y = py + ry;
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }

        void draw(int mx, int my) {
            int x = px + rx;
            int y = py + ry;
            boolean hov = hit(mx, my);
            int bg = (toggle && isSelected()) ? ACCENT : (hov ? BTN_HOVER : BTN);
            drawRect(x, y, x + w, y + h, bg);
            font().drawStringWithShadow(label, x + (w - font().getStringWidth(label)) / 2, y + (h - 8) / 2, TEXT);
        }
    }
}
