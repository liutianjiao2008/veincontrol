package com.zzy.vein;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class VeinScreen extends Screen {
    private static final int PER_PAGE = 12;
    private int page = 0;

    public VeinScreen() {
        super(Component.literal("连锁采集设置"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;

        this.addRenderableWidget(Button.builder(
            Component.literal(Config.ENABLED.get() ? "状态：开" : "状态：关"),
            b -> {
                Config.ENABLED.set(!Config.ENABLED.get());
                Config.SPEC.save();
                b.setMessage(Component.literal(Config.ENABLED.get() ? "状态：开" : "状态：关"));
            }).bounds(cx - 155, this.height - 56, 90, 20).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("< 上一页"),
            b -> { if (page > 0) { page--; rebuild(); } }
        ).bounds(cx - 55, this.height - 56, 70, 20).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("下一页 >"),
            b -> { if ((page + 1) * PER_PAGE < Config.BLOCKS.get().size()) { page++; rebuild(); } }
        ).bounds(cx + 20, this.height - 56, 70, 20).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("添加准心方块"),
            b -> { ClientEvents.addLookedAtBlock(); rebuild(); }
        ).bounds(cx + 95, this.height - 56, 90, 20).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("关闭"),
            b -> this.onClose()
        ).bounds(cx - 50, this.height - 30, 100, 20).build());
    }

    private void rebuild() {
        this.clearWidgets();
        this.init();
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        // 点击列表项 = 删除该方块
        List<? extends String> list = Config.BLOCKS.get();
        int start = page * PER_PAGE;
        int x = this.width / 2 - 150;
        int y0 = 50;
        for (int i = 0; i < PER_PAGE; i++) {
            int idx = start + i;
            if (idx >= list.size()) break;
            int rowY = y0 + i * 12;
            if (mx >= x && mx <= x + 300 && my >= rowY - 1 && my <= rowY + 10) {
                List<String> copy = new ArrayList<>();
                for (String s : list) copy.add(s);
                copy.remove(idx);
                Config.BLOCKS.set(copy);
                Config.SPEC.save();
                if (page > 0 && page * PER_PAGE >= copy.size()) page--;
                rebuild();
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gfx);
        gfx.drawCenteredString(this.font, this.title, this.width / 2, 14, 0xFFFFFF);

        List<? extends String> list = Config.BLOCKS.get();
        int totalPage = Math.max(1, (list.size() + PER_PAGE - 1) / PER_PAGE);
        gfx.drawString(this.font, "共 " + list.size() + " 项 / 第 " + (page + 1) + " 页（共 " + totalPage + " 页）", this.width / 2 - 150, 32, 0xAAAAAA, false);
        gfx.drawString(this.font, "点击列表中的条目即可删除", this.width / 2 + 20, 32, 0x888888, false);

        int x = this.width / 2 - 150;
        int y0 = 50;
        int start = page * PER_PAGE;
        for (int i = 0; i < PER_PAGE; i++) {
            int idx = start + i;
            if (idx >= list.size()) break;
            int rowY = y0 + i * 12;
            boolean hover = mouseX >= x && mouseX <= x + 300 && mouseY >= rowY - 1 && mouseY <= rowY + 10;
            if (hover) gfx.fill(x - 2, rowY - 1, x + 302, rowY + 10, 0x40FF0000);
            gfx.drawString(this.font, "· " + list.get(idx), x, rowY, hover ? 0xFF6666 : 0xDDDDDD, false);
        }
        super.render(gfx, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
