package com.wuest.prefab.structures.render;


import com.mojang.blaze3d.platform.GlStateManager;
import com.wuest.prefab.Prefab;
import com.wuest.prefab.Tuple;
import com.wuest.prefab.gui.GuiLangKeys;
import com.wuest.prefab.structures.base.BuildBlock;
import com.wuest.prefab.structures.base.Structure;
import com.wuest.prefab.structures.config.StructureConfiguration;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3d;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.ArrayList;

/**
 * @author WuestMan
 * This class was derived from Botania's MultiBlockRenderer.
 * Most changes are for extra comments for myself as well as to use my blocks class structure.
 * http://botaniamod.net/license.php
 */
@SuppressWarnings({"WeakerAccess", "ConstantConditions"})
public class StructureRenderHandler {
	// player's overlapping on structures and other things.
	public static StructureConfiguration currentConfiguration;
	public static Structure currentStructure;
	public static Direction assumedNorth;
	public static boolean rendering = false;
	public static boolean showedMessage = false;
	private static int dimension;
	private static int overlay = OverlayTexture.packUv(5, 10);

	/**
	 * Resets the structure to show in the world.
	 *
	 * @param structure     The structure to show in the world, pass null to clear out the client.
	 * @param assumedNorth  The assumed norther facing for this structure.
	 * @param configuration The configuration for this structure.
	 */
	public static void setStructure(Structure structure, Direction assumedNorth, StructureConfiguration configuration) {
		StructureRenderHandler.currentStructure = structure;
		StructureRenderHandler.assumedNorth = assumedNorth;
		StructureRenderHandler.currentConfiguration = configuration;
		StructureRenderHandler.showedMessage = false;

		MinecraftClient mc = MinecraftClient.getInstance();

		if (mc.world != null) {
			StructureRenderHandler.dimension = mc.world.getDimension().getLogicalHeight();
		}
	}

