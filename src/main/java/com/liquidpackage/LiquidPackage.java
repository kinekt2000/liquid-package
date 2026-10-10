// SPDX-License-Identifier: MIT
package com.liquidpackage;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liquidpackage.blockEntity.BlockEntitiesLib;
import com.liquidpackage.blocks.BlocksLib;
import com.liquidpackage.config.Config;
import com.liquidpackage.config.ConfigReader;
import com.liquidpackage.creative.CreativeTabsLib;
import com.liquidpackage.items.ItemsLib;
import com.liquidpackage.menu.MenuLib;

public class LiquidPackage implements ModInitializer {
	public static final String MOD_ID = "liquid-package";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static Config CONFIG = Config.DEFAULT;

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing {}", MOD_ID);
		CONFIG = ConfigReader.request(MOD_ID);
		ConfigReader.save(CONFIG, MOD_ID);

		BlockEntitiesLib.register();
		BlocksLib.register();
		CreativeTabsLib.register();
		ItemsLib.register();
		MenuLib.register();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
