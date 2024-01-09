package com.theomenden.prefabricated.structures.predefined;

import com.theomenden.prefabricated.ModRegistry;
import com.theomenden.prefabricated.Utils;
import com.theomenden.prefabricated.blocks.FullDyeColor;
import com.theomenden.prefabricated.config.EntityPlayerConfiguration;
import com.theomenden.prefabricated.structures.base.BuildBlock;
import com.theomenden.prefabricated.structures.base.BuildingMethods;
import com.theomenden.prefabricated.structures.base.Structure;
import com.theomenden.prefabricated.structures.config.HouseConfiguration;
import com.theomenden.prefabricated.structures.config.StructureConfiguration;
import it.unimi.dsi.fastutil.objects.ReferenceReferencePair;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;

/**
 * @author WuestMan
 */
@SuppressWarnings({"unused", "ConstantConditions", "UnusedAssignment"})
public class StructureHouse extends Structure {
    private BlockPos chestPosition = null;
    private ArrayList<BlockPos> furnacePositions = new ArrayList<>();
    private BlockPos trapDoorPosition = null;

    @Override
    protected Boolean CustomBlockProcessingHandled(StructureConfiguration configuration, BuildBlock block, Level world, BlockPos originalPos,
                                                   Block foundBlock, BlockState blockState, Player player) {
        HouseConfiguration houseConfig = (HouseConfiguration) configuration;

        if ((!houseConfig.addBed && foundBlock instanceof BedBlock)
                || (!houseConfig.addChest && foundBlock instanceof ChestBlock)
                || (!houseConfig.addTorches && foundBlock instanceof TorchBlock)
                || (!houseConfig.addCraftingTable && foundBlock instanceof CraftingTableBlock)
                || (!houseConfig.addFurnace && foundBlock instanceof FurnaceBlock)
                || (!houseConfig.addChest && foundBlock instanceof BarrelBlock)
                || (foundBlock instanceof SeagrassBlock)
                || (foundBlock instanceof TallSeagrassBlock)) {
            // Don't place the block, returning true means that this has been
            // "handled"
            return true;
        }

        if (foundBlock instanceof FurnaceBlock) {
            this.furnacePositions.add(block.getStartingPosition().getRelativePosition(
                    originalPos,
                    this.getClearSpace().getShape().getDirection(),
                    configuration.houseFacing));
        } else if (foundBlock instanceof TrapDoorBlock && houseConfig.addMineShaft && this.trapDoorPosition == null) {
            // The trap door will still be added, but the mineshaft may not be built.
            this.trapDoorPosition = block.getStartingPosition().getRelativePosition(
                    originalPos,
                    this.getClearSpace().getShape().getDirection(),
                    configuration.houseFacing);
        } else if ((foundBlock instanceof ChestBlock && this.chestPosition == null)
                || (foundBlock instanceof BarrelBlock && this.chestPosition == null)) {
            this.chestPosition = block.getStartingPosition().getRelativePosition(
                    originalPos,
                    this.getClearSpace().getShape().getDirection(),
                    configuration.houseFacing);
        } else if (foundBlock == Blocks.SPONGE && houseConfig.addMineShaft) {
            // Sponges are sometimes used in-place of trapdoors when trapdoors are used for decoration.
            this.trapDoorPosition = block.getStartingPosition().getRelativePosition(
                    originalPos,
                    this.getClearSpace().getShape().getDirection(),
                    configuration.houseFacing).above();
        } else if (foundBlock instanceof BedBlock && houseConfig.addBed) {
            BlockPos bedHeadPosition = block.getStartingPosition().getRelativePosition(originalPos, this.getClearSpace().getShape().getDirection(), configuration.houseFacing);
            BlockPos bedFootPosition = block.getSubBlock().getStartingPosition().getRelativePosition(
                    originalPos,
                    this.getClearSpace().getShape().getDirection(),
                    configuration.houseFacing);

            ReferenceReferencePair<BlockState, BlockState> blockStateTuple = BuildingMethods.getBedState(bedHeadPosition, bedFootPosition, houseConfig.bedColor);
            block.setBlockState(blockStateTuple.left());
            block.getSubBlock().setBlockState(blockStateTuple.right());

            this.priorityOneBlocks.add(block);

            // Return true so the bed is not placed.
            return true;
        }

        return false;
    }

    /**
     * This method is used after the main building is build for any additional structures or modifications.
     *
     * @param configuration The structure configuration.
     * @param world         The current world.
     * @param originalPos   The original position clicked on.
     * @param player        The player which initiated the construction.
     */
    @Override
    public void AfterBuilding(StructureConfiguration configuration, ServerLevel world, BlockPos originalPos, Player player) {
        HouseConfiguration houseConfig = (HouseConfiguration) configuration;
        EntityPlayerConfiguration playerConfig = EntityPlayerConfiguration.loadFromEntity(player);

        BuildingMethods.FillFurnaces(world, this.furnacePositions);

        if (this.chestPosition != null && houseConfig.addChestContents) {
            // Fill the chest.
            BuildingMethods.FillChest(world, this.chestPosition);
        }

        int minimumHeightForMineshaft = world.getMinBuildHeight() + 21;

        if (this.trapDoorPosition != null
                && this.trapDoorPosition.getY() > minimumHeightForMineshaft
                && houseConfig.addMineShaft) {
            // Build the mineshaft.
            BuildingMethods.PlaceMineShaft(world, this.trapDoorPosition.below(), houseConfig.houseFacing, false);
        }

        // Make sure to set this value so the player cannot fill the chest a second time.
        playerConfig.builtStarterHouse = true;

        // Make sure to send a message to the client to sync up the server player information and the client player
        // information.
        ServerPlayNetworking.send((ServerPlayer) player, ModRegistry.PlayerConfigSync, Utils.createMessageBuffer(playerConfig.createPlayerTag()));
    }

    @Override
    protected boolean hasGlassColor(StructureConfiguration configuration) {
        return true;
    }

    @Override
    protected FullDyeColor getGlassColor(StructureConfiguration configuration) {
        HouseConfiguration houseConfig = (HouseConfiguration) configuration;
        return houseConfig.glassColor;
    }
}
