package com.auroali.localizedweather.mixin;

import com.auroali.localizedweather.weather.LocalizedWeatherWorld;
import com.auroali.localizedweather.weather.WeatherManager;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.GameRules;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

@Mixin(World.class)
public abstract class WorldMixin implements LocalizedWeatherWorld {
    @Shadow public abstract Random getRandom();

    @Shadow public abstract GameRules getGameRules();

    @Shadow public abstract RegistryKey<World> getRegistryKey();

    @Shadow public abstract long getTime();

    @Unique
    public WeatherManager localizedweather$weatherManager;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void localizedweather$initWeatherManager(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates, CallbackInfo ci) {
        this.localizedweather$weatherManager = new WeatherManager((World) (Object) this);
    }

    @ModifyReturnValue(method = "hasRain", at = @At("RETURN"))
    public boolean localizedweather$modifyRainCheck2(boolean original, @Local(argsOnly = true) BlockPos pos) {
        return original && this.localizedweather$weatherManager.isStormingAt(pos);
    }

    @WrapOperation(method = "hasRain", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isRaining()Z"))
    public boolean localizedweather$modifyRainCheck(World instance, Operation<Boolean> original) {
        return true;
    }

    @Inject(method = "getRainGradient", at = @At("HEAD"), cancellable = true)
    public void localizedweather$modifyClientRainGradient(float delta, CallbackInfoReturnable<Float> cir) {

    }

    @Inject(method = "getThunderGradient", at = @At("HEAD"), cancellable = true)
    public void localizedweather$modifyClientThunderGradient(float delta, CallbackInfoReturnable<Float> cir) {

    }

    @Override
    public WeatherManager localizedweather$getWeatherManager() {
        return this.localizedweather$weatherManager;
    }
}
