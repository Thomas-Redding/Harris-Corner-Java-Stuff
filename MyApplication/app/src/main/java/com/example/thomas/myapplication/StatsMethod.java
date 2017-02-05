package com.example.thomas.myapplication;

import android.graphics.Bitmap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeSet;

/**
 * Created by thomas on 2/4/17.
 */

public class StatsMethod {
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

    public class LineModel {
        public double x;
        public double y;
        public double theta;
        public double residual_variance;
        public double explained_variance;
        public double total_variance;
    }

    public class Best_Fit_Line_Computer {
        int n = 0;
        double sum_x = 0;
        double sum_y = 0;
        double sum_xx = 0;
        double sum_xy = 0;
        double sum_yy = 0;

        Queue<IntPair> queue = new LinkedList<IntPair>();

        public void push(int x, int y) {
            this.queue.add(new IntPair(x, y));
            this.n += 1;
            this.sum_x += x;
            this.sum_y += y;
            this.sum_xx += x * x;
            this.sum_xy += x * y;
            this.sum_yy += y * y;
        }
        public IntPair pop() {
            IntPair rtn = this.queue.remove();
            int x = rtn.x;
            int y = rtn.y;
            this.n -= 1;
            this.sum_x -= x;
            this.sum_y -= y;
            this.sum_xx -= x * x;
            this.sum_xy -= x * y;
            this.sum_yy -= y * y;
            return rtn;
        }
        public LineModel get_line() {
            // https://en.wikipedia.org/wiki/Variance#Definition
            double xx = this.sum_xx - this.sum_x * this.sum_x / this.n;
            double yy = this.sum_yy - this.sum_y * this.sum_y / this.n;

            // http://www.statisticshowto.com/what-is-the-correlation-coefficient-formula/
            double xy = this.sum_xy - this.sum_x * this.sum_y / this.n;
            double cX = this.sum_x / this.n;
            double cY = this.sum_y / this.n;

            double A = (yy-xx)*(yy-xx) + 4*xy*xy;
            double B = -1*(yy-xx)*(yy-xx) - 4*xy*xy;
            double C = xy*xy;
            double det = B*B-4*A*C;
            double root_1 = (-1*B+Math.sqrt(det)) / (2*A);
            double root_2 = (-1*B-Math.sqrt(det)) / (2*A);
            ArrayList<Double> roots = new ArrayList<Double>();
            if (0 <= root_1 && root_1 <= 1) roots.add(Math.sqrt(root_1));
            if (0 <= root_2 && root_2 <= 1) roots.add(Math.sqrt(root_2));

            double max_score = Double.NEGATIVE_INFINITY;
            int best_root = -1;
            boolean flip_sign = false;
            for (int i = 0; i < roots.size(); ++i) {
                double  cos = roots.get(i);
                double  sin = Math.sqrt(1 - cos*cos);
                double score = cos*cos*xx + sin*sin*yy + 2*cos*sin*xy;
                if (score > max_score) {
                    max_score = score;
                    best_root = i;
                    flip_sign = false;
                }

                cos = roots.get(i);
                sin = -1*Math.sqrt(1 - cos*cos);
                score = cos*cos*xx + sin*sin*yy + 2*cos*sin*xy;
                if (score > max_score) {
                    max_score = score;
                    best_root = i;
                    flip_sign = true;
                }
            }

            if (best_root == -1)
                return null;

            double cos = roots.get(best_root);
            double sin = Math.sqrt(1 - cos*cos);
            sin *= flip_sign ? -1 : 1;

            // double resid_variance = (1-cos*cos)*this.sum_xx + (2*cos*cos*cX-2*cX+2*cos*sin*cY)*this.sum_x + (1-sin*sin)*this.sum_yy + (cX*cX+cY*cY-cos*cos*cX*cX-sin*sin*cY*cY)+(2*sin*sin*cY-2*cY+2*cos*sin*cX)*this.sum_y+(-2*cos*sin)*this.sum_xy;

            double explained_variance = cos*cos*this.sum_xx + 2*cos*sin*this.sum_xy + sin*sin*this.sum_yy + (-2*cX*cos*cos-2*cos*sin*cY)*this.sum_x + (-2*cY*sin*sin-2*cos*sin*cX)*this.sum_y + (cX*cX*cos*cos+sin*sin*cY*cY+2*cos*sin*cX*cY)*this.n;
            double total_variance = this.sum_xx - 2*cX*this.sum_x + cX*cX*this.n + this.sum_yy - 2*cY*this.sum_y + cY*cY*this.n;
            double resid_variance = total_variance - explained_variance;

            LineModel rtn = new LineModel();
            rtn.x = this.sum_x / this.n;
            rtn.y = this.sum_y / this.n;
            rtn.theta = Math.atan2(sin, cos);
            rtn.residual_variance = resid_variance;
            rtn.explained_variance = explained_variance;
            rtn.total_variance = total_variance;
            return rtn;
        }
    }

