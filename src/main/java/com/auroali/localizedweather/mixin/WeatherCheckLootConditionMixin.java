package com.auroali.localizedweather.mixin;

import com.auroali.localizedweather.weather.LocalizedWeatherWorld;
import com.auroali.localizedweather.weather.WeatherManager;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.loot.condition.WeatherCheckLootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WeatherCheckLootCondition.class)
public class WeatherCheckLootConditionMixin {
    @WrapOperation(method = "test(Lnet/minecraft/loot/context/LootContext;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;isRaining()Z"))
    public boolean localizedweather$modifyWeatherLootConditionRainCheck(ServerWorld instance, Operation<Boolean> original, @Local(argsOnly = true) LootContext context) {
        Vec3d position = context.get(LootContextParameters.ORIGIN);
        if (position == null && context.get(LootContextParameters.THIS_ENTITY) != null)
            position = context.get(LootContextParameters.THIS_ENTITY).getPos();
        else if (position == null && context.get(LootContextParameters.BLOCK_ENTITY) != null)
            position = context.get(LootContextParameters.BLOCK_ENTITY).getPos().toCenterPos();

        if (position == null)
            return false;

        WeatherManager weatherManager = ((LocalizedWeatherWorld) instance).localizedweather$getWeatherManager();
        return weatherManager.isStormingAt(position);
    }

    @WrapOperation(method = "test(Lnet/minecraft/loot/context/LootContext;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;isThundering()Z"))
    public boolean localizedweather$modifyWeatherLootConditionThunderCheck(ServerWorld instance, Operation<Boolean> original, @Local(argsOnly = true) LootContext context) {
        Vec3d position = context.get(LootContextParameters.ORIGIN);
        if (position == null && context.get(LootContextParameters.THIS_ENTITY) != null)
            position = context.get(LootContextParameters.THIS_ENTITY).getPos();
        else if (position == null && context.get(LootContextParameters.BLOCK_ENTITY) != null)
            position = context.get(LootContextParameters.BLOCK_ENTITY).getPos().toCenterPos();

        if (position == null)
            return false;

        WeatherManager weatherManager = ((LocalizedWeatherWorld) instance).localizedweather$getWeatherManager();
        return weatherManager.isThunderingAt(position);
    }
}
