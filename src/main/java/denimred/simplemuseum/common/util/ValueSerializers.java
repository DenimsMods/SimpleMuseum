package denimred.simplemuseum.common.util;

import net.minecraft.entity.EntitySize;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.Util;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.util.Constants;

import java.awt.Color;
import java.util.Map;
import java.util.function.Function;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;

public final class ValueSerializers {
    public static final IValueSerializer<Boolean> BOOLEAN =
            new IValueSerializer.Wrapped<Boolean>(DataSerializers.BOOLEAN) {
                @Override
                public Boolean read(CompoundNBT tag, String key) {
                    return tag.getBoolean(key);
                }

                @Override
                public void write(CompoundNBT tag, String key, Boolean value) {
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
                public Integer read(CompoundNBT tag, String key) {
                    return tag.getInt(key);
                }

                @Override
                public void write(CompoundNBT tag, String key, Integer value) {
                    tag.putInt(key, value);
                }

                @Override
                public Integer read(PacketBuffer buf) {
                    return buf.readInt();
                }

                @Override
                public void write(PacketBuffer buf, Integer value) {
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
            new IValueSerializer.Wrapped<Float>(DataSerializers.FLOAT) {
                @Override
                public Float read(CompoundNBT tag, String key) {
                    return tag.getFloat(key);
                }

                @Override
                public void write(CompoundNBT tag, String key, Float value) {
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
            new IValueSerializer.Wrapped<String>(DataSerializers.STRING) {
                @Override
                public String read(CompoundNBT tag, String key) {
                    return tag.getString(key);
                }

                @Override
                public void write(CompoundNBT tag, String key, String value) {
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
                public ResourceLocation read(CompoundNBT tag, String key) {
                    return new ResourceLocation(tag.getString(key));
                }

                @Override
                public void write(CompoundNBT tag, String key, ResourceLocation value) {
                    tag.putString(key, value.toString());
                }

                @Override
                public ResourceLocation read(PacketBuffer buf) {
                    return buf.readResourceLocation();
                }

                @Override
                public void write(PacketBuffer buf, ResourceLocation value) {
                    buf.writeResourceLocation(value);
                }

                @Override
                public ResourceLocation copyValue(ResourceLocation value) {
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

    public static final IValueSerializer<ITextComponent> TEXT_COMPONENT =
            new IValueSerializer.Wrapped<ITextComponent>(DataSerializers.TEXT_COMPONENT) {
                @Override
                public ITextComponent read(CompoundNBT tag, String key) {
                    final IFormattableTextComponent text =
                            ITextComponent.Serializer.getComponentFromJson(tag.getString(key));
                    return text != null ? text : StringTextComponent.EMPTY;
                }

                @Override
                public void write(CompoundNBT tag, String key, ITextComponent value) {
                    tag.putString(key, ITextComponent.Serializer.toJson(value));
                }

                @Override
                public Class<ITextComponent> getType() {
                    return ITextComponent.class;
                }

                @Override
                public int getTagId() {
                    return Constants.NBT.TAG_STRING;
                }
            };

    public static final IValueSerializer<Color> COLOR =
            new IValueSerializer<Color>() {
                @Override
                public Color read(CompoundNBT tag, String key) {
                    return new Color(tag.getInt(key));
                }

                @Override
                public void write(CompoundNBT tag, String key, Color value) {
                    tag.putInt(key, value.getRGB());
                }

                @Override
                public Color read(PacketBuffer buf) {
                    return new Color(buf.readInt(), true);
                }

                @Override
                public void write(PacketBuffer buf, Color value) {
                    buf.writeInt(value.getRGB());
                }

                @Override
                public Color copyValue(Color value) {
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

    public static final IValueSerializer<SoundCategory> SOUND_CATEGORY =
            new IValueSerializer<SoundCategory>() {
                @Override
                public SoundCategory read(CompoundNBT tag, String key) {
                    return SoundCategory.SOUND_CATEGORIES.get(tag.getString(key));
                }

                @Override
                public void write(CompoundNBT tag, String key, SoundCategory value) {
                    tag.putString(key, value.getName());
                }

                @Override
                public SoundCategory read(PacketBuffer buf) {
                    return buf.readEnumValue(SoundCategory.class);
                }

                @Override
                public void write(PacketBuffer buf, SoundCategory value) {
                    buf.writeEnumValue(value);
                }

                @Override
                public Class<SoundCategory> getType() {
                    return SoundCategory.class;
                }

                @Override
                public int getTagId() {
                    return Constants.NBT.TAG_STRING;
                }
            };

    public static final IValueSerializer<EntitySize> ENTITY_SIZE =
            new IValueSerializer<EntitySize>() {
                @Override
                public EntitySize read(CompoundNBT tag, String key) {
                    final ListNBT list = tag.getList(key, Constants.NBT.TAG_FLOAT);
                    return EntitySize.flexible(list.getFloat(0), list.getFloat(1));
                }

                @Override
                public void write(CompoundNBT tag, String key, EntitySize value) {
                    final ListNBT list = new ListNBT();
                    list.add(FloatNBT.valueOf(value.width));
                    list.add(FloatNBT.valueOf(value.height));
                    tag.put(key, list);
                }

                @Override
                public EntitySize read(PacketBuffer buf) {
                    return EntitySize.flexible(buf.readFloat(), buf.readFloat());
                }

                @Override
                public void write(PacketBuffer buf, EntitySize value) {
                    buf.writeFloat(value.width);
                    buf.writeFloat(value.height);
                }

                @Override
                public Class<EntitySize> getType() {
                    return EntitySize.class;
                }

                @Override
                public int getTagId() {
                    return Constants.NBT.TAG_LIST;
                }
            };

    public static final IValueSerializer<GlowColor> GLOW_COLOR =
            new IValueSerializer<GlowColor>() {
                @Override
                public GlowColor read(CompoundNBT tag, String key) {
                    return GlowColor.deserialize(tag.getCompound(key));
                }

                @Override
                public void write(CompoundNBT tag, String key, GlowColor value) {
                    tag.put(key, value.serialize());
                }

                @Override
                public GlowColor read(PacketBuffer buf) {
                    return new GlowColor(buf.readInt(), buf.readBoolean());
                }

                @Override
                public void write(PacketBuffer buf, GlowColor value) {
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
                        map.put(SoundCategory.class, SOUND_CATEGORY);
                        map.put(EntitySize.class, ENTITY_SIZE);
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
            public T read(CompoundNBT tag, String key) {
                return read.apply(tag.getInt(key));
            }

            @Override
            public void write(CompoundNBT tag, String key, T value) {
                tag.putInt(key, write.apply(value));
            }

            @Override
            public T read(PacketBuffer buf) {
                return read.apply(buf.readVarInt());
            }

            @Override
            public void write(PacketBuffer buf, T value) {
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
