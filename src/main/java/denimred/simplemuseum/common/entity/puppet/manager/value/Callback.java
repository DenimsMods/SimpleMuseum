package denimred.simplemuseum.common.entity.puppet.manager.value;

import java.util.function.BiConsumer;

import denimred.simplemuseum.common.entity.puppet.PuppetEntity;

public interface Callback<T> extends BiConsumer<PuppetEntity, T> {}
