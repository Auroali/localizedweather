package com.auroali.localizedweather.commands;

import com.auroali.localizedweather.commands.arguments.StormTypeArgumentType;
import com.auroali.localizedweather.weather.LocalizedWeatherWorld;
import com.auroali.localizedweather.weather.Storm;
import com.auroali.localizedweather.weather.StormType;
import com.auroali.localizedweather.weather.WeatherManager;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.Vec3d;

public class LocalizedWeatherCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return CommandManager.literal("localizedweather")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("spawn")
                        .then(CommandManager.argument("position", Vec3ArgumentType.vec3())
                                .then(CommandManager.argument("type", StormTypeArgumentType.stormType())
                                        .then(CommandManager.argument("radius", DoubleArgumentType.doubleArg(1))
                                                .then(CommandManager.argument("lifetime", IntegerArgumentType.integer(1))
                                                        .executes(ctx -> spawnStorm(
                                                                ctx,
                                                                Vec3ArgumentType.getVec3(ctx, "position"),
                                                                StormTypeArgumentType.getStormType(ctx, "type"),
                                                                DoubleArgumentType.getDouble(ctx, "radius"),
                                                                IntegerArgumentType.getInteger(ctx, "lifetime"))
                                                        )
                                                )
                                                .executes(ctx -> spawnStorm(
                                                        ctx,
                                                        Vec3ArgumentType.getVec3(ctx, "position"),
                                                        StormTypeArgumentType.getStormType(ctx, "type"),
                                                        DoubleArgumentType.getDouble(ctx, "radius"),
                                                        Integer.MAX_VALUE)
                                                )
                                        )
                                        .executes(ctx -> spawnStorm(
                                                ctx,
                                                Vec3ArgumentType.getVec3(ctx, "position"),
                                                StormTypeArgumentType.getStormType(ctx, "type"),
                                                50,
                                                Integer.MAX_VALUE)
                                        )
                                )
                                .executes(ctx -> spawnStorm(
                                        ctx,
                                        Vec3ArgumentType.getVec3(ctx, "position"),
                                        StormType.RAIN,
                                        50,
                                        Integer.MAX_VALUE
                                ))
                        )
                        .executes(ctx -> spawnStorm(ctx, ctx.getSource().getPosition(), StormType.RAIN, 50, Integer.MAX_VALUE))
                ).then(CommandManager.literal("clear")
                        .then(CommandManager.argument("position", Vec3ArgumentType.vec3())
                                .executes(ctx -> clearWeather(ctx, Vec3ArgumentType.getVec3(ctx, "position")))
                        ).executes(ctx -> clearWeather(ctx, null))
                );
    }

    private static int clearWeather(CommandContext<ServerCommandSource> ctx, Vec3d position) {
        WeatherManager weatherManager = ((LocalizedWeatherWorld)ctx.getSource().getWorld()).localizedweather$getWeatherManager();
        for(Storm storm : weatherManager.getStorms()) {
            if(position != null && !storm.isPositionInside(position))
                continue;
            weatherManager.removeStorm(storm);
        }
        return 0;
    }

    public static int spawnStorm(CommandContext<ServerCommandSource> ctx, Vec3d position, StormType type, double radius, int lifetime) {
        WeatherManager weatherManager = ((LocalizedWeatherWorld)ctx.getSource().getWorld()).localizedweather$getWeatherManager();
        Storm storm = new Storm(type, position, Vec3d.ZERO, radius, 0, Integer.MAX_VALUE, lifetime);
        weatherManager.addStorm(storm);
        return 0;
    }
}
