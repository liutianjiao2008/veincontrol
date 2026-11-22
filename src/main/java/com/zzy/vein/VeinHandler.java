package com.zzy.vein;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import java.util.List;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VeinHandler {
    @SubscribeEvent
    public void onBreak(BlockEvent.BreakEvent event) {
        if (!Config.ENABLED.get()) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        BlockPos origin = event.getPos();
        BlockState originState = level.getBlockState(origin);
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(originState.getBlock());
        List<? extends String> allowed = Config.BLOCKS.get();
        if (!allowed.contains(id.toString())) return;

        int limit = Config.MAX_BLOCKS.get();
        int w = Config.WIDTH.get(), hh2 = Config.HEIGHT.get(), dp = Config.DEPTH.get();

        // 立方体模式：长/宽/高 任一大于 1 时生效
        if (w > 1 || hh2 > 1 || dp > 1) {
            int hw = (w - 1) / 2, hh = (hh2 - 1) / 2, hd = (dp - 1) / 2;
            int done = 0;
            for (int dx = -hw; dx <= hw && done < limit; dx++) {
                for (int dy = -hh; dy <= hh && done < limit; dy++) {
                    for (int dz = -hd; dz <= hd && done < limit; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        BlockPos n = origin.offset(dx, dy, dz);
                        BlockState ns = level.getBlockState(n);
                        if (ns.getBlock() != originState.getBlock()) continue;
                        breakAndDrop(level, n, ns, event);
                        done++;
                    }
                }
            }
            return;
        }
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(origin);
        visited.add(origin);

        int broken = 0;
        while (!queue.isEmpty() && broken < limit) {
            BlockPos p = queue.poll();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        BlockPos n = p.offset(dx, dy, dz);
                        if (!visited.add(n)) continue;
                        BlockState ns = level.getBlockState(n);
                        if (ns.getBlock() != originState.getBlock()) continue;
                        if (n.equals(origin)) continue;
                        breakAndDrop(level, n, ns, event);
                        queue.add(n);
                        broken++;
                        if (broken >= limit) break;
                    }
                    if (broken >= limit) break;
                }
                if (broken >= limit) break;
            }
        }
    }

    private static void breakAndDrop(ServerLevel level, BlockPos n, BlockState ns, BlockEvent.BreakEvent event) {
        if (Config.DROPS_AT_PLAYER.get()) {
            List<ItemStack> drops = Block.getDrops(ns, level, n, level.getBlockEntity(n),
                    event.getPlayer(), event.getPlayer().getMainHandItem());
            level.removeBlock(n, false);
            BlockPos pp = event.getPlayer().blockPosition();
            for (ItemStack st : drops) {
                double ox = (level.random.nextDouble() - 0.5) * 0.8;
                double oz = (level.random.nextDouble() - 0.5) * 0.8;
                Containers.dropItemStack(level, pp.getX() + 0.5 + ox, pp.getY() + 0.6, pp.getZ() + 0.5 + oz, st);
            }
        } else {
            level.destroyBlock(n, true, event.getPlayer());
        }
    }
}
