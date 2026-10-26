package com.keinloree.ship;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/** /keinloree ship status|arrive|anchor|liftoff|goto x z [yaw]|helm|destroy|remove (nivel 2, lo exige la raiz). */
public final class ShipCommands {
    private ShipCommands() {
    }

    public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("ship")
                .then(Commands.literal("status").executes(ShipCommands::status))
                .then(Commands.literal("arrive").executes(ShipCommands::arrive))
                .then(Commands.literal("anchor").executes(ShipCommands::anchor))
                .then(Commands.literal("liftoff").executes(ShipCommands::liftoff))
                .then(Commands.literal("goto").then(Commands.argument("x", DoubleArgumentType.doubleArg())
                        .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                                .executes(c -> gotoCmd(c, Float.NaN))
                                .then(Commands.argument("yaw", FloatArgumentType.floatArg(-360.0F, 360.0F))
                                        .executes(c -> gotoCmd(c, FloatArgumentType.getFloat(c, "yaw")))))))
                .then(Commands.literal("helm").executes(ShipCommands::helm))
                .then(Commands.literal("destroy").executes(ShipCommands::destroy))
                .then(Commands.literal("remove").executes(ShipCommands::remove)));
    }

    private static ServerLevel level(CommandContext<CommandSourceStack> c) {
        return c.getSource().getServer().overworld();
    }

    private static void say(CommandContext<CommandSourceStack> c, String msg) {
        c.getSource().sendSuccess(() -> Component.literal(msg), false);
    }

    private static DestroyerEntity ship(CommandContext<CommandSourceStack> c) {
        DestroyerEntity s = DestroyerEntity.find(level(c));
        if (s == null) {
            c.getSource().sendFailure(Component.literal("No hay destructor cargado"));
        }
        return s;
    }

    private static int status(CommandContext<CommandSourceStack> c) {
        ServerLevel lv = level(c);
        ShipData d = ShipData.get(lv.getServer());
        DestroyerEntity s = DestroyerEntity.find(lv);
        if (s == null) {
            say(c, "Destructor: " + (d.ship == null ? "ninguno" : "no cargado, estado=" + DestroyerEntity.stateName(d.state)
                    + " ancla=" + d.anchor.toShortString() + " r=" + d.rot));
            return d.ship == null ? 0 : 1;
        }
        say(c, String.format("Destructor: %s (%d ticks) ancla=%s r=%d pos=(%.1f, %.1f, %.1f) yaw=%.1f vel=%.2f progreso=%.2f",
                DestroyerEntity.stateName(s.getState()), s.stateTicks(), s.getAnchor().toShortString(), s.getRot(),
                s.getX(), s.getY(), s.getZ(), s.getYRot(), s.getSpeed(), s.getProgress()));
        say(c, "Pasajeros=" + s.getPassengers().size() + " piloto=" + s.getPilotId() + " tripulacion=" + ShipCrew.alive(lv, s)
                + "/" + s.crew().size() + " banda Y del timon=" + ShipTransform.MIN_ANCHOR_Y + ".." + ShipTransform.MAX_ANCHOR_Y);
        return 1;
    }

    private static int arrive(CommandContext<CommandSourceStack> c) {
        ServerLevel lv = level(c);
        if (DestroyerEntity.find(lv) != null || ShipData.get(lv.getServer()).ship != null) {
            c.getSource().sendFailure(Component.literal("Ya hay un destructor (usa /keinloree ship remove)"));
            return 0;
        }
        Vec3 at = c.getSource().getPosition();
        DestroyerEntity s = c.getSource().getEntity() instanceof ServerPlayer p
                ? DestroyerEntity.arrive(lv, p) : DestroyerEntity.arriveNear(lv, at);
        if (s == null) {
            c.getSource().sendFailure(Component.literal("No se pudo invocar el destructor"));
            return 0;
        }
        say(c, "El destructor sale del hiperespacio; ancla " + s.getAnchor().toShortString() + " r=" + s.getRot());
        return 1;
    }

    private static int anchor(CommandContext<CommandSourceStack> c) {
        DestroyerEntity s = ship(c);
        if (s == null) {
            return 0;
        }
        if (!s.releaseHelm()) {
            c.getSource().sendFailure(Component.literal("Solo se puede anclar en vuelo"));
            return 0;
        }
        say(c, "Alineando para anclar");
        return 1;
    }

    private static int liftoff(CommandContext<CommandSourceStack> c) {
        DestroyerEntity s = ship(c);
        if (s == null) {
            return 0;
        }
        if (!s.liftoff()) {
            c.getSource().sendFailure(Component.literal("Solo se puede despegar anclada"));
            return 0;
        }
        say(c, "Despegando");
        return 1;
    }

    private static int gotoCmd(CommandContext<CommandSourceStack> c, float yaw) {
        DestroyerEntity s = ship(c);
        if (s == null) {
            return 0;
        }
        double x = DoubleArgumentType.getDouble(c, "x");
        double z = DoubleArgumentType.getDouble(c, "z");
        float y = Float.isNaN(yaw) ? s.getYRot() : yaw;
        if (!s.autopilotTo(new Vec3(x, s.getY(), z), y, null)) {
            c.getSource().sendFailure(Component.literal("Ahora no se puede (estado " + DestroyerEntity.stateName(s.getState()) + ")"));
            return 0;
        }
        say(c, "Piloto automatico hacia " + (int) x + ", " + (int) z + " yaw " + y);
        return 1;
    }

    private static int helm(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        DestroyerEntity s = ship(c);
        if (s == null) {
            return 0;
        }
        return s.requestHelm(p, true) ? 1 : 0;
    }

    private static int destroy(CommandContext<CommandSourceStack> c) {
        DestroyerEntity s = ship(c);
        if (s == null) {
            return 0;
        }
        s.destroy();
        say(c, "Secuencia de destruccion iniciada");
        return 1;
    }

    private static int remove(CommandContext<CommandSourceStack> c) {
        ServerLevel lv = level(c);
        ShipData d = ShipData.get(lv.getServer());
        DestroyerEntity s = DestroyerEntity.find(lv);
        boolean blocks = s != null ? DestroyerEntity.blocksPresent(s.getState()) : d.hasBlocks();
        if (blocks) {
            ShipBlocks.Job j = new ShipBlocks.Job(false, s != null ? s.getAnchor() : d.anchor, s != null ? s.getRot() : d.rot, false, 0);
            j.finish(lv);
        }
        if (s != null) {
            s.discard();
        }
        d.clear();
        say(c, "Destructor eliminado" + (blocks ? " (bloques borrados)" : ""));
        return 1;
    }
}
