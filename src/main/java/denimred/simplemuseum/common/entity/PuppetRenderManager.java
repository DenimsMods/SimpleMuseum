package denimred.simplemuseum.common.entity;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;

import java.awt.Color;

import denimred.simplemuseum.client.util.ClientUtil;
import denimred.simplemuseum.common.init.MuseumDataSerializers;
import denimred.simplemuseum.common.util.IntPercent;

import static net.minecraftforge.common.util.Constants.NBT.TAG_BYTE;
import static net.minecraftforge.common.util.Constants.NBT.TAG_INT;

public class PuppetRenderManager extends PuppetManager {
    // The root NBT key that this manager uses
    public static final String RENDER_MANAGER_NBT = "RenderManager";
    // NBT keys for managed variables
    public static final String CULL_NBT = "Cull";
    public static final String TRANSLUCENT_NBT = "Translucent";
    public static final String LIGHTING_NBT = "Lighting";
    public static final String EASTER_EGGS_NBT = "EasterEggs";
    public static final String ERROR_BANNERS_NBT = "ErrorBanners";
    public static final String SCALE_NBT = "Scale";
    public static final String COLOR_NBT = "Color";
    // Data keys for managed variables
    public static final DataParameter<Boolean> CULL_KEY =
            MuseumPuppetEntity.createKeyContextual(DataSerializers.BOOLEAN);
    public static final DataParameter<Boolean> TRANSLUCENT_KEY =
            MuseumPuppetEntity.createKeyContextual(DataSerializers.BOOLEAN);
    public static final DataParameter<Boolean> LIGHTING_KEY =
            MuseumPuppetEntity.createKeyContextual(DataSerializers.BOOLEAN);
    public static final DataParameter<Boolean> EASTER_EGGS_KEY =
            MuseumPuppetEntity.createKeyContextual(DataSerializers.BOOLEAN);
    public static final DataParameter<Boolean> ERROR_BANNERS_KEY =
            MuseumPuppetEntity.createKeyContextual(DataSerializers.BOOLEAN);
    public static final DataParameter<Integer> SCALE_KEY =
            MuseumPuppetEntity.createKeyContextual(MuseumDataSerializers.integer());
    public static final DataParameter<Color> COLOR_KEY =
            MuseumPuppetEntity.createKeyContextual(MuseumDataSerializers.color());
    // Defaults for managed variables
    public static final boolean CULL_DEFAULT = false;
    public static final boolean TRANSLUCENT_DEFAULT = false;
    public static final boolean LIGHTING_DEFAULT = true;
    public static final boolean EASTER_EGGS_DEFAULT = true;
    public static final boolean ERROR_BANNERS_DEFAULT = true;
    public static final int SCALE_DEFAULT = 100;
    public static final Color COLOR_DEFAULT = Color.WHITE;
    // Managed variables
    public final IntPercent scale =
            new IntPercent(1, 1000, SCALE_DEFAULT, i -> dataManager.set(SCALE_KEY, i));
    private boolean cull = CULL_DEFAULT;
    private boolean translucent = TRANSLUCENT_DEFAULT;
    private boolean lighting = LIGHTING_DEFAULT;
    private boolean easterEggs = EASTER_EGGS_DEFAULT;
    private boolean errorBanners = ERROR_BANNERS_DEFAULT;
    private Color color = COLOR_DEFAULT;

    public PuppetRenderManager(MuseumPuppetEntity puppet) {
        super(puppet, RENDER_MANAGER_NBT);
    }

    public AxisAlignedBB getRenderBounds() {
        final AxisAlignedBB bounds =
                ClientUtil.getModelBounds(puppet).orElseGet(this::getOffsetPuppetBounds);
        final float scale = this.scale.asFloat();
        return new AxisAlignedBB(
                        bounds.minX * scale,
                        bounds.minY * scale,
                        bounds.minZ * scale,
                        bounds.maxX * scale,
                        bounds.maxY * scale,
                        bounds.maxZ * scale)
                .offset(puppet.getPositionVec());
    }

    private AxisAlignedBB getOffsetPuppetBounds() {
        return puppet.getBoundingBox().offset(puppet.getPositionVec().inverse());
    }

    public RenderType getRenderType(ResourceLocation texture) {
        return cull
                ? translucent
                        ? RenderType.getEntityTranslucentCull(texture)
                        : RenderType.getEntityCutout(texture)
                : translucent
                        ? RenderType.getEntityTranslucent(texture)
                        : RenderType.getEntityCutoutNoCull(texture);
    }

    public boolean canRenderHiddenDeathEffects() {
        return puppet.isDead() && puppet.isInvisible() && ClientUtil.isHoldingCane();
    }

    public boolean isCull() {
        return cull;
    }

