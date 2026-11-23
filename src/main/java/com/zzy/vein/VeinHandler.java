package com.zzy.vein;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
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

        Player player = event.getPlayer();
        BlockPos origin = event.getPos();
        BlockState originState = level.getBlockState(origin);
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(originState.getBlock());
        if (!Config.BLOCKS.get().contains(id.toString())) return;

        int limit = Config.MAX_BLOCKS.get();
        int w = Config.WIDTH.get(), hh = Config.HEIGHT.get(), dp = Config.DEPTH.get();

        // 1. 先按长宽高收集“起始方块”（种子）
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        visited.add(origin);

        if (w > 1 || hh > 1 || dp > 1) {
            Direction facing = player.getDirection();
            int fx = facing.getStepX(), fz = facing.getStepZ();
            int rx = -fz, rz = fx;
            int hw = (w - 1) / 2;
            for (int dl = 0; dl < dp; dl++) {
                for (int dy = 0; dy > -hh; dy--) {
                    for (int dw = -hw; dw <= hw; dw++) {
                        if (dl == 0 && dy == 0 && dw == 0) continue;
                        int dx = fx * dl + rx * dw;
                        int dz = fz * dl + rz * dw;
                        BlockPos n = origin.offset(dx, dy, dz);
                        BlockState ns = level.getBlockState(n);
                        if (ns.getBlock() != originState.getBlock()) continue;
                        if (visited.add(n)) queue.add(n);
                    }
                }
            }
        } else {
            // 纯连通模式：从原点周围开始
            for (int dx = -1; dx <= 1; dx++)
                for (int dy = -1; dy <= 1; dy++)
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        BlockPos n = origin.offset(dx, dy, dz);
                        if (level.getBlockState(n).getBlock() == originState.getBlock() && visited.add(n)) queue.add(n);
                    }
        }

        // 2. 从所有种子出发做连通扩展（不再受形状盒子限制）
        int broken = 0;
        while (!queue.isEmpty() && broken < limit) {
            BlockPos p = queue.poll();
            BlockState ns = level.getBlockState(p);
            if (ns.getBlock() != originState.getBlock()) continue;
            if (!canBreak(level, p, ns)) continue;
            breakAndDrop(level, p, ns, player);
            broken++;

            for (int dx = -1; dx <= 1; dx++)
                for (int dy = -1; dy <= 1; dy++)
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        BlockPos n = p.offset(dx, dy, dz);
                        if (!visited.add(n)) continue;
                        if (level.getBlockState(n).getBlock() == originState.getBlock()) queue.add(n);
                    }
        }
    }

    private static boolean canBreak(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.isAir()) return false;
        return state.getDestroySpeed(level, pos) >= 0;
    }

    private static void breakAndDrop(ServerLevel level, BlockPos pos, BlockState state, Player player) {
        boolean creative = player.isCreative();
        ItemStack tool = player.getMainHandItem();

        if (creative) {
            level.removeBlock(pos, false);
            return;
        }

        if (Config.DROPS_AT_PLAYER.get()) {
            List<ItemStack> drops = Block.getDrops(state, level, pos, level.getBlockEntity(pos), player, tool);
            level.removeBlock(pos, false);
            BlockPos pp = player.blockPosition();
            for (ItemStack st : drops) {
                double ox = (level.random.nextDouble() - 0.5) * 0.8;
                double oz = (level.random.nextDouble() - 0.5) * 0.8;
                Containers.dropItemStack(level, pp.getX() + 0.5 + ox, pp.getY() + 0.6, pp.getZ() + 0.5 + oz, st);
            }
        } else {
            Block.dropResources(state, level, pos, level.getBlockEntity(pos), player, tool);
            level.removeBlock(pos, false);
        }

        int fortune = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, tool);
        int silk = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool);
        int exp = state.getExpDrop(level, level.random, pos, fortune, silk);
        if (exp > 0) {
            BlockPos target = Config.DROPS_AT_PLAYER.get() ? player.blockPosition() : pos;
            ExperienceOrb.award(level, Vec3.atCenterOf(target), exp);
        }

        if (Config.CONSUME_DURABILITY.get() && !tool.isEmpty() && tool.isDamageableItem()) {
            tool.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(net.minecraft.world.InteractionHand.MAIN_HAND));
        }
    }
}
