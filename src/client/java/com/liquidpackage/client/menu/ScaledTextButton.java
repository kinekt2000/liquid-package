package com.liquidpackage.client.menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/** A vanilla looking button whose label is drawn smaller, so it fits into small buttons. */
public class ScaledTextButton extends Button {
    private static final int COLOR_ACTIVE = 0xFFFFFFFF;
    private static final int COLOR_INACTIVE = 0xFFA0A0A0;

    private final float textScale;

    public ScaledTextButton(int x, int y, int width, int height, Component message, OnPress onPress,
            float textScale) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.textScale = textScale;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        extractDefaultSprite(graphics);

        Font font = Minecraft.getInstance().font;
        graphics.pose().pushMatrix();
        graphics.pose().translate(getX() + getWidth() / 2f, getY() + getHeight() / 2f);
        graphics.pose().scale(textScale, textScale);
        graphics.centeredText(font, getMessage(), 0, -font.lineHeight / 2, active ? COLOR_ACTIVE : COLOR_INACTIVE);
        graphics.pose().popMatrix();
    }
}
