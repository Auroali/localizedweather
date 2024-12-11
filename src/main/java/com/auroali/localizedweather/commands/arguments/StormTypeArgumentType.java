package com.auroali.localizedweather.commands.arguments;

import com.auroali.localizedweather.weather.StormType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.server.command.ServerCommandSource;

import java.util.function.Supplier;

public class StormTypeArgumentType extends EnumArgumentType<StormType> {
    protected StormTypeArgumentType() {
        super(StormType.CODEC, StormType::values);
    }

    public static EnumArgumentType<StormType> stormType() {
        return new StormTypeArgumentType();
    }

    public static StormType getStormType(CommandContext<ServerCommandSource> context, String id) {
        return context.getArgument(id, StormType.class);
    }
}
