package me.sirimperivm.rgbColorsApi.patterns;

import me.sirimperivm.rgbColorsApi.ColorApi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("all")
public class RainbowPattern implements HexPattern {

    Pattern pattern = Pattern.compile("&#RAINBOW(.*?)");

    public String process(String string) {
        Matcher matcher = pattern.matcher(string);
        while (matcher.find()) {
            String content = matcher.group(1);
            string = string.replace(matcher.group(), ColorApi.rainbow(content));
        }
        return string;
    }
}
