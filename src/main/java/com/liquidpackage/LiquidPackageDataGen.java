// SPDX-License-Identifier: MIT
package com.liquidpackage;

import com.liquidpackage.datagen.TagsProvider;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class LiquidPackageDataGen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDatagen) {
        var pack = fabricDatagen.createPack();

        pack.addProvider(TagsProvider.Blocks::new);
    }

}
