package denimred.simplemuseum.common.entity.puppet.manager.value;

import java.util.function.BiPredicate;

import denimred.simplemuseum.common.entity.puppet.PuppetEntity;

public interface Validator<T> extends BiPredicate<PuppetEntity, T> {}
