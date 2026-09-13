package com.vincenthuto.putapinthat.jei;

import com.vincenthuto.putapinthat.client.PinTexture;
import com.vincenthuto.putapinthat.client.PinnedRecipeManager;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.buttons.IButtonState;
import mezz.jei.api.gui.buttons.IIconButtonController;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import net.minecraft.network.chat.Component;

public final class PinRecipeButtonController implements IIconButtonController {
    private final IRecipeLayoutDrawable<?> layout;
    private final PinnedRecipeManager manager;

    public PinRecipeButtonController(IRecipeLayoutDrawable<?> layout) {
        this.layout = layout;
        this.manager = PinnedRecipeManager.getInstance();
    }

    @Override
    public boolean onPress(IJeiUserInput input) {
        if (!input.isSimulate()) {
            ClientPinFeedback.show(manager.toggle(layout));
        }
        return true;
    }

    @Override
    public void getTooltips(ITooltipBuilder builder) {
        String key = manager.isPinned(layout) ? "tooltip.putapinthat.unpin" : "tooltip.putapinthat.pin";
        builder.add(Component.translatable(key));
    }

    @Override
    public void initState(IButtonState state) {
        state.setIcon(PinTexture.drawable());
        updateState(state);
    }

    @Override
    public void updateState(IButtonState state) {
        state.setVisible(true);
        state.setActive(true);
        state.setForcePressed(manager.isPinned(layout));
    }
}
