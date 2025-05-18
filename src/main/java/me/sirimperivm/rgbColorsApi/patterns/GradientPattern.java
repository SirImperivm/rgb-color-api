package me.sirimperivm.rgbColorsApi.patterns;

import me.sirimperivm.rgbColorsApi.ColorApi;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("all")
public class GradientPattern implements HexPattern {

    Pattern pattern = Pattern.compile("&#([A-Fa-f0-9]{6})-&#([A-Fa-f0-9]{6})(.*?)");

    public String process(String string) {
        Matcher matcher = pattern.matcher(string);
        while (matcher.find()) {
            String start = matcher.group(1);
            String end = matcher.group(2);
            String content = matcher.group(3);

            string = string.replace(matcher.group(), ColorApi.color(content, new Color(Integer.parseInt(start, 16)), new Color(Integer.parseInt(end, 16))));
        }
        return string;
    }
}
