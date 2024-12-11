package com.auroali.localizedweather.mixin;

import com.auroali.localizedweather.weather.LocalizedWeatherWorld;
import com.auroali.localizedweather.weather.WeatherManager;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BeeEntity.class)
public abstract class BeeEntityMixin extends AnimalEntity {
    protected BeeEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @WrapOperation(method = "canEnterHive", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isRaining()Z"))
    public boolean localizedweather$modifyBeeRainCheck(World instance, Operation<Boolean> original) {
        WeatherManager weatherManager = ((LocalizedWeatherWorld) instance).localizedweather$getWeatherManager();
        return weatherManager.isStormingAt(this.getPos());
    }
}
