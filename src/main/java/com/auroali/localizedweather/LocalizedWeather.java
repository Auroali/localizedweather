package com.auroali.localizedweather;

import com.auroali.localizedweather.commands.LocalizedWeatherCommand;
import com.auroali.localizedweather.commands.arguments.StormTypeArgumentType;
import com.auroali.localizedweather.network.AddStormS2C;
import com.auroali.localizedweather.network.ResetStormsS2C;
import com.auroali.localizedweather.weather.LocalizedWeatherWorld;
import com.auroali.localizedweather.weather.Storm;
import com.auroali.localizedweather.weather.WeatherManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalizedWeather implements ModInitializer {
    public static final String MODID = "localizedweather";

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    public static final GameRules.Key<GameRules.IntRule> STORM_SPAWN_CHANCE = GameRuleRegistry.register("localizedweather_stormChance", GameRules.Category.UPDATES, GameRuleFactory.createIntRule(1024, 0));
    public static final GameRules.Key<GameRules.IntRule> STORM_SPAWN_RADIUS = GameRuleRegistry.register("localizedweather_stormMaxSpawnDistance", GameRules.Category.UPDATES, GameRuleFactory.createIntRule(1024, 1));
    public static final GameRules.Key<GameRules.IntRule> STORM_MAX_RADIUS = GameRuleRegistry.register("localizedweather_stormMaxRadius", GameRules.Category.UPDATES, GameRuleFactory.createIntRule(750, 1));
    public static final GameRules.Key<GameRules.IntRule> STORM_MIN_RADIUS = GameRuleRegistry.register("localizedweather_stormMinRadius", GameRules.Category.UPDATES, GameRuleFactory.createIntRule(150, 1));
    public static final GameRules.Key<GameRules.IntRule> STORM_TIMER = GameRuleRegistry.register("localizedweather_stormMinSpawnTime", GameRules.Category.UPDATES, GameRuleFactory.createIntRule(600, 0));

    @Override
    public void onInitialize() {
        ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, packetSender, minecraftServer) -> {
            World world = serverPlayNetworkHandler.getPlayer().getWorld();
            WeatherManager weatherManager = ((LocalizedWeatherWorld) world).localizedweather$getWeatherManager();

            for (Storm storm : weatherManager.getStorms()) {
                packetSender.sendPacket(new AddStormS2C(storm));
            }
        });

        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            ServerPlayNetworking.send(player, new ResetStormsS2C());
            WeatherManager manager = ((LocalizedWeatherWorld) player.getWorld()).localizedweather$getWeatherManager();
            for (Storm storm : manager.getStorms()) {
                ServerPlayNetworking.send(player, new AddStormS2C(storm));
            }
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, player, alive) -> {
            if (player.getServerWorld() == oldPlayer.getServerWorld())
                return;
            ServerPlayNetworking.send(player, new ResetStormsS2C());
            WeatherManager manager = ((LocalizedWeatherWorld) player.getWorld()).localizedweather$getWeatherManager();
            for (Storm storm : manager.getStorms()) {
                ServerPlayNetworking.send(player, new AddStormS2C(storm));
            }
        });

        EntitySleepEvents.ALLOW_SLEEP_TIME.register((player, pos, vanillaResult) -> {
            WeatherManager manager = ((LocalizedWeatherWorld) player.getWorld()).localizedweather$getWeatherManager();
            return manager.isThunderingAt(player.getPos()) ? ActionResult.SUCCESS : ActionResult.PASS;
        });

        ArgumentTypeRegistry.registerArgumentType(LocalizedWeather.id("storm_type_argument"), StormTypeArgumentType.class, ConstantArgumentSerializer.of(StormTypeArgumentType::stormType));

        CommandRegistrationCallback.EVENT.register((commandDispatcher, commandRegistryAccess, registrationEnvironment) -> {
            commandDispatcher.register(LocalizedWeatherCommand.register());
        });
    }

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }
}