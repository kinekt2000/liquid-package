// SPDX-License-Identifier: MIT
package com.liquidpackage.menu;

import com.liquidpackage.LiquidPackage;
import com.liquidpackage.menu.sublimator.SublimatorMenu;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;

public class MenuLib {

    public static final MenuType<SublimatorMenu> SUBLIMATOR_MENU = Registry.register(BuiltInRegistries.MENU,
            LiquidPackage.id("sublimator_menu"),
            new ExtendedMenuType<>(SublimatorMenu::new, BlockPos.STREAM_CODEC));

    public static void register() {
        LiquidPackage.LOGGER.info("Registering menu for " + LiquidPackage.MOD_ID);
    }
}
