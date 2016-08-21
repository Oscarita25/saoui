package com.saomc.saoui.events;

import com.saomc.saoui.SoundCore;
import com.saomc.saoui.renders.StaticRenderer;
import com.saomc.saoui.screens.death.DeathScreen;
import com.saomc.saoui.screens.ingame.IngameGUI;
import com.saomc.saoui.screens.menu.IngameMenuGUI;
import com.saomc.saoui.screens.menu.StartupGUI;
import com.saomc.saoui.util.OptionCore;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class RenderHandler {

    static final List<EntityLivingBase> deadHandlers = new ArrayList<>();
    private static boolean menuGUI = true;

    static void checkingameGUI() {
        if (EventCore.mc.ingameGUI != null && !(EventCore.mc.ingameGUI instanceof IngameGUI))
            EventCore.mc.ingameGUI = new IngameGUI(EventCore.mc);
    }

    static void deathHandlers() {
        deadHandlers.forEach(ent -> {
            if (ent != null) {
                final boolean deadStart = (ent.deathTime == 1);
                final boolean deadExactly = (ent.deathTime >= 18);
                if (deadStart) {
                    ent.deathTime++;
                    SoundCore.playAtEntity(ent, SoundCore.PARTICLES_DEATH);
                }

                if (deadExactly) {
                    StaticRenderer.doSpawnDeathParticles(EventCore.mc, ent);
                    ent.setDead();
                }
            }
        });
        deadHandlers.removeIf(ent -> ent.isDead);
    }

    static void guiInstance(GuiOpenEvent e) {
        if (OptionCore.DEBUG_MODE.getValue()) System.out.print(e.getGui() + " called GuiOpenEvent \n");

        if (e.getGui() instanceof GuiIngameMenu) {
            if (!(EventCore.mc.currentScreen instanceof IngameMenuGUI)) {
                e.setGui(new IngameMenuGUI(null));
            }
        }
        if (e.getGui() instanceof GuiInventory && !OptionCore.DEFAULT_INVENTORY.getValue()) {
            if (EventCore.mc.playerController.isInCreativeMode())
                e.setGui(new GuiContainerCreative(EventCore.mc.thePlayer));
            else if (!(EventCore.mc.currentScreen instanceof IngameMenuGUI))
                e.setGui(new IngameMenuGUI((GuiInventory) EventCore.mc.currentScreen));
            else e.setCanceled(true);
        }
        if (e.getGui() instanceof GuiGameOver && (!OptionCore.DEFAULT_DEATH_SCREEN.getValue())) {
            if (!(e.getGui() instanceof DeathScreen)) {
                e.setGui(new DeathScreen());
            }
        }
        if (e.getGui() instanceof IngameMenuGUI)
            if (EventCore.mc.currentScreen instanceof GuiOptions) {
                e.setCanceled(true);
                EventCore.mc.currentScreen.onGuiClosed();
                EventCore.mc.setIngameFocus();
            }

    }

    static void deathCheck() {
        if (EventCore.mc.currentScreen instanceof DeathScreen && EventCore.mc.thePlayer.getHealth() > 0.0F) {
            EventCore.mc.currentScreen.onGuiClosed();
            EventCore.mc.setIngameFocus();
        }
    }

    static void renderPlayer(RenderPlayerEvent.Post e) {
        if (!OptionCore.UI_ONLY.getValue()) {
            if (e.getEntityPlayer() != null) {
                StaticRenderer.render(e.getRenderer().getRenderManager(), e.getEntityPlayer(), e.getEntityPlayer().posX, e.getEntityPlayer().posY, e.getEntityPlayer().posZ);
            }
        }
    }

    static void renderEntity(RenderLivingEvent.Post e) {
        if (!OptionCore.UI_ONLY.getValue()) {
            if (e.getEntity() != EventCore.mc.thePlayer) {
                StaticRenderer.render(e.getRenderer().getRenderManager(), e.getEntity(), e.getX(), e.getY(), e.getZ());
            }
        }
    }

    static void mainMenuGUI(GuiOpenEvent e) {
        if (menuGUI)
            if (e.getGui() instanceof GuiMainMenu)
                if (StartupGUI.shouldShow()) {
                    e.setGui(new StartupGUI());
                    menuGUI = false;
                } //else if (e.getGui() instanceof GuiMainMenu)
        //e.setGui(new MainMenuGUI());
    }

}