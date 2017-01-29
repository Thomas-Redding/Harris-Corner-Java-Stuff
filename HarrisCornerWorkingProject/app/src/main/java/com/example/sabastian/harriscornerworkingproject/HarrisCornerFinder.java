
package com.example.sabastian.harriscornerworkingproject;


import android.graphics.Bitmap;
import android.graphics.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/*
 *  2% - getting the pixels
 * 36% - getting the intensities
 * 41% - blurring
 *  5% - cumulating
 *  16% - finding corners using eigenvalues
 * 1156 pixels per millisecond
 */

public class HarrisCornerFinder {
    private int median(int[] intensities, int x, int y, int w, int h) {
        if (x == 0 || y == 0 || x == w-1 || y == h-1) {
            int i = x + y * w;
            return intensities[i];
        }

        int rtn = 0;
        for (int X = -1; X <= 1; ++X) {
            for (int Y = -1; Y <= 1; ++Y) {
                rtn += intensities[(x+X) + (y+Y) * w];
            }
        }
        return rtn / 9;

        /*
        int[] lst = new int[9];
        int counter = 0;
        for (int X = -1; X <= 1; ++X) {
            for (int Y = -1; Y <= 1; ++Y) {
                lst[counter] = intensities[(x+X) + (y+Y) * w];
                ++counter;
            }
        }
        Arrays.sort(lst);
        return lst[4];
        */
    }

    private int[][] blurIntensities(int[] intense, int w, int h) {
        // 12 micro-seconds
        // (int) (255 * Color.luminance(img.getPixel(x, y)));
        // our current way is ~12 times faster

        int[][] rtn = new int[w][h];
        int i;
        for (int x = 0; x < w; ++x) {
            for (int y = 0; y < h; ++y) {
                rtn[x][y] = median(intense, x, y, w, h);
            }
        }
        return rtn;
    }

