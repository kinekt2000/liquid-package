// SPDX-License-Identifier: MIT
package com.liquidpackage.client;

import com.liquidpackage.client.datagen.GenModelProvider;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class LiquidPackageDataGenClient implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDatagen) {
        var pack = fabricDatagen.createPack();
        pack.addProvider(GenModelProvider::new);

    }
}
