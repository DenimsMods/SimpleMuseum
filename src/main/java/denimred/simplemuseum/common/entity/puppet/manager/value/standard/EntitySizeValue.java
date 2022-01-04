package denimred.simplemuseum.common.entity.puppet.manager.value.standard;

import net.minecraft.world.entity.EntityDimensions;

import denimred.simplemuseum.common.entity.puppet.manager.PuppetValueManager;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValue;

public final class EntitySizeValue extends PuppetValue<EntityDimensions, EntitySizeProvider> {
    EntitySizeValue(EntitySizeProvider provider, PuppetValueManager manager) {
        super(provider, manager);
    }

    public float getWidth() {
        return value.width;
    }

    public void setWidth(float width) {
        super.set(EntityDimensions.scalable(this.clampWidth(width), this.getHeight()));
    }

    public float getHeight() {
        return value.height;
    }

    public void setHeight(float height) {
        super.set(EntityDimensions.scalable(this.getWidth(), this.clampHeight(height)));
    }

    @Override
    public void set(EntityDimensions size) {
        super.set(this.clampSize(size));
    }

    private EntityDimensions clampSize(EntityDimensions size) {
        // We do this in kind of a roundabout manner since EntitySize doesn't implement equals()
        // which can cause a stack overflow in the entity data manager since the value will always
        // be considered dirty.
        final float width = this.clampWidth(size.width);
        final float height = this.clampHeight(size.height);
        if (size.width != width || size.height != height) {
            return EntityDimensions.scalable(width, height);
        }
        return size;
    }

    private float clampWidth(float width) {
        return Math.min(Math.max(width, provider.min.width), provider.max.width);
    }

    private float clampHeight(float height) {
        return Math.min(Math.max(height, provider.min.height), provider.max.height);
    }

    @Override
    public boolean isDefault() {
        return value.width == provider.defaultValue.width
                && value.height == provider.defaultValue.height;
    }

    public boolean testWidth(float width) {
        return width >= provider.min.width && width <= provider.max.width;
    }

    public boolean testHeight(float height) {
        return height >= provider.min.height && height <= provider.max.height;
    }

    @Override
    public boolean test(EntityDimensions size) {
        return this.testWidth(size.width) && this.testHeight(size.height);
    }
}
