package com.keinloree.client.vfx;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * RenderTypes propios del motor de VFX. Extiende RenderType solo para poder usar los RenderStateShard protegidos.
 * Todos: sin culling, sin escribir profundidad, con test de profundidad, sin luz de mapa.
 */
public final class ModRenderTypes extends RenderType {
    private static final Map<ResourceLocation, RenderType> GLOW = new HashMap<>();
    private static final Map<ResourceLocation, RenderType> ALPHA = new HashMap<>();
    private static final List<RenderType> ALL = new ArrayList<>();
    private static RenderType beamFlat;
    private static RenderType alphaFlat;

    private static final ShaderStateShard TEX_COLOR_SHADER = new ShaderStateShard(GameRenderer::getPositionTexColorShader);

    private ModRenderTypes() {
        super("keinloree_unused", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, false,
                () -> { }, () -> { });
    }

    /** Aditivo con textura (POSITION_TEX_COLOR): el brillo sale de rgb * color, la textura aporta la forma. */
    public static synchronized RenderType glowAdditive(ResourceLocation tex) {
        return GLOW.computeIfAbsent(tex, t -> track(create("keinloree_glow_" + t.getPath(),
                DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 4096, false, false,
                CompositeState.builder()
                        .setShaderState(TEX_COLOR_SHADER)
                        .setTextureState(new TextureStateShard(t, true, false))
                        .setTransparencyState(ADDITIVE_TRANSPARENCY)
                        .setWriteMaskState(COLOR_WRITE)
                        .setCullState(NO_CULL)
                        .setDepthTestState(LEQUAL_DEPTH_TEST)
                        .createCompositeState(false))));
    }

    /** Aditivo sin textura (POSITION_COLOR): haces, esferas, conos. */
    public static synchronized RenderType beamAdditive() {
        if (beamFlat == null) {
            beamFlat = track(create("keinloree_beam_additive", DefaultVertexFormat.POSITION_COLOR,
                    VertexFormat.Mode.QUADS, 4096, false, false,
                    CompositeState.builder()
                            .setShaderState(POSITION_COLOR_SHADER)
                            .setTransparencyState(ADDITIVE_TRANSPARENCY)
                            .setWriteMaskState(COLOR_WRITE)
                            .setCullState(NO_CULL)
                            .setDepthTestState(LEQUAL_DEPTH_TEST)
                            .createCompositeState(false)));
        }
        return beamFlat;
    }

    /** Mezcla normal con textura (humo oscuro). */
    public static synchronized RenderType glowAlpha(ResourceLocation tex) {
        return ALPHA.computeIfAbsent(tex, t -> track(create("keinloree_alpha_" + t.getPath(),
                DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 4096, false, false,
                CompositeState.builder()
                        .setShaderState(TEX_COLOR_SHADER)
                        .setTextureState(new TextureStateShard(t, true, false))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setWriteMaskState(COLOR_WRITE)
                        .setCullState(NO_CULL)
                        .setDepthTestState(LEQUAL_DEPTH_TEST)
                        .createCompositeState(false))));
    }

    /** Mezcla normal sin textura (esferas oscuras). */
    public static synchronized RenderType alphaFlat() {
        if (alphaFlat == null) {
            alphaFlat = track(create("keinloree_alpha_flat", DefaultVertexFormat.POSITION_COLOR,
                    VertexFormat.Mode.QUADS, 4096, false, false,
                    CompositeState.builder()
                            .setShaderState(POSITION_COLOR_SHADER)
                            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                            .setWriteMaskState(COLOR_WRITE)
                            .setCullState(NO_CULL)
                            .setDepthTestState(LEQUAL_DEPTH_TEST)
                            .createCompositeState(false)));
        }
        return alphaFlat;
    }

    private static RenderType track(RenderType t) {
        ALL.add(t);
        return t;
    }

    /** Todos los tipos creados hasta ahora (para reservar buffers propios). */
    static synchronized List<RenderType> all() {
        return new ArrayList<>(ALL);
    }
}
