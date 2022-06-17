package denimred.simplemuseum.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.client.gui.widget.BetterButton;
import denimred.simplemuseum.client.gui.widget.BetterTextFieldWidget;
import denimred.simplemuseum.client.gui.widget.LabelWidget;
import denimred.simplemuseum.client.gui.widget.NestedWidget;
import denimred.simplemuseum.client.gui.widget.WidgetList;
import denimred.simplemuseum.client.renderer.entity.PuppetRenderer;
import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;

public class ItemManagementScreen extends AbstractContainerScreen<ChestMenu> {

    private static final ResourceLocation BACKGROUND = new ResourceLocation(SimpleMuseum.MOD_ID, "textures/gui/only_inventory.png");

    @Nullable protected final Screen parent;
    private PuppetEntity puppet;
    @Nullable private BoneWidget selected = null;

    private WidgetList<ItemManagementScreen> boneWidgets;
    private WidgetList<ItemManagementScreen> nbtWidgets;
    private Checkbox copyData;
    private Checkbox copySize;

    private BetterTextFieldWidget stackSizeEntry;

    public ItemManagementScreen(PuppetEntity entity, @Nullable Screen parentScreen) {
        super(new ChestMenu(MenuType.GENERIC_9x1, 0, Minecraft.getInstance().player.inventory, new Container() {
            @Override
            public void clearContent() {}
            @Override
            public int getContainerSize() {
                return 0;
            }
            @Override
            public boolean isEmpty() {
                return true;
            }
            @Override
            public ItemStack getItem(int index) {
                return null;
            }
            @Override
            public ItemStack removeItem(int index, int count) {
                return null;
            }
            @Override
            public ItemStack removeItemNoUpdate(int index) {
                return null;
            }
            @Override
            public void setItem(int index, ItemStack stack) {}
            @Override
            public void setChanged() {}
            @Override
            public boolean stillValid(Player player) {
                return false;
            }
        }, 0), Minecraft.getInstance().player.inventory, new TextComponent("Item Management"));

        puppet = entity;
        parent = parentScreen;

        imageWidth = 176;
        imageHeight = 100;
    }

    @Override
    protected void init() {
        buttons.clear();
        selected = null;
        leftPos = width / 2;
        topPos = height - imageHeight - 15;

        boneWidgets = addButton(new WidgetList<>(this, 0, 0, width / 2, height));

        nbtWidgets = addButton(new WidgetList<>(this, leftPos, 60, width / 2, height - imageHeight - 60));
        nbtWidgets.visible = false;

        stackSizeEntry = addButton(new BetterTextFieldWidget(font, leftPos + 60, 20, 20, 20, new TextComponent("Stack Size")));
        stackSizeEntry.visible = false;

        boolean check = copyData != null && copyData.selected();
        copyData = addButton(new Checkbox(leftPos + imageWidth, topPos + 28, 150, 20, new TextComponent("NBT"), check));
        copyData.visible = false;

        check = copySize != null && copySize.selected();
        copySize = addButton(new Checkbox(leftPos + imageWidth, topPos + 50, 150, 20, new TextComponent("Stack Size"), check));
        copySize.visible = false;

        populateBoneList();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        if (selected != null) {
            fillGradient(poseStack, width / 2, 0, width, height, 0x66000000, 0xCC000000);
            super.render(poseStack, mouseX, mouseY, partialTicks);
            drawString(poseStack, font, "Copy Settings", leftPos + imageWidth + 1, topPos + 17, 0xFFFFFF);
            if(selected.heldItem != null) {
                drawString(poseStack, font, "Stack Size", leftPos + 82, 26, 0xFFFFFF);
                ItemStack stack = selected.heldItem;
                RenderSystem.pushMatrix();
                RenderSystem.translatef(width / 2f + 5, 5, 0);
                RenderSystem.scalef(3f, 3f, 3f);

                itemRenderer.renderAndDecorateItem(stack, 0, 0);

                //Trouble getting this to render in front of item.
                //itemRenderer.renderGuiItemDecorations(font, stack, 0, 0, null);

                RenderSystem.popMatrix();
                renderWrappedToolTip(poseStack, Collections.singletonList(stack.getDisplayName()), width / 2 + 50, 18, font);
            }
            renderTooltip(poseStack, mouseX, mouseY);
        } else {
            for (int i = 0; i < this.buttons.size(); ++i) {
                this.buttons.get(i).render(poseStack, mouseX, mouseY, partialTicks);
            }
        }
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
        if(selected != null) {
            this.minecraft.getTextureManager().bind(BACKGROUND);
            this.blit(poseStack, leftPos, topPos + 13, 0, 0, imageWidth, imageHeight);
        }
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
        if(selected != null && slot != null && !slot.getItem().isEmpty()) {
            ItemStack itemStack = copyData.selected() ? slot.getItem().copy() : new ItemStack(slot.getItem().getItem());
            itemStack.setCount(copySize.selected() ? slot.getItem().getCount() : 1);

            populateNbtList(itemStack);

            boolean flag = itemStack != null && !itemStack.isEmpty();
            if(flag)
                stackSizeEntry.setValue(""+itemStack.getCount());
            stackSizeEntry.visible = flag;

            selected.heldItem = itemStack;
            selected.displayText = Arrays.asList(itemStack.getDisplayName(), itemStack.serializeNBT().getPrettyDisplay());

            puppet.setHeldItem(selected.boneName, itemStack);
            super.slotClicked(slot, slotId, mouseButton, type);
        }
    }

