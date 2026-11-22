package com.zzy.vein;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class Config {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.IntValue MAX_BLOCKS;
    public static final ForgeConfigSpec.BooleanValue DROPS_AT_PLAYER;
    public static final ForgeConfigSpec.IntValue WIDTH;
    public static final ForgeConfigSpec.IntValue HEIGHT;
    public static final ForgeConfigSpec.IntValue DEPTH;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BLOCKS;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.comment("Vein mining settings").push("vein");
        ENABLED = b.comment("Master switch").define("enabled", true);
        MAX_BLOCKS = b.comment("Max blocks per vein").defineInRange("maxBlocks", 128, 1, 1000);
        DROPS_AT_PLAYER = b.comment("掉落物直接落在玩家脚下").define("dropsAtPlayer", true);
        WIDTH = b.comment("连锁宽度(1=不限制)").defineInRange("width", 3, 1, 32);
        HEIGHT = b.comment("连锁高度(1=不限制)").defineInRange("height", 3, 1, 32);
        DEPTH = b.comment("连锁深度(1=不限制)").defineInRange("depth", 3, 1, 32);
        List<String> defaults = new ArrayList<>(List.of(
            "minecraft:coal_ore", "minecraft:deepslate_coal_ore",
            "minecraft:iron_ore", "minecraft:deepslate_iron_ore",
            "minecraft:copper_ore", "minecraft:deepslate_copper_ore",
            "minecraft:gold_ore", "minecraft:deepslate_gold_ore",
            "minecraft:redstone_ore", "minecraft:deepslate_redstone_ore",
            "minecraft:lapis_ore", "minecraft:deepslate_lapis_ore",
            "minecraft:diamond_ore", "minecraft:deepslate_diamond_ore",
            "minecraft:emerald_ore", "minecraft:deepslate_emerald_ore",
            "minecraft:nether_gold_ore", "minecraft:nether_quartz_ore",
            "minecraft:oak_log", "minecraft:birch_log", "minecraft:spruce_log",
            "minecraft:jungle_log", "minecraft:acacia_log", "minecraft:dark_oak_log"
        ));
        BLOCKS = b.comment("Block ids that trigger vein mining")
                   .defineListAllowEmpty(List.of("blocks"), defaults, o -> o instanceof String);
        b.pop();
        SPEC = b.build();
    }
}
