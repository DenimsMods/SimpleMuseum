package denimred.simplemuseum.common.util;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.IDataSerializer;

public interface IValueSerializer<T> extends IDataSerializer<T> {
    T read(CompoundNBT tag, String key);

    void write(CompoundNBT tag, String key, T value);

    @Override
    T read(PacketBuffer buf);

    @Override
    void write(PacketBuffer buf, T value);

    @Override
    default T copyValue(T value) {
        return value;
    }

    Class<T> getType();

    int getTagId();

    abstract class Wrapped<T> implements IValueSerializer<T> {
        public final IDataSerializer<T> parent;

        public Wrapped(IDataSerializer<T> parent) {
            this.parent = parent;
        }

        @Override
        public T read(PacketBuffer buf) {
            return parent.read(buf);
        }

        @Override
        public void write(PacketBuffer buf, T value) {
            parent.write(buf, value);
        }

        @Override
        public T copyValue(T value) {
            return parent.copyValue(value);
        }

        @Override
        public DataParameter<T> createKey(int id) {
            return parent.createKey(id);
        }
    }
}
