package me.sirimperivm.rgbColorsApi;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("all")
public class ColorApi {

    private RgbColorApi rgbColorApi;

    public ColorApi(RgbColorApi rgbColorApi) {
        this.rgbColorApi = rgbColorApi;
    }

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})-&#([A-Fa-f0-9]{6})");
    private static final Pattern RAINBOW_PATTERN = Pattern.compile("&#RAINBOW");

    private static final String[] RAINBOW_COLORS = { "FF0000", "FF7F00", "FFFF00", "00FF00", "0000FF", "4B0082", "9400D3" };

    public static String colorize(String text) {
        if (text == null || text.isEmpty()) return null;

        StringBuilder result = new StringBuilder();
        Matcher gradientMatcher = GRADIENT_PATTERN.matcher(text);
        Matcher hexMatcher = HEX_PATTERN.matcher(text);
        Matcher rainbowMatcher = RAINBOW_PATTERN.matcher(text);

        int lastIndex = 0;
        while (gradientMatcher.find()) {
            result.append(text, lastIndex, gradientMatcher.start());
            String gradientText = getNextSegment(text.substring(gradientMatcher.end()));
            result.append(applyGradient(gradientText, gradientMatcher.group(1), gradientMatcher.group(2)));
            lastIndex = gradientMatcher.end() + gradientText.length() ;
        }

        while (hexMatcher.find()) {
            result.append(text, lastIndex, hexMatcher.start());
            String hexText = getNextSegment(text.substring(hexMatcher.end()));
            result.append(applyHex(hexText, hexMatcher.group(1)));
            lastIndex = hexMatcher.end() + hexText.length();
        }

        while (rainbowMatcher.find()) {
            result.append(text, lastIndex, rainbowMatcher.start());
            String rainbowText = getNextSegment(text.substring(rainbowMatcher.end()));
            result.append(applyRainbow(rainbowText));
            lastIndex = rainbowMatcher.end() + rainbowText.length();
        }

        if (lastIndex < text.length()) {
            result.append(ChatColor.translateAlternateColorCodes('&', text.substring(lastIndex)));
        }

        return result.toString();
    }

    private static String getNextSegment(String text) {
        Matcher nextMatcher =  Pattern.compile("&#[A-Fa-f0-9]{6}-&#[A-Fa-f0-9]{6}|&#[A-Fa-f0-9]{6}|&#RAINBOW").matcher(text);
        return nextMatcher.find() ? text.substring(0, nextMatcher.start()) : text;
    }

    private static String applyHex(String text, String hexColor) {
        StringBuilder result = new StringBuilder();
        ChatColor color = ChatColor.of("#" + hexColor);
        for (char c : text.toCharArray()) {
            result.append(color).append(c);
        }
        return result.toString();
    }

    private static String applyGradient(String text, String startHex, String endHex) {
        int length = text.length();
        if (length == 0) return "";

        StringBuilder gradientMessage = new StringBuilder();

        int r1 = Integer.parseInt(startHex.substring(0, 2), 16);
        int g1 = Integer.parseInt(startHex.substring(2, 4), 16);
        int b1 = Integer.parseInt(startHex.substring(4, 6), 16);

        int r2 = Integer.parseInt(endHex.substring(0, 2), 16);
        int g2 = Integer.parseInt(endHex.substring(2, 4), 16);
        int b2 = Integer.parseInt(endHex.substring(4, 6), 16);

        for (int i = 0; i < length; i++) {
            double ratio = (double) i / (length-1);
            int r = (int) (r1 + (r2 - r1) * ratio);
            int g = (int) (g1 + (g2 - g1) * ratio);
            int b = (int) (b1 + (b2 - b1) * ratio);

            String hexColor = String.format("#%02X%02X%02X", r, g, b);
            gradientMessage.append(ChatColor.of(hexColor)).append(text.charAt(i));
        }

        return gradientMessage.toString();
    }

    private static String applyRainbow(String text) {
        int length = text.length();
        if (length == 0) return "";

        StringBuilder rainbowMessage = new StringBuilder();

        for (int i = 0; i < length; i++) {
            String colorHex = RAINBOW_COLORS[i % RAINBOW_COLORS.length];
            rainbowMessage.append(ChatColor.of("#" + colorHex)).append(text.charAt(i));
        }

        return rainbowMessage.toString();
    }
}
