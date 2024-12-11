package com.auroali.localizedweather.mixin;

import com.auroali.localizedweather.weather.LocalizedWeatherWorld;
import com.auroali.localizedweather.weather.WeatherManager;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(targets = "net.minecraft.entity.passive.BeeEntity$PollinateGoal")
public abstract class BeePollinateGoalMixin {

    // field for outer class
    // (equivalent to (i think) BeeEntity.this)
    @Final
    @Shadow BeeEntity field_20377;

    @WrapOperation(method = "canBeeStart", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isRaining()Z"))
    public boolean localizedweather$captureWorld(World instance, Operation<Boolean> original, @Share("world") LocalRef<World> world) {
        WeatherManager weatherManager = ((LocalizedWeatherWorld)instance).localizedweather$getWeatherManager();
        return weatherManager.isStormingAt(this.field_20377.getPos());
    }

    @WrapOperation(method = "canBeeContinue", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isRaining()Z"))
    public boolean localizedweather$modifyBeePollinateGoalContinue(World instance, Operation<Boolean> original) {
        WeatherManager weatherManager = ((LocalizedWeatherWorld)instance).localizedweather$getWeatherManager();
        return weatherManager.isStormingAt(this.field_20377.getPos());
    }

}
