package me.sirimperivm.rgbColorsApi;

import me.sirimperivm.rgbColorsApi.enums.ColorizeType;
import net.md_5.bungee.api.ChatColor;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("all")
public class ColorApi {

    private static final List<String> legacyColors = Arrays.asList("&0", "&1", "&2", "&3", "&4", "&5", "&6", "&7", "&8", "&9", "&a", "&b", "&c", "&d", "&e", "§0", "§1", "§2", "§3", "§4", "§5", "§6", "§7", "§8", "§9", "§a", "§b", "§c", "§d", "§e");
    private static final List<String> specialChars = Arrays.asList("&l", "&n", "&o", "&k", "&m", "§l", "§n", "§o", "§k", "§m");
    
    private static final Pattern patternNormal = Pattern.compile("&#([0-9A-Fa-f]{6})(.+?)(?=&#|$)");
    private static final Pattern patternGrad = Pattern.compile("&#([0-9A-Fa-f]{6})-&#([0-9A-Fa-f]{6})(.+?)(?=&#|$)");
    private static final Pattern patternRainbow = Pattern.compile("&#RAINBOW(.+?)(?=&#|$)");
    
    private static final String[] RAINBOW_COLORS = { "FF0000", "FF7F00", "FFFF00", "00FF00", "0000FF", "4B0082", "9400D3" };

    public static String colorizeType(ColorizeType type, String input) {
        return switch (type) {
            case GRADIENT -> colorizeGradient(input);
            case RGB -> colorizeRGB(input);
            case CLASSIC -> colorizeClassic(input);
            case RAINBOW -> colorizeRainbow(input);
        };
    }

    public static String clear(String input) {
        input = removePatterns(input);
        input = removeLegacyColors(input);
        input = removeSpecialChars(input);
        return input;
    }

    public static String removeSpecialChars(String input) {
        for (String chars : specialChars) {
            if (!input.contains(chars)) {
                continue;
            }
            input = input.replaceAll(chars, "");
        }

        return input;
    }

    public static String removeLegacyColors(String input) {
        for (String color : legacyColors) {
            if (!input.contains(color)) {
                continue;
            }
            input = input.replaceAll(color, "");
        }
        return input;
    }

    public static String removePatterns(String input) {
        input = input.replaceAll("&#([0-9A-Fa-f]{6})-&#([0-9A-Fa-f]{6})", "");
        input = input.replaceAll("&#([0-9A-Fa-f]{6})", "");
        input = input.replaceAll("&#RAINBOW", "");
        return input;
    }

    public static String colorize(String input) {
        input = colorizeRainbow(input);
        input = colorizeGradient(input);
        input = colorizeRGB(input);
        return colorizeClassic(input);
    }

    public static String colorizeClassic(String input) {
        input = ChatColor.translateAlternateColorCodes('&', input);
        return input;
    }

    public static String colorizeGradient(String input) {
        Matcher matcher = patternGrad.matcher(input);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String startColor = matcher.group(1);
            String endColor = matcher.group(2);
            String text = matcher.group(3);
            
            matcher.appendReplacement(result, color(text, 
                new Color(Integer.parseInt(startColor, 16)), 
                new Color(Integer.parseInt(endColor, 16))));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }

    public static String colorizeRGB(String input) {
        Matcher matcher = patternNormal.matcher(input);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String color = matcher.group(1);
            String text = matcher.group(2);
            
            StringBuilder colored = new StringBuilder();
            ChatColor chatColor = ChatColor.of("#" + color);
            
            for (char c : text.toCharArray()) {
                colored.append(chatColor).append(c);
            }
            
            matcher.appendReplacement(result, colored.toString());
        }
        matcher.appendTail(result);
        
        return result.toString();
    }

    public static String colorizeRainbow(String input) {
        Matcher matcher = patternRainbow.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String text = matcher.group(1);
            matcher.appendReplacement(result, applyRainbow(text));
        }
        matcher.appendTail(result);

        return result.toString();
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

    public static String color(String input, Color first, Color second) {
        ChatColor[] colors = createGradient(first, second, removeSpecialChars(input).length());
        return apply(input, colors);
    }

    private static String apply(String input, ChatColor[] colors) {
        StringBuilder specialColors = new StringBuilder();
        StringBuilder stringBuilder = new StringBuilder();
        String[] characters = input.split("");
        int outIndex = 0;

        for (int i = 0; i < characters.length; i++) {
            if (!characters[i].equals("&") && !characters[i].equals("§")) {
                stringBuilder.append(colors[outIndex++]).append(specialColors).append(characters[i]);
                continue;
            }
            if (i + 1 >= characters.length) {
                stringBuilder.append(colors[outIndex++]).append(specialColors).append(characters[i]);
                continue;
            }
            if (characters[i + 1].equals("r")) {
                specialColors.setLength(0);
            } else {
                specialColors.append(characters[i]);
                specialColors.append(characters[i + 1]);
            }
            i++;
        }
        return stringBuilder.toString();
    }

    private static ChatColor[] createGradient(Color first, Color second, int amount) {
        ChatColor[] colors = new ChatColor[amount];
        int amountR = Math.abs(first.getRed() - second.getRed()) / (amount - 1);
        int amountG = Math.abs(first.getGreen() - second.getGreen()) / (amount - 1);
        int amountB = Math.abs(first.getBlue() - second.getBlue()) / (amount - 1);
        int[] colorDir = new int[]{first.getRed() < second.getRed() ? +1 : -1, first.getGreen() < second.getGreen() ? +1 : -1, first.getBlue() < second.getBlue() ? +1 : -1};

        for (int i = 0; i < amount; i++) {
            Color color = new Color(first.getRed() + ((amountR * i) * colorDir[0]), first.getGreen() + ((amountG * i) * colorDir[1]), first.getBlue() + ((amountB * i) * colorDir[2]));
            colors[i] = ChatColor.of(color);
        }
        return colors;
    }

    public static ChatColor getColor(String matcher) {
        return ChatColor.of(new Color(Integer.parseInt(matcher, 16)));
    }
}