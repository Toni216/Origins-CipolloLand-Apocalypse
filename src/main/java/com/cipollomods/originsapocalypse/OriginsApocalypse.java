package com.cipollomods.originsapocalypse;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(OriginsApocalypse.MOD_ID)
public class OriginsApocalypse {
    public static final String MOD_ID = "origins_apocalypse";
    private static final Logger LOGGER = LogUtils.getLogger();

    public OriginsApocalypse() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        LOGGER.info("Origins: Apocalypse has loaded correctly.");
        MinecraftForge.EVENT_BUS.register(this);
    }
}