package com.zzy.vein;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

public class VeinScreen extends Screen {
    private static final int PER_PAGE = 8;
    private int page = 0;
    private EditBox wBox, hBox, dBox;

    public VeinScreen() {
        super(Component.literal("连锁采集设置"));
    }

    private static int clamp99(String s, int fallback) {
        try {
            int v = Integer.parseInt(s.trim());
            return Math.max(1, Math.min(99, v));
        } catch (Exception e) {
            return fallback;
        }
    }

    public static String displayName(String id) {
        try {
            ResourceLocation rl = new ResourceLocation(id);
            if (!BuiltInRegistries.BLOCK.containsKey(rl)) return id + " §c(未注册)";
            Block b = BuiltInRegistries.BLOCK.get(rl);
            String cn = b.getName().getString();
            if (cn == null || cn.isEmpty()) return id;
            return cn + " §8" + id;
        } catch (Exception e) {
            return id;
        }
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int sy = this.height - 84;

        wBox = makeBox(cx - 128, sy, Config.WIDTH.get(), "宽");
        hBox = makeBox(cx - 52, sy, Config.HEIGHT.get(), "高");
        dBox = makeBox(cx + 24, sy, Config.DEPTH.get(), "长");

        this.addRenderableWidget(Button.builder(Component.literal("应用"), b -> applyBoxes())
                .bounds(cx + 66, sy, 50, 20).build());

        int r2 = sy + 22;
        this.addRenderableWidget(Button.builder(
                Component.literal(Config.ENABLED.get() ? "状态：开" : "状态：关"),
                b -> {
                    Config.ENABLED.set(!Config.ENABLED.get());
                    Config.SPEC.save();
                    b.setMessage(Component.literal(Config.ENABLED.get() ? "状态：开" : "状态：关"));
                }).bounds(cx - 155, r2, 70, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("纯连通"), b -> {
            Config.WIDTH.set(1);
            Config.HEIGHT.set(1);
            Config.DEPTH.set(1);
            Config.SPEC.save();
            rebuild();
        }).bounds(cx - 80, r2, 60, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("< 上页"),
                b -> { if (page > 0) { page--; rebuild(); } })
                .bounds(cx - 15, r2, 55, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("下页 >"),
                b -> { if ((page + 1) * PER_PAGE < Config.BLOCKS.get().size()) { page++; rebuild(); } })
                .bounds(cx + 45, r2, 55, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("添加准心方块"), b -> {
            ClientEvents.addLookedAtBlock();
            rebuild();
        }).bounds(cx + 105, r2, 90, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("关闭"), b -> this.onClose())
                .bounds(cx - 50, sy + 44, 100, 20).build());
    }

    private EditBox makeBox(int x, int y, int val, String label) {
        EditBox box = new EditBox(this.font, x, y, 36, 18, Component.literal(label));
        box.setValue(String.valueOf(val));
        box.setMaxLength(2);
        box.setFilter(s -> s.isEmpty() || s.matches("\\d{1,2}"));
        this.addRenderableWidget(box);
        return box;
    }

    private void applyBoxes() {
        int w = clamp99(wBox.getValue(), Config.WIDTH.get());
        int h = clamp99(hBox.getValue(), Config.HEIGHT.get());
        int d = clamp99(dBox.getValue(), Config.DEPTH.get());
        Config.WIDTH.set(w);
        Config.HEIGHT.set(h);
        Config.DEPTH.set(d);
        Config.SPEC.save();
        wBox.setValue(String.valueOf(w));
        hBox.setValue(String.valueOf(h));
        dBox.setValue(String.valueOf(d));
    }

    private void rebuild() {
        if (wBox != null) applyBoxes();
        this.clearWidgets();
        this.init();
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
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
        gfx.drawString(this.font, "共 " + list.size() + " 项 / 第 " + (page + 1) + " 页(共 " + totalPage + " 页)",
                this.width / 2 - 150, 32, 0xAAAAAA, false);

        String shape = (Config.WIDTH.get() > 1 || Config.HEIGHT.get() > 1 || Config.DEPTH.get() > 1)
                ? "宽" + Config.WIDTH.get() + " 高" + Config.HEIGHT.get() + " 长" + Config.DEPTH.get() + "（按朝向）"
                : "连通模式";
        gfx.drawString(this.font, shape, this.width / 2 + 20, 32, 0x66CCFF, false);

        int x = this.width / 2 - 150;
        int y0 = 50;
        int start = page * PER_PAGE;
        for (int i = 0; i < PER_PAGE; i++) {
            int idx = start + i;
            if (idx >= list.size()) break;
            int rowY = y0 + i * 12;
            boolean hover = mouseX >= x && mouseX <= x + 300 && mouseY >= rowY - 1 && mouseY <= rowY + 10;
            if (hover) gfx.fill(x - 2, rowY - 1, x + 302, rowY + 10, 0x40FF0000);
            gfx.drawString(this.font, "· " + displayName(list.get(idx)), x, rowY, hover ? 0xFF6666 : 0xDDDDDD, false);
        }

        // 输入框标签
        int sy = this.height - 84;
        gfx.drawString(this.font, "宽", this.width / 2 - 150, sy + 5, 0xAAAAAA, false);
        gfx.drawString(this.font, "高", this.width / 2 - 74, sy + 5, 0xAAAAAA, false);
        gfx.drawString(this.font, "长", this.width / 2 + 2, sy + 5, 0xAAAAAA, false);

        super.render(gfx, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
