package com.auroali.localizedweather.mixin;

import com.auroali.localizedweather.weather.LocalizedWeatherWorld;
import com.auroali.localizedweather.weather.WeatherManager;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.FireBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(FireBlock.class)
public class FireBlockMixin {
    @WrapOperation(method = "scheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;isRaining()Z"))
    public boolean localizedweather$modifyFireRainCheck(ServerWorld instance, Operation<Boolean> original, @Local(argsOnly = true) BlockPos pos) {
        WeatherManager weatherManager = ((LocalizedWeatherWorld)instance).localizedweather$getWeatherManager();
        return weatherManager.isStormingAt(pos);
    }
}
