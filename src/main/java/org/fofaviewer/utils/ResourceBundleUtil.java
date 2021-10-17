package org.fofaviewer.utils;

import java.util.ResourceBundle;
import java.util.Locale;

public class ResourceBundleUtil {

    private static final ResourceBundle resource;

    static {
        if (Locale.getDefault().getLanguage().equals(Locale.CHINESE.getLanguage())) {
            resource = ResourceBundle.getBundle("locales", Locale.CHINA);
        }else{
            resource = ResourceBundle.getBundle("locales", Locale.US);
        }
    }

    private ResourceBundleUtil() {
    }

    public static ResourceBundle getResource() {
        return resource;
    }

}

