// SPDX-License-Identifier: MIT
package com.liquidpackage.client.datagen;

import com.liquidpackage.blocks.SublimatorBlock;
import com.liquidpackage.blocks.BlocksLib;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

public class ModelSublimator {
    public static void generate(BlockModelGenerators blockModelGenerators) {
        Block block = BlocksLib.SUBLIMATOR.block();
        var dispatch = PropertyDispatch.initial(SublimatorBlock.FACING, SublimatorBlock.HEAT_STATE,
                SublimatorBlock.LIT);

        for (SublimatorBlock.HeatState heat : SublimatorBlock.HeatState.values()) {
            for (boolean isLit : new boolean[] { true, false }) {
                String heatSuffix = heat.getSerializedName();
                String litSuffix = isLit ? "lit" : "unlit";

                Identifier modelId = createSubModel(blockModelGenerators, block, heatSuffix, litSuffix);
                MultiVariant variant = BlockModelGenerators.plainVariant(modelId);

                dispatch
                        .select(Direction.NORTH, heat, isLit, variant)
                        .select(Direction.EAST, heat, isLit, variant.with(BlockModelGenerators.Y_ROT_90))
                        .select(Direction.SOUTH, heat, isLit, variant.with(BlockModelGenerators.Y_ROT_180))
                        .select(Direction.WEST, heat, isLit, variant.with(BlockModelGenerators.Y_ROT_270));
            }
        }

        blockModelGenerators.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block).with(dispatch));

        Identifier defaultItemModel = ModelLocationUtils.getModelLocation(block, "_cold_unlit");
        blockModelGenerators.registerSimpleItemModel(block, defaultItemModel);
    }

    private static Identifier createSubModel(BlockModelGenerators generator, Block block, String frontSuffix,
            String topSuffix) {
        Material topMaterial = new Material(ModelLocationUtils.getModelLocation(block, "_top_" + topSuffix));
        Material bottomMaterial = new Material(ModelLocationUtils.getModelLocation(block, "_bottom"));
        Material sideMaterial = new Material(ModelLocationUtils.getModelLocation(block, "_side"));
        Material frontMaterial = new Material(ModelLocationUtils.getModelLocation(block, "_front_" + frontSuffix));

        TextureMapping textures = new TextureMapping()
                .put(TextureSlot.TOP, topMaterial)
                .put(TextureSlot.BOTTOM, bottomMaterial)
                .put(TextureSlot.SIDE, sideMaterial)
                .put(TextureSlot.FRONT, frontMaterial);

        return ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.createWithSuffix(block, "_" + frontSuffix + "_" + topSuffix,
                textures, generator.modelOutput);
    }

}
