package me.sirimperivm.rgbColorsApi;

@SuppressWarnings("all")
public class RgbColorApi {

    public static String colorize(String message) {
        ColorApi colorApi = new ColorApi();
        return colorApi.process(message);
    }
}
