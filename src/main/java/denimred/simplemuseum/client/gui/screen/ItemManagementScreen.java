package denimred.simplemuseum.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.nbt.TagTypes;
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
import net.minecraftforge.common.util.Constants;

import org.lwjgl.system.CallbackI;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.client.gui.widget.BetterButton;
import denimred.simplemuseum.client.gui.widget.BetterTextFieldWidget;
import denimred.simplemuseum.client.gui.widget.FloatFieldWidget;
import denimred.simplemuseum.client.gui.widget.IntFieldWidget;
import denimred.simplemuseum.client.gui.widget.LabelWidget;
import denimred.simplemuseum.client.gui.widget.NestedWidget;
import denimred.simplemuseum.client.gui.widget.WidgetList;
import denimred.simplemuseum.client.renderer.entity.PuppetRenderer;
import denimred.simplemuseum.client.util.LazyUtil;
import denimred.simplemuseum.common.entity.puppet.PuppetEntity;
import denimred.simplemuseum.common.item.HeldItemStack;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;

public class ItemManagementScreen extends AbstractContainerScreen<ChestMenu> {

    private static final ResourceLocation BACKGROUND = new ResourceLocation(SimpleMuseum.MOD_ID, "textures/gui/only_inventory.png");

    @Nullable protected final Screen parent;
    private PuppetEntity puppet;
    @Nullable private BoneWidget selected = null;

    private WidgetList<ItemManagementScreen> boneWidgets;
    private WidgetList<ItemManagementScreen> nbtWidgets;

    private IntFieldWidget stackSizeEntry;
    private FloatFieldWidget scaleEntry;
    private BetterCheckbox armorDisplay;

    private BetterButton saveBtn;
    private BetterButton cancelBtn;
    private BetterButton clearBtn;
    private Checkbox copyData;
    private Checkbox copySize;

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

        nbtWidgets = addButton(new WidgetList<>(this, leftPos, 60, width / 2, height - imageHeight - 61));
        nbtWidgets.visible = false;

        stackSizeEntry = addButton(new IntFieldWidget(font, leftPos + 60, 18, 20, 20, new TextComponent("Stack Size"), 0, 64));
        stackSizeEntry.visible = false;

        scaleEntry = addButton(new FloatFieldWidget(font, leftPos + 140, 18, 25, 20, new TextComponent("Scale"), 0f, 100f));
        scaleEntry.setValue("1.0");
        scaleEntry.visible = false;

        armorDisplay = addButton(new BetterCheckbox(leftPos + 60, 39, new TextComponent("Display As Armor"), false));
        armorDisplay.visible = false;

        saveBtn = addButton(new BetterButton(width - 101, height - 41, 100, 20, new TextComponent("Save"), btn -> saveChanges()));
        cancelBtn = addButton(new BetterButton(width - 101, height - 21, 100, 20, new TextComponent("Close"), btn -> onClose()));
        clearBtn = addButton(new BetterButton(leftPos + imageWidth - 2, topPos + 87, 20, 20, new TextComponent("X").withStyle(ChatFormatting.RED), btn -> clearItem()));

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
                drawString(poseStack, font, "Stack Size", leftPos + 82, 24, 0xFFFFFF);
                drawString(poseStack, font, "Scale", leftPos + 167, 24, 0xFFFFFF);
                ItemStack stack = selected.heldItem.itemStack;

                RenderSystem.pushMatrix();
                RenderSystem.translatef(width / 2f + 5, 5, 0);
                RenderSystem.scalef(3f, 3f, 3f);

                itemRenderer.renderAndDecorateItem(stack, 0, 0);

                //Render Damage Bar, not working yet cus I gotta do something to add the Alpha to the colour int, and me too dumb :^) -Ryan
//                if(stack.isDamaged()) {
//                    RenderSystem.disableDepthTest();
//                    RenderSystem.disableTexture();
//                    RenderSystem.disableAlphaTest();
//                    RenderSystem.disableBlend();
//                    double health = stack.getItem().getDurabilityForDisplay(stack);
//                    int i = Math.round(13.0f - (float) health * 13.0f);
//                    int j = stack.getItem().getRGBDurabilityForDisplay(stack);
//                    fill(poseStack, 0, 16, 13, 18, 0xFF000000);
//                    fill(poseStack, 0, 16, i, 17, j);
//                    RenderSystem.enableBlend();
//                    RenderSystem.enableAlphaTest();
//                    RenderSystem.enableTexture();
//                    RenderSystem.enableDepthTest();
//                }


