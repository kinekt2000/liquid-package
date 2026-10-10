// SPDX-License-Identifier: MIT
package com.liquidpackage.blockEntity;

import com.liquidpackage.LiquidPackage;
import com.liquidpackage.blocks.BlocksLib;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BlockEntitiesLib {
    public static record Bundle<T extends BlockEntity>(BlockEntityType<T> type, ResourceKey<BlockEntityType<?>> key) {
    }

    public final static Bundle<SublimatorBlockEntity> SUBLIMATOR_BE = create("sublimator_be",
            SublimatorBlockEntity::new, BlocksLib.SUBLIMATOR.block());

    private static <T extends BlockEntity> Bundle<T> create(String name,
            FabricBlockEntityTypeBuilder.Factory<T> factory, Block block) {
        Identifier id = Identifier.fromNamespaceAndPath(LiquidPackage.MOD_ID, name);

        ResourceKey<BlockEntityType<?>> key = ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, id);
        BlockEntityType<T> type = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id,
                FabricBlockEntityTypeBuilder.create(factory, block).build());

        return new Bundle<>(type, key);
    }

    public static void register() {
        LiquidPackage.LOGGER.info("Registering mod block entities for " + LiquidPackage.MOD_ID);
    }
}
