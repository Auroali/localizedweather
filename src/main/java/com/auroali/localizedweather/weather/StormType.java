package com.auroali.localizedweather.weather;

import net.minecraft.util.StringIdentifiable;

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
