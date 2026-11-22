package com.zzy.vein;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class VeinScreen extends Screen {
    public VeinScreen() {
        super(Component.literal("\u94fe\u9501\u91c7\u96c6\u8bbe\u7f6e"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        this.addRenderableWidget(Button.builder(
            Component.literal(Config.ENABLED.get() ? "\u72b6\u6001: \u5f00" : "\u72b6\u6001: \u5173"),
            b -> {
                Config.ENABLED.set(!Config.ENABLED.get());
                Config.SPEC.save();
                b.setMessage(Component.literal(Config.ENABLED.get() ? "\u72b6\u6001: \u5f00" : "\u72b6\u6001: \u5173"));
            }).bounds(cx - 100, this.height - 60, 95, 20).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("\u6dfb\u52a0\u51c6\u5fc3\u65b9\u5757"),
            b -> ClientEvents.addLookedAtBlock()
        ).bounds(cx + 5, this.height - 60, 95, 20).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("\u5173\u95ed"),
            b -> this.onClose()
        ).bounds(cx - 50, this.height - 32, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gfx);
        gfx.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFF);
        List<? extends String> list = Config.BLOCKS.get();
        int y = 34;
        int col = this.width / 2 - 150;
        gfx.drawString(this.font, "\u94fe\u9501\u65b9\u5757 (" + list.size() + "):", col, y - 14, 0xAAAAAA, false);
        int shown = 0;
        for (String s : list) {
            if (shown >= 14) { gfx.drawString(this.font, "...", col, y, 0xAAAAAA, false); break; }
            gfx.drawString(this.font, "- " + s, col, y, 0xDDDDDD, false);
            y += 11;
            shown++;
        }
        super.render(gfx, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
