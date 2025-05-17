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
        if (text == null || text.isEmpty()) return text;

        StringBuilder result = new StringBuilder();
        int lastIndex = 0;

        Pattern pattern = Pattern.compile("&#([A-Fa-f0-9]{6})-&#([A-Fa-f0-9]{6})|&#([A-Fa-f0-9]{6})|&#RAINBOW");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            result.append(ChatColor.translateAlternateColorCodes('&', text.substring(lastIndex, matcher.start())));

            String matchedSegment = getNextSegment(text.substring(matcher.end()));

            // Controllo sul tipo di match trovato
            if (matcher.group(1) != null && matcher.group(2) != null) {
                result.append(applyGradient(matchedSegment, matcher.group(1), matcher.group(2)));
            } else if (matcher.group(3) != null) {
                result.append(applyHex(matchedSegment, matcher.group(3)));
            } else {
                result.append(applyRainbow(matchedSegment));
            }

            lastIndex = matcher.end() + matchedSegment.length();
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
            double ratio = (length > 1) ? (double) i / (length - 1) : 0;
        
            int r = (int) Math.round(r1 * (1 - ratio) + r2 * ratio);
            int g = (int) Math.round(g1 * (1 - ratio) + g2 * ratio);
            int b = (int) Math.round(b1 * (1 - ratio) + b2 * ratio);

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