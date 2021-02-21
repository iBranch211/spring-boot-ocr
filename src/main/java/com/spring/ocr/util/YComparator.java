package com.spring.ocr.util;

import org.opencv.core.Point;

import java.util.Comparator;

//Set sorting rules (sort according to Y coordinate)
public class YComparator implements Comparator<Point> {
    private boolean reverseOrder; // Reverse order
    public YComparator(boolean reverseOrder) {
        this.reverseOrder = reverseOrder;
    }

    public int compare(Point arg0, Point arg1) {
        if(reverseOrder)
            return (int)arg1.y - (int)arg0.y;
        else
            return (int)arg0.y - (int)arg1.y;
    }
}