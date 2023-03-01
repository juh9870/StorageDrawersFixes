package com.juh9870.storagedrawersfixes;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(StorageDrawersFixes.ID)
public class StorageDrawersFixes {
    public static final String ID = "storagedrawersfixes";

    public StorageDrawersFixes() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.register(this);
    }
}
