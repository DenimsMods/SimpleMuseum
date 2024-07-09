package dev.denimred.simplemuseum.puppet.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;

import static net.minecraft.util.FastColor.ARGB32.*;

public final class PuppetGuiUtils {
    public static void vLineGradient(GuiGraphics guiGraphics, int x, int minY, int maxY, int colorFrom, int colorTo) {
        if (maxY < minY) {
            int i = minY;
            minY = maxY;
            maxY = i;
        }
        guiGraphics.fillGradient(x, minY + 1, x + 1, maxY, colorFrom, colorTo);
    }

    public static void hLineGradient(GuiGraphics guiGraphics, int minX, int maxX, int y, int colorFrom, int colorTo) {
        if (maxX < minX) {
            int i = minX;
            minX = maxX;
            maxX = i;
        }
        fillHGradient(guiGraphics, minX, y, maxX + 1, y + 1, colorFrom, colorTo);
    }

    @SuppressWarnings("deprecation")
    public static void fillHGradient(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int colorFrom, int colorTo) {
        var consumer = guiGraphics.bufferSource().getBuffer(RenderType.gui());
        var a1 = (float) alpha(colorFrom) / 255.0F;
        var r1 = (float) red(colorFrom) / 255.0F;
        var g1 = (float) green(colorFrom) / 255.0F;
        var b1 = (float) blue(colorFrom) / 255.0F;

        var a2 = (float) alpha(colorTo) / 255.0F;
        var r2 = (float) red(colorTo) / 255.0F;
        var g2 = (float) green(colorTo) / 255.0F;
        var b2 = (float) blue(colorTo) / 255.0F;

        var m4f = guiGraphics.pose().last().pose();
        consumer.vertex(m4f, (float) x1, (float) y1, 0).color(r1, g1, b1, a1).endVertex();
        consumer.vertex(m4f, (float) x1, (float) y2, 0).color(r1, g1, b1, a1).endVertex();
        consumer.vertex(m4f, (float) x2, (float) y2, 0).color(r2, g2, b2, a2).endVertex();
        consumer.vertex(m4f, (float) x2, (float) y1, 0).color(r2, g2, b2, a2).endVertex();

        guiGraphics.flushIfUnmanaged();
    }
}
