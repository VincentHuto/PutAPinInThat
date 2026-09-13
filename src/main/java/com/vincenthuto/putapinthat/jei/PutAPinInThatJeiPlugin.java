package com.vincenthuto.putapinthat.jei;

import com.vincenthuto.putapinthat.PutAPinInThat;
import com.vincenthuto.putapinthat.client.ClearPinsButtonTexture;
import com.vincenthuto.putapinthat.client.PinTexture;
import com.vincenthuto.putapinthat.client.PinnedRecipeManager;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IAdvancedRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public final class PutAPinInThatJeiPlugin implements IModPlugin {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(PutAPinInThat.MOD_ID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerAdvanced(IAdvancedRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();
        PinTexture.initialize(guiHelper);
        ClearPinsButtonTexture.initialize(guiHelper);
        registration.addRecipeButtonFactory(PinRecipeButtonController::new);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        PinnedRecipeManager.getInstance().setRuntime(jeiRuntime);
    }

    @Override
    public void onRuntimeUnavailable() {
        PinnedRecipeManager.getInstance().clearRuntime();
    }
}
