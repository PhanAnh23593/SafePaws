package com.zippy.auth.util;

import com.zippy.auth.constant.SecurityConstants;

/** Parses the Authorization header without logging credential values. */
public final class BearerTokenUtils {
    private BearerTokenUtils() {}

    public static String extract(String header) {
        String prefix = SecurityConstants.BEARER_PREFIX;
        if (header == null || !header.regionMatches(true, 0, prefix, 0, prefix.length())) {
            return null;
        }
        return header.substring(prefix.length());
    }
}
