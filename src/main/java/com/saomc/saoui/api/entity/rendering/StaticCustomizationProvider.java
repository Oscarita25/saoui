package com.saomc.saoui.api.entity.rendering;

import com.saomc.saoui.SAOCore;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

/**
 * Part of saoui
 * <p>
 * Represents a static (read: unchanging) customization for an entity.
 * This will be the most common implementation.
 *
 * @author Bluexin
 */
public class StaticCustomizationProvider implements ICustomizationProvider {

    private static final String KEY = new ResourceLocation(SAOCore.MODID, "staticProvider").toString();

    private double xOffset, yOffset, zOffset;
    private double scale;
    private int barCount;

    /**
     * Creates a new unchanging customization provider.
     *
     * @param xOffset  the offset to use along the X axis
     * @param yOffset  the offset to use along the Y axis
     * @param zOffset  the offset to use along the Z axis
     * @param scale    the scale to apply
     * @param barCount the number of hp bars to render
     */
    public StaticCustomizationProvider(double xOffset, double yOffset, double zOffset, double scale, int barCount) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
        this.scale = scale;
        this.barCount = barCount;
    }

    /**
     * @return offset along the X axis to use for rendering
     */
    @Override
    public double getXOffset() {
        return this.xOffset;
    }

    /**
     * @return offset along the Y axis to use for rendering
     */
    @Override
    public double getYOffset() {
        return this.yOffset;
    }

    /**
     * @return offset along the Z axis to use for rendering
     */
    @Override
    public double getZOffset() {
        return this.zOffset;
    }

    /**
     * @return scale to use for rendering
     */
    @Override
    public double getScale() {
        return this.scale;
    }

    /**
     * @return number of HP bars to render
     */
    @Override
    public int getBarCount() {
        return this.barCount;
    }

    /**
     * Save any data to NBT format.
     * The implementation has to create his own sub-tag and to behave properly.
     * This will be used for sync between client and server, and saving on world shutting down.
     *
     * @param tag the NBT tag to save to
     */
    @Override
    public void save(NBTTagCompound tag) {
        NBTTagCompound atag = new NBTTagCompound();
        atag.setDouble("xOffset", this.xOffset);
        atag.setDouble("yOffset", this.yOffset);
        atag.setDouble("zOffset", this.zOffset);
        atag.setDouble("scale", this.scale);
        atag.setInteger("barCount", this.barCount);
        tag.setTag(KEY, atag);
    }

    /**
     * Load any data from NBT format.
     * The implementation has to retrieve his own sub-tag and to behave properly.
     * This will be used for sync between client and server, and loading on world starting up.
     *
     * @param tag the NBT tag to save to
     */
    @Override
    public void load(NBTTagCompound tag) {
        NBTTagCompound atag = tag.getCompoundTag(KEY);
        this.xOffset = atag.getDouble("xOffset");
        this.yOffset = atag.getDouble("yOffset");
        this.zOffset = atag.getDouble("zOffset");
        this.scale = atag.getDouble("scale");
        this.barCount = atag.getInteger("barCount");
    }
}