    public void setSelected(BoneWidget widget) {
        selected = widget;
        populateNbtList(widget.heldItem);
        stackSizeEntry.visible = widget.heldItem != null && !widget.heldItem.isEmpty();
        nbtWidgets.visible = true;
        copyData.visible = true;
        copySize.visible = true;
    }

    private void populateBoneList() {
        GeoModel model = ((PuppetRenderer) minecraft.getEntityRenderDispatcher().getRenderer(puppet)).getGeoModelProvider().getModel(puppet.sourceManager.model.get());
        for(GeoBone bone : model.topLevelBones) {
            addBoneAndChildren(bone, "");
        }
    }

    private void populateNbtList(ItemStack itemStack) {
        nbtWidgets.clear();
        if(itemStack == null || itemStack.isEmpty())
            return;
        CompoundTag nbt = itemStack.serializeNBT();
        if(nbt.contains("tag")) {
            CompoundTag data = nbt.getCompound("tag");
            for (String s : data.getAllKeys()) {
                Tag tag = data.get(s);
                nbtWidgets.add(getNBTWidget(s, tag));
            }
        }
    }

    private NBTWidget getNBTWidget(String key, Tag tag) {
        NBTWidget widget;
        if(tag instanceof CompoundTag) {
            widget = new NBTCollectionWidget(NBTCollectionType.COMPOUND, key);
            for(String childKey : ((CompoundTag) tag).getAllKeys()) {
                ((NBTCollectionWidget)widget).addNBTChild(getNBTWidget(childKey, ((CompoundTag) tag).get(childKey)));
            }
        }
        else if (tag instanceof ListTag) {
            widget = new NBTCollectionWidget(NBTCollectionType.LIST, key);
            int i = 0;
            for(Tag child : ((ListTag) tag)) {
                ((NBTCollectionWidget)widget).addNBTChild(getNBTWidget(""+i++, child));
            }
        }
        else {
            widget = new NBTEntryWidget(key, tag.getAsString());
        }
        return widget;
    }

    private void addBoneAndChildren(GeoBone bone, String path) {
        BoneWidget widget = new BoneWidget(0, 0, width, 30, new TextComponent(path + bone.name), this, bone.name, puppet.getHeldItem(bone.name));
        if(widget.heldItem != null)
            widget.displayText = Arrays.asList(widget.heldItem.getDisplayName(), widget.heldItem.serializeNBT().getPrettyDisplay());
        boneWidgets.add(widget);
        for(GeoBone child : bone.childBones) {
            addBoneAndChildren(child, path + bone.name + "/");
        }
    }

    private class BoneWidget extends NestedWidget {
        public String boneName;
        @Nullable public ItemStack heldItem;
        public List<FormattedText> displayText = new ArrayList<>();

        private ItemManagementScreen parent;
        private LabelWidget label;

        public BoneWidget(int x, int y, int width, int height, Component title, ItemManagementScreen parentScreen, String bone, @Nullable ItemStack itemStack) {
            super(x, y, width, height, title);
            parent = parentScreen;
            label = addChild(new LabelWidget(0, 0, font, LabelWidget.AnchorX.LEFT, LabelWidget.AnchorY.CENTER, FormattedText.of(title.getString())));
            boneName = bone;
            heldItem = itemStack;
        }

        @Override
        public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
            if(isSelected())
                fillGradient(poseStack, x, y, x + width, y + height, 0xFF1d81bf, 0xFF176da3);
            super.renderButton(poseStack, mouseX, mouseY, partialTicks);
        }

        @Override
        protected void recalculateChildren() {
            label.y = y + (height / 2);
            label.x = x + 2;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
            boolean flag = (this.active && this.visible && mouseX >= (double)this.x && mouseY >= (double)this.y && mouseX < (double)(this.x + this.width) && mouseY < (double)(this.y + this.height)) && isValidClickButton(mouseButton);
            if(flag)
                this.onClick(mouseX, mouseY);
            return flag;
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            if(!isSelected())
                parent.setSelected(this);
        }

