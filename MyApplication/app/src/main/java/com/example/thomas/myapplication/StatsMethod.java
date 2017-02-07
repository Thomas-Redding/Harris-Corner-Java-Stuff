package com.example.thomas.myapplication;

import android.graphics.Bitmap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
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

    private class Eigenvector {
        public double x;
        public double y;
        public double lambda() {
            return Math.sqrt(x*x+y*y);
        }
        public Eigenvector(double X, double Y, double lambda) {
            x = X;
            y = Y;
            double len = Math.sqrt(x*x+y*y);
            x *= lambda/len;
            y *= lambda/len;
        }
    }

    private Eigenvector[] compute_eigens(double A, double B, double C) {
        double trace = A + C;
        double det = A*C - B*B;
        double big_lambda = trace/2 + Math.sqrt(trace*trace/4-det);
        double small_lambda = trace/2 - Math.sqrt(trace*trace/4-det);
        Eigenvector[] rtn = new Eigenvector[2];
        rtn[0] = new Eigenvector(B, big_lambda-A, big_lambda);
        rtn[1] = new Eigenvector(B, small_lambda-A, small_lambda);
        return rtn;
    }

    private IntPair[] guessCorners(int[][] borders, double theta) {
        double cos = Math.cos(theta);
        double sin = Math.sin(theta);
        double adj;
        double opp;

        double min_adj = 1e300;
        IntPair min_adj_pt = new IntPair(0, 0);
        double max_adj = -1e300;
        IntPair max_adj_pt = new IntPair(0, 0);
        double min_opp = 1e300;
        IntPair min_opp_pt = new IntPair(0, 0);
        double max_opp = -1e300;
        IntPair max_opp_pt = new IntPair(0, 0);

        for (int x = 0; x < borders.length; ++x) {
            for (int y = 0; y < borders[x].length; ++y) {
                if (borders[x][y] < 2)
                    continue;

                adj = cos * x + sin * y;
                opp = -1 * sin * x + cos * y;

                if (adj > max_adj) {
                    max_adj = adj;
                    max_adj_pt.x = x;
                    max_adj_pt.y = y;
                }
                if (adj < min_adj) {
                    min_adj = adj;
                    min_adj_pt.x = x;
                    min_adj_pt.y = y;
                }
                if (opp > max_opp) {
                    max_opp = opp;
                    max_opp_pt.x = x;
                    max_opp_pt.y = y;
                }
                if (opp < min_opp) {
                    min_opp = opp;
                    min_opp_pt.x = x;
                    min_opp_pt.y = y;
                }
            }
        }
        IntPair[] rtn = new IntPair[4];
        rtn[0] = min_adj_pt; // smallest "x"
        rtn[1] = min_opp_pt; // smallest "y"
        rtn[2] = max_adj_pt; // largest "x"
        rtn[3] = max_opp_pt; // largest "y"
        return rtn;
    }

    IntPair find_key_of_largest_value(TreeMap<IntPair, Integer> dict) {
        Set<IntPair> keys = dict.keySet();
        int max_val = -100;
        IntPair max_key = null;
        for (IntPair key : keys) {
            int val = dict.get(key);
            if (val > max_val) {
                max_val = val;
                max_key = key;
            }
        }
        return max_key;
    }

    public TreeSet<IntPair> findCorners(Bitmap img) {
        // create a 2d array of intensities
        long start_time = System.currentTimeMillis();
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
        long processing_time = System.currentTimeMillis();

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

        long painting_time = System.currentTimeMillis();

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

        long borders_time = System.currentTimeMillis();

        pixels_to_look_at = new TreeSet<IntPair>();
        pixels_looked_at = new TreeSet<IntPair>();
        pixels_to_look_at.add(farthest_point);

        while (!pixels_to_look_at.isEmpty()) {
            IntPair pixel = pixels_to_look_at.first();
            pixels_looked_at.add(pixel);
            pixels_to_look_at.remove(pixel);
            borders[pixel.x][pixel.y] = 2;
            if (pixel.x != 0 && borders[pixel.x - 1][pixel.y] == 1) {
                IntPair new_pixel = new IntPair(pixel.x - 1, pixel.y);
                if (!pixels_looked_at.contains(new_pixel))
                    pixels_to_look_at.add(new_pixel);
            }
            if (pixel.x != borders.length-1 && borders[pixel.x + 1][pixel.y] == 1) {
                IntPair new_pixel = new IntPair(pixel.x + 1, pixel.y);
                if (!pixels_looked_at.contains(new_pixel))
                    pixels_to_look_at.add(new_pixel);
            }
            if (pixel.y != 0 && borders[pixel.x][pixel.y - 1] == 1) {
                IntPair new_pixel = new IntPair(pixel.x, pixel.y - 1);
                if (!pixels_looked_at.contains(new_pixel))
                    pixels_to_look_at.add(new_pixel);
            }
            if (pixel.y != borders[0].length-1 && borders[pixel.x][pixel.y + 1] == 1) {
                IntPair new_pixel = new IntPair(pixel.x, pixel.y + 1);
                if (!pixels_looked_at.contains(new_pixel))
                    pixels_to_look_at.add(new_pixel);
            }
            if (pixel.x != 0 && pixel.y != 0 && borders[pixel.x - 1][pixel.y - 1] == 1) {
                IntPair new_pixel = new IntPair(pixel.x - 1, pixel.y - 1);
                if (!pixels_looked_at.contains(new_pixel))
                    pixels_to_look_at.add(new_pixel);
            }
            if (pixel.x != borders.length-1 && pixel.y != 0 && borders[pixel.x + 1][pixel.y - 1] == 1) {
                IntPair new_pixel = new IntPair(pixel.x + 1, pixel.y - 1);
                if (!pixels_looked_at.contains(new_pixel))
                    pixels_to_look_at.add(new_pixel);
            }
            if (pixel.x != 0 && pixel.y != borders[0].length-1 && borders[pixel.x - 1][pixel.y + 1] == 1) {
                IntPair new_pixel = new IntPair(pixel.x - 1, pixel.y + 1);
                if (!pixels_looked_at.contains(new_pixel))
                    pixels_to_look_at.add(new_pixel);
            }
            if (pixel.x != borders.length-1 && pixel.y != borders[0].length-1 && borders[pixel.x + 1][pixel.y + 1] == 1) {
                IntPair new_pixel = new IntPair(pixel.x + 1, pixel.y + 1);
                if (!pixels_looked_at.contains(new_pixel))
                    pixels_to_look_at.add(new_pixel);
            }
        }

        long cleaning_time = System.currentTimeMillis();

        TreeMap<IntPair, Integer> counts = new TreeMap<IntPair, Integer>();
        for (double theta = 0; theta < Math.PI/2; theta += 10 * Math.PI/180) {
            IntPair[] set = guessCorners(borders, theta);
            for (i = 0; i < set.length; ++i) {
                if (counts.containsKey(set[i]))
                    counts.put(set[i], counts.get(set[i])+1);
                else
                    counts.put(set[i], 1);
                ++borders[set[i].x][set[i].y];
            }
        }

        long corner_time = System.currentTimeMillis();

        for (IntPair key : counts.keySet()) {
            for (IntPair key2 : counts.keySet()) {
                int dist = (key.x-key2.x)*(key.x-key2.x) + (key.y-key2.y)*(key.y-key2.y);
                if (key == key2)
                    continue;
                if (dist < 20) {
                    int count1 = counts.get(key);
                    int count2 = counts.get(key2);
                    if (count1 > count2) {
                        counts.put(key, count1+count2);
                        counts.put(key2, 0);
                    }
                    else {
                        counts.put(key, 0);
                        counts.put(key2, count1+count2);
                    }
                }
            }
        }

        TreeSet<IntPair> corners = new TreeSet<IntPair>();

        for (i = 0; i < 4; ++i) {
            IntPair corner = find_key_of_largest_value(counts);
            corners.add(corner);
            counts.remove(corner);
        }

        for (IntPair c : corners) {
            System.out.println(c.x + ", " + c.y);
        }

        System.out.println("---Times---");
        System.out.println(processing_time - start_time);
        System.out.println(painting_time - processing_time); // time hog
        System.out.println(borders_time - painting_time);
        System.out.println(cleaning_time - borders_time);
        System.out.println(corner_time - cleaning_time);

        return corners;

        // return borders;

        /*

        int sum_x = 0;
        int sum_y = 0;
        int sum_xx = 0;
        int sum_xy = 0;
        int sum_yy = 0;
        TreeSet<IntPair> edge_points = new TreeSet<IntPair>();
        max_dist = -1;
        IntPair ray1 = new IntPair(0, 0);
        for (int X = -20; X < 20; ++X) {
            for (int Y = -20; Y < 20; ++Y) {
                if (borders[farthest_point.x+X][farthest_point.y+Y] == 2) {
                    edge_points.add(new IntPair(X, Y));
                    if (X*X+Y*Y > max_dist) {
                        max_dist = X*X+Y*Y;
                        ray1.x = X;
                        ray1.y = Y;
                    }
                }
            }
        }

        double ray_x = ray1.x;
        double ray_y = ray1.y;
        double len = Math.sqrt(ray_x*ray_x + ray_y*ray_y);
        ray_x /= len;
        ray_y /= len;

        double dist;
        max_dist = -1;
        IntPair ray2 = new IntPair(0, 0);
        for (IntPair point : edge_points) {
            dist = (1-ray_x*ray_x)*point.x*point.x + (1-ray_y*ray_y)*point.y*point.y - 2*ray_x*ray_y*point.x*point.y;
            if (dist > max_dist) {
                max_dist = dist;
                ray2.x = point.x;
                ray2.y = point.y;
            }
        }

        long ray_time = System.currentTimeMillis();

        Best_Fit_Line_Computer null_model = new Best_Fit_Line_Computer();
        Best_Fit_Line_Computer recent_model = new Best_Fit_Line_Computer();
        Best_Fit_Line_Computer old_model = new Best_Fit_Line_Computer();

        IntPairComparator foo = new IntPairComparator(farthest_point, ray1, ray2);
        PriorityQueue<IntPair> queue = new PriorityQueue<IntPair>(foo);
        TreeSet<IntPair> visited = new TreeSet<IntPair>();
        IntPair point = new IntPair(farthest_point.x + ray1.x, farthest_point.y + ray1.y);
        int counter = 0;
        while (point != null) {
            null_model.push(point.x, point.y);
            recent_model.push(point.x, point.y);
            if (counter > 20) {
                IntPair pt = recent_model.pop();
                old_model.push(pt.x, pt.y);
                if (counter > 40) {
                    LineModel null_line = null_model.get_line();
                    LineModel recent_line = recent_model.get_line();
                    LineModel old_line = old_model.get_line();
                    if (counter%20 == 0) {
                        System.out.println("---");
                        System.out.println("TURN: " + pt.x + ", " + pt.y);
                        System.out.println("NEW:  " + point.x + ", " + point.y);
                        System.out.println("NULL: " + null_line.x + ", " + null_line.y + " : " + null_line.explained_variance + ", " + null_line.total_variance);
                        System.out.println("RECE: " + recent_line.x + ", " + recent_line.y + " : " + recent_line.explained_variance + ", " + recent_line.total_variance);
                        System.out.println("OLD:  " + old_line.x + ", " + old_line.y + " : " + old_line.explained_variance + ", " + old_line.total_variance);}
                    if (counter >= 1000)
                        return borders;
                }
            }
            visited.add(point);
            queue.clear();
            if (point.x != 0) {
                IntPair left = new IntPair(point.x-1, point.y);
                System.out.println(left.x + ", " + left.y + " : " + borders[left.x][left.y]);
                if (!visited.contains(left) && borders[left.x][left.y] == 2)
                    queue.add(left);
            }
            if (point.x != w-1) {
                IntPair right = new IntPair(point.x+1, point.y);
                if (!visited.contains(right) && borders[right.x][right.y] == 2)
                    queue.add(right);
            }
            if (point.y != 0) {
                IntPair up = new IntPair(point.x, point.y-1);
                if (!visited.contains(up) && borders[up.x][up.y] == 2)
                    queue.add(up);
            }
            if (point.y != h-1) {
                IntPair down = new IntPair(point.x, point.y+1);
                if (!visited.contains(down) && borders[down.x][down.y] == 2)
                    queue.add(down);
            }
            if (point.x != 0 && point.y != 0) {
                IntPair up_left = new IntPair(point.x-1, point.y-1);
                if (!visited.contains(up_left) && borders[up_left.x][up_left.y] == 2)
                    queue.add(up_left);
            }
            if (point.x != 0 && point.y != h-1) {
                IntPair up_left = new IntPair(point.x-1, point.y+1);
                if (!visited.contains(up_left) && borders[up_left.x][up_left.y] == 2)
                    queue.add(up_left);
            }
            if (point.x != w-1 && point.y != 0) {
                IntPair up_left = new IntPair(point.x+1, point.y-1);
                if (!visited.contains(up_left) && borders[up_left.x][up_left.y] == 2)
                    queue.add(up_left);
            }
            if (point.x != w-1 && point.y != h-1) {
                IntPair up_left = new IntPair(point.x+1, point.y+1);
                if (!visited.contains(up_left) && borders[up_left.x][up_left.y] == 2)
                    queue.add(up_left);
            }
            if (queue.size() == 0)
                point = null;
            else
                point = queue.peek();

            ++counter;
        }


        return borders;
        */
    }

    private class IntPairComparator implements Comparator<IntPair>
    {
        IntPair start;
        IntPair ray1;
        IntPair ray2;
        public IntPairComparator(IntPair s, IntPair r1, IntPair r2) {
            start = s;
            ray1 = r1;
            ray2 = r2;
        }
        @Override
        public int compare(IntPair a, IntPair b)
        {
            // Assume neither string is null. Real code should
            // probably be more robust
            // You could also just return x.length() - y.length(),
            // which would be more efficient.
            double proj1 = ((a.x - start.x) * ray1.x + (a.y - start.y) * ray1.y) / Math.sqrt(ray1.x*ray1.x+ray1.y*ray1.y);
            double proj2 = Math.sqrt(a.x*a.x+a.y*a.y-proj1*proj1);
            double dist_a = proj1 + proj2;

            proj1 = ((b.x - start.x) * ray1.x + (b.y - start.y) * ray1.y) / Math.sqrt(ray1.x*ray1.x+ray1.y*ray1.y);
            proj2 = Math.sqrt(b.x*b.x+b.y*b.y-proj1*proj1);
            double dist_b = proj1 + proj2;

            if (dist_a < dist_b)
                return -1;
            else if (dist_b < dist_a)
                return 1;
            return 0;
        }
    }
}
