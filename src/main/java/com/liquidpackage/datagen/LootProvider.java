package com.liquidpackage.datagen;

import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.minecraft.core.HolderLookup.Provider;

public class LootProvider extends FabricBlockLootSubProvider {

    protected LootProvider(FabricPackOutput packOutput, CompletableFuture<Provider> registriesFuture) {
        super(packOutput, registriesFuture);
    }

    @Override
    public void generate() {
        dropSelf(com.liquidpackage.blocks.BlocksLib.SUBLIMATOR.block());
    }

}
