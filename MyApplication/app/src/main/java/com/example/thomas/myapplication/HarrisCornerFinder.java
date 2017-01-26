
package com.example.thomas.myapplication;


import android.graphics.Bitmap;
import android.graphics.Color;
import java.util.ArrayList;

public class HarrisCornerFinder {
    public ArrayList<HarrisCorner> findCorners(Bitmap img) {
        // create a 2d array of intensities
        int img_w = img.getWidth();
        int img_h = img.getHeight();
        int[][] intensities = new int[img_w][img_h];
        for (int x = 0; x < img_w; ++x) {
            for (int y = 0; y < img_h; ++y) {
                intensities[x][y] = (int) (255 * Color.luminance(img.getPixel(x, y)));
            }
        }

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
        for (int x = 1; x < img_w; ++x) {
            for (int y = 1; y < img_h; ++y) {
                float dx = intensities[x][y] - intensities[x-1][y];
                float dy = intensities[x][y] - intensities[x][y-1];
                cumXX[x][y] = cumXX[x-1][y] + cumXX[x][y-1] - cumXX[x-1][y-1] + dx * dx;
                cumXY[x][y] = cumXY[x-1][y] + cumXY[x][y-1] - cumXY[x-1][y-1] + dx * dy;
                cumYY[x][y] = cumYY[x-1][y] + cumYY[x][y-1] - cumYY[x-1][y-1] + dy * dy;
            }
        }

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
                            megaList.get(corner-1).x += x + w/2;
                            megaList.get(corner-1).y += y + h/2;
                            megaList.get(corner-1).n += 1;
                        }
                    }
                }
            }
        }

        for (int i = 0; i < megaList.size(); ++i) {
            megaList.get(i).x /= megaList.get(i).n;
            megaList.get(i).y /= megaList.get(i).n;
        }

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
            return -1 * isCorner[x-1][y];
        }

        if (x < isCorner.length - 1 && isCorner[x+1][y] < 0 ) {
            return -1 * isCorner[x+1][y];
        }

        if (y < isCorner[0].length - 1 && isCorner[x+1][y] < 0 ) {
            return -1 * isCorner[x][y+1];
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
