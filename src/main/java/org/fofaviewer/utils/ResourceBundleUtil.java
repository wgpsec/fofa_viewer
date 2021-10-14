package org.fofaviewer.utils;

import java.util.ResourceBundle;
import java.util.Locale;

public class ResourceBundleUtil {

    private static final ResourceBundle resource;

    static {
        if (Locale.getDefault() != Locale.CHINA) {
            resource = ResourceBundle.getBundle("locales", Locale.US);
        }else{
            resource = ResourceBundle.getBundle("locales", Locale.CHINA);
        }
    }

    private ResourceBundleUtil() {
    }

    public static ResourceBundle getResource() {
        return resource;
    }

}

