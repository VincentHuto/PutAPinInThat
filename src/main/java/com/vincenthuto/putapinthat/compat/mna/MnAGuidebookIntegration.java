package com.vincenthuto.putapinthat.compat.mna;

import com.mna.api.guidebook.RegisterGuidebooksEvent;
import com.vincenthuto.putapinthat.PutAPinInThat;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

public final class MnAGuidebookIntegration {
    private MnAGuidebookIntegration() {
    }

    public static void initialize() {
        MinecraftForge.EVENT_BUS.addListener(MnAGuidebookIntegration::registerGuidebook);
        PutAPinInThat.LOGGER.info("Mana and Artifice Codex integration enabled");
    }

    private static void registerGuidebook(RegisterGuidebooksEvent event) {
        event.getRegistry().addGuidebookPath(
                ResourceLocation.fromNamespaceAndPath(PutAPinInThat.MOD_ID, "guide"));
        PutAPinInThat.LOGGER.info("Registered Put A Pin In That's Codex Arcana guide path");
    }
}
