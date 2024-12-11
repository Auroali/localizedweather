package com.auroali.localizedweather;

import com.auroali.localizedweather.network.AddStormS2C;
import com.auroali.localizedweather.network.RemoveStormS2C;
import com.auroali.localizedweather.network.ResetStormsS2C;
import com.auroali.localizedweather.weather.LocalizedWeatherWorld;
import com.auroali.localizedweather.weather.Storm;
import com.auroali.localizedweather.weather.StormType;
import com.auroali.localizedweather.weather.WeatherManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class LocalizedWeatherClient implements ClientModInitializer {
    boolean enableDebugging = Boolean.parseBoolean(System.getProperty("localizedweather.enableDebugging", "false"));

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(AddStormS2C.ID, (addStormS2C, clientPlayerEntity, packetSender) -> {
            World world = clientPlayerEntity.getWorld();
            WeatherManager manager = ((LocalizedWeatherWorld)world).localizedweather$getWeatherManager();

            manager.addStorm(addStormS2C.storm());
        });
        ClientPlayNetworking.registerGlobalReceiver(RemoveStormS2C.ID, (removeStormS2C, clientPlayerEntity, packetSender) -> {
            World world = clientPlayerEntity.getWorld();
            WeatherManager manager = ((LocalizedWeatherWorld)world).localizedweather$getWeatherManager();

            manager.removeStormById(removeStormS2C.id());
        });
        ClientPlayNetworking.registerGlobalReceiver(ResetStormsS2C.ID, (resetStormsS2C, player, responseSender) -> {
            WeatherManager manager = ((LocalizedWeatherWorld)player.getWorld()).localizedweather$getWeatherManager();
            manager.getStorms()
                    .stream()
                    .mapToInt(Storm::getId)
                    .forEach(manager::removeStormById);
        });

        if(this.enableDebugging) {
            registerDebugEvents();
        }
    }

    private static void registerDebugEvents() {
        WorldRenderEvents.LAST.register(worldRenderContext -> {
            World world = worldRenderContext.world();
            WeatherManager manager = ((LocalizedWeatherWorld) world).localizedweather$getWeatherManager();
            MatrixStack stack = worldRenderContext.matrixStack();
            VertexConsumerProvider consumers = worldRenderContext.consumers();
            Camera camera = worldRenderContext.camera();

            VertexConsumer buffer = consumers.getBuffer(RenderLayer.getLines());
            // this probably breaks rendering but given that its a debug feature i dont think its a huge problem
            // disable fog so that the bounding boxes are visible from far away
            RenderSystem.setShaderFogStart(Float.MAX_VALUE);
            RenderSystem.setShaderFogEnd(Float.MAX_VALUE);
            for (Storm storm : manager.getStorms()) {
                Vec3d pos = storm.getCenter().subtract(camera.getPos());
                double radius = storm.getRadius();
                // this is just to get an interesting and unique color to seperate the bounding boxes with
                int color = storm.getId() ^ -3127558;
                float r = ((color >> 16) & 255) / 255.f;
                float g = ((color >> 8) & 255) / 255.f;
                float b = (color & 255) / 255.f;
                WorldRenderer.drawBox(
                        stack,
                        buffer,
                        pos.x + radius,
                        world.getBottomY() - camera.getPos().getY(),
                        pos.z - radius,
                        pos.x - radius,
                        world.getTopY() - camera.getPos().getY(),
                        pos.z + radius,
                        r,
                        g,
                        b,
                        storm.getType() == StormType.THUNDER ? 1.0f : 0.25f
                );
            }

            // tmp
            // render all chunks in storm
            ChunkPos.stream(ChunkSectionPos.from(camera.getBlockPos()).toChunkPos(), 32)
                    .forEach(pos -> {
                        if(!manager.hasStormInChunk(pos, null))
                            return;
                        double startX = pos.getStartX() - camera.getPos().getX();
                        double startZ = pos.getStartZ() - camera.getPos().getZ();
                        double endX = pos.getEndX() - camera.getPos().getX() + 1;
                        double endZ = pos.getEndZ() - camera.getPos().getZ() + 1;

                        float r = 1.f;
                        float g = 0.86f;
                        float b = 0.02f;
                        WorldRenderer.drawBox(
                                stack,
                                buffer,
                                startX,
                                world.getBottomY() - camera.getPos().getY(),
                                startZ,
                                endX,
                                world.getTopY() - camera.getPos().getY(),
                                endZ,
                                r,
                                g,
                                b,
                                0.5f
                        );
                    });
        });
    }
}
