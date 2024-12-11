package com.auroali.localizedweather.weather;

import com.auroali.localizedweather.LocalizedWeather;
import com.auroali.localizedweather.events.StormEvents;
import com.auroali.localizedweather.network.AddStormS2C;
import com.auroali.localizedweather.network.RemoveStormS2C;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;

public class WeatherManager {
    private static final double FALLOFF_DISTANCE_SQUARED = 400.d;

    private final Int2ObjectMap<Storm> storms = new Int2ObjectOpenHashMap<>();
    private boolean needsSave;
    private final World world;

    public WeatherManager(World world) {
        this.world = world;
    }

    public Collection<Storm> getStorms() {
        return this.storms.values();
    }

    public void markDirty() {
        this.needsSave = true;
    }

    public boolean needsSave() {
        return this.needsSave;
    }

    public void addStorm(Storm storm) {
        this.storms.put(storm.getId(), storm);
        StormEvents.SPAWN.invoker().onStormSpawn(this.world, this, storm);
        if (this.world instanceof ServerWorld serverWorld) {
            AddStormS2C packet = new AddStormS2C(storm);
            serverWorld.getPlayers().forEach(p -> ServerPlayNetworking.send(p, packet));
        }
        this.markDirty();
    }

    public void removeStorm(Storm storm) {
        this.removeStormById(storm.getId());
    }

    public void removeStormById(int id) {
        Storm storm = this.storms.get(id);
        if (storm != null) {
            storm.shouldRemove = true;
            StormEvents.REMOVED.invoker().onStormRemoved(this.world, this, storm);
        }
        this.storms.remove(id);
        if (this.world instanceof ServerWorld serverWorld) {
            RemoveStormS2C packet = new RemoveStormS2C(id);
            serverWorld.getPlayers().forEach(p -> ServerPlayNetworking.send(p, packet));
        }
        this.markDirty();
    }

    public boolean isStormingAt(BlockPos pos) {
        for (Storm storm : this.storms.values()) {
            if (storm.isPositionInside(pos))
                return true;
        }
        return false;
    }

    public boolean isStormingAt(Vec3d pos) {
        for (Storm storm : this.storms.values()) {
            if (storm.isPositionInside(pos))
                return true;
        }
        return false;
    }

    /**
     * Checks if a chunk has a storm
     *
     * @param pos  the chunk's position
     * @param type the type of storm
     * @return if the chunk has a storm
     * @apiNote if the storm type is null, any storm is checked
     */
    public boolean hasStormInChunk(ChunkPos pos, StormType type) {
        for (Storm storm : this.storms.values()) {
            if (type != null && storm.getType() != type)
                continue;

            double minX = pos.getStartX();
            double minZ = pos.getStartZ();
            double maxX = pos.getEndX();
            double maxZ = pos.getEndZ();

            double stormBoundsMinX = storm.getCenter().getX() - storm.getRadius();
            double stormBoundsMinZ = storm.getCenter().getZ() - storm.getRadius();
            double stormBoundsMaxX = storm.getCenter().getX() + storm.getRadius();
            double stormBoundsMaxZ = storm.getCenter().getZ() + storm.getRadius();

            if (maxX < stormBoundsMinX || minX > stormBoundsMaxX)
                continue;
            if (maxZ < stormBoundsMinZ || minZ > stormBoundsMaxZ)
                continue;

            double stormX = storm.getCenter().getX();
            double stormZ = storm.getCenter().getZ();
            double radius = storm.getRadius();

            // find closest edge
            double edgeX = minX;
            double edgeZ = minZ;

            if (Math.abs(maxX - stormX) < Math.abs(edgeX - stormX))
                edgeX = maxX;
            if (Math.abs(maxZ - stormZ) < Math.abs(edgeZ - stormZ))
                edgeZ = maxZ;

            edgeX = stormX - edgeX;
            edgeZ = stormZ - edgeZ;
            if ((edgeX * edgeX) + (edgeZ * edgeZ) > radius * radius)
                continue;

            return true;
        }
        return false;
    }

    public boolean isThunderingAt(Vec3d pos) {
        for (Storm storm : this.storms.values()) {
            if (storm.getType() == StormType.THUNDER && storm.isPositionInside(pos))
                return true;
        }
        return false;
    }

    public boolean isRainingAt(Vec3d pos) {
        for (Storm storm : this.storms.values()) {
            if (storm.isPositionInside(pos))
                return true;
        }
        return false;
    }

    public float getRainGradientAt(Vec3d pos) {
        if (this.isRainingAt(pos))
            return 1.0f;
        float falloff = 0.0f;
        for (Storm storm : this.storms.values()) {
            if (storm.getType() == StormType.RAIN)
                falloff = Math.max(falloff, this.calculateGradientFalloff(pos, storm));
        }
        return falloff;
    }

    public float getThunderGradientAt(Vec3d pos) {
        if (this.isThunderingAt(pos))
            return 1.0f;
        float falloff = 0.0f;
        for (Storm storm : this.storms.values()) {
            if (storm.getType() == StormType.THUNDER)
                falloff = Math.max(falloff, this.calculateGradientFalloff(pos, storm));
        }
        return falloff;
    }

    private float calculateGradientFalloff(Vec3d pos, Storm storm) {
        double dist = pos.squaredDistanceTo(storm.getCenter().getX(), pos.getY(), storm.getCenter().getZ()) - storm.getRadius() * storm.getRadius();
        if (dist < 0)
            return 0;
        dist = MathHelper.clamp(dist / FALLOFF_DISTANCE_SQUARED, 0.d, 1.d);
        return (float) (1 - dist) * (float) (1 - dist);
    }

    public void save(File saveFile, File backupFile) {
        if (!this.needsSave)
            return;

        NbtList storms = new NbtList();
        for (Storm storm : this.storms.values()) {
            storms.add(storm.write(new NbtCompound()));
        }

        NbtCompound data = new NbtCompound();
        data.put("storms", storms);
        NbtHelper.putDataVersion(data);
        try {
            File tmpFile = File.createTempFile("storms", "dat");
            NbtIo.writeCompressed(data, tmpFile);
            Util.backupAndReplace(saveFile, tmpFile, backupFile);
            this.needsSave = false;
            LocalizedWeather.LOGGER.info("Saved storms for world {}", this.world.getRegistryKey().getValue());
        } catch (IOException e) {
            LocalizedWeather.LOGGER.error("Failed to save storms for world {}", this.world.getRegistryKey().getValue());
        }
    }

    public void read(File file, File backupFile) {
        if (!file.exists())
            return;

        NbtCompound compound;
        try (FileInputStream stream = new FileInputStream(file)) {
            compound = NbtIo.readCompressed(stream);
        } catch (IOException e) {
            // we couldn't read the original file, so maybe the backup file will work?
            if (backupFile != null && backupFile.exists()) {
                LocalizedWeather.LOGGER.warn("Falling back to backup storms file for world {}", this.world.getRegistryKey().getValue());
                this.read(backupFile, null);
                return;
            }
            // both files failed to read
            LocalizedWeather.LOGGER.error("Failed to read storms file for world {}", this.world.getRegistryKey().getValue());
            return;
        }

        // load all the storms
        NbtList storms = compound.getList("storms", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < storms.size(); i++) {
            Storm storm = Storm.fromNbt(storms.getCompound(i));
            this.storms.put(storm.getId(), storm);
        }

        LocalizedWeather.LOGGER.info("Loaded {} storm(s) for world {}", storms.size(), this.world.getRegistryKey().getValue());
    }
}
