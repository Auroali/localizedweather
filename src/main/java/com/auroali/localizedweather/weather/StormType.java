package com.auroali.localizedweather.weather;

import net.minecraft.util.StringIdentifiable;

/**
 * Enum that represents the type of storm
 * <br> Do note that while thunder is its own distinct type, if you are checking for rain at a given position you should
 * check both RAIN and THUNDER as it rains during thunder
 */
public enum StormType implements StringIdentifiable {
    RAIN,
    THUNDER;

    public static final com.mojang.serialization.Codec<StormType> CODEC = StringIdentifiable.createCodec(StormType::values);

    @Override
    public String asString() {
        return switch (this) {
            case RAIN -> "rain";
            case THUNDER -> "thunder";
        };
    }
}
