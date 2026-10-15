package com.ricardo.jugador2;

import com.ricardo.jugador2.commands.Jugador2Command;
import com.ricardo.jugador2.npc.NpcManager;
import com.ricardo.jugador2.selftest.SelfTestScenario;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Jugador2Mod.MOD_ID)
public class Jugador2Mod {
    public static final String MOD_ID = "jugador2";

    private static NpcManager manager;

    public Jugador2Mod(FMLJavaModLoadingContext context) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Jugador2Config.SPEC);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarted);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStopping);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStopped);
        MinecraftForge.EVENT_BUS.addListener(this::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    /** Gestor del NPC del servidor en marcha, o null si no hay servidor. */
    public static NpcManager manager() {
        return manager;
    }

    private void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        manager = new NpcManager(server, Jugador2Config.read());
        manager.restoreOnStart(server.overworld());
        if (SelfTestScenario.enabled()) {
            SelfTestScenario.start(server);
        }
    }

    private void onServerStopping(ServerStoppingEvent event) {
        if (manager != null) {
            manager.saveState();
        }
    }

    private void onServerStopped(ServerStoppedEvent event) {
        manager = null;
    }

    private void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (manager != null) {
            manager.tick();
        }
        if (SelfTestScenario.isRunning()) {
            SelfTestScenario.tick(event.getServer());
        }
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        Jugador2Command.register(event.getDispatcher());
    }
}
