package com.vincenthuto.putapinthat;

import com.mojang.logging.LogUtils;
import com.vincenthuto.putapinthat.client.ClientBootstrap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(PutAPinInThat.MOD_ID)
public final class PutAPinInThat {
    public static final String MOD_ID = "putapinthat";
    public static final Logger LOGGER = LogUtils.getLogger();

    public PutAPinInThat() {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientBootstrap::initialize);
    }
}
