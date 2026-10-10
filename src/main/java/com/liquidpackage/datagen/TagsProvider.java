// SPDX-License-Identifier: MIT
package com.liquidpackage.datagen;

import java.util.concurrent.CompletableFuture;

import com.liquidpackage.blocks.BlocksLib;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.tags.BlockTags;

public class TagsProvider {
    public static class Blocks extends FabricTagsProvider.BlockTagsProvider {

        public Blocks(FabricPackOutput output, CompletableFuture<Provider> registryLookupFuture) {
            super(output, registryLookupFuture);
        }

        @Override
        protected void addTags(HolderLookup.Provider registries) {
            builder(BlockTags.MINEABLE_WITH_PICKAXE)
                    .add(BlocksLib.SUBLIMATOR.keyBlock());

            builder(BlockTags.NEEDS_STONE_TOOL)
                    .add(BlocksLib.SUBLIMATOR.keyBlock());
        }
    }

}
