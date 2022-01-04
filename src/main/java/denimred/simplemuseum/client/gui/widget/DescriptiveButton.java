package denimred.simplemuseum.client.gui.widget;

import net.minecraft.network.chat.MutableComponent;

import java.util.Collections;
import java.util.List;

import denimred.simplemuseum.common.i18n.Descriptive;

public class DescriptiveButton extends BetterButton implements Descriptive {
    private final MutableComponent description;

    public DescriptiveButton(
            int x,
            int y,
            int width,
            int height,
            MutableComponent title,
            MutableComponent description,
            OnPress press) {
        this(x, y, width, height, title, description, press, NO_TOOLTIP);
    }

    public DescriptiveButton(
            int x,
            int y,
            int width,
            int height,
            MutableComponent title,
            MutableComponent description,
            OnPress press,
            OnTooltip tooltip) {
        super(x, y, width, height, title, press, tooltip);
        this.description = description;
    }

    @Override
    public MutableComponent getTitle() {
        return (MutableComponent) this.getMessage();
    }

    @Override
    public List<MutableComponent> getDescription() {
        return Collections.singletonList(description);
    }
}
