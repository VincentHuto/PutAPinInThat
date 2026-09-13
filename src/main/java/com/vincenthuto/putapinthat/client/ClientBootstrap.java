package com.vincenthuto.putapinthat.client;

import com.vincenthuto.putapinthat.PutAPinInThat;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public final class ClientBootstrap {
    private ClientBootstrap() {
    }

    public static void initialize() {
        var loadingContext = FMLJavaModLoadingContext.get();
        var modBus = loadingContext.getModEventBus();
        modBus.addListener(ClientBootstrap::registerKeys);
        modBus.addListener(ClientBootstrap::registerOverlays);
        loadingContext.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        MinecraftForge.EVENT_BUS.register(ClientEvents.class);
        initializeOptionalIntegration("mna",
                "com.vincenthuto.putapinthat.compat.mna.MnAGuidebookIntegration");
        PinnedRecipeManager.getInstance().load();
    }

    private static void initializeOptionalIntegration(String modId, String integrationClassName) {
        if (!ModList.get().isLoaded(modId)) {
            return;
        }

        try {
            Class.forName(integrationClassName).getMethod("initialize").invoke(null);
        } catch (ReflectiveOperationException exception) {
            PutAPinInThat.LOGGER.error("Could not initialize {} integration", modId, exception);
        }
    }

    private static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(ClientEvents.PIN_RECIPE_KEY);
    }

    private static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerBelow(VanillaGuiOverlay.CHAT_PANEL.id(),
                PutAPinInThat.MOD_ID + "_pinned_recipes_below_chat", PinnedRecipeHud::renderBelowChat);
        event.registerAbove(VanillaGuiOverlay.CHAT_PANEL.id(),
                PutAPinInThat.MOD_ID + "_pinned_recipes_above_chat", PinnedRecipeHud::renderAboveChat);
    }
}
