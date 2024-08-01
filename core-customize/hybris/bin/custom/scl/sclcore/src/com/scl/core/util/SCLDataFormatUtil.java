package com.scl.core.util;

public class SCLDataFormatUtil {
    public static Integer calculatePercentage(double size, double total) {
        return (int) ((size/total)*100);
    }
}
