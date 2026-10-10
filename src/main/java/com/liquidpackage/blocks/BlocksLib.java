package com.liquidpackage.blocks;

import com.liquidpackage.LiquidPackage;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class BlocksLib {
        public static record Bundle(Block block, ResourceKey<Block> keyBlock, BlockItem item,
                        ResourceKey<Item> keyItem) {
        }

        public static final Bundle SUBLIMATOR = create(SublimatorBlock.ID, SublimatorBlock.KEY, new SublimatorBlock());

        // private static Bundle create(String name, Function<BlockBehaviour.Properties,
        // Block> function) {

        // Identifier id = Identifier.fromNamespaceAndPath(LiquidPackage.MOD_ID, name);

        // ResourceKey<Block> keyBlock = ResourceKey.create(Registries.BLOCK, id);
        // Block block = function.apply(BlockBehaviour.Properties.of().setId(keyBlock));

        // return create(id, keyBlock, block);
        // }

        private static Bundle create(Identifier id, ResourceKey<Block> keyBlock, Block block) {

                ResourceKey<Item> keyItem = ResourceKey.create(Registries.ITEM, id);
                BlockItem item = new BlockItem(block, new Item.Properties().useBlockDescriptionPrefix().setId(keyItem));

                Registry.register(BuiltInRegistries.ITEM, id, item);
                Registry.register(BuiltInRegistries.BLOCK, id, block);

                return new Bundle(block, keyBlock, item, keyItem);
        }

        public static void register() {
                LiquidPackage.LOGGER.info("Registering mod blocks for " + LiquidPackage.MOD_ID);
        }
}
