// SPDX-License-Identifier: MIT
package com.liquidpackage.data;

import com.liquidpackage.LiquidPackage;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class Components {
    public static final DataComponentType<Identifier> FLUID_TYPE = create("fluid_type");

    private static DataComponentType<Identifier> create(String name) {
        var id = Identifier.fromNamespaceAndPath(LiquidPackage.MOD_ID, name);
        var value = DataComponentType.<Identifier>builder().persistent(Identifier.CODEC).build();

        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id, value);
    }

}
