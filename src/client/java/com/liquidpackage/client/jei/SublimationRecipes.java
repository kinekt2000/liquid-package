package com.liquidpackage.client.jei;

import java.util.ArrayList;
import java.util.List;

import com.liquidpackage.LiquidPackage;
import com.liquidpackage.items.ItemsLib;
import com.liquidpackage.items.ItemsLib.GenericShellBundle;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/** Recipe list shared by every recipe viewer integration, built from the config. */
public final class SublimationRecipes {
    private SublimationRecipes() {
    }

    public static List<SublimationRecipe> fromConfig() {
        List<SublimationRecipe> recipes = new ArrayList<>();

        for (int i = 0; i < LiquidPackage.CONFIG.size(); ++i) {
            GenericShellBundle bundle = ItemsLib.genericItems[i];
            Fluid fluid = BuiltInRegistries.FLUID.get(bundle.liquidId())
                    .map(net.minecraft.core.Holder::value)
                    .orElse(Fluids.EMPTY);
            if (fluid == Fluids.EMPTY) {
                LiquidPackage.LOGGER.warn("Unknown fluid {}, no recipe display for shell {}", bundle.liquidId(), i);
                continue;
            }

            int buckets = Math.max(1, LiquidPackage.CONFIG.get(i).liquidPerPackage());
            recipes.add(new SublimationRecipe(fluid, buckets, new ItemStack(bundle.item())));
        }
        return recipes;
    }
}
