package com.vgrazi.jca.util;

import java.awt.*;

public class Parsers {
    /**
     * Given a comma separated String of decimal integers, converts to a color
     */
    public static Color parseColor(String colorString) {
        String[] split = colorString.split("[,;]");
        Color color = new Color(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
        return color;
    }

    /**
     * Parses a font of the form Name;Type;Size
     * (Must not contain spaces. For Bold Italic use "bold-italic")
     * @param fontDescriptor
     * @return
     */
    public static Font parseFont(String fontDescriptor) {
        String[] split = fontDescriptor.split("[;,]");
        Font font = new Font(split[0], parseFontStyle(split[1]), Integer.parseInt(split[2]));
        return font;
    }

    public static int parseFontStyle(String styleName) {
        int styleCode;
        String lcStyle = styleName.toLowerCase();
        switch (lcStyle) {
            case "plain":
                styleCode = Font.PLAIN;
                break;
            case "bold":
                styleCode = Font.BOLD;
                break;
            case "italic":
                styleCode = Font.ITALIC;
                break;
            case "bold-italic":
                styleCode = Font.BOLD+Font.ITALIC;
                break;
            default:
                throw new IllegalArgumentException("Unknown Font Style " + styleName);
        }
        return styleCode;
    }
}