    public ArrayList<HarrisCorner> findCorners(Bitmap img) {
        // create a 2d array of intensities
        int img_w = img.getWidth();
        int img_h = img.getHeight();
        System.out.println(img_w + " x " + img_h);
        long start_time = System.currentTimeMillis();

        int[] intense = new int[img_w * img_h];
        img.getPixels(intense, 0, img_w, 0, 0, img_w, img_h);

        long pixels_time = System.currentTimeMillis();

        int red, green, blue;
        for (int i = 0; i < intense.length; ++i) {
            red   = (0xFF000000 & intense[i]) / 16777216;
            green = (0x00FF0000 & intense[i]) / 65536;
            blue  = (0x0000FF00 & intense[i]) / 256;
            intense[i] = (red + green + blue) / 3;
        }

        long intensities_time = System.currentTimeMillis();

        int[][] intensities = blurIntensities(intense, img_w, img_h);
        long blur_time = System.currentTimeMillis();

        // compute cummulative tables
        float[][] cumXX = new float[img_w][img_h];
        float[][] cumXY = new float[img_w][img_h];
        float[][] cumYY = new float[img_w][img_h];
        for (int x = 0; x < img_w; ++x) {
            cumXX[x][0] = 0;
            cumXY[x][0] = 0;
            cumYY[x][0] = 0;
        }
        for (int y = 0; y < img_h; ++y) {
            cumXX[0][y] = 0;
            cumXY[0][y] = 0;
            cumYY[0][y] = 0;
        }
        for (int x = 1; x < img_w-1; ++x) {
            for (int y = 1; y < img_h-1; ++y) {
                float dx = intensities[x+1][y] - intensities[x-1][y];
                float dy = intensities[x][y+1] - intensities[x][y-1];
                cumXX[x][y] = cumXX[x-1][y] + cumXX[x][y-1] - cumXX[x-1][y-1] + dx * dx;
                cumXY[x][y] = cumXY[x-1][y] + cumXY[x][y-1] - cumXY[x-1][y-1] + dx * dy;
                cumYY[x][y] = cumYY[x-1][y] + cumYY[x][y-1] - cumYY[x-1][y-1] + dy * dy;
            }
        }

        long cumulation_time = System.currentTimeMillis();

        int w = 6;   // must be even
        int h = 6;   // must be even
        double dist_threshold = 30000;
        double angle_threshold = 0.4;
        ArrayList<HarrisCorner> megaList = new ArrayList<HarrisCorner>();


        for (int x = 0; x < img_w - w; ++x) {
            for (int y = 0; y < img_h - h; ++y) {
                float A = cumXX[x + w][y + h] + cumXX[x][y] - cumXX[x + w][y] - cumXX[x][y + h];
                float B = cumXY[x + w][y + h] + cumXY[x][y] - cumXY[x + w][y] - cumXY[x][y + h];
                float C = cumYY[x + w][y + h] + cumYY[x][y] - cumYY[x + w][y] - cumYY[x][y + h];
                float[] eigens = compute_eigenvalues(A, B, C);

                if (eigens[0]+eigens[1] > dist_threshold) {
                    // we are an edge or a corner
                    double angle = Math.atan2(eigens[0], eigens[1]);
                    if (angle_threshold < angle && angle < Math.PI/2 - angle_threshold) {
                        // we are a corner
                        int corner = isTouchingCorner(x, y, intensities);
                        if (corner == -1) {
                            // we have a new corner
                            intensities[x][y] = -1 * (megaList.size() + 1);
                            megaList.add(new HarrisCorner(x+w/2, y+h/2));
                        }
                        else {
                            // part of an existing corner
                            intensities[x][y] = -1 * corner;
                            megaList.get(corner-1).x += x + w/2;
                            megaList.get(corner-1).y += y + h/2;
                            megaList.get(corner-1).n += 1;
                        }
                    }
                }
            }
        }

        long eigen_time = System.currentTimeMillis();

        System.out.println("Pixels: " + (pixels_time - start_time));
        System.out.println("Intens: " + (intensities_time - pixels_time));
        System.out.println("Blurss: " + (blur_time  - intensities_time));
        System.out.println("Cumuls: " + (cumulation_time - blur_time));
        System.out.println("Eigens: " + (eigen_time - cumulation_time));

        for (int i = 0; i < megaList.size(); ++i) {
            megaList.get(i).x /= megaList.get(i).n;
            megaList.get(i).y /= megaList.get(i).n;
        }

        long end_time = System.currentTimeMillis();
        long long_time_diff = end_time - start_time;
        System.out.println("-------------");
        System.out.println(long_time_diff);

        return megaList;
    }

    /*
     * @param x - x-position of pixel
     * @param y - y-position of pixel
     * @param isCorner - a 2d array where negative values indicate a corner and
     * positive values indicate no corner. If two cells have the same negative value,
     * then they are part of the same corner.
     * @return - returns -1 if not touching a corner; returns a positive number if touching a corner
     * The positive number uniquely indicates which corner it is touching.
     */
    private int isTouchingCorner(int x, int y, int[][] isCorner) {
        if (x > 0 && isCorner[x-1][y] < 0 ) {
            return -1 * isCorner[x-1][y];
        }

        if (y > 0 && isCorner[x][y-1] < 0 ) {
            return -1 * isCorner[x][y-1];
        }

        if (x < isCorner.length - 1 && isCorner[x+1][y] < 0 ) {
            return -1 * isCorner[x+1][y];
        }

        if (y < isCorner[0].length - 1 && isCorner[x+1][y] < 0 ) {
            return -1 * isCorner[x+1][y];
        }

        return -1;
    }

    public class HarrisCorner {
        public HarrisCorner(int X, int Y) {
            x = X;
            y = Y;
            n = 1;
        }
        int x = 0;
        int y = 0;
        int n = 0;
    }

    private float[] compute_eigenvalues(float A, float B, float C) {
        float trace = A + C;
        float det = A*C - B*B;
        float[] rtn = new float[2];
        rtn[0] = trace/2 + (float) Math.sqrt(trace*trace/4-det);
        rtn[1] = trace/2 - (float) Math.sqrt(trace*trace/4-det);
        return rtn;
    }
}
