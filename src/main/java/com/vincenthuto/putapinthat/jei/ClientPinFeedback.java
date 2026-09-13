package com.vincenthuto.putapinthat.jei;

import com.vincenthuto.putapinthat.pin.PinToggleResult;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class ClientPinFeedback {
    private ClientPinFeedback() {
    }

    public static void show(PinToggleResult result) {
        String key = switch (result) {
            case ADDED -> "message.putapinthat.pinned";
            case REMOVED -> "message.putapinthat.unpinned";
            case UNSUPPORTED -> "message.putapinthat.unsupported";
        };
        if (result == PinToggleResult.UNSUPPORTED) {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                player.displayClientMessage(Component.translatable(key), true);
            }
        }
    }
}
