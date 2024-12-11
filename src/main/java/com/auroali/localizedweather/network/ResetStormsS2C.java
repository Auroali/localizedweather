package com.auroali.localizedweather.network;

import com.auroali.localizedweather.LocalizedWeather;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;

public class ResetStormsS2C implements FabricPacket {
    public static final PacketType<ResetStormsS2C> ID = PacketType.create(LocalizedWeather.id("reset_storms_s2c"), buf -> new ResetStormsS2C());

    @Override
    public void write(PacketByteBuf buf) {

    }

    @Override
    public PacketType<?> getType() {
        return ID;
    }
}
