// SPDX-License-Identifier: MIT
package com.liquidpackage.client;

import com.liquidpackage.client.menu.SublimatorScreen;
import com.liquidpackage.client.texture.FluidShellSpriteSource;
import com.liquidpackage.menu.MenuLib;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.SpriteSourceRegistry;
import net.minecraft.client.gui.screens.MenuScreens;

public class LiquidPackageClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MenuScreens.register(MenuLib.SUBLIMATOR_MENU, SublimatorScreen::new);
		SpriteSourceRegistry.register(FluidShellSpriteSource.TYPE, FluidShellSpriteSource.CODEC);
	}
}
