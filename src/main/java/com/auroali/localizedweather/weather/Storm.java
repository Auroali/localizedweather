package com.auroali.localizedweather.weather;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.concurrent.atomic.AtomicInteger;

public class Storm {
    private static final AtomicInteger ID_PROVIDER = new AtomicInteger();
    private int id = ID_PROVIDER.incrementAndGet();
    private final StormType type;
    private Vec3d center;
    private final Vec3d direction;
    private double radius;
    private final double radiusDecay;
    private final int decayChance;
    private final int lifetime;
    private int ticksAlive;
    boolean shouldRemove;

    public Storm(StormType type, Vec3d center, Vec3d direction, double radius, double radiusDecay, int decayChance, int lifetime) {
        this.type = type;
        this.center = center;
        this.direction = direction;
        this.radius = radius;
        this.radiusDecay = radiusDecay;
        this.decayChance = decayChance;
        this.lifetime = lifetime;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public StormType getType() {
        return this.type;
    }

    public Vec3d getCenter() {
        return this.center;
    }

    public double getRadius() {
        return this.radius;
    }

    public void move(Random random) {
        this.center = this.center.add(this.direction);
        if(this.decayChance == 0 || random.nextInt(this.decayChance) == 0)
            this.radius = this.radius - this.radius * this.radiusDecay;
        if(this.radius == 0.d || this.ticksAlive++ > this.lifetime)
            this.shouldRemove = true;
    }

    public boolean shouldRemove() {
        return this.shouldRemove;
    }

    public void setCenter(Vec3d pos) {
        this.center = pos;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    /**
     * @param pos the position to check
     * @return if the position is within the storm
     */
    public boolean isPositionInside(BlockPos pos) {
        return pos.getSquaredDistanceFromCenter(this.center.getX(), pos.getY(), this.center.getZ()) <= this.radius*this.radius;
    }

    /**
     * @param pos the position to check
     * @return if the position is within the storm
     */
    public boolean isPositionInside(Vec3d pos) {
        return pos.squaredDistanceTo(this.center.getX(), pos.getY(), this.center.getZ()) <= this.radius*this.radius;
    }

    public NbtCompound write(NbtCompound compound) {
        compound.putByte("Type", (byte) this.type.ordinal());
        compound.putDouble("CenterX", this.center.getX());
        compound.putDouble("CenterZ", this.center.getZ());
        compound.putDouble("DirectionX", this.direction.getX());
        compound.putDouble("DirectionZ", this.direction.getZ());
        compound.putDouble("Radius", this.radius);
        compound.putDouble("RadiusDecay", this.radiusDecay);
        compound.putInt("DecayChance", this.decayChance);
        compound.putInt("Lifetime", this.lifetime);
        compound.putInt("Age", this.ticksAlive);
        return compound;
    }

    public static Storm fromNbt(NbtCompound compound) {
        StormType type = StormType.values()[compound.getByte("Type")];
        double centerX = compound.getDouble("CenterX");
        double centerZ = compound.getDouble("CenterZ");
        double directionX = compound.getDouble("DirectionX");
        double directionZ = compound.getDouble("DirectionZ");
        double radius = compound.getDouble("Radius");
        double radiusDecay = compound.getDouble("RadiusDecay");
        int decayChance = compound.getInt("DecayChance");
        int lifetime = compound.getInt("Lifetime");
        int ticksAlive = compound.getInt("Age");
        Storm storm = new Storm(
                type,
                new Vec3d(centerX, 0, centerZ),
                new Vec3d(directionX, 0, directionZ),
                radius,
                radiusDecay,
                decayChance,
                lifetime
        );
        storm.ticksAlive = ticksAlive;
        return storm;
    }
}