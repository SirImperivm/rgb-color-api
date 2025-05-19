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
    private static final Pattern patternNormal = Pattern.compile("\\[#([0-9A-Fa-f]{6})\\]");
    private static final Pattern patternGrad = Pattern.compile("\\[#([0-9A-Fa-f]{6})>\\](.*?)\\[#([0-9A-Fa-f]{6})<\\]");
    private static final Pattern patternOneFromTwo = Pattern.compile("\\[#([0-9A-Fa-f]{6})<>\\]");

    public static String colorizeType(ColorizeType type, String input) {
        return switch (type) {
            case GRADIENT -> colorizeGradient(input);
            case RGB -> colorizeRGB(input);
            case CLASSIC -> colorizeClassic(input);
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
        input = input.replaceAll("\\[#([0-9A-Fa-f]{6})>\\]", "");
        input = input.replaceAll("\\[#([0-9A-Fa-f]{6})<\\]", "");
        input = input.replaceAll("\\[#([0-9A-Fa-f]{6})\\]", "");
        return input;
    }

    public static String colorize(String input) {
        input = colorizeGradient(input);
        input = colorizeRGB(input);
        return input;
    }

    public static String colorizeClassic(String input) {
        input = ChatColor.translateAlternateColorCodes('&', input);
        return input;
    }

    public static String colorizeGradient(String input) {
        Matcher matcher = patternOneFromTwo.matcher(input);

        StringBuilder output = new StringBuilder();

        while (matcher.find()) {
            String text = matcher.group(1);
            matcher.appendReplacement(output, "[#" + text + "<][#" + text + ">]");
        }
        matcher.appendTail(output);

        input = output.toString();

        matcher = patternGrad.matcher(input);
        while (matcher.find()) {
            input = input.replace(matcher.group(), color(matcher.group(2), new Color(Integer.parseInt(matcher.group(1), 16)), new Color(Integer.parseInt(matcher.group(3), 16))));
        }
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    public static String colorizeRGB(String input) {
        Matcher matcher = patternNormal.matcher(input);
        StringBuilder result = new StringBuilder(input.length());

        int lastEnd = 0;
        while (matcher.find()) {
            String color = matcher.group(1);
            result.append(input, lastEnd, matcher.start());

            if (color != null) {
                result.append(getColor(color));
            }

            lastEnd = matcher.end();
        }

        result.append(input.substring(lastEnd));
        return result.toString();
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