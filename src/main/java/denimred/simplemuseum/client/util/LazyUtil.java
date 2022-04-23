package denimred.simplemuseum.client.util;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.TagType;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.common.util.Constants.NBT;

import java.util.LinkedList;
import java.util.Map;

import denimred.simplemuseum.client.gui.screen.PuppetConfigScreen;
import denimred.simplemuseum.client.gui.widget.LabelWidget;
import denimred.simplemuseum.client.gui.widget.value.AnimationWidget;
import denimred.simplemuseum.client.gui.widget.value.BoolWidget;
import denimred.simplemuseum.client.gui.widget.value.CustomNameWidget;
import denimred.simplemuseum.client.gui.widget.value.EntitySizeWidget;
import denimred.simplemuseum.client.gui.widget.value.FloatWidget;
import denimred.simplemuseum.client.gui.widget.value.IntWidget;
import denimred.simplemuseum.client.gui.widget.value.NameplateBehaviorWidget;
import denimred.simplemuseum.client.gui.widget.value.PuppetGoalTreeWidget;
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
    private static final Map<Integer, TagType<?>> NBT_TYPES =
            Util.make(
                    new Int2ReferenceOpenHashMap<>(),
                    map -> {
                        map.put(NBT.TAG_END, EndTag.TYPE);
                        map.put(NBT.TAG_BYTE, ByteTag.TYPE);
                        map.put(NBT.TAG_SHORT, ShortTag.TYPE);
                        map.put(NBT.TAG_INT, IntTag.TYPE);
                        map.put(NBT.TAG_LONG, LongTag.TYPE);
                        map.put(NBT.TAG_FLOAT, FloatTag.TYPE);
                        map.put(NBT.TAG_DOUBLE, DoubleTag.TYPE);
                        map.put(NBT.TAG_BYTE_ARRAY, ByteArrayTag.TYPE);
                        map.put(NBT.TAG_STRING, StringTag.TYPE);
                        map.put(NBT.TAG_LIST, ListTag.TYPE);
                        map.put(NBT.TAG_COMPOUND, CompoundTag.TYPE);
                        map.put(NBT.TAG_INT_ARRAY, IntArrayTag.TYPE);
                        map.put(NBT.TAG_LONG_ARRAY, LongArrayTag.TYPE);
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
                        map.put(PuppetBehaviorManager.GOAL_TREE, PuppetGoalTreeWidget::new);
                    });

    public static String getNbtTagName(int id) {
        return NBT_TYPES.getOrDefault(id, TagType.createInvalid(id)).getPrettyName();
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
            final MutableComponent text =
                    new TextComponent("Missing widget for " + value.provider.key.toString())
                            .withStyle(ChatFormatting.RED);
            label =
                    this.addChild(
                            new LabelWidget(
                                    0,
                                    0,
                                    ClientUtil.MC.font,
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
