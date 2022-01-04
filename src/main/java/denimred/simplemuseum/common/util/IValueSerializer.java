package denimred.simplemuseum.common.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;

public interface IValueSerializer<T> extends EntityDataSerializer<T> {
    T read(CompoundTag tag, String key);

    void write(CompoundTag tag, String key, T value);

    @Override
    T read(FriendlyByteBuf buf);

    @Override
    void write(FriendlyByteBuf buf, T value);

    @Override
    default T copy(T value) {
        return value;
    }

    Class<T> getType();

    int getTagId();

    abstract class Wrapped<T> implements IValueSerializer<T> {
        public final EntityDataSerializer<T> parent;

        public Wrapped(EntityDataSerializer<T> parent) {
            this.parent = parent;
        }

        @Override
        public T read(FriendlyByteBuf buf) {
            return parent.read(buf);
        }

        @Override
        public void write(FriendlyByteBuf buf, T value) {
            parent.write(buf, value);
        }

        @Override
        public T copy(T value) {
            return parent.copy(value);
        }

        @Override
        public EntityDataAccessor<T> createAccessor(int id) {
            return parent.createAccessor(id);
        }
    }
}
