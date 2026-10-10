// SPDX-License-Identifier: MIT
package com.liquidpackage.client.rei;

import com.liquidpackage.blocks.BlocksLib;
import com.liquidpackage.client.jei.SublimationRecipes;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;

/** Loaded by REI through the {@code rei_client} entrypoint; never touched when REI is absent. */
public class LiquidPackageReiPlugin implements REIClientPlugin {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new SublimationReiCategory());
        registry.addWorkstations(SublimationDisplay.ID, EntryStacks.of(BlocksLib.SUBLIMATOR.item()));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        SublimationRecipes.fromConfig().forEach(recipe -> registry.add(new SublimationDisplay(recipe)));
    }
}
