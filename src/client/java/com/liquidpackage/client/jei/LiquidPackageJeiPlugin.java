package com.liquidpackage.client.jei;

import com.liquidpackage.LiquidPackage;
import com.liquidpackage.blocks.BlocksLib;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.Identifier;

/** Loaded by JEI through the {@code jei_mod_plugin} entrypoint; never touched when JEI is absent. */
public class LiquidPackageJeiPlugin implements IModPlugin {
    @Override
    public Identifier getPluginUid() {
        return LiquidPackage.id("jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new SublimationCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(SublimationCategory.TYPE, SublimationRecipes.fromConfig());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(SublimationCategory.TYPE, BlocksLib.SUBLIMATOR.item());
    }
}
