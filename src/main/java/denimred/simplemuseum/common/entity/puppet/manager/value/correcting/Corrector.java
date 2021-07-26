package denimred.simplemuseum.common.entity.puppet.manager.value.correcting;

import java.util.function.BiFunction;

import denimred.simplemuseum.common.entity.puppet.PuppetEntity;

public interface Corrector<T> extends BiFunction<PuppetEntity, T, T> {}
