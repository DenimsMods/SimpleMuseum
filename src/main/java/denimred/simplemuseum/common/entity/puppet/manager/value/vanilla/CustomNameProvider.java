package denimred.simplemuseum.common.entity.puppet.manager.value.vanilla;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import denimred.simplemuseum.common.entity.puppet.manager.PuppetValueManager;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetKey;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValueProvider;
import denimred.simplemuseum.common.util.IValueSerializer;
import denimred.simplemuseum.common.util.ValueSerializers;

public final class CustomNameProvider extends PuppetValueProvider<Component, CustomNameValue> {
    public CustomNameProvider(PuppetKey key) {
        this(key, ValueSerializers.TEXT_COMPONENT);
    }

    public CustomNameProvider(PuppetKey key, IValueSerializer<Component> serializer) {
        super(key, TextComponent.EMPTY, serializer, null, null);
    }

    @Override
    public CustomNameValue provideFor(PuppetValueManager manager) {
        return new CustomNameValue(this, manager);
    }
}
