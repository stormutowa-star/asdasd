package com.keinloree.vfx;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.keinloree.story.StoryCommands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;

/** Comando de pruebas {@code /keinloree vfx <tipo|all>}: lanza el efecto delante del jugador. */
public final class VfxCommand {
    private VfxCommand() {
    }

    /** Engancha el subcomando (llamar una vez, antes de RegisterCommandsEvent). */
    public static void init() {
        StoryCommands.extend(VfxCommand::extend);
    }

    private static void extend(LiteralArgumentBuilder<CommandSourceStack> root) {
        List<String> names = new ArrayList<>();
        for (VfxType t : VfxType.values()) {
            names.add(t.cmd());
        }
        names.add("all");
        root.then(Commands.literal("vfx")
                .then(Commands.argument("tipo", StringArgumentType.word())
                        .suggests((c, b) -> SharedSuggestionProvider.suggest(names, b))
                        .executes(c -> {
                            ServerPlayer p = c.getSource().getPlayerOrException();
                            String n = StringArgumentType.getString(c, "tipo");
                            if (n.equals("all")) {
                                demoAll((ServerLevel) p.level(), p);
                                return 1;
                            }
                            for (VfxType t : VfxType.values()) {
                                if (t.cmd().equals(n)) {
                                    demo((ServerLevel) p.level(), t, p.getEyePosition().add(forward(p).scale(7.0D)), p);
                                    return 1;
                                }
                            }
                            c.getSource().sendFailure(Component.literal("Tipo de VFX desconocido: " + n));
                            return 0;
                        })));
    }

    private static Vec3 forward(ServerPlayer p) {
        Vec3 f = p.getLookAngle();
        Vec3 h = new Vec3(f.x, 0.0D, f.z);
        return h.lengthSqr() < 1.0E-6D ? new Vec3(0.0D, 0.0D, 1.0D) : h.normalize();
    }

    /** Lanza todos los tipos en fila (izquierda a derecha) a 9 bloques delante del jugador. */
    private static void demoAll(ServerLevel level, ServerPlayer p) {
        Vec3 f = forward(p);
        Vec3 right = new Vec3(-f.z, 0.0D, f.x);
        VfxType[] all = VfxType.values();
        for (int i = 0; i < all.length; i++) {
            double off = (i - (all.length - 1) / 2.0D) * 4.5D;
            Vec3 at = p.getEyePosition().add(f.scale(14.0D)).add(right.scale(-off));
            demo(level, all[i], at, p);
        }
    }

    /** Un efecto de ejemplo de cada tipo centrado en {@code at} (a la altura de los ojos del jugador). */
    public static void demo(ServerLevel level, VfxType type, Vec3 at, ServerPlayer p) {
        Vec3 f = forward(p);
        Vec3 right = new Vec3(-f.z, 0.0D, f.x);
        Vec3 ground = new Vec3(at.x, p.getY() + 0.1D, at.z);
        int g = Vfx.GREEN;
        switch (type) {
            case BEAM -> Vfx.beam(level, at.add(right.scale(3.5D)).add(0.0D, -0.6D, 0.0D), at.subtract(right.scale(3.5D)).add(0.0D, 0.6D, 0.0D), g, 0.3F, 60);
            case RING -> Vfx.ring(level, at.add(0.0D, 1.0D, 0.0D), f, g, 2.2F, 60);
            case SHOCKWAVE -> Vfx.shockwave(level, ground, g, 7.0F, 40);
            case SLASH -> Vfx.slash(level, at.add(0.0D, -0.4D, 0.0D).subtract(right.scale(1.6D)), p.getYRot() + 90.0F, 0.0F, 75.0F, 3.2F, 0xFF5A3C, 24);
            case ORB -> Vfx.orb(level, at, g, 1.4F, 70);
            case PILLAR -> Vfx.pillar(level, ground, 0.0F, g, 80);
            case RUNE_CIRCLE -> Vfx.runeCircle(level, ground.add(0.0D, 0.05D, 0.0D), g, 4.0F, 100);
            case LIGHTNING -> Vfx.lightning(level, at.add(0.0D, 3.0D, 0.0D), ground, g, 50);
            case CODE_RAIN -> Vfx.codeRain(level, ground, 4.0F, 90);
            case BURST -> Vfx.burst(level, at, g, 40, 0.5F);
            case DISTORTION -> Vfx.distortion(level, at, g, 1.8F, 70);
            case TIME_FREEZE -> Vfx.timeFreeze(level, at, 2.6F, 80);
            case HYPERSPACE -> Vfx.hyperspace(level, at, right, 1.0F);
            case TRAIL -> Vfx.trail(level, p, g, 100);
            case EXPLOSION -> Vfx.explosion(level, ground.add(0.0D, 0.1D, 0.0D), 1.6F);
            case MUZZLE -> Vfx.muzzle(level, at, right.add(0.0D, 0.3D, 0.0D).normalize(), g);
        }
    }
}
