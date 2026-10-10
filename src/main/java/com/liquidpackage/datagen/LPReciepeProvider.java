package com.liquidpackage.datagen;

import java.util.concurrent.CompletableFuture;

import com.liquidpackage.blocks.BlocksLib;
import com.liquidpackage.items.ItemsLib;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;

public class LPReciepeProvider extends FabricRecipeProvider {

    public LPReciepeProvider(FabricPackOutput output, CompletableFuture<Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public String getName() {
        return "LiquidPackageRecipes";
    }

    @Override
    protected RecipeProvider createRecipeProvider(Provider registries, RecipeOutput output) {
        return new RecipeProvider(registries, output) {
            @Override
            public void buildRecipes() {
                shapeless(RecipeCategory.MISC, ItemsLib.EMPTY_SHELL.item())
                        .requires(ItemTags.COALS)
                        .unlockedBy(getHasName(Items.COAL), has(ItemTags.COALS))
                        .save(output);

                shaped(RecipeCategory.MISC, BlocksLib.SUBLIMATOR.block().asItem())
                        .pattern("CSC")
                        .pattern("BFB")
                        .pattern("BIB")
                        .define('C', Ingredient.of(Items.CALCITE))
                        .define('S', ItemsLib.EMPTY_SHELL.item())
                        .define('B', Ingredient.of(Items.COPPER_BARS.weathering().unaffected()))
                        .define('F', Ingredient.of(Blocks.BLAST_FURNACE))
                        .define('I', Ingredient.of(Blocks.IRON_BLOCK))
                        .unlockedBy(getHasName(ItemsLib.EMPTY_SHELL.item()), has(ItemsLib.EMPTY_SHELL.item()))
                        .save(output);
            }
        };
    }
}
