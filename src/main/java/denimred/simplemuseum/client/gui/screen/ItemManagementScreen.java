package denimred.simplemuseum.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import denimred.simplemuseum.SimpleMuseum;
import denimred.simplemuseum.client.gui.widget.LabelWidget;
import denimred.simplemuseum.client.gui.widget.NestedWidget;
import denimred.simplemuseum.client.gui.widget.WidgetList;
import denimred.simplemuseum.client.renderer.entity.PuppetModel;
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
        boneWidgets = addButton(new WidgetList<>(this, 0, 0, width / 2, height));
        leftPos = ((width / 4) * 3) - (imageWidth / 2);
        topPos = height - imageHeight - 15;
        populateBoneList();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        if (selected != null) {
            fillGradient(poseStack, width / 2, 0, width, height, 0x66000000, 0xCC000000);
            if(selected.heldItem != null) {
                ItemStack stack = selected.heldItem;
                RenderSystem.pushMatrix();
                RenderSystem.translatef(width / 2f, 0, 0);
                RenderSystem.scalef(5f, 5f, 5f);

                itemRenderer.renderAndDecorateItem(stack, 0, 0);

                //Trouble getting this to render in front of item.
                //itemRenderer.renderGuiItemDecorations(font, stack, 0, 0, null);

                RenderSystem.popMatrix();
                renderWrappedToolTip(poseStack, selected.displayText, width / 2 - 10, 95, font);
            }
            super.render(poseStack, mouseX, mouseY, partialTicks);
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
            ItemStack itemStack = slot.getItem();
            selected.heldItem = itemStack;
            selected.displayText = Arrays.asList(itemStack.getDisplayName(), itemStack.serializeNBT().getPrettyDisplay());
            puppet.setHeldItem(selected.boneName, itemStack.copy());
            super.slotClicked(slot, slotId, mouseButton, type);
        }
    }

    public void setSelected(BoneWidget widget) {
        selected = widget;
    }

    private void populateBoneList() {
        GeoModel model = ((PuppetRenderer) minecraft.getEntityRenderDispatcher().getRenderer(puppet)).getGeoModelProvider().getModel(puppet.sourceManager.model.get());
        for(GeoBone bone : model.topLevelBones) {
            addBoneAndChildren(bone, "");
        }
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

}
