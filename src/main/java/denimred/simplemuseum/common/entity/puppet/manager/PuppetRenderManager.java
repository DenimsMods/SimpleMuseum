package denimred.simplemuseum.common.entity.puppet.manager;

import com.mojang.datafixers.util.Pair;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

import java.util.Arrays;
import java.util.Comparator;

import denimred.simplemuseum.client.util.ClientUtil;
import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetKey;
import denimred.simplemuseum.common.entity.puppet.manager.value.primitive.BoolProvider;
import denimred.simplemuseum.common.entity.puppet.manager.value.primitive.BoolValue;
import denimred.simplemuseum.common.entity.puppet.manager.value.primitive.FloatProvider;
import denimred.simplemuseum.common.entity.puppet.manager.value.primitive.FloatValue;
import denimred.simplemuseum.common.entity.puppet.manager.value.standard.BasicProvider;
import denimred.simplemuseum.common.entity.puppet.manager.value.standard.BasicValue;
import denimred.simplemuseum.common.i18n.I18nUtil;
import denimred.simplemuseum.common.util.IValueSerializer;
import denimred.simplemuseum.common.util.ValueSerializers;

public final class PuppetRenderManager extends PuppetValueManager {
    public static final String NBT_KEY = "RenderManager";
    public static final String TRANSLATION_KEY = I18nUtil.valueManager(NBT_KEY);

    public static final FloatProvider SCALE =
            new FloatProvider(
                    key("Scale"),
                    1.0F,
                    (puppet, scale) -> puppet.refreshDimensions(),
                    0.01F,
                    100.0F);
    //    public static final BasicProvider<RenderLayer> DEFAULT_RENDER_LAYER =
    //            new BasicProvider<>(
    //                    key("DefaultRenderLayer"), RenderLayer.CUTOUT, RenderLayer.SERIALIZER);
    public static final BasicProvider<NameplateBehavior> NAMEPLATE_BEHAVIOR =
            new BasicProvider<>(
                    key("NameplateBehavior"),
                    NameplateBehavior.ON_HOVER,
                    NameplateBehavior.SERIALIZER);
    public static final BoolProvider IGNORE_LIGHTING =
            new BoolProvider(key("IgnoreLighting"), false);
    public static final BoolProvider FLAMING = new BoolProvider(key("Flaming"), false);
    //    public static final BasicProvider<Color> TINT_COLOR =
    //            new BasicProvider<>(key("TintColor"), Color.WHITE);
    //    public static final BasicProvider<GlowColor> GLOW_COLOR =
    //            new BasicProvider<>(key("GlowColor"), GlowColor.DEFAULT);

    public final FloatValue scale = this.value(SCALE);
    //    public final BasicValue<RenderLayer> defaultRenderLayer =
    // this.value(DEFAULT_RENDER_LAYER);
    public final BasicValue<NameplateBehavior> nameplateBehavior = this.value(NAMEPLATE_BEHAVIOR);
    public final BoolValue ignoreLighting = this.value(IGNORE_LIGHTING);
    public final BoolValue flaming = this.value(FLAMING);
    //    public final BasicValue<Color> tintColor = this.value(TINT_COLOR);
    //    public final BasicValue<GlowColor> glowColor = this.value(GLOW_COLOR);

    public PuppetRenderManager(PuppetEntity puppet) {
        super(puppet, NBT_KEY, TRANSLATION_KEY);
    }

    private static PuppetKey key(String provider) {
        return new PuppetKey(NBT_KEY, provider);
    }

    public AABB getRenderBounds() {
        final AABB bounds =
                ClientUtil.getPuppetBounds(puppet)
                        .map(Pair::getSecond)
                        .orElseGet(this::getOffsetPuppetBounds);
        final float scale = puppet.getScale();
        return new AABB(
                        bounds.minX * scale,
                        bounds.minY * scale,
                        bounds.minZ * scale,
                        bounds.maxX * scale,
                        bounds.maxY * scale,
                        bounds.maxZ * scale)
                .move(puppet.position());
    }

    private AABB getOffsetPuppetBounds() {
        return puppet.getBoundingBox().move(puppet.position().reverse());
    }

    public RenderType getRenderType(ResourceLocation texture) {
        return RenderType.entityCutoutNoCull(
                texture); // ClientUtil.typeFromLayer(defaultRenderLayer.get(), texture);
    }

    public boolean canRenderHiddenDeathEffects() {
        return puppet.isDead() && puppet.isCompletelyDead() && ClientUtil.isHoldingCane();
    }

    public enum RenderLayer {
        CUTOUT(0),
        CUTOUT_CULL(1),
        TRANSLUCENT(2),
        TRANSLUCENT_CULL(3);

        private static final RenderLayer[] VALUES =
                Arrays.stream(values())
                        .sorted(Comparator.comparingInt(RenderLayer::getId))
                        .toArray(RenderLayer[]::new);
        public static final IValueSerializer<RenderLayer> SERIALIZER =
                ValueSerializers.forIndexed(
                        RenderLayer.class, RenderLayer::fromId, RenderLayer::getId);

        private final int id;

        RenderLayer(int id) {
            this.id = id;
        }

        public static RenderLayer fromId(int id) {
            return id < 0 || id >= VALUES.length ? VALUES[0] : VALUES[id];
        }

        public int getId() {
            return id;
        }
    }

    public enum NameplateBehavior {
        ON_HOVER(0),
        ALWAYS(1),
        NEVER(2);

        private static final NameplateBehavior[] VALUES =
                Arrays.stream(values())
                        .sorted(Comparator.comparingInt(NameplateBehavior::getId))
                        .toArray(NameplateBehavior[]::new);
        public static final IValueSerializer<NameplateBehavior> SERIALIZER =
                ValueSerializers.forIndexed(
                        NameplateBehavior.class,
                        NameplateBehavior::fromId,
                        NameplateBehavior::getId);

        private final int id;

        NameplateBehavior(int id) {
            this.id = id;
        }

        public static NameplateBehavior fromId(int id) {
            return id < 0 || id >= VALUES.length ? VALUES[0] : VALUES[id];
        }

        public int getId() {
            return id;
        }
    }
}