    public int[][] findCorners(Bitmap img) {
        // create a 2d array of intensities
        int w = img.getWidth();
        int h = img.getHeight();
        int[] intense = new int[w * h];
        int[][] intensities = new int[w][h];
        img.getPixels(intense, 0, w, 0, 0, w, h);

        int red, green, blue, i;
        for (int x = 0; x < w; ++x) {
            for (int y = 0; y < h; ++y) {
                i = x + y * w;
                red   = (0xFF000000 & intense[i]) / 16777216;
                green = (0x00FF0000 & intense[i]) / 65536;
                blue  = (0x0000FF00 & intense[i]) / 256;
                intensities[x][y] = (red + green + blue) / 3;
            }
        }

        TreeSet<IntPair> pixels_to_look_at = new TreeSet<IntPair>();
        TreeSet<IntPair> pixels_looked_at = new TreeSet<IntPair>();
        pixels_to_look_at.add(new IntPair(w/2, h/2));

        boolean[][] table = new boolean[w][h];

        int diff;
        int threshold = 4;
        while (!pixels_to_look_at.isEmpty()) {
            IntPair pixel = pixels_to_look_at.first();
            pixels_looked_at.add(pixel);
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
            if (pixel.y != h - 1) {
                diff = Math.abs(intensities[pixel.x][pixel.y + 1] - intensities[pixel.x][pixel.y]);
                if (diff < threshold) {
                    IntPair new_pixel = new IntPair(pixel.x, pixel.y + 1);
                    if (!pixels_looked_at.contains(new_pixel))
                        pixels_to_look_at.add(new_pixel);
                }
            }
            table[pixel.x][pixel.y] = true;
        }

        int[][] borders = new int[w][h];
        IntPair farthest_point = new IntPair(w/2, h/2);
        double max_dist = 0;
        for (int x = 1; x < w - 1; ++x) {
            for (int y = 1; y < h - 1; ++y) {
                if (!table[x][y]) {
                    if (table[x+1][y] || table[x-1][y] || table[x][y+1] || table[x][y-1]) {
                        borders[x][y] = 1;
                        double dist = (x-w/2.0)*(x-w/2.0) + (y-h/2.0)*(y-h/2.0);
                        if (dist > max_dist) {
                            max_dist = dist;
                            farthest_point.x = x;
                            farthest_point.y = y;
                        }
                    }
                }
            }
        }

        pixels_to_look_at = new TreeSet<IntPair>();
        pixels_looked_at = new TreeSet<IntPair>();
        pixels_to_look_at.add(farthest_point);

        while (!pixels_to_look_at.isEmpty()) {
            IntPair pixel = pixels_to_look_at.first();
            pixels_looked_at.add(pixel);
            pixels_to_look_at.remove(pixel);
            borders[pixel.x][pixel.y] = 2;
            if (borders[pixel.x - 1][pixel.y] == 1) {
                IntPair new_pixel = new IntPair(pixel.x - 1, pixel.y);
                if (!pixels_looked_at.contains(new_pixel))
                    pixels_to_look_at.add(new_pixel);
            }
            if (borders[pixel.x + 1][pixel.y] == 1) {
                IntPair new_pixel = new IntPair(pixel.x + 1, pixel.y);
                if (!pixels_looked_at.contains(new_pixel))
                    pixels_to_look_at.add(new_pixel);
            }
            if (borders[pixel.x][pixel.y - 1] == 1) {
                IntPair new_pixel = new IntPair(pixel.x, pixel.y - 1);
                if (!pixels_looked_at.contains(new_pixel))
                    pixels_to_look_at.add(new_pixel);
            }
            if (borders[pixel.x][pixel.y + 1] == 1) {
                IntPair new_pixel = new IntPair(pixel.x, pixel.y + 1);
                if (!pixels_looked_at.contains(new_pixel))
                    pixels_to_look_at.add(new_pixel);
            }
            if (borders[pixel.x - 1][pixel.y - 1] == 1) {
                IntPair new_pixel = new IntPair(pixel.x - 1, pixel.y - 1);
                if (!pixels_looked_at.contains(new_pixel))
                    pixels_to_look_at.add(new_pixel);
            }
            if (borders[pixel.x + 1][pixel.y - 1] == 1) {
                IntPair new_pixel = new IntPair(pixel.x + 1, pixel.y - 1);
                if (!pixels_looked_at.contains(new_pixel))
                    pixels_to_look_at.add(new_pixel);
            }
            if (borders[pixel.x - 1][pixel.y + 1] == 1) {
                IntPair new_pixel = new IntPair(pixel.x - 1, pixel.y + 1);
                if (!pixels_looked_at.contains(new_pixel))
                    pixels_to_look_at.add(new_pixel);
            }
            if (borders[pixel.x + 1][pixel.y + 1] == 1) {
                IntPair new_pixel = new IntPair(pixel.x + 1, pixel.y + 1);
                if (!pixels_looked_at.contains(new_pixel))
                    pixels_to_look_at.add(new_pixel);
            }
        }

        // Best_Fit_Line_Computer model = new Best_Fit_Line_Computer();
        // LineModel line = model.get_line();

        return borders;
    }
}
