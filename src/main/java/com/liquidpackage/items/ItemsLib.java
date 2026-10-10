// SPDX-License-Identifier: MIT
package com.liquidpackage.items;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.liquidpackage.LiquidPackage;
import com.liquidpackage.config.Config;

import org.jspecify.annotations.Nullable;

import net.fabricmc.fabric.api.registry.FuelValueEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class ItemsLib {
    public static record Bundle(Item item, ResourceKey<Item> keyItem) {
    }

    public static record GenericShellBundle(Item item, ResourceKey<Item> key, Identifier liquidId, int index) {
    }

    public static class NamedItem extends Item {
        final Component baseName;
        final Identifier liquidId;

        public NamedItem(Properties properties, Component baseName, Identifier liquidId) {
            super(properties);

            this.baseName = baseName;
            this.liquidId = liquidId;
        }

        /**
         * Resolved when the name is requested, not at registration: fluids of
         * mods that initialize after this one are not registered yet by then.
         */
        @Override
        public Component getName(ItemStack itemStack) {
            Fluid fluid = BuiltInRegistries.FLUID.get(liquidId)
                    .map(net.minecraft.core.Holder::value)
                    .orElse(Fluids.EMPTY);

            String fluidTranslatableName = fluid == Fluids.EMPTY
                    ? "item.liquid-package.unknown_fluid"
                    : "block." + liquidId.toLanguageKey();

            return Component
                    .literal(baseName.getString() + ": " + Component.translatable(fluidTranslatableName).getString());
        }

    }

    public static final Bundle EMPTY_SHELL = create("empty_shell",
            properties -> new Item(properties.stacksTo(64)));

    public static final GenericShellBundle[] genericItems = new GenericShellBundle[Config.CAPACITY];
    private static final Map<Identifier, GenericShellBundle> genericItemsMap = new HashMap<>();

    static {
        for (int i = 0; i < Config.CAPACITY; ++i) {
            ResourceKey<Item> key = createResourceKey("shell_%d".formatted(i));
            genericItems[i] = new GenericShellBundle(null, key, null, i);
        }
    }

    private static Bundle create(String name, Function<Item.Properties, Item> function) {
        ResourceKey<Item> keyItem = createResourceKey(name);
        return new Bundle(registerItem(keyItem, function), keyItem);
    }

    private static Item registerItem(ResourceKey<Item> key, Function<Item.Properties, Item> function) {
        Identifier id = key.identifier();
        Properties props = new Item.Properties().setId(key).component(DataComponents.ITEM_NAME,
                Component.literal("TEST NAME"));
        return Registry.register(BuiltInRegistries.ITEM, id, function.apply(props));
    }

    private static void registerGenericItem(int index) {
        if (index >= Config.CAPACITY)
            return;
        var bundle = genericItems[index];

        Identifier liquidId = Identifier.parse(LiquidPackage.CONFIG.get(index).liquidId());
        genericItems[index] = new GenericShellBundle(
                registerItem(bundle.key(),
                        props -> new NamedItem(
                                props.stacksTo(64),
                                Component.translatable("item.liquid-package.packed_shell"),
                                liquidId)),
                bundle.key(),
                liquidId,
                bundle.index());
        genericItemsMap.put(bundle.key().identifier(), genericItems[index]);
    }

    private static ResourceKey<Item> createResourceKey(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(LiquidPackage.MOD_ID, name);
        ResourceKey<Item> keyItem = ResourceKey.create(Registries.ITEM, id);

        return keyItem;
    }

    public static GenericShellBundle getGenericById(Identifier id) {
        return genericItemsMap.get(id);
    }

    /**
     * Filled shell for a configured fluid, or null if the fluid has no config
     * entry.
     */
    public static @Nullable GenericShellBundle getGenericByLiquid(Identifier liquidId) {
        for (int i = 0; i < LiquidPackage.CONFIG.size(); ++i) {
            if (liquidId.equals(genericItems[i].liquidId()))
                return genericItems[i];
        }
        return null;
    }

    public static void register() {
        LiquidPackage.LOGGER.info("Registering items for {}", LiquidPackage.MOD_ID);

        for (int index = 0; index < LiquidPackage.CONFIG.size(); ++index) {
            registerGenericItem(index);
        }

        FuelValueEvents.BUILD.register((builder, context) -> {
            if (LiquidPackage.CONFIG.emptyShellFuelTime() > 0) {
                builder.add(ItemsLib.EMPTY_SHELL.item(), LiquidPackage.CONFIG.emptyShellFuelTime());

                for (Config.Item cfgItem : LiquidPackage.CONFIG) {
                    if (cfgItem.fuelTime() > 0) {
                        var generic = getGenericByLiquid(Identifier.parse(cfgItem.liquidId()));
                        if (generic != null) {
                            builder.add(generic.item(), cfgItem.fuelTime());
                        }
                    }
                }
            }
        });
    }
}
