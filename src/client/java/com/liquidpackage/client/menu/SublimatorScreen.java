package com.liquidpackage.client.menu;

import org.joml.Matrix3x2fStack;
import org.joml.Vector2i;

import com.liquidpackage.LiquidPackage;
import com.liquidpackage.menu.sublimator.SublimatorMenu;

import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Fluids;

public class SublimatorScreen extends AbstractContainerScreen<SublimatorMenu> {
    private static final int SCREEN_WIDTH = 176;
    private static final int SCREEN_HEIGHT = 184;

    private static final float RESET_TEXT_SCALE = 0.75f;

    private static final Identifier TX_BACKGROUND = LiquidPackage.id("textures/gui/sublimator/sublimator.png");
    private static final Identifier TX_ARROW = LiquidPackage.id("textures/gui/sublimator/arrow.png");
    private static final Identifier TX_THERMOMETER = LiquidPackage.id("textures/gui/sublimator/thermometer.png");
    private static final Identifier TX_TANK = LiquidPackage.id("textures/gui/sublimator/tank.png");

    public SublimatorScreen(SublimatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, SCREEN_WIDTH, SCREEN_HEIGHT);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelY = 6;
        this.inventoryLabelY = 91;

        Button reset = new ScaledTextButton(leftPos + 127, topPos + 85, 31, 10,
                Component.translatable("liquid-package.gui.reset"),
                button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId,
                        SublimatorMenu.BUTTON_RESET),
                RESET_TEXT_SCALE);
        reset.setTooltip(Tooltip.create(Component.translatable("liquid-package.gui.reset.tooltip")));
        addRenderableWidget(reset);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        graphics.blit(RenderPipelines.GUI_TEXTURED, TX_BACKGROUND, x, y, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, 256, 256);
        renderProgressArrows(graphics, x, y);
        renderProgressThermometer(graphics, x, y);
        renderTank(graphics, x, y);
    }

    private void renderTank(GuiGraphicsExtractor graphics, int x, int y) {
        final int TANK_WIDTH = 17;
        final int TANK_HEIGHT = 39;
        final Vector2i v2iTankPos = new Vector2i(x + 25, y + 40);

        var tank = this.menu.getBlockEntity().getFluidStorage();
        long amount = tank.amount;
        long capacity = tank.getCapacity();

        if (tank.variant.getFluid() != Fluids.EMPTY && amount > 0) {
            int fluidHeight = (int) (((float) amount / capacity) * TANK_HEIGHT);
            fluidHeight = Math.min(TANK_HEIGHT, Math.max(0, fluidHeight));

            TextureAtlasSprite sprite = getFluidTexture(tank.variant);
            int sprizeSize = sprite.contents().height();

            int color = 0xFFFFFF;
            if (minecraft.level != null) {
                color = FluidVariantRendering.getColor(tank.variant, minecraft.level,
                        this.menu.getBlockEntity().getBlockPos());
            }

            graphics.enableScissor(
                    v2iTankPos.x,
                    v2iTankPos.y,
                    v2iTankPos.x + TANK_WIDTH,
                    v2iTankPos.y + TANK_HEIGHT);

            graphics.pose().pushMatrix();
            graphics.pose().translate(v2iTankPos.x, v2iTankPos.y + TANK_HEIGHT);

            for (int tile = 0; tile < (fluidHeight + sprizeSize) / sprizeSize; ++tile) {

                graphics.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        sprite,
                        0,
                        tile * sprizeSize - fluidHeight,
                        TANK_WIDTH,
                        sprizeSize,
                        color);

            }
            graphics.pose().popMatrix();
            graphics.disableScissor();
        }

        graphics.pose().pushMatrix();
        graphics.pose().translate(v2iTankPos.x, v2iTankPos.y);

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TX_TANK,
                0, 0, 0, 0,
                TANK_WIDTH, TANK_HEIGHT, TANK_WIDTH, TANK_HEIGHT);

        graphics.pose().popMatrix();
    }

    private TextureAtlasSprite getFluidTexture(FluidVariant fluidVariant) {
        var fluidState = fluidVariant.getFluid().defaultFluidState();
        var fluidModel = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluidState);
        var sprite = fluidModel.stillMaterial().sprite();
        return sprite != null ? sprite : getMissingTexture();
    }

    private TextureAtlasSprite getMissingTexture() {
        Identifier BLOCK_ATLAS = Identifier.fromNamespaceAndPath("minecraft", "textures/atlas/blocks.png");

        return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(BLOCK_ATLAS)
                .getSprite(MissingTextureAtlasSprite.getLocation());
    }

    private void renderProgressArrows(GuiGraphicsExtractor graphics, int x, int y) {
        final int ARROW_LENGTH = 24;
        final int ARROW_WIDTH = 16;

        if (menu.isCrafting()) {
            final int progress = menu.getArrowProgress();
            final Vector2i v2iLeftArrow = new Vector2i(x + 55, y + 41);
            final Vector2i v2iRightArrow = new Vector2i(x + 99, y + 40);
            final Vector2i v2iTopArrow = new Vector2i(x + 79, y + 16);
            final Vector2i v2iBotArrow = new Vector2i(x + 80, y + 60);

            Matrix3x2fStack pose = graphics.pose();

            // left -> right
            pose.pushMatrix();
            pose.translate(v2iLeftArrow.x, v2iLeftArrow.y);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TX_ARROW, 0, 0, 0, 0, progress, ARROW_WIDTH, ARROW_LENGTH,
                    ARROW_WIDTH);
            pose.popMatrix();

            // top -> bottom
            pose.pushMatrix();
            pose.translate(v2iTopArrow.x, v2iTopArrow.y);
            pose.rotate((float) (Math.PI / 2.0));
            pose.translate(0, -ARROW_WIDTH);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TX_ARROW, 0, 0, 0, 0, progress, ARROW_WIDTH, ARROW_LENGTH,
                    ARROW_WIDTH);
            pose.popMatrix();

            // right -> left
            pose.pushMatrix();
            pose.translate(v2iRightArrow.x, v2iRightArrow.y);
            pose.rotate((float) Math.PI); // Идеальные 180 градусов
            pose.translate(-ARROW_LENGTH, -ARROW_WIDTH);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TX_ARROW, 0, 0, 0, 0, progress, ARROW_WIDTH, ARROW_LENGTH,
                    ARROW_WIDTH);
            pose.popMatrix();

            // bottom -> top
            pose.pushMatrix();
            pose.translate(v2iBotArrow.x, v2iBotArrow.y);
            pose.rotate((float) (Math.PI * 1.5));
            pose.translate(-ARROW_LENGTH, 0);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TX_ARROW, 0, 0, 0, 0, progress, ARROW_WIDTH, ARROW_LENGTH,
                    ARROW_WIDTH);
            pose.popMatrix();
        }
    }

    private void renderProgressThermometer(GuiGraphicsExtractor graphics, int x, int y) {
        final int THERMOMETER_WIDTH = 9;
        final int THERMOMETER_HEIGHT = 28;

        final int progress = menu.getHeatProgress();
        final Vector2i v2iPos = new Vector2i(x + 138, y + 52);

        if (menu.getHeatProgress() > 0) {
            Matrix3x2fStack pose = graphics.pose();

            pose.pushMatrix();
            pose.translate(v2iPos.x, v2iPos.y + THERMOMETER_HEIGHT);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TX_THERMOMETER,
                    0, 0 - progress,
                    0, THERMOMETER_HEIGHT - progress,
                    THERMOMETER_WIDTH, progress,
                    THERMOMETER_WIDTH, THERMOMETER_HEIGHT);
            pose.popMatrix();
        }
    }

}
