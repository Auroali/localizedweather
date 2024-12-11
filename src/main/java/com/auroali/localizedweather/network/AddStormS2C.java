package com.auroali.localizedweather.network;

import com.auroali.localizedweather.LocalizedWeather;
import com.auroali.localizedweather.weather.Storm;
import com.auroali.localizedweather.weather.StormType;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3d;

/**
 * Packet that tells the client to add the contained storm to its WeatherManager
 * <br> If a storm with the same id already exists, it is replaced by this one
 * <br> The storm read from the packet is stripped of certain information the client doesn't need to know,
 * such as radius decay or the storm's movement direction
 *
 * @param storm the storm to add
 */
public record AddStormS2C(Storm storm) implements FabricPacket {
    public static PacketType<AddStormS2C> ID = PacketType.create(LocalizedWeather.id("add_storm_s2c"), AddStormS2C::read);

    public static AddStormS2C read(PacketByteBuf buf) {
        int id = buf.readVarInt();
        StormType type = StormType.values()[buf.readByte()];
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        double radius = buf.readDouble();
        Storm storm = new Storm(type, new Vec3d(x, y, z), Vec3d.ZERO, radius, 0.d, 0, 0);
        storm.setId(id);
        return new AddStormS2C(storm);
    }

    @Override
    public void write(PacketByteBuf packetByteBuf) {
        packetByteBuf.writeVarInt(this.storm().getId());
        packetByteBuf.writeByte(this.storm().getType().ordinal());
        packetByteBuf.writeDouble(this.storm().getCenter().getX());
        packetByteBuf.writeDouble(this.storm().getCenter().getY());
        packetByteBuf.writeDouble(this.storm().getCenter().getZ());
        packetByteBuf.writeDouble(this.storm().getRadius());
    }

    @Override
    public PacketType<?> getType() {
        return ID;
    }
}
