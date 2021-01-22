package denimred.simplemuseum.client.gui.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import denimred.simplemuseum.client.gui.widget.ResourceFieldWidget;
import denimred.simplemuseum.client.util.ClientUtil;

public class SelectResourceScreen extends AbstractSelectObjectScreen<ResourceLocation> {
    protected final ResourceFieldWidget caller;

    protected SelectResourceScreen(
            Screen parent, ITextComponent title, ResourceFieldWidget caller) {
        super(parent, title);
        this.caller = caller;
    }

    @Override
    protected void onSave() {
        if (selected != null) {
            caller.setLocation(selected.value, true);
        }
    }

    @Override
    protected boolean isSelected(ListWidget.Entry entry) {
        return entry.value.equals(caller.getLocation());
    }

    @Override
    protected CompletableFuture<List<ResourceLocation>> getEntriesAsync() {
        final String path = caller.getPathPrefix().replaceFirst("/$", "");
        return ClientUtil.getCachedResourcesAsync(path, caller::validate);
    }
}
