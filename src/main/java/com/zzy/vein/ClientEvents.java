package com.zzy.vein;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = VeinControl.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents {

    public static final int BTN_W = 72;
    public static final int BTN_H = 18;
    public static final int PAD = 4;

    // 强调色
    private static final int C_ON    = 0xFF3DD68C;
    private static final int C_OFF   = 0xFFE5484D;
    private static final int C_SET   = 0xFF4A90D9;
    private static final int C_ADD   = 0xFFE5A50A;

    @SubscribeEvent
    public static void registerOverlay(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("veinctl_hud", (gui, gfx, partialTick, width, height) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            Font font = mc.font;
            int x = width - BTN_W - 6;
            int y = 6;
            double[] mp = mouseGui(mc);
            boolean on = Config.ENABLED.get();

            drawBtn(gfx, font, x, y, on ? "连锁 · 开" : "连锁 · 关", on ? C_ON : C_OFF, hit(mp, x, y));

            int y2 = y + BTN_H + PAD;
            drawBtn(gfx, font, x, y2, "设置", C_SET, hit(mp, x, y2));

            int y3 = y2 + BTN_H + PAD;
            drawBtn(gfx, font, x, y3, "添加", C_ADD, hit(mp, x, y3));
        });
    }

    private static void drawBtn(GuiGraphics gfx, Font font, int x, int y, String label, int accent, boolean hover) {
        // 投影
        gfx.fill(x + 1, y + 2, x + BTN_W + 1, y + BTN_H + 2, 0x60000000);
        // 底
        gfx.fill(x, y, x + BTN_W, y + BTN_H, hover ? 0xF0353535 : 0xD01A1A1A);
        // 左侧强调条
        gfx.fill(x, y, x + 3, y + BTN_H, accent);
        // 高光边 / 暗边
        gfx.fill(x, y, x + BTN_W, y + 1, hover ? 0xFFB0B0B0 : 0xFF707070);
        gfx.fill(x, y + BTN_H - 1, x + BTN_W, y + BTN_H, 0xFF0A0A0A);
        // 文字居中
        int tw = font.width(label);
        gfx.drawString(font, label, x + 3 + (BTN_W - 3 - tw) / 2, y + (BTN_H - 8) / 2,
                hover ? 0xFFFFFF : 0xE0E0E0, false);
    }

    private static double[] mouseGui(Minecraft mc) {
        double sw = mc.getWindow().getScreenWidth();
        double sh = mc.getWindow().getScreenHeight();
        return new double[]{
                mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / sw,
                mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / sh
        };
    }

    private static boolean hit(double[] mp, int x, int y) {
        return mp[0] >= x && mp[0] <= x + BTN_W && mp[1] >= y && mp[1] <= y + BTN_H;
    }

    @Mod.EventBusSubscriber(modid = VeinControl.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class Mouse {

        @SubscribeEvent
        public static void onKey(InputEvent.Key event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.screen != null) return;
            if (event.getAction() != GLFW.GLFW_PRESS) return;
            if (event.getKey() == GLFW.GLFW_KEY_LEFT_ALT) {
                if (mc.mouseHandler.isMouseGrabbed()) mc.mouseHandler.releaseMouse();
                else mc.mouseHandler.grabMouse();
            }
        }

        @SubscribeEvent
        public static void onClick(InputEvent.MouseButton.Pre event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen != null) return;
            if (event.getButton() != 0 || event.getAction() != 1) return;
            if (mc.getWindow() == null) return;

            double[] mp = mouseGui(mc);
            int x = mc.getWindow().getGuiScaledWidth() - BTN_W - 6;
            int y = 6;

            if (hit(mp, x, y)) {
                event.setCanceled(true);
                try { Config.ENABLED.set(!Config.ENABLED.get()); Config.SPEC.save(); } catch (Exception ignored) {}
                if (!mc.mouseHandler.isMouseGrabbed()) mc.mouseHandler.releaseMouse();
                return;
            }
            int y2 = y + BTN_H + PAD;
            if (hit(mp, x, y2)) {
                event.setCanceled(true);
                mc.setScreen(new VeinScreen());
                return;
            }
            int y3 = y2 + BTN_H + PAD;
            if (hit(mp, x, y3)) {
                event.setCanceled(true);
                addLookedAtBlock();
                if (!mc.mouseHandler.isMouseGrabbed()) mc.mouseHandler.releaseMouse();
            }
        }
    }

    public static void addLookedAtBlock() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.BLOCK) {
            mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("\u00a7c\u8bf7\u5148\u7528\u51c6\u5fc3\u5bf9\u51c6\u4e00\u4e2a\u65b9\u5757"), true);
            return;
        }
        BlockHitResult bhr = (BlockHitResult) mc.hitResult;
        BlockPos pos = bhr.getBlockPos();
        if (mc.level == null) return;
        String id = BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(pos).getBlock()).toString();
        List<String> list = new ArrayList<>();
        for (String s : Config.BLOCKS.get()) list.add(s);
        if (list.contains(id)) {
            mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("\u00a7e\u5df2\u5b58\u5728: " + VeinScreen.displayName(id)), true);
            return;
        }
        list.add(id);
        try { Config.BLOCKS.set(list); Config.SPEC.save(); } catch (Exception ignored) {}
        mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("\u00a7a\u5df2\u6dfb\u52a0: " + VeinScreen.displayName(id)), true);
    }
}
