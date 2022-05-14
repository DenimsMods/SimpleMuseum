package denimred.simplemuseum.client.gui.screen.behavior;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.phys.Vec3;

import denimred.simplemuseum.client.gui.widget.BetterButton;
import denimred.simplemuseum.client.gui.widget.BetterTextFieldWidget;
import denimred.simplemuseum.client.gui.widget.LabelWidget;
import denimred.simplemuseum.client.gui.widget.NestedWidget;
import denimred.simplemuseum.client.gui.widget.WidgetList;
import denimred.simplemuseum.common.entity.puppet.goals.movement.Movement;
import denimred.simplemuseum.common.entity.puppet.goals.movement.Point;

public class PuppetMovementEditorScreen extends Screen {

    private Movement movement;
    private WidgetList<PuppetMovementEditorScreen> positionList;

    public PuppetMovementEditorScreen(Movement movement) {
        super(new TextComponent("Movement Editor"));
        this.movement = movement;
    }

    @Override
    protected void init() {
        //ToDo: Give buttons actual actions :^)
        addButton(new BetterButton(2, height - 22, 100, 20, new TextComponent("Cancel"), Button::onPress));
        addButton(new BetterButton(width - 102, height - 22, 100, 20, new TextComponent("Confirm"), Button::onPress));
        addButton(new LabelWidget(width / 4, 2, font, LabelWidget.AnchorX.CENTER, LabelWidget.AnchorY.TOP, FormattedText.of("Positions")));
        positionList = addWidget(new WidgetList<>(this, 0, 12, width / 2, height - 36));
        for(Point point : movement.getMovementPoints()) {
            positionList.add(new PositionWidget(0, 0, 0, 30, point.pos));
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.fillGradient(poseStack, width / 2, 0, width, this.height - 24, -1072689136, -804253680);
        this.fillGradient(poseStack, 0, height - 24, width, this.height, -804253680, -804253680);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        positionList.render(poseStack, mouseX, mouseY, partialTicks);
    }

    private class PositionWidget extends NestedWidget {

        public BetterTextFieldWidget posXEntry;
        public BetterTextFieldWidget posYEntry;
        public BetterTextFieldWidget posZEntry;

        public PositionWidget(int x, int y, int width, int height, Vec3 pos) {
            super(x, y, width, height, new TextComponent("Pos"));
            //ToDo: Make float entry or make FloatWidget entry public
            addChild(new LabelWidget(0, 0, font, LabelWidget.AnchorX.LEFT, LabelWidget.AnchorY.TOP, FormattedText.of("PosX")));
            posXEntry = addChild(new BetterTextFieldWidget(font, 0, 0, 50, 20, new TextComponent("PosX")));
            posXEntry.setValue(""+pos.x);
            addChild(new LabelWidget(0, 0, font, LabelWidget.AnchorX.LEFT, LabelWidget.AnchorY.TOP, FormattedText.of("PosY")));
            posYEntry = addChild(new BetterTextFieldWidget(font, 0, 0, 50, 20, new TextComponent("PosY")));
            posYEntry.setValue(""+pos.y);
            addChild(new LabelWidget(0, 0, font, LabelWidget.AnchorX.LEFT, LabelWidget.AnchorY.TOP, FormattedText.of("PosZ")));
            posZEntry = addChild(new BetterTextFieldWidget(font, 0, 0, 50, 20, new TextComponent("PosZ")));
            posZEntry.setValue(""+pos.z);
        }

        @Override
        protected void recalculateChildren() {
            int childMargin = 3;
            int xPos = x;
            for(AbstractWidget child : children) {
                child.x = xPos;
                child.y = y + ((height / 2) - (child.getHeight() / 2));
                xPos += child.getWidth() + childMargin;
            }
        }
    }

}
