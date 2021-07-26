package denimred.simplemuseum.common.util;

import net.minecraft.nbt.CompoundNBT;

import java.awt.Color;
import java.util.Objects;

import static net.minecraftforge.common.util.Constants.NBT.TAG_BYTE;
import static net.minecraftforge.common.util.Constants.NBT.TAG_INT;

public final class GlowColor {
    public static final GlowColor DEFAULT = new GlowColor(Color.WHITE, true);
    public static final String RGB_NBT = "RGB";
    public static final String USE_TEAM_COLOR_NBT = "UseTeamColor";
    public final int rgb;
    public final boolean useTeamColor;

    public GlowColor(Color color, boolean useTeamColor) {
        this(color.getRGB(), useTeamColor);
    }

    public GlowColor(int rgb, boolean useTeamColor) {
        this.rgb = rgb;
        this.useTeamColor = useTeamColor;
    }

    public static GlowColor deserialize(CompoundNBT tag) {
        final int rgb = tag.contains(RGB_NBT, TAG_INT) ? tag.getInt(RGB_NBT) : DEFAULT.rgb;
        final boolean useTeamColor =
                tag.contains(USE_TEAM_COLOR_NBT, TAG_BYTE)
                        ? tag.getBoolean(USE_TEAM_COLOR_NBT)
                        : DEFAULT.useTeamColor;
        return new GlowColor(rgb, useTeamColor);
    }

    public CompoundNBT serialize() {
        final CompoundNBT tag = new CompoundNBT();
        tag.putInt(RGB_NBT, rgb);
        tag.putBoolean(USE_TEAM_COLOR_NBT, useTeamColor);
        return tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final GlowColor glowColor = (GlowColor) o;
        return rgb == glowColor.rgb && useTeamColor == glowColor.useTeamColor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rgb, useTeamColor);
    }
}
