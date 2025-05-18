package me.sirimperivm.rgbColorsApi.patterns;

import me.sirimperivm.rgbColorsApi.ColorApi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("all")
public class SolidPattern implements HexPattern {

    Pattern pattern = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public String process(String string) {
        Matcher matcher = pattern.matcher(string);
        while (matcher.find()) {
            String color = matcher.group(1);
            string = string.replace(matcher.group(), ColorApi.getColor(color) + "");
        }
        return string;
    }
}
