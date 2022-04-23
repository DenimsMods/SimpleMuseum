package denimred.simplemuseum.common.util;

import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraftforge.common.util.Constants;

import java.awt.Color;
import java.util.Map;
import java.util.function.Function;

import denimred.simplemuseum.common.entity.puppet.goals.PuppetGoalTree;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;

public final class ValueSerializers {
    public static final IValueSerializer<Boolean> BOOLEAN =
            new IValueSerializer.Wrapped<Boolean>(EntityDataSerializers.BOOLEAN) {
                @Override
                public Boolean read(CompoundTag tag, String key) {
                    return tag.getBoolean(key);
                }

                @Override
                public void write(CompoundTag tag, String key, Boolean value) {
                    tag.putBoolean(key, value);
                }

                @Override
                public Class<Boolean> getType() {
                    return Boolean.class;
                }

                @Override
                public int getTagId() {
                    return Constants.NBT.TAG_BYTE;
                }
            };

    public static final IValueSerializer<Integer> INTEGER =
            new IValueSerializer<Integer>() {
                @Override
                public Integer read(CompoundTag tag, String key) {
                    return tag.getInt(key);
                }

                @Override
                public void write(CompoundTag tag, String key, Integer value) {
                    tag.putInt(key, value);
                }

                @Override
                public Integer read(FriendlyByteBuf buf) {
                    return buf.readInt();
                }

                @Override
                public void write(FriendlyByteBuf buf, Integer value) {
                    buf.writeInt(value);
                }

                @Override
                public Class<Integer> getType() {
                    return Integer.class;
                }

                @Override
                public int getTagId() {
                    return Constants.NBT.TAG_INT;
                }
            };

    public static final IValueSerializer<Float> FLOAT =
            new IValueSerializer.Wrapped<Float>(EntityDataSerializers.FLOAT) {
                @Override
                public Float read(CompoundTag tag, String key) {
                    return tag.getFloat(key);
                }

                @Override
                public void write(CompoundTag tag, String key, Float value) {
                    tag.putFloat(key, value);
                }

                @Override
                public Class<Float> getType() {
                    return Float.class;
                }

                @Override
                public int getTagId() {
                    return Constants.NBT.TAG_FLOAT;
                }
            };

    public static final IValueSerializer<String> STRING =
            new IValueSerializer.Wrapped<String>(EntityDataSerializers.STRING) {
                @Override
                public String read(CompoundTag tag, String key) {
                    return tag.getString(key);
                }

                @Override
                public void write(CompoundTag tag, String key, String value) {
                    tag.putString(key, value);
                }

                @Override
                public Class<String> getType() {
                    return String.class;
                }

                @Override
                public int getTagId() {
                    return Constants.NBT.TAG_STRING;
                }
            };

    public static final IValueSerializer<ResourceLocation> RESOURCE_LOCATION =
            new IValueSerializer<ResourceLocation>() {
                @Override
                public ResourceLocation read(CompoundTag tag, String key) {
                    return new ResourceLocation(tag.getString(key));
                }

                @Override
                public void write(CompoundTag tag, String key, ResourceLocation value) {
                    tag.putString(key, value.toString());
                }

                @Override
                public ResourceLocation read(FriendlyByteBuf buf) {
                    return buf.readResourceLocation();
                }

                @Override
                public void write(FriendlyByteBuf buf, ResourceLocation value) {
                    buf.writeResourceLocation(value);
                }

                @Override
                public ResourceLocation copy(ResourceLocation value) {
                    return new ResourceLocation(value.getNamespace(), value.getPath());
                }

                @Override
                public Class<ResourceLocation> getType() {
                    return ResourceLocation.class;
                }

                @Override
                public int getTagId() {
                    return Constants.NBT.TAG_STRING;
                }
            };

    public static final IValueSerializer<Component> TEXT_COMPONENT =
            new IValueSerializer.Wrapped<Component>(EntityDataSerializers.COMPONENT) {
                @Override
                public Component read(CompoundTag tag, String key) {
                    final MutableComponent text = Component.Serializer.fromJson(tag.getString(key));
                    return text != null ? text : TextComponent.EMPTY;
                }

                @Override
                public void write(CompoundTag tag, String key, Component value) {
                    tag.putString(key, Component.Serializer.toJson(value));
                }

                @Override
                public Class<Component> getType() {
                    return Component.class;
                }

                @Override
                public int getTagId() {
                    return Constants.NBT.TAG_STRING;
                }
            };

    public static final IValueSerializer<Color> COLOR =
            new IValueSerializer<Color>() {
                @Override
                public Color read(CompoundTag tag, String key) {
                    return new Color(tag.getInt(key));
                }

                @Override
                public void write(CompoundTag tag, String key, Color value) {
                    tag.putInt(key, value.getRGB());
                }

                @Override
                public Color read(FriendlyByteBuf buf) {
                    return new Color(buf.readInt(), true);
                }

                @Override
                public void write(FriendlyByteBuf buf, Color value) {
                    buf.writeInt(value.getRGB());
                }

                @Override
                public Color copy(Color value) {
                    return new Color(value.getRGB(), true);
                }

                @Override
                public Class<Color> getType() {
                    return Color.class;
                }

                @Override
                public int getTagId() {
                    return Constants.NBT.TAG_INT;
                }
            };