        public boolean isSelected() {
            return parent.selected == this;
        }
    }

    private abstract class NBTWidget extends NestedWidget {
        public NBTWidget(int x, int y, int width, int height, Component title) {
            super(x, y, width, height, title);
        }

        @Override
        public void recalculateChildren() {
            super.recalculateChildren();
        }

        public abstract BetterTextFieldWidget getTagEntry();
    }

    private class NBTEntryWidget extends NBTWidget {
        BetterTextFieldWidget tagEntry;
        BetterTextFieldWidget dataEntry;
        BetterButton deleteButton;

        public NBTEntryWidget(String tag, String data) {
            super(0, 0, 0, 20, TextComponent.EMPTY);
            if(tag != null && !tag.isEmpty()) {
                tagEntry = addChild(new BetterTextFieldWidget(font, 0, 0, 100, 20, new TextComponent("Tag Entry")));
                tagEntry.setValue(tag);
            }

            dataEntry = addChild(new BetterTextFieldWidget(font, 0, 0, 150, 20, new TextComponent("Data Entry")));
            dataEntry.setMaxLength(64);
            dataEntry.setValue(data);

            deleteButton = addChild(new BetterButton(0, 0, 20, 20, new TextComponent("X"), btn -> deleteEntry()));
        }

        @Override
        public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
            if(tagEntry != null)
                tagEntry.renderButton(poseStack, mouseX, mouseY, partialTicks);
            dataEntry.renderButton(poseStack, mouseX, mouseY, partialTicks);
            deleteButton.renderButton(poseStack, mouseX, mouseY, partialTicks);
            super.renderButton(poseStack, mouseX, mouseY, partialTicks);
        }

        @Override
        public void recalculateChildren() {
            deleteButton.x = (x + width) - 20;
            deleteButton.y = y;
            dataEntry.y = y;
            if(tagEntry != null) {
                tagEntry.x = x + 20;
                tagEntry.y = y;
                dataEntry.x = x + 122;
                dataEntry.setWidth(width - 147);
            }
            else {
                dataEntry.x = x + 20;
                dataEntry.setWidth(width - 45);
            }
        }

        @Override
        public BetterTextFieldWidget getTagEntry() {
            return tagEntry;
        }

        private void deleteEntry() {

        }
    }

    private class NBTCollectionWidget extends NBTWidget {
        public int layer = 0;
        NBTCollectionType type;
        BetterTextFieldWidget tagEntry;
        BetterButton deleteButton;
        private List<NBTWidget> nbtChildren = new ArrayList<>();

        public NBTCollectionWidget(NBTCollectionType collectionType, String tag) {
            super(0, 0, 0, 30, new TextComponent(tag));
            this.type = collectionType;
            tagEntry = addChild(new BetterTextFieldWidget(font, 0, 5, 100, 20, new TextComponent("Tag Entry")));
            tagEntry.setValue(tag);
            deleteButton = addChild(new BetterButton(0, 5, 20, 20, new TextComponent("X"), btn -> deleteEntry()));
        }

        public void addNBTChild(NBTWidget child) {
            if(type == NBTCollectionType.LIST)
                child.getTagEntry().setEditable(false);
            nbtChildren.add(addChild(child));
            setHeight(30 + nbtChildren.size() * 20);
        }

        @Override
        public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
            int[] colours = {
                    0x66FF0000,
                    0x6600FF00,
                    0x660000FF,
            };
            int lineLeft = x + ((layer+1) * 2);

            if(type == NBTCollectionType.COMPOUND) {
                hLine(poseStack, lineLeft, x + 20, y + 10, colours[layer % 3]);
                NBTWidget lastChild = nbtChildren.get(nbtChildren.size() - 1);
                vLine(poseStack, lineLeft, y + 10, lastChild.y + 10, colours[layer % 3]);
                for(NBTWidget child : nbtChildren)
                    hLine(poseStack, lineLeft, x + 20, child.y + 10, colours[layer % 3]);
            }
            else {
                hLine(poseStack, lineLeft, x + 20, y + 19, colours[layer % 3]);
                vLine(poseStack, lineLeft, y + 19, y + height - 1, colours[layer % 3]);
                hLine(poseStack, lineLeft, x + 20, y + height - 1, colours[layer % 3]);
            }


            if(tagEntry != null)
                tagEntry.renderButton(poseStack, mouseX, mouseY, partialTicks);
            deleteButton.renderButton(poseStack, mouseX, mouseY, partialTicks);
            super.renderButton(poseStack, mouseX, mouseY, partialTicks);
        }

        @Override
        public void recalculateChildren() {
            tagEntry.x = x + 10;
            tagEntry.y = y;
            deleteButton.x = (x + width) - 20;
            deleteButton.y = y;

            int h = 20;
            NBTWidget prevChild = null;
            for(int i = 0; i < nbtChildren.size(); i++) {
                NBTWidget child = nbtChildren.get(i);
                if(child instanceof NBTCollectionWidget)
                    ((NBTCollectionWidget) child).layer = layer + 1;
                child.x = x;
                if(prevChild != null)
                    child.y = prevChild.y + prevChild.getHeight();
                else
                    child.y = y + 20 + (i * 20);
                child.setWidth(width);
                h += child.getHeight();
                prevChild = child;
            }
            height = h;
        }

        private void deleteEntry() {

        }

        @Override
        public BetterTextFieldWidget getTagEntry() {
            return tagEntry;
        }
    }

    private enum NBTCollectionType {
        COMPOUND, LIST
    }

}
