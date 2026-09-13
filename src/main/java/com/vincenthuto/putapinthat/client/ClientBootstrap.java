package com.vincenthuto.putapinthat.client;

import com.vincenthuto.putapinthat.PutAPinInThat;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public final class ClientBootstrap {
    private ClientBootstrap() {
    }

    public static void initialize() {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(ClientBootstrap::registerKeys);
        modBus.addListener(ClientBootstrap::registerOverlays);
        MinecraftForge.EVENT_BUS.register(ClientEvents.class);
        PinnedRecipeManager.getInstance().load();
    }

    private static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(ClientEvents.PIN_RECIPE_KEY);
    }

    private static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll(PutAPinInThat.MOD_ID + "_pinned_recipes", PinnedRecipeHud::render);
    }
}
