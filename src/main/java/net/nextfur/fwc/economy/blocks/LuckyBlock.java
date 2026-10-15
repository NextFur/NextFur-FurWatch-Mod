package net.nextfur.fwc.economy.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.nextfur.fwc.init.FwModItems;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class LuckyBlock extends Block {
    public static final MapCodec<LuckyBlock> CODEC = simpleCodec(LuckyBlock::new);

    public LuckyBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends LuckyBlock> codec() {
        return CODEC;
    }

    public static LuckyBlock create() {
        return new LuckyBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.GOLD)
                .sound(SoundType.METAL)
                .strength(0.5f, 1.0f));
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = new ArrayList<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        // 70% chance of 1$ (coin_1m) with multiplier (2-5x) chance at 50%
        if (random.nextDouble() < 0.70) {
            int count = (random.nextDouble() < 0.50) ? random.nextInt(2, 6) : 1;
            drops.add(new ItemStack(FwModItems.COIN_1M.get(), count));
        }

        // 50% chance of 5$ (bill_5m) with multiplier (2-3x) chance at 20%
        if (random.nextDouble() < 0.50) {
            int count = (random.nextDouble() < 0.20) ? random.nextInt(2, 4) : 1;
            drops.add(new ItemStack(FwModItems.BILL_5M.get(), count));
        }

        // 35% chance of 10$ (bill_10m) with multiplier (2x) chance at 10%
        if (random.nextDouble() < 0.35) {
            int count = (random.nextDouble() < 0.10) ? 2 : 1;
            drops.add(new ItemStack(FwModItems.BILL_10M.get(), count));
        }

        // 15% chance of 20$ (bill_20m) with multiplier (2x) chance at 5%
        if (random.nextDouble() < 0.15) {
            int count = (random.nextDouble() < 0.05) ? 2 : 1;
            drops.add(new ItemStack(FwModItems.BILL_20M.get(), count));
        }

        // 7% chance of 50$ (bill_50m) with multiplier (2x) chance at 3%
        if (random.nextDouble() < 0.07) {
            int count = (random.nextDouble() < 0.03) ? 2 : 1;
            drops.add(new ItemStack(FwModItems.BILL_50M.get(), count));
        }

        // 3% chance of 100$ (bill_100m) with multiplier (2x) chance at 2%
        if (random.nextDouble() < 0.03) {
            int count = (random.nextDouble() < 0.02) ? 2 : 1;
            drops.add(new ItemStack(FwModItems.BILL_100M.get(), count));
        }

        // 1% chance of 200$ (bill_200m) with multiplier (2-10x) chance at 1%
        if (random.nextDouble() < 0.01) {
            int count = (random.nextDouble() < 0.01) ? random.nextInt(2, 11) : 1;
            drops.add(new ItemStack(FwModItems.BILL_200M.get(), count));
        }

        return drops;
    }
}
