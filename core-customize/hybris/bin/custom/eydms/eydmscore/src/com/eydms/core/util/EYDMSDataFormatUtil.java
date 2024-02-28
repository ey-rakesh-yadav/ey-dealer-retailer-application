package com.eydms.core.util;

public class EYDMSDataFormatUtil {
    public static Integer calculatePercentage(double size, double total) {
        return (int) ((size/total)*100);
    }
}
