package net.nextfur.fwc.init;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.economy.data.CheckData;
import net.nextfur.fwc.economy.data.WalletData;

public class FwDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, FwMain.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<WalletData>> WALLET_DATA =
            DATA_COMPONENT_TYPES.register("wallet_data", () -> DataComponentType.<WalletData>builder()
                    .persistent(WalletData.CODEC)
                    .networkSynchronized(WalletData.STREAM_CODEC)
                    .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CheckData>> CHECK_DATA =
            DATA_COMPONENT_TYPES.register("check_data", () -> DataComponentType.<CheckData>builder()
                    .persistent(CheckData.CODEC)
                    .networkSynchronized(CheckData.STREAM_CODEC)
                    .build());
}
