package com.example.thomas.myapplication;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;

import static android.R.attr.key;

/**
 * Created by thomas on 1/29/17.
 */

public class TableCornerFinder {
    public class IntPair implements Comparable<IntPair> {
        int x;
        int y;
        public IntPair(int x, int y) {
            super();
            this.x = x;
            this.y = y;
        }

        @Override
        public int compareTo(IntPair other) {
            return (1000000 * x + y) - (1000000 * other.x + other.y);
        }
    }

    public IntPair[] findTableCorners(Bitmap img, ArrayList<HarrisCornerFinder.HarrisCorner> lst, int[][] arr) {
        System.out.println("findTableCorners()");
        int w = img.getWidth();
        int h = img.getHeight();
        int[] intense = new int[w * h];
        int[][] intensities = new int[w][h];
        img.getPixels(intense, 0, w, 0, 0, w, h);
        int red, green, blue;
        for (int x = 0; x < w; ++x) {
            for (int y = 0; y < h; ++y) {
                int i = x + y * w;
                red   = (0xFF000000 & intense[i]) / 16777216;
                green = (0x00FF0000 & intense[i]) / 65536;
                blue  = (0x0000FF00 & intense[i]) / 256;
                intensities[x][y] = (red + green + blue) / 3;
            }
        }

        TreeSet<IntPair> pixels_to_look_at = new TreeSet<IntPair>();
        TreeSet<IntPair> pixels_looked_at = new TreeSet<IntPair>();
        IntPair middle_pixel = new IntPair(w/2, h/2);
        pixels_to_look_at.add(middle_pixel);
        int diff;
        int threshold = 4;
        int count = 0;
        double dx, dy, d;
        TreeSet<IntPair> true_corners = new TreeSet<IntPair>();
        ArrayList<IntPair> rtn = new ArrayList<IntPair>();
        while (!pixels_to_look_at.isEmpty()) {
            IntPair pixel = pixels_to_look_at.first();
            pixels_looked_at.add(pixel);
            for (int i = 0; i < lst.size(); ++i) {
                dx = lst.get(i).x - pixel.x;
                dy = lst.get(i).y - pixel.y;
                d = Math.sqrt(dx*dx + dy*dy);
                if (d < 10) {
                    true_corners.add(new IntPair(lst.get(i).x, lst.get(i).y));
                }
            }
            rtn.add(pixel);
            pixels_to_look_at.remove(pixel);
            if (pixel.x != 0) {
                diff = Math.abs(intensities[pixel.x - 1][pixel.y] - intensities[pixel.x][pixel.y]);
                if (diff < threshold) {
                    IntPair new_pixel = new IntPair(pixel.x - 1, pixel.y);
                    if (!pixels_looked_at.contains(new_pixel))
                        pixels_to_look_at.add(new_pixel);
                }
            }
            if (pixel.y != 0) {
                diff = Math.abs(intensities[pixel.x][pixel.y - 1] - intensities[pixel.x][pixel.y]);
                if (diff < threshold) {
                    IntPair new_pixel = new IntPair(pixel.x, pixel.y - 1);
                    if (!pixels_looked_at.contains(new_pixel))
                        pixels_to_look_at.add(new_pixel);
                }
            }
            if (pixel.x != w - 1) {
                diff = Math.abs(intensities[pixel.x + 1][pixel.y] - intensities[pixel.x][pixel.y]);
                if (diff < threshold) {
                    IntPair new_pixel = new IntPair(pixel.x + 1, pixel.y);
                    if (!pixels_looked_at.contains(new_pixel))
                        pixels_to_look_at.add(new_pixel);
                }
            }
            if (pixel.y != 0) {
                diff = Math.abs(intensities[pixel.x][pixel.y + 1] - intensities[pixel.x][pixel.y]);
                if (diff < threshold) {
                    IntPair new_pixel = new IntPair(pixel.x, pixel.y + 1);
                    if (!pixels_looked_at.contains(new_pixel))
                        pixels_to_look_at.add(new_pixel);
                }
            }
            ++count;
        }

        System.out.println("KEYS");
        for(IntPair key : true_corners) {
            System.out.println(key.x + ", " + key.y);
        }
        System.out.println("----");
        IntPair[] foo = new IntPair[true_corners.size()];
        true_corners.toArray(foo);
        return foo;
        /*
        System.out.println(count);
        IntPair[] intpair_arr = new IntPair[rtn.size()];
        rtn.toArray(intpair_arr);
        return intpair_arr;
        */
    }
}
