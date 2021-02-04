package denimred.simplemuseum.client.util;

import java.util.Optional;

public class NumberUtil {
    public static String parseString(double d) {
        String s = Double.toString(d);
        if (!s.isEmpty() && s.charAt(0) != '-') {
            s = "+".concat(s);
        }
        return s;
    }

    public static String parseString(float d) {
        String s = Float.toString(d);
        if (!s.isEmpty() && s.charAt(0) != '-') {
            s = "+".concat(s);
        }
        return s;
    }

    public static boolean isValidDouble(String s) {
        if (s.isEmpty() || s.equals("+") || s.equals("-")) {
            return true;
        }
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidFloat(String s) {
        if (s.isEmpty() || s.equals("+") || s.equals("-")) {
            return true;
        }
        try {
            Float.parseFloat(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static Optional<Double> parseDouble(String s) {
        try {
            return Optional.of(Double.parseDouble(s));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static Optional<Float> parseFloat(String s) {
        try {
            return Optional.of(Float.parseFloat(s));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
