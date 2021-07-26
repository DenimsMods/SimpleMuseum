package denimred.simplemuseum.common.entity.puppet.manager.value.vanilla;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import denimred.simplemuseum.common.entity.puppet.manager.PuppetValueManager;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetKey;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValueProvider;
import denimred.simplemuseum.common.util.IValueSerializer;
import denimred.simplemuseum.common.util.ValueSerializers;

public final class CustomNameProvider extends PuppetValueProvider<ITextComponent, CustomNameValue> {
    public CustomNameProvider(PuppetKey key) {
        this(key, ValueSerializers.TEXT_COMPONENT);
    }

    public CustomNameProvider(PuppetKey key, IValueSerializer<ITextComponent> serializer) {
        super(key, StringTextComponent.EMPTY, serializer, null, null);
    }

    @Override
    public CustomNameValue provideFor(PuppetValueManager manager) {
        return new CustomNameValue(this, manager);
    }
}
