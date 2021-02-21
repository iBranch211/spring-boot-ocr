package com.spring.ocr.util;

import org.opencv.core.Point;

import java.util.Comparator;

//Set sorting rules (sort according to X coordinate)
public class XComparator implements Comparator<Point> {
        private boolean reverseOrder; // Reverse order
        public XComparator(boolean reverseOrder) {
            this.reverseOrder = reverseOrder;
        }

        public int compare(Point arg0, Point arg1) {
            if(reverseOrder)
                return (int)arg1.x - (int)arg0.x;
            else
                return (int)arg0.x - (int)arg1.x;
        }
    }