// SPDX-License-Identifier: MIT
package com.liquidpackage.client.rei;

import java.util.List;

import com.liquidpackage.LiquidPackage;
import com.liquidpackage.blockEntity.SublimatorBlockEntity;
import com.liquidpackage.client.jei.SublimationRecipe;
import com.liquidpackage.items.ItemsLib;

import org.jspecify.annotations.Nullable;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;

public class SublimationDisplay extends BasicDisplay {
    public static final CategoryIdentifier<SublimationDisplay> ID = CategoryIdentifier
            .of(LiquidPackage.id("sublimation"));

    private final int ticks;

    public SublimationDisplay(SublimationRecipe recipe) {
        super(List.of(
                EntryIngredients.of(recipe.fluid(), recipe.buckets() * FluidConstants.BUCKET),
                EntryIngredients.of(ItemsLib.EMPTY_SHELL.item())),
                List.of(EntryIngredients.of(recipe.output())));
        this.ticks = recipe.buckets() * SublimatorBlockEntity.TICKS_PER_BUCKET;
    }

    public int ticks() {
        return ticks;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ID;
    }

    /** Built locally from the config on every client, so it is never sent over the network. */
    @Override
    public @Nullable DisplaySerializer<? extends Display> getSerializer() {
        return null;
    }
}
