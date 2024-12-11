package com.auroali.localizedweather.mixin;

import com.auroali.localizedweather.weather.LocalizedWeatherWorld;
import com.auroali.localizedweather.weather.WeatherManager;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BeehiveBlockEntity.class)
public class BeehiveBlockEntityMixin {
    @WrapOperation(method = "releaseBee", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isRaining()Z"))
    private static boolean localizedweather$modifyBeehiveRainCheck(World instance, Operation<Boolean> original, @Local(argsOnly = true, ordinal = 0) BlockPos pos) {
        WeatherManager weatherManager = ((LocalizedWeatherWorld)instance).localizedweather$getWeatherManager();
        return weatherManager.isStormingAt(pos);
    }
}
