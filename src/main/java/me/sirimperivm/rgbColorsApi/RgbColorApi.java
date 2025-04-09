package me.sirimperivm.rgbColorsApi;

import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("all")
public class RgbColorApi extends JavaPlugin {

    private ColorApi colorApi;

    @Override
    public void onEnable() {
        colorApi = new ColorApi(this);
    }

    @Override
    public void onDisable() {
    }

    public static String colorize(String message) {
        return ColorApi.colorize(message);
    }
}
