package denimred.simplemuseum.client.gui.widget;

import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Consumer;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.client.gui.screen.PuppetConfigScreen;
import denimred.simplemuseum.common.entity.puppet.manager.PuppetSourceManager;
import denimred.simplemuseum.common.entity.puppet.manager.PuppetValueManager;
import denimred.simplemuseum.common.i18n.Descriptive;

public final class ManagerTabs extends NestedWidget {
    public static final ResourceLocation BUTTON_TEXTURE =
            new ResourceLocation(SimpleMuseum.MOD_ID, "textures/gui/manager_tabs.png");
    private static String lastManager = PuppetSourceManager.NBT_KEY;
    private final Consumer<PuppetValueManager> callback;
    private String selected = lastManager;

    public ManagerTabs(
            PuppetConfigScreen parent,
            int x,
            int y,
            List<PuppetValueManager> managers,
            Consumer<PuppetValueManager> callback) {
        super(x, y, 20 * managers.size(), 20, TextComponent.EMPTY);
        this.callback = callback.andThen(m -> lastManager = m.nbtKey);
        final int count = managers.size();
        for (int i = 0; i < count; i++) {
            this.addChild(
                    new TabButton(x + 20 * i, y, i, managers.get(i), parent::renderWidgetTooltip));
        }
        for (PuppetValueManager manager : managers) {
            if (manager.nbtKey.equals(selected)) {
                callback.accept(manager);
            }
        }
    }

    public class TabButton extends IconButton implements Descriptive {
        private final PuppetValueManager manager;

        public TabButton(int x, int y, int index, PuppetValueManager manager, OnTooltip tooltip) {
            super(
                    x,
                    y,
                    20,
                    20,
                    BUTTON_TEXTURE,
                    index * 20,
                    0,
                    64,
                    128,
                    20,
                    b -> {
                        if (!selected.equals(manager.nbtKey)) {
                            selected = manager.nbtKey;
                            callback.accept(manager);
                        }
                    },
                    tooltip,
                    manager.getTitle());
            this.manager = manager;
        }

        @Override
        public void playDownSound(SoundManager manager) {
            if (!selected.equals(this.manager.nbtKey)) {
                super.playDownSound(manager);
            }
        }

        @Override
        protected int getYImage(boolean isHovered) {
            if (selected.equals(manager.nbtKey)) {
                return 2;
            } else if (isHovered) {
                return 1;
            }
            return 0;
        }

        @Override
        public MutableComponent getTitle() {
            return manager.getTitle();
        }

        @Override
        public List<MutableComponent> getDescription() {
            return manager.getDescription();
        }
    }
}
