package denimred.simplemuseum.client.gui.widget;

import net.minecraft.util.text.IFormattableTextComponent;

import java.util.Collections;
import java.util.List;

import denimred.simplemuseum.common.i18n.Descriptive;

public class DescriptiveButton extends BetterButton implements Descriptive {
    private final IFormattableTextComponent description;

    public DescriptiveButton(
            int x,
            int y,
            int width,
            int height,
            IFormattableTextComponent title,
            IFormattableTextComponent description,
            IPressable pressable) {
        this(x, y, width, height, title, description, pressable, EMPTY_TOOLTIP);
    }

    public DescriptiveButton(
            int x,
            int y,
            int width,
            int height,
            IFormattableTextComponent title,
            IFormattableTextComponent description,
            IPressable pressable,
            ITooltip tooltip) {
        super(x, y, width, height, title, pressable, tooltip);
        this.description = description;
    }

    @Override
    public IFormattableTextComponent getTitle() {
        return (IFormattableTextComponent) this.getMessage();
    }

    @Override
    public List<IFormattableTextComponent> getDescription() {
        return Collections.singletonList(description);
    }
}
