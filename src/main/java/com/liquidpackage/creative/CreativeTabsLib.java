package com.liquidpackage.creative;

import com.liquidpackage.LiquidPackage;
import com.liquidpackage.blocks.BlocksLib;
import com.liquidpackage.items.ItemsLib;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class CreativeTabsLib {
    public static final CreativeModeTab LIQUID_PACKAGE_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(LiquidPackage.MOD_ID, "liquidpackage"),
            FabricCreativeModeTab.builder()
                    .icon(() -> new ItemStack(BlocksLib.SUBLIMATOR.item()))
                    .title(Component.translatable("creativemodetab.liquid-package.tabname"))
                    .displayItems((enabledFeatures, entries) -> {
                        entries.accept(BlocksLib.SUBLIMATOR.item());
                        entries.accept(ItemsLib.EMPTY_SHELL.item());

                        for (int index = 0; index < LiquidPackage.CONFIG.size(); ++index) {
                            entries.accept(ItemsLib.genericItems[index].item());
                        }
                    })
                    .build());

    public static void register() {
        LiquidPackage.LOGGER.info("Registering creative tabs for {}", LiquidPackage.MOD_ID);
    }
}
