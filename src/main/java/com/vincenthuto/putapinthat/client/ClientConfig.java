package com.vincenthuto.putapinthat.client;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ClientConfig {
    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.EnumValue<ChatLayer> CHAT_LAYER;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("hud");
        CHAT_LAYER = builder
                .comment("Controls whether chat renders behind or above pinned recipe cards.",
                        "BEHIND_PINS keeps recipes unobstructed. ABOVE_PINS keeps chat unobstructed.")
                .defineEnum("chatLayer", ChatLayer.BEHIND_PINS);
        builder.pop();
        SPEC = builder.build();
    }

    private ClientConfig() {
    }

    public static boolean pinsRenderAboveChat() {
        return CHAT_LAYER.get() == ChatLayer.BEHIND_PINS;
    }

    public enum ChatLayer {
        BEHIND_PINS,
        ABOVE_PINS
    }
}
