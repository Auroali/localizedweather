package com.auroali.localizedweather.events;

import com.auroali.localizedweather.weather.Storm;
import com.auroali.localizedweather.weather.WeatherManager;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.World;

public class StormEvents {
    /**
     * Invoked immediately after a storm is added to a WeatherManager, but before it is sent to the client
     */
    public static final Event<StormSpawn> SPAWN = EventFactory.createArrayBacked(StormSpawn.class, callbacks -> (world, manager, storm) -> {
        for (StormSpawn callback : callbacks) {
            callback.onStormSpawn(world, manager, storm);
        }
    });

    /**
     * Invoked immediately before a storm is removed from a WeatherManager
     */
    public static final Event<StormRemoved> REMOVED = EventFactory.createArrayBacked(StormRemoved.class, callbacks -> (world, manager, storm) -> {
        for (StormRemoved callback : callbacks) {
            callback.onStormRemoved(world, manager, storm);
        }
    });

    @FunctionalInterface
    public interface StormSpawn {
        void onStormSpawn(World world, WeatherManager manager, Storm storm);
    }

    @FunctionalInterface
    public interface StormRemoved {
        void onStormRemoved(World world, WeatherManager manager, Storm storm);
    }
}
