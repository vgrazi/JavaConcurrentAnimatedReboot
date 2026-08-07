package com.vgrazi.jca.util;

import io.micrometer.core.lang.Nullable;

public class StringUtils {
    public static boolean isBlank(@Nullable String string) {
        if (isEmpty(string)) {
            return true;
        } else {
            for(int i = 0; i < string.length(); ++i) {
                if (!Character.isWhitespace(string.charAt(i))) {
                    return false;
                }
            }

            return true;
        }
    }

    public static boolean isEmpty(@Nullable String string) {
        return string == null || string.isEmpty();
    }

}