    public static final IValueSerializer<SoundSource> SOUND_CATEGORY =
            new IValueSerializer<SoundSource>() {
                @Override
                public SoundSource read(CompoundTag tag, String key) {
                    return SoundSource.BY_NAME.get(tag.getString(key));
                }

                @Override
                public void write(CompoundTag tag, String key, SoundSource value) {
                    tag.putString(key, value.getName());
                }

                @Override
                public SoundSource read(FriendlyByteBuf buf) {
                    return buf.readEnum(SoundSource.class);
                }

                @Override
                public void write(FriendlyByteBuf buf, SoundSource value) {
                    buf.writeEnum(value);
                }

                @Override
                public Class<SoundSource> getType() {
                    return SoundSource.class;
                }

                @Override
                public int getTagId() {
                    return Constants.NBT.TAG_STRING;
                }
            };

    public static final IValueSerializer<EntityDimensions> ENTITY_SIZE =
            new IValueSerializer<EntityDimensions>() {
                @Override
                public EntityDimensions read(CompoundTag tag, String key) {
                    final ListTag list = tag.getList(key, Constants.NBT.TAG_FLOAT);
                    return EntityDimensions.scalable(list.getFloat(0), list.getFloat(1));
                }

                @Override
                public void write(CompoundTag tag, String key, EntityDimensions value) {
                    final ListTag list = new ListTag();
                    list.add(FloatTag.valueOf(value.width));
                    list.add(FloatTag.valueOf(value.height));
                    tag.put(key, list);
                }

                @Override
                public EntityDimensions read(FriendlyByteBuf buf) {
                    return EntityDimensions.scalable(buf.readFloat(), buf.readFloat());
                }

                @Override
                public void write(FriendlyByteBuf buf, EntityDimensions value) {
                    buf.writeFloat(value.width);
                    buf.writeFloat(value.height);
                }

                @Override
                public Class<EntityDimensions> getType() {
                    return EntityDimensions.class;
                }

                @Override
                public int getTagId() {
                    return Constants.NBT.TAG_LIST;
                }
            };

    public static final IValueSerializer<PuppetGoalTree> GOAL_TREE =
            new IValueSerializer<PuppetGoalTree>() {
                @Override
                public PuppetGoalTree read(CompoundTag tag, String key) {
                    PuppetGoalTree tree = new PuppetGoalTree();
                    return tree;
                }

                @Override
                public void write(CompoundTag tag, String key, PuppetGoalTree value) {

                }

                @Override
                public PuppetGoalTree read(FriendlyByteBuf buf) {
                    PuppetGoalTree tree = new PuppetGoalTree();
                    return tree;
                }

                @Override
                public void write(FriendlyByteBuf buf, PuppetGoalTree value) {

                }

                @Override
                public Class<PuppetGoalTree> getType() {
                    return PuppetGoalTree.class;
                }

                @Override
                public int getTagId() {
                    return Constants.NBT.TAG_COMPOUND;
                }
            };

    public static final IValueSerializer<GlowColor> GLOW_COLOR =
            new IValueSerializer<GlowColor>() {
                @Override
                public GlowColor read(CompoundTag tag, String key) {
                    return GlowColor.deserialize(tag.getCompound(key));
                }

                @Override
                public void write(CompoundTag tag, String key, GlowColor value) {
                    tag.put(key, value.serialize());
                }

                @Override
                public GlowColor read(FriendlyByteBuf buf) {
                    return new GlowColor(buf.readInt(), buf.readBoolean());
                }

                @Override
                public void write(FriendlyByteBuf buf, GlowColor value) {
                    buf.writeInt(value.rgb);
                    buf.writeBoolean(value.useTeamColor);
                }

                @Override
                public Class<GlowColor> getType() {
                    return GlowColor.class;
                }

                @Override
                public int getTagId() {
                    return Constants.NBT.TAG_COMPOUND;
                }
            };

    private static final Map<Class<?>, IValueSerializer<?>> LAZY_GUESSES =
            Util.make(
                    new Reference2ReferenceOpenHashMap<>(),
                    map -> {
                        map.put(String.class, STRING);
                        map.put(ResourceLocation.class, RESOURCE_LOCATION);
                        map.put(SoundSource.class, SOUND_CATEGORY);
                        map.put(EntityDimensions.class, ENTITY_SIZE);
                        map.put(GlowColor.class, GLOW_COLOR);
                        map.put(Color.class, COLOR);
                        map.put(Boolean.class, BOOLEAN);
                        map.put(Boolean.TYPE, BOOLEAN);
                        map.put(Integer.class, INTEGER);
                        map.put(Integer.TYPE, INTEGER);
                        map.put(Float.class, FLOAT);
                        map.put(Float.TYPE, FLOAT);
                    });

    @SuppressWarnings("unchecked")
    public static <T> IValueSerializer<T> lazyGuess(T value) {
        final Class<?> clazz = value.getClass();
        final IValueSerializer<?> serializer = LAZY_GUESSES.get(clazz);
        if (serializer == null) {
            throw new IllegalArgumentException("Can't guess serializer for " + clazz);
        } else {
            return (IValueSerializer<T>) serializer;
        }
    }

    public static <T> IValueSerializer<T> forIndexed(
            Class<T> type, Function<Integer, T> read, Function<T, Integer> write) {
        return new IValueSerializer<T>() {
            @Override
            public T read(CompoundTag tag, String key) {
                return read.apply(tag.getInt(key));
            }

            @Override
            public void write(CompoundTag tag, String key, T value) {
                tag.putInt(key, write.apply(value));
            }

            @Override
            public T read(FriendlyByteBuf buf) {
                return read.apply(buf.readVarInt());
            }

            @Override
            public void write(FriendlyByteBuf buf, T value) {
                buf.writeVarInt(write.apply(value));
            }

            @Override
            public Class<T> getType() {
                return type;
            }

            @Override
            public int getTagId() {
                return Constants.NBT.TAG_INT;
            }
        };
    }
}
