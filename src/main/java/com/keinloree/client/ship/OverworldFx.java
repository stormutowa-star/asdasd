package com.keinloree.client.ship;

import com.keinloree.entity.CargoShip;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import org.joml.Matrix4f;

/**
 * Efectos del Overworld que sustituyen a los vanilla (Forge los guarda en un HashMap y el nuestro entra despues):
 * identicos salvo que NO se dibujan nubes cuando la camara esta dentro de un destructor (anclado o en vuelo) o el
 * jugador va en una cabina (caza o nave de carga).
 */
public final class OverworldFx extends DimensionSpecialEffects.OverworldEffects {
    private static boolean cockpit;

    public static void register(RegisterDimensionSpecialEffectsEvent event) {
        event.register(BuiltinDimensionTypes.OVERWORLD_EFFECTS, new OverworldFx());
    }

    /** Otros paquetes (cazas) llaman aqui cada tick: true = el jugador local esta en una cabina cerrada. */
    public static void setInCockpit(boolean value) {
        cockpit = value;
    }

    public static boolean isInCockpit() {
        return cockpit;
    }

    @Override
    public boolean renderClouds(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, double camX, double camY,
                                double camZ, Matrix4f projectionMatrix) {
        return suppressClouds(new Vec3(camX, camY, camZ));
    }

    /** true si en esta posicion de camara no deben verse nubes. */
    public static boolean suppressClouds(Vec3 cam) {
        if (cockpit || ShipClient.shipContaining(cam) != null) {
            return true;
        }
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && mc.player.getVehicle() instanceof CargoShip;
    }
}
