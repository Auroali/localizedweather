package com.auroali.localizedweather.mixin;

import com.auroali.localizedweather.LocalizedWeather;
import com.auroali.localizedweather.network.AddStormS2C;
import com.auroali.localizedweather.weather.Storm;
import com.auroali.localizedweather.weather.StormType;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.RandomSequencesState;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.spawner.Spawner;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends WorldMixin {
    @Shadow @Final private ServerWorldProperties worldProperties;

    @Shadow public abstract List<ServerPlayerEntity> getPlayers();

    @Shadow public abstract @Nullable ServerPlayerEntity getRandomAlivePlayer();

    @Shadow @Final private MinecraftServer server;
    @Shadow @Final private List<ServerPlayerEntity> players;
    @Unique
    private int localizedweather$lastStormSpawnTicks;

    @Unique
    private File localizedweather$stormSaveFile;
    @Unique
    private File localizedweather$stormBackupFile;
    @Unique
    private final Set<Storm> localizedweather$unsyncedStorms = new HashSet<>();

    @Inject(method = "<init>", at = @At("RETURN"))
    public void localizedweather$setupStormFiles(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey<World> worldKey, DimensionOptions dimensionOptions, WorldGenerationProgressListener worldGenerationProgressListener, boolean debugWorld, long seed, List<Spawner> spawners, boolean shouldTickTime, RandomSequencesState randomSequencesState, CallbackInfo ci) {
        this.localizedweather$stormSaveFile = session.getWorldDirectory(this.getRegistryKey()).resolve("storms.dat").toFile();
        this.localizedweather$stormBackupFile = session.getWorldDirectory(this.getRegistryKey()).resolve("storms.dat_old").toFile();
        this.localizedweather$weatherManager.read(this.localizedweather$stormSaveFile, this.localizedweather$stormBackupFile);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;wakeSleepingPlayers()V"))
    public void localizedweather$handleClearingWeatherAfterSleep(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        if(!this.getGameRules().getBoolean(GameRules.DO_WEATHER_CYCLE))
            return;

        Set<Storm> storms = new HashSet<>();
        this.players.stream().filter(PlayerEntity::isSleeping)
                .forEach(player -> {
                    for(Storm storm : this.localizedweather$weatherManager.getStorms()) {
                        if(storm.isPositionInside(player.getPos()))
                            storms.add(storm);
                    }
                });
        storms.forEach(this.localizedweather$weatherManager::removeStorm);
    }

    @Inject(method = "tickWeather", at = @At("HEAD"), cancellable = true)
    public void localizedweather$tickWeather(CallbackInfo ci) {
        // reset stuff
        if(this.worldProperties.isRaining())
            this.worldProperties.setRaining(false);
        if(this.worldProperties.isThundering())
            this.worldProperties.setThundering(false);

        boolean canTickWeather = this.getGameRules().getBoolean(GameRules.DO_WEATHER_CYCLE);

        // tick existing storms
        Collection<Storm> storms = this.localizedweather$weatherManager.getStorms();
        List<Storm> toRemove = new ArrayList<>();
        for(Storm storm : storms) {
            Vec3d center = storm.getCenter();
            double radius = storm.getRadius();
            if(canTickWeather)
                storm.move(this.getRandom());
            if(!storm.shouldRemove() && (!storm.getCenter().equals(center) || storm.getRadius() != radius)) {
                this.localizedweather$weatherManager.markDirty();
                this.localizedweather$unsyncedStorms.add(storm);
            }
            if(storm.shouldRemove())
                toRemove.add(storm);
        }

        if(this.localizedweather$lastStormSpawnTicks > 0) {
            this.localizedweather$lastStormSpawnTicks--;
        }

        // spawn new storms
        for(int i = 0; i < MathHelper.clamp(this.getPlayers().size() / 4, 1, 8); i++) {
            if(this.localizedweather$lastStormSpawnTicks != 0 || !canTickWeather || this.getRandom().nextInt(this.getGameRules().getInt(LocalizedWeather.STORM_SPAWN_CHANCE)) != 0)
                continue;

            StormType type = this.getRandom().nextInt(8) == 0 ? StormType.THUNDER : StormType.RAIN;
            Vec3d candidateCenter = Vec3d.ZERO;
            PlayerEntity entity = this.getRandomAlivePlayer();
            if(entity != null)
                candidateCenter = entity.getPos();
            
            int searchRadius = this.getGameRules().getInt(LocalizedWeather.STORM_SPAWN_RADIUS);
            double centerX = candidateCenter.getX() + this.getRandom().nextGaussian() * searchRadius;
            double centerZ = candidateCenter.getZ() + this.getRandom().nextGaussian() * searchRadius;

            double directionX = this.getRandom().nextGaussian() * 0.025;
            double directionZ = this.getRandom().nextGaussian() * 0.025;

            int maxRadius = this.getGameRules().getInt(LocalizedWeather.STORM_MAX_RADIUS);
            int minRadius = this.getGameRules().getInt(LocalizedWeather.STORM_MIN_RADIUS);
            double radius = Math.max(this.getRandom().nextDouble() * maxRadius, minRadius);
            double radiusDecay = this.getRandom().nextDouble() * 0.0025;
            int decayChance = this.getRandom().nextBetween(8, 256);
            int lifetime = this.getRandom().nextBetween(3600, 18000);

            Storm storm = new Storm(type, new Vec3d(centerX, 0, centerZ), new Vec3d(directionX, 0, directionZ), radius, radiusDecay, decayChance, lifetime);
            this.localizedweather$weatherManager.addStorm(storm);
            // update storm timer
            this.localizedweather$lastStormSpawnTicks = this.getGameRules().getInt(LocalizedWeather.STORM_TIMER);
            LocalizedWeather.LOGGER.info("Added storm of type {} at ({}, {}) with radius {} and lifetime {}t in dimension {}", type, centerX, centerZ, radius, lifetime, this.getRegistryKey().getValue());
        }

        // sync storms every 10 ticks
        // hopefully avoids packet spam
        if(this.getTime() % 5 == 0) {
            for(Storm storm : this.localizedweather$unsyncedStorms) {
                if(storm.shouldRemove())
                    continue;

                AddStormS2C packet = new AddStormS2C(storm);
                for (ServerPlayerEntity player : this.getPlayers()) {
                    ServerPlayNetworking.send(player, packet);
                }
            }
            this.localizedweather$unsyncedStorms.clear();
        }

        // remove dead storms
        for(Storm storm : toRemove) {
            this.localizedweather$weatherManager.removeStorm(storm);
            LocalizedWeather.LOGGER.info("Removed storm at ({}, {})", storm.getCenter().getX(), storm.getCenter().getZ());
        }
        ci.cancel();
    }

    // i know persistent state manager exists but i didnt write the weather manager as a persistent state and dont want to change it
    // so im saving it manually
    @Inject(method = "saveLevel", at = @At("HEAD"))
    public void localizedweather$saveWeatherManager(CallbackInfo ci) {
        this.localizedweather$weatherManager.save(this.localizedweather$stormSaveFile, this.localizedweather$stormBackupFile);
    }

    @WrapOperation(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;isThundering()Z"))
    public boolean localizedweather$modifyThunderCheck(ServerWorld instance, Operation<Boolean> original, @Local(argsOnly = true) WorldChunk chunk) {
        return this.localizedweather$weatherManager.hasStormInChunk(chunk.getPos(), StormType.THUNDER);
    }

    @WrapOperation(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;isRaining()Z"))
    public boolean localizedweather$modifyChunkRainCheck(ServerWorld instance, Operation<Boolean> original, @Local(argsOnly = true) WorldChunk chunk) {
        return this.localizedweather$weatherManager.hasStormInChunk(chunk.getPos(), null);
    }
}