                RenderSystem.popMatrix();
                renderWrappedToolTip(poseStack, Collections.singletonList(stack.getDisplayName()), width / 2 + 52, 17, font);
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
            //Yeah I know, imageWidth is *technically* smaller than the image, but I don't wanna redo all the positions >:(
            this.blit(poseStack, leftPos, topPos + 13, 0, 0, imageWidth + 24, imageHeight);
        }
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
    }

    private void saveChanges() {

    }

    private void clearItem() {
        selected.heldItem = null;
        setSelected(selected);
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
        if(selected != null && slot != null && !slot.getItem().isEmpty()) {
            ItemStack itemStack = copyData.selected() ? slot.getItem().copy() : new ItemStack(slot.getItem().getItem());
            itemStack.setCount(copySize.selected() ? slot.getItem().getCount() : 1);

            populateNbtList(itemStack);

            boolean flag = itemStack != null && !itemStack.isEmpty();
            if(flag)
                stackSizeEntry.setValue("" + itemStack.getCount());
            stackSizeEntry.visible = flag;
            scaleEntry.visible = flag;
            armorDisplay.visible = flag;

            selected.heldItem = new HeldItemStack(itemStack);
            selected.heldItem.armorDisplay = armorDisplay.selected();

            super.slotClicked(slot, slotId, mouseButton, type);
        }
    }

    public void setSelected(BoneWidget widget) {
        //Save values before swapping
        if(selected != null && selected.heldItem != null) {
            selected.heldItem.scale = scaleEntry.getFloatValue();
            selected.heldItem.itemStack.setCount(stackSizeEntry.getIntValue());
            selected.heldItem.armorDisplay = armorDisplay.selected();
        }

        //Now we swap
        selected = widget;
        if(widget.heldItem != null) {
            populateNbtList(widget.heldItem.itemStack);
            if ((widget.heldItem.armorDisplay && !armorDisplay.selected()) || (!widget.heldItem.armorDisplay && armorDisplay.selected()))
                armorDisplay.onPress();
        } else
            nbtWidgets.clear();

        boolean flag = widget.heldItem != null && !widget.heldItem.itemStack.isEmpty();
        if(flag) {
            nbtWidgets.visible = true;
            stackSizeEntry.setValue("" + widget.heldItem.itemStack.getCount());
            scaleEntry.setValue(""+widget.heldItem.scale);
            armorDisplay.setSelected(widget.heldItem.armorDisplay);
        } else {
            nbtWidgets.visible = false;
            scaleEntry.setValue("1.0");
            armorDisplay.setSelected(false);
        }
        stackSizeEntry.visible = flag;
        scaleEntry.visible = flag;
        armorDisplay.visible = flag;

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
                nbtWidgets.add(getNBTWidget(s, tag, null));
            }
        }
    }

    private NBTWidget getNBTWidget(String key, Tag tag, @Nullable NBTWidget parentWidget) {
        NBTWidget widget;
        if(tag instanceof CompoundTag) {
            widget = new NBTCollectionWidget(NBTCollectionType.COMPOUND, key, parentWidget);
            for(String childKey : ((CompoundTag) tag).getAllKeys()) {
                ((NBTCollectionWidget)widget).addNBTChild(getNBTWidget(childKey, ((CompoundTag) tag).get(childKey), widget));
            }
        }
        else if (tag instanceof ListTag) {
            widget = new NBTCollectionWidget(NBTCollectionType.LIST, key, parentWidget);
            int i = 0;
            for(Tag child : ((ListTag) tag)) {
                ((NBTCollectionWidget)widget).addNBTChild(getNBTWidget(""+i++, child, widget));
            }
        }
        else {
            widget = new NBTEntryWidget(key, tag, parentWidget);
        }
        return widget;
    }

    private CompoundTag serializeNBTList() {
        CompoundTag tag = new CompoundTag();
        for(AbstractWidget w : nbtWidgets.getChildren()) {
            try {
                Tag t = serializeNBTWidgetAndChildren((NBTWidget) w);
            } catch (CommandSyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return tag;
    }

    private Tag serializeNBTWidgetAndChildren(NBTWidget widget) throws CommandSyntaxException {
        Tag tag = null;
        if(widget instanceof NBTCollectionWidget) {

        } else {
            TagParser parser = new TagParser(new StringReader(((NBTEntryWidget)widget).getValue()));
            tag = parser.readValue();
        }
        return tag;
    }

    private void addBoneAndChildren(GeoBone bone, String path) {
        BoneWidget widget = new BoneWidget(0, 0, width, 30, new TextComponent(path + bone.name), this, bone.name, puppet.getHeldItem(bone.name));
        boneWidgets.add(widget);
        for(GeoBone child : bone.childBones) {
            addBoneAndChildren(child, path + bone.name + "/");
        }
    }

    private class BetterCheckbox extends Checkbox {
        public BetterCheckbox(int x, int y, Component arg, boolean bl) {
            super(x, y, 20, 20, arg, bl);
        }

        public void setSelected(boolean selected) {
            if(selected != selected())
                onPress();
        }
    }

    private class BoneWidget extends NestedWidget {
        public String boneName;
        @Nullable public HeldItemStack heldItem;

        private ItemManagementScreen parent;
        private LabelWidget label;

        public BoneWidget(int x, int y, int width, int height, Component title, ItemManagementScreen parentScreen, String bone, @Nullable HeldItemStack heldItemStack) {
            super(x, y, width, height, title);
            parent = parentScreen;
            label = addChild(new LabelWidget(0, 0, font, LabelWidget.AnchorX.LEFT, LabelWidget.AnchorY.CENTER, FormattedText.of(title.getString())));
            boneName = bone;
            heldItem = heldItemStack;
        }

        @Override
        public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
            if(isSelected())
                fillGradient(poseStack, x, y, x + width, y + height, 0xFF1d81bf, 0xFF176da3);
            if(heldItem != null && heldItem.itemStack != null && !heldItem.itemStack.isEmpty())
                itemRenderer.renderAndDecorateItem(heldItem.itemStack, x + 1, y + (height / 2) - 8);
            super.renderButton(poseStack, mouseX, mouseY, partialTicks);
        }

        @Override
        protected void recalculateChildren() {
            label.y = y + (height / 2);
            label.x = x + 21;
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
        @Nullable NBTWidget parent;

        public NBTWidget(int x, int y, int width, int height, Component title, @Nullable NBTWidget parentWidget) {
            super(x, y, width, height, title);
            parent = parentWidget;
        }

        @Override
        public void recalculateChildren() {
            super.recalculateChildren();
        }

        public abstract BetterTextFieldWidget getKeyEntry();
    }

    private class NBTEntryWidget extends NBTWidget {
        BetterTextFieldWidget keyEntry;
        BetterTextFieldWidget dataEntry;
        BetterButton deleteButton;

        public NBTEntryWidget(String key, Tag tag, @Nullable NBTWidget parentWidget) {
            super(0, 0, 0, 20, TextComponent.EMPTY, parentWidget);

            if(key != null && !key.isEmpty()) {
                keyEntry = addChild(new BetterTextFieldWidget(font, 0, 0, 100, 20, new TextComponent("Tag Entry")));
                keyEntry.setValue(key);
            }

            if(tag instanceof IntTag)
                dataEntry = addChild(new IntFieldWidget(font, 0, 0, 150, 20, new TextComponent("Data Entry")));
            else if(tag instanceof FloatTag)
                dataEntry = addChild(new FloatFieldWidget(font, 0, 0, 150, 20, new TextComponent("Data Entry")));
            else
                dataEntry = addChild(new BetterTextFieldWidget(font, 0, 0, 150, 20, new TextComponent("Data Entry")));

            dataEntry.setMaxLength(256);
            dataEntry.setValue(tag.toString());

            deleteButton = addChild(new BetterButton(0, 0, 20, 20, new TextComponent("X"), btn -> deleteEntry()));
        }

        public String getValue() {
            return dataEntry.getValue();
        }

        @Override
        public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
            if(keyEntry != null)
                keyEntry.renderButton(poseStack, mouseX, mouseY, partialTicks);
            dataEntry.renderButton(poseStack, mouseX, mouseY, partialTicks);
            deleteButton.renderButton(poseStack, mouseX, mouseY, partialTicks);
            super.renderButton(poseStack, mouseX, mouseY, partialTicks);
        }

        @Override
        public void recalculateChildren() {
            deleteButton.x = (x + width) - 20;
            deleteButton.y = y;
            dataEntry.y = y;
            if(keyEntry != null) {
                keyEntry.x = x + 20;
                keyEntry.y = y;
                dataEntry.x = x + 122;
                dataEntry.setWidth(width - 147);
            }
            else {
                dataEntry.x = x + 20;
                dataEntry.setWidth(width - 45);
            }
        }

        @Override
        public BetterTextFieldWidget getKeyEntry() {
            return keyEntry;
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

        public NBTCollectionWidget(NBTCollectionType collectionType, String tag, @Nullable NBTWidget parentWidget) {
            super(0, 0, 0, 30, new TextComponent(tag), parentWidget);
            this.type = collectionType;
            tagEntry = addChild(new BetterTextFieldWidget(font, 0, 5, 100, 20, new TextComponent("Tag Entry")));
            tagEntry.setValue(tag);
            deleteButton = addChild(new BetterButton(0, 5, 20, 20, new TextComponent("X"), btn -> deleteEntry()));
        }

        public void addNBTChild(NBTWidget child) {
            if(type == NBTCollectionType.LIST)
                child.getKeyEntry().setEditable(false);
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
        public BetterTextFieldWidget getKeyEntry() {
            return tagEntry;
        }
    }

    private enum NBTCollectionType {
        COMPOUND, LIST
    }

}
