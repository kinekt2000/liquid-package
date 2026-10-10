package com.liquidpackage.client.rei;

import java.util.ArrayList;
import java.util.List;

import com.liquidpackage.blocks.BlocksLib;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;

public class SublimationReiCategory implements DisplayCategory<SublimationDisplay> {
    @Override
    public CategoryIdentifier<? extends SublimationDisplay> getCategoryIdentifier() {
        return SublimationDisplay.ID;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("liquid-package.jei.sublimation");
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(BlocksLib.SUBLIMATOR.item());
    }

    @Override
    public int getDisplayHeight() {
        return 56;
    }

    @Override
    public List<Widget> setupDisplay(SublimationDisplay display, Rectangle bounds) {
        Point origin = new Point(bounds.getCenterX() - 55, bounds.getY() + 8);

        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));
        widgets.add(Widgets.createSlot(new Point(origin.x, origin.y))
                .entries(display.getInputEntries().get(0)).markInput());
        widgets.add(Widgets.createSlot(new Point(origin.x + 24, origin.y))
                .entries(display.getInputEntries().get(1)).markInput());
        widgets.add(Widgets.createArrow(new Point(origin.x + 52, origin.y))
                .animationDurationTicks(display.ticks()));
        widgets.add(Widgets.createSlot(new Point(origin.x + 88, origin.y))
                .entries(display.getOutputEntries().get(0)).markOutput());

        widgets.add(Widgets.createLabel(new Point(bounds.getCenterX(), origin.y + 24),
                Component.translatable("liquid-package.jei.time", display.ticks() / 20)).noShadow().centered());
        widgets.add(Widgets.createLabel(new Point(bounds.getCenterX(), origin.y + 34),
                Component.translatable("liquid-package.jei.heat")).noShadow().centered());
        return widgets;
    }
}
