package com.liquidpackage.client.texture;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import com.liquidpackage.LiquidPackage;
import com.liquidpackage.config.Config;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.MapCodec;

import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ARGB;

/**
 * Adds one sprite per configured shell ({@code liquid-package:item/shell_N}) to
 * the item atlas. Each sprite is the empty shell texture with its body filled
 * by the fluid's still texture, keeping its animation.
 */
public class FluidShellSpriteSource implements SpriteSource {
    public static final Identifier TYPE = LiquidPackage.id("fluid_shell");
    public static final MapCodec<FluidShellSpriteSource> CODEC = MapCodec.unit(FluidShellSpriteSource::new);

    public static final Identifier SHELL_TEXTURE = LiquidPackage.id("item/empty_shell");

    /** Shell pixels darker than this are the outline and stay fully opaque. */
    private static final int OUTLINE_LUMINANCE = 0x60;

    /** Opacity of the shell body over the fluid: min + gloss * luminance. */
    private static final float BODY_MIN_OPACITY = 0.25f;
    private static final float BODY_GLOSS_OPACITY = 0.35f;

    /** Vanilla water texture is grayscale and tinted by the biome at runtime. */
    private static final Map<Identifier, Integer> TINTS = Map.of(
            Identifier.withDefaultNamespace("water"), 0x3F76E4);

    @Override
    public void run(ResourceManager resourceManager, Output output) {
        Optional<Resource> shell = resourceManager.getResource(TEXTURE_ID_CONVERTER.idToFile(SHELL_TEXTURE));
        if (shell.isEmpty()) {
            LiquidPackage.LOGGER.error("Missing shell texture {}", SHELL_TEXTURE);
            return;
        }

        // Slots without a config entry get the plain shell so their models resolve
        for (int i = 0; i < Config.CAPACITY; ++i) {
            Identifier spriteId = LiquidPackage.id("item/shell_%d".formatted(i));
            Identifier fluidId = i < LiquidPackage.CONFIG.size()
                    ? Identifier.parse(LiquidPackage.CONFIG.get(i).liquidId())
                    : null;
            output.add(spriteId, new Loader(spriteId, fluidId, resourceManager, shell.get()));
        }
    }

    @Override
    public MapCodec<? extends SpriteSource> codec() {
        return CODEC;
    }

    private record Loader(Identifier spriteId, @Nullable Identifier fluidId, ResourceManager resourceManager,
            Resource shell)
            implements DiscardableLoader {

        @Override
        public @Nullable SpriteContents get(SpriteResourceLoader loader) {
            try (NativeImage shellImage = read(shell)) {
                int w = shellImage.getWidth();
                int h = shellImage.getHeight();
                FrameSize shellSize = new FrameSize(w, h);

                Optional<Resource> fluid = fluidId == null
                        ? Optional.empty()
                        : resourceManager.getResource(TEXTURE_ID_CONVERTER.idToFile(stillTexture(fluidId)));
                if (fluid.isEmpty()) {
                    if (fluidId != null)
                        LiquidPackage.LOGGER.warn("No still texture for fluid {}, using plain shell", fluidId);
                    NativeImage plain = new NativeImage(w, h, false);
                    plain.copyFrom(shellImage);
                    return new SpriteContents(spriteId, shellSize, plain);
                }

                Optional<AnimationMetadataSection> animation = fluid.get().metadata()
                        .getSection(AnimationMetadataSection.TYPE);
                try (NativeImage fluidImage = read(fluid.get())) {
                    FrameSize fluidFrame = animation
                            .map(a -> a.calculateFrameSize(fluidImage.getWidth(), fluidImage.getHeight()))
                            .orElseGet(() -> new FrameSize(fluidImage.getWidth(), fluidImage.getWidth()));
                    int frames = Math.max(1, fluidImage.getHeight() / fluidFrame.height());

                    // Same vertical strip layout as the fluid texture, one shell per fluid frame
                    NativeImage result = new NativeImage(w, h * frames, false);
                    int tint = TINTS.getOrDefault(fluidId, 0xFFFFFF);
                    for (int frame = 0; frame < frames; ++frame) {
                        compose(shellImage, fluidImage, fluidFrame, frame, tint, result);
                    }
                    return new SpriteContents(spriteId, shellSize, result,
                            frames > 1 ? animation : Optional.empty(), List.of(), Optional.empty());
                }
            } catch (IOException e) {
                LiquidPackage.LOGGER.error("Unable to build sprite {}", spriteId, e);
                return null;
            }
        }

        private static Identifier stillTexture(Identifier fluidId) {
            return fluidId.withPath(path -> "block/" + path + "_still");
        }

        private static NativeImage read(Resource resource) throws IOException {
            try (InputStream in = resource.open()) {
                return NativeImage.read(in);
            }
        }

        /**
         * Draws the fluid under the shell: outline pixels stay opaque, the
         * glossy body becomes translucent so the fluid shows through it.
         * Writes one animation frame of the result.
         */
        private static void compose(NativeImage shell, NativeImage fluid, FrameSize fluidFrame, int frame, int tint,
                NativeImage out) {
            int w = shell.getWidth();
            int h = shell.getHeight();
            for (int y = 0; y < h; ++y) {
                for (int x = 0; x < w; ++x) {
                    int pixel = shell.getPixel(x, y);
                    if (ARGB.alpha(pixel) == 0)
                        continue;

                    int luminance = ARGB.greyscale(pixel) & 0xFF;
                    float shellOpacity = luminance < OUTLINE_LUMINANCE
                            ? 1f
                            : BODY_MIN_OPACITY + BODY_GLOSS_OPACITY * luminance / 255f;

                    int fx = x * fluidFrame.width() / w;
                    int fy = frame * fluidFrame.height() + y * fluidFrame.height() / h;
                    int under = ARGB.multiply(ARGB.opaque(fluid.getPixel(fx, fy)), ARGB.opaque(tint));

                    int r = mix(ARGB.red(pixel), ARGB.red(under), shellOpacity);
                    int g = mix(ARGB.green(pixel), ARGB.green(under), shellOpacity);
                    int b = mix(ARGB.blue(pixel), ARGB.blue(under), shellOpacity);
                    out.setPixel(x, frame * h + y, ARGB.color(ARGB.alpha(pixel), r, g, b));
                }
            }
        }

        private static int mix(int over, int under, float overOpacity) {
            return Math.round(over * overOpacity + under * (1f - overOpacity));
        }

        @Override
        public void discard() {
        }
    }
}
