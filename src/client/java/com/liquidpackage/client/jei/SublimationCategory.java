// SPDX-License-Identifier: MIT
package com.liquidpackage.client.jei;

import com.liquidpackage.LiquidPackage;
import com.liquidpackage.blockEntity.SublimatorBlockEntity;
import com.liquidpackage.blocks.BlocksLib;
import com.liquidpackage.items.ItemsLib;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class SublimationCategory implements IRecipeCategory<SublimationRecipe> {
    public static final IRecipeType<SublimationRecipe> TYPE = IRecipeType.create(
            LiquidPackage.id("sublimation"), SublimationRecipe.class);

    private static final int WIDTH = 120;
    private static final int HEIGHT = 56;

    private final IDrawable icon;

    public SublimationCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(BlocksLib.SUBLIMATOR.item());
    }

    @Override
    public IRecipeType<SublimationRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("liquid-package.jei.sublimation");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SublimationRecipe recipe, IFocusGroup focuses) {
        long amount = recipe.buckets() * FluidConstants.BUCKET;

        builder.addInputSlot(1, 3)
                .setFluidRenderer(amount, false, 16, 34)
                .add(recipe.fluid(), amount);

        builder.addInputSlot(32, 3)
                .add(new ItemStack(ItemsLib.EMPTY_SHELL.item()));

        builder.addOutputSlot(92, 3)
                .add(recipe.output());
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder extras, SublimationRecipe recipe, IFocusGroup focuses) {
        int ticks = recipe.buckets() * SublimatorBlockEntity.TICKS_PER_BUCKET;
        extras.addAnimatedRecipeArrowWidget(ticks).setPosition(60, 3);

        extras.addText(java.util.List.of(
                Component.translatable("liquid-package.jei.time", ticks / 20),
                Component.translatable("liquid-package.jei.heat")), WIDTH - 24, 24)
                .setPosition(24, 28);
    }
}
