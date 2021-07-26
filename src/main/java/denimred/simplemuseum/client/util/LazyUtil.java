package denimred.simplemuseum.client.util;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import net.minecraft.nbt.ByteArrayNBT;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.EndNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.INBTType;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.LongArrayNBT;
import net.minecraft.nbt.LongNBT;
import net.minecraft.nbt.ShortNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.Util;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.Constants.NBT;

import java.util.LinkedList;
import java.util.Map;

import denimred.simplemuseum.client.gui.screen.test.PuppetConfigScreen;
import denimred.simplemuseum.client.gui.widget.LabelWidget;
import denimred.simplemuseum.client.gui.widget.value.AnimationWidget;
import denimred.simplemuseum.client.gui.widget.value.BoolWidget;
import denimred.simplemuseum.client.gui.widget.value.CustomNameWidget;
import denimred.simplemuseum.client.gui.widget.value.EntitySizeWidget;
import denimred.simplemuseum.client.gui.widget.value.FloatWidget;
import denimred.simplemuseum.client.gui.widget.value.IntWidget;
import denimred.simplemuseum.client.gui.widget.value.NameplateBehaviorWidget;
import denimred.simplemuseum.client.gui.widget.value.SoundCategoryWidget;
import denimred.simplemuseum.client.gui.widget.value.SoundWidget;
import denimred.simplemuseum.client.gui.widget.value.SourceWidget;
import denimred.simplemuseum.client.gui.widget.value.ValueWidget;
import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import denimred.simplemuseum.common.entity.puppet.manager.PuppetAnimationManager;
import denimred.simplemuseum.common.entity.puppet.manager.PuppetAudioManager;
import denimred.simplemuseum.common.entity.puppet.manager.PuppetBehaviorManager;
import denimred.simplemuseum.common.entity.puppet.manager.PuppetRenderManager;
import denimred.simplemuseum.common.entity.puppet.manager.PuppetSourceManager;
import denimred.simplemuseum.common.entity.puppet.manager.PuppetValueManager;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValue;
import denimred.simplemuseum.common.entity.puppet.manager.value.PuppetValueProvider;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;

public class LazyUtil {
    private static final Map<Integer, INBTType<?>> NBT_TYPES =
            Util.make(
                    new Int2ReferenceOpenHashMap<>(),
                    map -> {
                        map.put(NBT.TAG_END, EndNBT.TYPE);
                        map.put(NBT.TAG_BYTE, ByteNBT.TYPE);
                        map.put(NBT.TAG_SHORT, ShortNBT.TYPE);
                        map.put(NBT.TAG_INT, IntNBT.TYPE);
                        map.put(NBT.TAG_LONG, LongNBT.TYPE);
                        map.put(NBT.TAG_FLOAT, FloatNBT.TYPE);
                        map.put(NBT.TAG_DOUBLE, DoubleNBT.TYPE);
                        map.put(NBT.TAG_BYTE_ARRAY, ByteArrayNBT.TYPE);
                        map.put(NBT.TAG_STRING, StringNBT.TYPE);
                        map.put(NBT.TAG_LIST, ListNBT.TYPE);
                        map.put(NBT.TAG_COMPOUND, CompoundNBT.TYPE);
                        map.put(NBT.TAG_INT_ARRAY, IntArrayNBT.TYPE);
                        map.put(NBT.TAG_LONG_ARRAY, LongArrayNBT.TYPE);
                    });
    private static final Map<PuppetValueProvider<?, ?>, ValueWidgetFactory> VALUE_WIDGET_FACTORIES =
            Util.make(
                    new Reference2ReferenceOpenHashMap<>(),
                    map -> {
                        map.put(PuppetSourceManager.MODEL, SourceWidget::new);
                        map.put(PuppetSourceManager.TEXTURE, SourceWidget::new);
                        map.put(PuppetSourceManager.ANIMATIONS, SourceWidget::new);
                        map.put(PuppetAnimationManager.IDLE, AnimationWidget::new);
                        map.put(PuppetAnimationManager.MOVING, AnimationWidget::new);
                        map.put(PuppetAnimationManager.IDLE_SNEAK, AnimationWidget::new);
                        map.put(PuppetAnimationManager.MOVING_SNEAK, AnimationWidget::new);
                        map.put(PuppetAnimationManager.SPRINTING, AnimationWidget::new);
                        map.put(PuppetAnimationManager.SITTING, AnimationWidget::new);
                        map.put(PuppetAnimationManager.DEATH, AnimationWidget::new);
                        map.put(PuppetAnimationManager.DEATH_LENGTH, IntWidget::new);
                        map.put(PuppetAudioManager.AMBIENT, SoundWidget::new);
                        map.put(PuppetAudioManager.CATEGORY, SoundCategoryWidget::new);
                        map.put(PuppetRenderManager.SCALE, FloatWidget::new);
                        map.put(
                                PuppetRenderManager.NAMEPLATE_BEHAVIOR,
                                NameplateBehaviorWidget::new);
                        map.put(PuppetRenderManager.FLAMING, BoolWidget::new);
                        map.put(PuppetRenderManager.IGNORE_LIGHTING, BoolWidget::new);
                        map.put(PuppetBehaviorManager.CUSTOM_NAME, CustomNameWidget::new);
                        map.put(PuppetBehaviorManager.PHYSICAL_SIZE, EntitySizeWidget::new);
                    });

    public static String getNbtTagName(int id) {
        return NBT_TYPES.getOrDefault(id, INBTType.getEndNBT(id)).getTagName();
    }

    public static Multimap<PuppetValueManager, ValueWidget<?, ?>> makeValueWidgets(
            PuppetConfigScreen screen, PuppetEntity source) {
        final Multimap<PuppetValueManager, ValueWidget<?, ?>> map =
                Multimaps.newListMultimap(
                        new Reference2ReferenceLinkedOpenHashMap<>(), LinkedList::new);
        for (PuppetValueManager manager : source.getManagers()) {
            for (PuppetValue<?, ?> value : manager.getValues()) {
                final ValueWidgetFactory factory = VALUE_WIDGET_FACTORIES.get(value.provider);
                if (factory != null) {
                    map.put(manager, factory.create(screen, value));
                } else {
                    map.put(manager, new MissingValueWidget<>(screen, value));
                }
            }
        }
        return map;
    }

    private interface ValueWidgetFactory {
        ValueWidget<?, ?> create(PuppetConfigScreen parent, PuppetValue<?, ?> value);
    }

    private static class MissingValueWidget<T> extends ValueWidget<T, PuppetValue<T, ?>> {
        private final LabelWidget label;

        public MissingValueWidget(PuppetConfigScreen parent, PuppetValue<T, ?> value) {
            super(parent, 0, 0, 0, 32, value);
            final IFormattableTextComponent text =
                    new StringTextComponent("Missing widget for " + value.provider.key.toString())
                            .mergeStyle(TextFormatting.RED);
            label =
                    this.addChild(
                            new LabelWidget(
                                    0,
                                    0,
                                    ClientUtil.MC.fontRenderer,
                                    LabelWidget.AnchorX.CENTER,
                                    LabelWidget.AnchorY.CENTER,
                                    text));
        }

        @Override
        protected void recalculateChildren() {
            label.x = x + width / 2;
            label.y = y + height / 2;
        }
    }
}
