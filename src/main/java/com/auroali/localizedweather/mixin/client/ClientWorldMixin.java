package com.auroali.localizedweather.mixin.client;

import com.auroali.localizedweather.mixin.WorldMixin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin extends WorldMixin {

    @Override
    public void localizedweather$modifyClientRainGradient(float delta, CallbackInfoReturnable<Float> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        Vec3d pos = client.gameRenderer.getCamera().getPos();
        cir.setReturnValue(this.localizedweather$weatherManager.getRainGradientAt(pos));
    }

    @Override
    public void localizedweather$modifyClientThunderGradient(float delta, CallbackInfoReturnable<Float> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        Vec3d pos = client.gameRenderer.getCamera().getPos();
        cir.setReturnValue(this.localizedweather$weatherManager.getThunderGradientAt(pos));
    }
}
