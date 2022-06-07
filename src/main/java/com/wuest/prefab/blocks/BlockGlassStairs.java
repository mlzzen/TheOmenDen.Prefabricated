package com.wuest.prefab.blocks;

import com.wuest.prefab.ModRegistry;
import com.wuest.prefab.Utils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * This class allows custom stairs blocks to be created.
 *
 * @author Brian
 */
public class BlockGlassStairs extends StairBlock {
    public BlockGlassStairs(BlockState state, Block.Properties properties) {
        super(state, properties);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
        boolean foundBlock = Utils.doesBlockStateHaveTag(adjacentBlockState, new ResourceLocation("c", "glass"));
        Block adjacentBlock = adjacentBlockState.getBlock();

        return foundBlock || adjacentBlock == this
                || (adjacentBlock == ModRegistry.GlassSlab
                && adjacentBlockState.getValue(SlabBlock.TYPE) == SlabType.DOUBLE);
    }
}
