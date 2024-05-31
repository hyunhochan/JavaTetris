package tetrisProject;

import java.awt.*;
import java.util.Hashtable;

public class Configuration extends Object {

    private static Hashtable config = new Hashtable();

    public static String getValue(String key) {
        if (config.containsKey(key)) {
            return config.get(key).toString();
        } else {
            try {
                return System.getProperty(key);
            } catch (SecurityException ignore) {
                return null;
            }
        }
    }

    public static String getValue(String key, String def) {
        String value = getValue(key);

        return (value == null) ? def : value;
    }

    public static void setValue(String key, String value) {
        config.put(key, value);
    }

    
}