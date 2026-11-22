package com.zzy.vein;

import com.mojang.blaze3d.vertex.PoseStack;
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

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = VeinControl.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents {

    public static final int BTN_W = 66;
    public static final int BTN_H = 18;
    public static final int PAD = 4;

    @SubscribeEvent
    public static void registerOverlay(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("veinctl_hud", (gui, gfx, partialTick, width, height) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            Font font = mc.font;
            int x = width - BTN_W - 6;
            int y = 6;

            // toggle button
            boolean on = Config.ENABLED.get();
            gfx.fill(x, y, x + BTN_W, y + BTN_H, on ? 0xC0208020 : 0xC0802020);
            gfx.drawString(font, on ? "\u94fe\u9501: \u5f00" : "\u94fe\u9501: \u5173", x + 6, y + 5, 0xFFFFFF, false);

            // settings button
            int y2 = y + BTN_H + PAD;
            gfx.fill(x, y2, x + BTN_W, y2 + BTN_H, 0xC0303030);
            gfx.drawString(font, "\u8bbe\u7f6e", x + 20, y2 + 5, 0xFFFFFF, false);

            // add-target button
            int y3 = y2 + BTN_H + PAD;
            gfx.fill(x, y3, x + BTN_W, y3 + BTN_H, 0xC0303030);
            gfx.drawString(font, "\u6dfb\u52a0", x + 20, y3 + 5, 0xFFFFFF, false);
        });
    }

    private static boolean inBox(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Mod.EventBusSubscriber(modid = VeinControl.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class Mouse {
        @SubscribeEvent
        public static void onClick(InputEvent.MouseButton.Post event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen != null) return;
            if (event.getButton() != 0 || event.getAction() != 1) return;
            if (mc.getWindow() == null) return;

            double sw = mc.getWindow().getScreenWidth();
            double sh = mc.getWindow().getScreenHeight();
            double gx = mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / sw;
            double gy = mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / sh;

            int width = mc.getWindow().getGuiScaledWidth();
            int x = width - BTN_W - 6;
            int y = 6;

            if (inBox(gx, gy, x, y, BTN_W, BTN_H)) {
                Config.ENABLED.set(!Config.ENABLED.get());
                Config.SPEC.save();
                return;
            }
            int y2 = y + BTN_H + PAD;
            if (inBox(gx, gy, x, y2, BTN_W, BTN_H)) {
                mc.setScreen(new VeinScreen());
                return;
            }
            int y3 = y2 + BTN_H + PAD;
            if (inBox(gx, gy, x, y3, BTN_W, BTN_H)) {
                addLookedAtBlock();
            }
        }
    }

    public static void addLookedAtBlock() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.BLOCK) return;
        BlockHitResult bhr = (BlockHitResult) mc.hitResult;
        BlockPos pos = bhr.getBlockPos();
        if (mc.level == null) return;
        String id = BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(pos).getBlock()).toString();
        List<String> list = new ArrayList<>();
        for (String s : Config.BLOCKS.get()) list.add(s);
        if (list.contains(id)) return;
        list.add(id);
        Config.BLOCKS.set(list);
        Config.SPEC.save();
        if (mc.player != null) mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("\u5df2\u6dfb\u52a0: " + id), true);
    }
}
