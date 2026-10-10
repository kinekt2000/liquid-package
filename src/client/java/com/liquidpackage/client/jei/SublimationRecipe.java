package com.liquidpackage.client.jei;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

/** One configured shell: {@code buckets} of {@code fluid} turn an empty shell into {@code output}. */
public record SublimationRecipe(Fluid fluid, int buckets, ItemStack output) {
}
