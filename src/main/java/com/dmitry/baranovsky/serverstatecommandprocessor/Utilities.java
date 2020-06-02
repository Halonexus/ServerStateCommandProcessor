package com.dmitry.baranovsky.serverstatecommandprocessor;

import java.util.regex.Pattern;

public abstract class Utilities {

    public static String[] splitArguments(String arguments, String pattern) {
        Pattern splitPattern = Pattern.compile(pattern);
        return splitPattern.split(arguments);
    }

    public static String[] splitArguments(String arguments, String pattern, int parts) {
        Pattern splitPattern = Pattern.compile(pattern);
        return splitPattern.split(arguments, parts);
    }
}
