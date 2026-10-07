package com.ricardo.ludokressh;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Solo para la prueba automática de GitHub Actions (no va dentro del .jar del mod):
 * al abrirse el primer menú, muestra la espada en grande con su tooltip, hace una captura y cierra el juego.
 */
@Mod.EventBusSubscriber(modid = LudoKresshMod.MODID, value = Dist.CLIENT)
public final class CiPreview {
    private static boolean opened;

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (opened || event.getScreen() instanceof PreviewScreen) {
            return;
        }
        opened = true;
        Minecraft mc = Minecraft.getInstance();
        mc.tell(() -> mc.setScreen(new PreviewScreen()));
    }

    static final class PreviewScreen extends Screen {
        private int frames;

        PreviewScreen() {
            super(Component.literal("Ludo Kressh Sword preview"));
        }

        @Override
        public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
            g.fill(0, 0, width, height, 0xFF8B8B8B);
            ItemStack sword = new ItemStack(LudoKresshMod.LUDO_KRESSH_SWORD.get());

            int size = Math.min(width / 2, height) - 16;
            float scale = size / 16.0F;
            g.pose().pushPose();
            g.pose().translate(8, (height - size) / 2.0F, 0);
            g.pose().scale(scale, scale, 1);
            g.renderItem(sword, 0, 0);
            g.pose().popPose();

            int x = width / 2 + 8;
            g.drawString(font, "Slot:", x, 12, 0xFFFFFFFF);
            g.renderItem(sword, x + 30, 8);
            g.renderItem(new ItemStack(Items.NETHERITE_SWORD), x + 50, 8);
            g.renderTooltip(font, sword, x - 8, 44);

            frames++;
            if (frames == 40) {
                g.flush();
                Screenshot.grab(minecraft.gameDirectory, "ludokressh_gui.png", minecraft.getMainRenderTarget(), msg -> { });
            }
            if (frames == 100) {
                minecraft.stop();
            }
        }
    }
}
