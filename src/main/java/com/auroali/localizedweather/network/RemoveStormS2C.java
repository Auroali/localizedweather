package com.auroali.localizedweather.network;

import com.auroali.localizedweather.LocalizedWeather;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;

public record RemoveStormS2C(int id) implements FabricPacket {
    public static PacketType<RemoveStormS2C> ID = PacketType.create(LocalizedWeather.id("remove_storm_s2c"), RemoveStormS2C::new);
    public RemoveStormS2C(PacketByteBuf buf) {
        this(buf.readVarInt());
    }

    @Override
    public void write(PacketByteBuf packetByteBuf) {
        packetByteBuf.writeVarInt(this.id());
    }

    @Override
    public PacketType<?> getType() {
        return ID;
    }
}
