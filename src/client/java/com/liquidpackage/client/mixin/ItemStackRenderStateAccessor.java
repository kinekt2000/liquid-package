package com.liquidpackage.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.cuboid.ItemTransform;

@Mixin(ItemStackRenderState.class)
public interface ItemStackRenderStateAccessor {
    @Accessor("layers")
    ItemStackRenderState.LayerRenderState[] getLayers();

    @Accessor("activeLayerCount")
    int getActiveLayerCount();

    @Accessor("activeLayerCount")
    void setActiveLayerCount(int count);

    @Mixin(ItemStackRenderState.LayerRenderState.class)
    interface LayerRenderStateAccessor {
        @Accessor("itemTransform")
        ItemTransform getItemTransform();
    }
}
