package denimred.simplemuseum.client.util;

public interface ISavable<T> {
    T save();

    void load(T t);
}