	/**
	 * This is to render the currently bound structure.
	 *
	 * @param player The player to render the structure for.
	 * @param src    The ray trace for where the player is currently looking.
	 */
	public static void renderPlayerLook(PlayerEntity player, HitResult src, MatrixStack matrixStack) {
		if (StructureRenderHandler.currentStructure != null
				&& StructureRenderHandler.dimension == player.world.getDimension().getLogicalHeight()
				&& StructureRenderHandler.currentConfiguration != null
				&& Prefab.serverConfiguration.enableStructurePreview) {
			rendering = true;
			boolean didAny = false;

			VertexConsumerProvider.Immediate entityVertexConsumer = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
			ArrayList<Tuple<BlockState, BlockPos>> entityModels = new ArrayList<>();

			for (BuildBlock buildBlock : StructureRenderHandler.currentStructure.getBlocks()) {
				Block foundBlock = Registry.BLOCK.get(buildBlock.getResourceLocation());

				if (foundBlock != null) {
					// Get the unique block state for this block.
					BlockState blockState = foundBlock.getDefaultState();
					buildBlock = BuildBlock.SetBlockState(
							StructureRenderHandler.currentConfiguration,
							player.world,
							StructureRenderHandler.currentConfiguration.pos,
							StructureRenderHandler.assumedNorth,
							buildBlock,
							foundBlock,
							blockState,
							StructureRenderHandler.currentStructure);

					// In order to get the proper relative position I also need the structure's original facing.
					BlockPos pos = buildBlock.getStartingPosition().getRelativePosition(
							StructureRenderHandler.currentConfiguration.pos,
							StructureRenderHandler.currentStructure.getClearSpace().getShape().getDirection(),
							StructureRenderHandler.currentConfiguration.houseFacing);

					BlockRenderType blockRenderType = blockState.getRenderType();

					if (blockRenderType == BlockRenderType.ENTITYBLOCK_ANIMATED) {
						if (ShaderHelper.hasIncompatibleMods) {
							entityModels.add(new Tuple<>(buildBlock.getBlockState(), pos));
						}

						continue;
					}

					if (StructureRenderHandler.renderComponentInWorld(player.world, buildBlock, entityVertexConsumer, matrixStack, pos, blockRenderType)) {
						didAny = true;
					}
				}
			}

			ShaderHelper.useShader(ShaderHelper.alphaShader, shader -> {
				// getUniformLocation
				int alpha = GlStateManager.getUniformLocation(shader, "alpha");
				ShaderHelper.FLOAT_BUF.position(0);
				ShaderHelper.FLOAT_BUF.put(0, 0.4F);

				// uniform1
				GlStateManager.uniform1(alpha, ShaderHelper.FLOAT_BUF);
			});

			// Draw function.
			entityVertexConsumer.draw(TexturedRenderLayers.getItemEntityTranslucentCull());

			ShaderHelper.releaseShader();

			for (Tuple<BlockState, BlockPos> pair : entityModels) {
				BlockPos blockPos = pair.getSecond();
				StructureRenderHandler.renderBlock(matrixStack, new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()), pair.getFirst(), entityVertexConsumer, BlockRenderType.ENTITYBLOCK_ANIMATED);
			}

			if (!didAny) {
				// Nothing was generated, tell the user this through a chat message and re-set the structure information.
				StructureRenderHandler.setStructure(null, Direction.NORTH, null);

				TranslatableText message = new TranslatableText(GuiLangKeys.GUI_PREVIEW_COMPLETE);
				message.setStyle(Style.EMPTY.withColor(Formatting.GREEN));
				player.sendMessage(message, false);

			} else if (!StructureRenderHandler.showedMessage) {
				TranslatableText message = new TranslatableText(GuiLangKeys.GUI_PREVIEW_NOTICE);
				message.setStyle(Style.EMPTY.withColor(Formatting.GREEN));

				player.sendMessage(message, false);
				StructureRenderHandler.showedMessage = true;
			}
		}
	}

	private static boolean renderComponentInWorld(World world, BuildBlock buildBlock, VertexConsumerProvider entityVertexConsumer, MatrixStack matrixStack, BlockPos pos, BlockRenderType blockRenderType) {
		// Don't render this block if it's going to overlay a non-air/water block.
		BlockState targetBlock = world.getBlockState(pos);
		if (targetBlock.getMaterial() != Material.AIR && targetBlock.getMaterial() != Material.WATER) {
			return false;
		}

		StructureRenderHandler.doRenderComponent(buildBlock, pos, entityVertexConsumer, matrixStack, blockRenderType);

		if (buildBlock.getSubBlock() != null) {
			Block foundBlock = Registry.BLOCK.get(buildBlock.getSubBlock().getResourceLocation());
			BlockState blockState = foundBlock.getDefaultState();

			BuildBlock subBlock = BuildBlock.SetBlockState(
					StructureRenderHandler.currentConfiguration,
					world, StructureRenderHandler.currentConfiguration.pos,
					assumedNorth,
					buildBlock.getSubBlock(),
					foundBlock,
					blockState,
					StructureRenderHandler.currentStructure);

			BlockPos subBlockPos = subBlock.getStartingPosition().getRelativePosition(
					StructureRenderHandler.currentConfiguration.pos,
					StructureRenderHandler.currentStructure.getClearSpace().getShape().getDirection(),
					StructureRenderHandler.currentConfiguration.houseFacing);

			BlockRenderType subBlockRenderType = subBlock.getBlockState().getRenderType();

			return StructureRenderHandler.renderComponentInWorld(world, subBlock, entityVertexConsumer, matrixStack, subBlockPos, subBlockRenderType);
		}

		return true;
	}

	private static void doRenderComponent(BuildBlock buildBlock, BlockPos pos, VertexConsumerProvider entityVertexConsumer, MatrixStack matrixStack, BlockRenderType blockRenderType) {
		BlockState state = buildBlock.getBlockState();
		StructureRenderHandler.renderBlock(matrixStack, new Vec3d(pos.getX(), pos.getY(), pos.getZ()), state, entityVertexConsumer, blockRenderType);
	}

	private static void renderBlock(MatrixStack matrixStack, Vec3d pos, BlockState state, VertexConsumerProvider entityVertexConsumer, BlockRenderType blockRenderType) {
		MinecraftClient minecraft = MinecraftClient.getInstance();
		Camera camera = minecraft.getEntityRenderDispatcher().camera;
		Vec3d projectedView = camera.getPos();
		double renderPosX = projectedView.getX();
		double renderPosY = projectedView.getY();
		double renderPosZ = projectedView.getZ();

		// push
		matrixStack.push();

		// Translate function.
		matrixStack.translate(-renderPosX, -renderPosY, -renderPosZ);

		BlockRenderManager renderer = minecraft.getBlockRenderManager();

		// Translate.
		matrixStack.translate(pos.x, pos.y, pos.z);
		BakedModel bakedModel = renderer.getModel(state);

		if (blockRenderType == BlockRenderType.MODEL) {
			// getColor function.
			int color = minecraft.getBlockColors().getColor(state, null, null, 0);
			float r = (float) (color >> 16 & 255) / 255.0F;
			float g = (float) (color >> 8 & 255) / 255.0F;
			float b = (float) (color & 255) / 255.0F;

			renderer.getModelRenderer().render(
					matrixStack.peek(),
					// TODO: This used to be "getTranslucentBlockType"
					entityVertexConsumer.getBuffer(TexturedRenderLayers.getItemEntityTranslucentCull()),
					state,
					bakedModel,
					r,
					g,
					b,
					0xF000F0,
					OverlayTexture.DEFAULT_UV);
		}

		// pop
		matrixStack.pop();
	}

	private static void renderModel(MatrixStack matrixStack, Vector3d pos, BlockState state, VertexConsumerProvider entityVertexConsumer) {

	}


}