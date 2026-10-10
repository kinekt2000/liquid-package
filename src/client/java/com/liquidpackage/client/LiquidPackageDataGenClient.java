// SPDX-License-Identifier: MIT
package com.liquidpackage.client;

import com.liquidpackage.client.datagen.GenModelProvider;
import com.liquidpackage.datagen.LPBlockLootProvider;
import com.liquidpackage.datagen.LPReciepeProvider;
import com.liquidpackage.datagen.LPTagsProvider;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class LiquidPackageDataGenClient implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDatagen) {
        // A single pack: separate entrypoints each get their own run and hash cache,
        // and the later run deletes the earlier run's files as "stale".
        var pack = fabricDatagen.createPack();
        pack.addProvider(GenModelProvider::new);
        pack.addProvider(LPBlockLootProvider::new);
        pack.addProvider(LPTagsProvider.Blocks::new);
        pack.addProvider(LPReciepeProvider::new);
    }
}
