// SPDX-License-Identifier: MIT
package com.liquidpackage.client.datagen;

import com.liquidpackage.items.ItemsLib;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class GenModelProvider extends FabricModelProvider {

    private static void generateGenericItem(ItemModelGenerators itemModelGenerators, ResourceKey<Item> resourceKey) {
        Identifier itemId = resourceKey.identifier();

        // Sprite is composed at runtime by FluidShellSpriteSource
        Material shellSprite = new Material(itemId.withPrefix("item/"));

        ModelTemplates.FLAT_ITEM.create(
                itemId.withPrefix("item/"),
                TextureMapping.layer0(shellSprite),
                itemModelGenerators.modelOutput);
    }

    public GenModelProvider(FabricPackOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockModelGenerators) {
        ModelSublimator.generate(blockModelGenerators);
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerators) {
        itemModelGenerators.generateFlatItem(ItemsLib.EMPTY_SHELL.item(), ModelTemplates.FLAT_ITEM);

        for (var generic : ItemsLib.genericItems) {
            generateGenericItem(itemModelGenerators, generic.key());
        }
    }
}
