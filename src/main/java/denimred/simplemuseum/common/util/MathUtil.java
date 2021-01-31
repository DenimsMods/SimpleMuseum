package denimred.simplemuseum.common.util;

import net.minecraft.util.math.vector.Vector3d;

public final class MathUtil {
    public static float angleBetween(Vector3d a, Vector3d b) {
        return (float) Math.toDegrees(Math.atan2(b.z - a.z, b.x - a.x) - Math.PI / 2.0D);
    }
}
