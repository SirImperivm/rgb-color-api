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
        int length = withoutSpecialChar(string).length();
        
        if (length == 0) {
            return originalString;
        }
        
        ChatColor[] colors = createGradient(start, end, length);
        return apply(originalString, colors);
    }

    public static String rainbow(String string) {
        String originalString = string;
        int length = withoutSpecialChar(string).length();
        
        if (length == 0) {
            return originalString;
        }
        
        ChatColor[] colors = createRainbow(length);
        return apply(originalString, colors);
    }

    public static ChatColor getColor(String string) {
        return SUPPORTS_RGB ? ChatColor.of(new Color(Integer.parseInt(string, 16))) : getClosestColor(new Color(Integer.parseInt(string, 16)));
    }

    private static String apply(String source, ChatColor[] colors) {
        if (source.isEmpty() || colors.length == 0) {
            return source;
        }
        
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
                    if (outIndex < colors.length) {
                        stringBuilder.append(colors[outIndex++]).append(specialColors).append(characters[i]);
                    } else {
                        stringBuilder.append(specialColors).append(characters[i]);
                    }
                }
            } else {
                if (outIndex < colors.length) {
                    stringBuilder.append(colors[outIndex++]).append(specialColors).append(characters[i]);
                } else {
                    stringBuilder.append(specialColors).append(characters[i]);
                }
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
        if (step <= 0) {
            return new ChatColor[0];
        }
        
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
        if (step <= 0) {
            return new ChatColor[0];
        }
        
        ChatColor[] colors = new ChatColor[step];
        
        int stepR = (step > 1) ? Math.abs(start.getRed() - end.getRed()) / (step - 1) : 0;
        int stepG = (step > 1) ? Math.abs(start.getGreen() - end.getGreen()) / (step - 1) : 0;
        int stepB = (step > 1) ? Math.abs(start.getBlue() - end.getBlue()) / (step - 1) : 0;

        int[] direction = { (start.getRed() < end.getRed()) ? 1 : -1, (start.getGreen() < end.getGreen()) ? 1 : -1, (start.getBlue() < end.getBlue()) ? 1 : -1 };

        for (int i=0; i<step; i++) {
            Color color = new Color(
                start.getRed() + ((step > 1) ? stepR * direction[0] * i : 0),
                start.getGreen() + ((step > 1) ? stepG * direction[1] * i : 0),
                start.getBlue() + ((step > 1) ? stepB * direction[2] * i : 0)
            );
        
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