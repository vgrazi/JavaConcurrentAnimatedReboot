package com.vgrazi.jca.util;

import java.awt.*;

public class ColorParser {
    /**
     * Given a comma separated String of decimal integers, converts to a color
     */
    public static Color parseColor(String colorString) {
        String[] split = colorString.split(",");
        Color color = new Color(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
        return color;
    }
}