    public void setCull(boolean cull) {
        this.cull = cull;
        dataManager.set(CULL_KEY, cull);
    }

    public boolean isTranslucent() {
        return translucent;
    }

    public void setTranslucent(boolean translucent) {
        this.translucent = translucent;
        dataManager.set(TRANSLUCENT_KEY, translucent);
    }

    public boolean isLighting() {
        return lighting;
    }

    public void setLighting(boolean lighting) {
        this.lighting = lighting;
        dataManager.set(LIGHTING_KEY, lighting);
    }

    public boolean isEasterEggs() {
        return easterEggs;
    }

    public void setEasterEggs(boolean easterEggs) {
        this.easterEggs = easterEggs;
        dataManager.set(EASTER_EGGS_KEY, easterEggs);
    }

    public boolean isErrorBanners() {
        return errorBanners;
    }

    public void setErrorBanners(boolean errorBanners) {
        this.errorBanners = errorBanners;
        dataManager.set(ERROR_BANNERS_KEY, errorBanners);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color c) {
        color = c;
        dataManager.set(COLOR_KEY, c);
    }

    @Override
    public void registerDataKeys() {
        dataManager.register(CULL_KEY, CULL_DEFAULT);
        dataManager.register(TRANSLUCENT_KEY, TRANSLUCENT_DEFAULT);
        dataManager.register(LIGHTING_KEY, LIGHTING_DEFAULT);
        dataManager.register(EASTER_EGGS_KEY, EASTER_EGGS_DEFAULT);
        dataManager.register(ERROR_BANNERS_KEY, ERROR_BANNERS_DEFAULT);
        dataManager.register(SCALE_KEY, SCALE_DEFAULT);
        dataManager.register(COLOR_KEY, COLOR_DEFAULT);
    }

    @Override
    public void onDataChanged(DataParameter<?> key) {
        if (key.equals(CULL_KEY)) this.setCull(dataManager.get(CULL_KEY));
        else if (key.equals(TRANSLUCENT_KEY)) this.setTranslucent(dataManager.get(TRANSLUCENT_KEY));
        else if (key.equals(LIGHTING_KEY)) this.setLighting(dataManager.get(LIGHTING_KEY));
        else if (key.equals(EASTER_EGGS_KEY)) this.setEasterEggs(dataManager.get(EASTER_EGGS_KEY));
        else if (key.equals(ERROR_BANNERS_KEY))
            this.setErrorBanners(dataManager.get(ERROR_BANNERS_KEY));
        else if (key.equals(SCALE_KEY)) scale.set(dataManager.get(SCALE_KEY));
        else if (key.equals(COLOR_KEY)) this.setColor(dataManager.get(COLOR_KEY));
    }

    @Override
    public void readNBT(CompoundNBT tag) {
        if (tag.contains(CULL_NBT, TAG_BYTE)) this.setCull(tag.getBoolean(CULL_NBT));
        if (tag.contains(TRANSLUCENT_NBT, TAG_BYTE))
            this.setTranslucent(tag.getBoolean(TRANSLUCENT_NBT));
        if (tag.contains(LIGHTING_NBT, TAG_BYTE)) this.setLighting(tag.getBoolean(LIGHTING_NBT));
        if (tag.contains(EASTER_EGGS_NBT, TAG_BYTE))
            this.setEasterEggs(tag.getBoolean(EASTER_EGGS_NBT));
        if (tag.contains(ERROR_BANNERS_NBT, TAG_BYTE))
            this.setErrorBanners(tag.getBoolean(ERROR_BANNERS_NBT));
        if (tag.contains(SCALE_NBT, TAG_INT)) scale.set(tag.getInt(SCALE_NBT));
        if (tag.contains(COLOR_NBT, TAG_INT)) this.setColor(new Color(tag.getInt(COLOR_NBT), true));
    }

    @Override
    public void writeNBT(CompoundNBT tag) {
        if (cull != CULL_DEFAULT) tag.putBoolean(CULL_NBT, cull);
        if (translucent != TRANSLUCENT_DEFAULT) tag.putBoolean(TRANSLUCENT_NBT, translucent);
        if (lighting != LIGHTING_DEFAULT) tag.putBoolean(LIGHTING_NBT, lighting);
        if (easterEggs != EASTER_EGGS_DEFAULT) tag.putBoolean(EASTER_EGGS_NBT, easterEggs);
        if (errorBanners != ERROR_BANNERS_DEFAULT) tag.putBoolean(ERROR_BANNERS_NBT, errorBanners);
        final int scale = this.scale.asInt();
        if (scale != SCALE_DEFAULT) tag.putInt(SCALE_NBT, scale);
        if (!color.equals(COLOR_DEFAULT)) tag.putInt(COLOR_NBT, color.getRGB());
    }
}
