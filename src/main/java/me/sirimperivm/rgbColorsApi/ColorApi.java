package me.sirimperivm.rgbColorsApi;

import me.sirimperivm.rgbColorsApi.patterns.GradientPattern;
import me.sirimperivm.rgbColorsApi.patterns.HexPattern;
import me.sirimperivm.rgbColorsApi.patterns.RainbowPattern;
import me.sirimperivm.rgbColorsApi.patterns.SolidPattern;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;

import java.awt.*;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("all")
public class ColorApi {

    private static final int VERSION = getVersion();
    private static final boolean SUPPORTS_RGB = (VERSION >= 16);
    private static final List<String> SPECIAL_COLORS = List.of("&k", "&l", "&m", "&n", "&o", "§k", "§l", "§m", "§n", "§o");
    private static final HashMap<Color, ChatColor> COLORS = new HashMap<>();
    private static final List<HexPattern> HEX_PATTERNS = List.of(new GradientPattern(), new RainbowPattern(), new SolidPattern());

    public ColorApi() {
        COLORS.put(new Color(0), ChatColor.getByChar('0'));
        COLORS.put(new Color(170), ChatColor.getByChar('1'));
        COLORS.put(new Color(43520), ChatColor.getByChar('2'));
        COLORS.put(new Color(43690), ChatColor.getByChar('3'));
        COLORS.put(new Color(11141120), ChatColor.getByChar('4'));
        COLORS.put(new Color(11141290), ChatColor.getByChar('5'));
        COLORS.put(new Color(16755200), ChatColor.getByChar('6'));
        COLORS.put(new Color(11184810), ChatColor.getByChar('7'));
        COLORS.put(new Color(5592405), ChatColor.getByChar('8'));
        COLORS.put(new Color(5592575), ChatColor.getByChar('9'));
        COLORS.put(new Color(5635925), ChatColor.getByChar('a'));
        COLORS.put(new Color(5636095), ChatColor.getByChar('b'));
        COLORS.put(new Color(16733525), ChatColor.getByChar('c'));
        COLORS.put(new Color(16733695), ChatColor.getByChar('d'));
        COLORS.put(new Color(16777045), ChatColor.getByChar('e'));
        COLORS.put(new Color(16777215), ChatColor.getByChar('f'));
    }

    public static String process(String string) {
        for (HexPattern pattern : HEX_PATTERNS) {
            string = pattern.process(string);
        }
        string = ChatColor.translateAlternateColorCodes('&', string);
        return string;
    }

    public static List<String> process(List<String> strings) {
        return strings.stream().map(ColorApi::process).toList();
    }

    public static String color(String string, Color color) {
        return (SUPPORTS_RGB ? ChatColor.of(color) : getClosestColor(color)) + string;
    }

    public static String color(String string, Color start, Color end) {
        String originalString = string;
        ChatColor[] colors = createGradient(start, end, withoutSpecialChar(string).length());
        return apply(originalString, colors);
    }

    public static String rainbow(String string) {
        String originalString = string;
        ChatColor[] colors = createRainbow(withoutSpecialChar(string).length());
        return apply(originalString, colors);
    }

    public static ChatColor getColor(String string) {
        return SUPPORTS_RGB ? ChatColor.of(new Color(Integer.parseInt(string, 16))) : getClosestColor(new Color(Integer.parseInt(string, 16)));
    }

    private static String apply(String source, ChatColor[] colors) {
        StringBuilder specialColors = new StringBuilder();
        StringBuilder stringBuilder = new StringBuilder();
        String[] characters = source.split("");
        int outIndex = 0;
        for (int i=0; i< characters.length; i++) {
            if (characters[i].equals("&") || characters[i].equals("§")) {
                if (i+1 < characters.length) {
                    if (characters[i+1].equals("r")) {
                        specialColors.setLength(0);
                    } else {
                        specialColors.append(characters[i]);
                        specialColors.append(characters[i+1]);
                    }
                    i++;
                } else {
                    stringBuilder.append(colors[outIndex++]).append(specialColors).append(characters[i]);
                }
            } else {
                stringBuilder.append(colors[outIndex++]).append(specialColors).append(characters[i]);
            }
        }
        return stringBuilder.toString();
    }

    private static String withoutSpecialChar(String source) {
        String workingString = source;
        for (String color : SPECIAL_COLORS) {
            if (workingString.contains(color))
                workingString = workingString.replace(color, "");
        }
        return workingString;
    }

    private static ChatColor[] createRainbow(int step) {
        ChatColor[] colors = new ChatColor[step];
        double colorStep = 1.0D / step;
        for (int i = 0; i < step; i++) {
            Color color = Color.getHSBColor((float)(colorStep * i), 1F, 1F);
            if (SUPPORTS_RGB) {
                colors[i] = ChatColor.of(color);
            } else {
                colors[i] = getClosestColor(color);
            }
        }
        return colors;
    }

    private static ChatColor[] createGradient(Color start, Color end, int step) {
        ChatColor[] colors = new ChatColor[step];

        int stepR = Math.abs(start.getRed() - end.getRed()) / (step -1);
        int stepG = Math.abs(start.getGreen() - end.getGreen()) / (step -1);
        int stepB = Math.abs(start.getBlue() - end.getBlue()) / (step -1);

        int[] direction = { (start.getRed() < end.getRed()) ? 1 : -1, (start.getGreen() < end.getGreen()) ? 1 : -1, (start.getBlue() < end.getBlue()) ? 1 : -1 };

        for (int i=0; i<step; i++) {
            Color color = new Color(start.getRed() + stepR * direction[0] * i, start.getGreen() + stepG * direction[1] * i, start.getBlue() + stepB * direction[2] * i);
            if (SUPPORTS_RGB) {
                colors[i] = ChatColor.of(color);
            } else {
                colors[i] = getClosestColor(color);
            }
        }
        return colors;
    }

    private static ChatColor getClosestColor(Color color) {
        Color nearestColor = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Color constantColor : COLORS.keySet()) {
            double distance = Math.pow((color.getRed() - constantColor.getRed()), 2D) + Math.pow((color.getGreen() - constantColor.getGreen()), 2D) + Math.pow((color.getBlue() - constantColor.getBlue()), 2D);
            if (nearestDistance > distance) {
                nearestColor = constantColor;
                nearestDistance = distance;
            }
        }
        return COLORS.get(nearestColor);
    }

    /*
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
    }*/

    private static int getVersion() {
        String version = Bukkit.getVersion();
        Validate.notEmpty(version, "Cannot get version from empty server version string");
        int index = version.lastIndexOf("MC:");
        if (index != -1) {
            version = version.substring(index + 4, version.length() - 1);
        } else if (version.endsWith("SNAPSHOT")) {
            index = version.indexOf('-');
            version = version.substring(0, index);
        }
        int lastDot = version.lastIndexOf('.');
        if (version.indexOf('.') != lastDot)
            version = version.substring(0, lastDot);
        return Integer.parseInt(version.substring(2));
    }
}