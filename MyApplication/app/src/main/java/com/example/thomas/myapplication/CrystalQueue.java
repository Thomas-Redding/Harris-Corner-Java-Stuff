package com.example.thomas.myapplication;

/**
 * Created by Sabastian on 2/9/17.
 */

public class CrystalQueue<T>  {

    private int[] x_arr, y_arr;

    private int total, first, next;

    public CrystalQueue()
    {
        x_arr = new int[2];
        y_arr = new int[2];
    }

    private void resize(int capacity)
    {
        int[] x_tmp = new int[capacity];
        int[] y_tmp = new int[capacity];

        for (int i = 0; i < total; i++) {
            x_tmp[i] = x_arr[(first + i) % x_arr.length];
            y_tmp[i] = y_arr[(first + i) % x_arr.length];
        }

        x_arr = x_tmp;
        y_arr = y_tmp;
        first = 0;
        next = total;
    }

    //enque a pair of x and y coodinates
    public int enqueue(IntPair elements)
    {
        if (x_arr.length == total) {
            resize(x_arr.length * 2);
            resize(y_arr.length * 2);

        }
        x_arr[next++] = elements.x;
        x_arr[next++] = elements.y;

        if (next == x_arr.length) next = 0;
        total++;
        return total;
    }

    //returns a pair of x,y coodinates
    public IntPair dequeue()
    {
        if (total == 0) throw new java.util.NoSuchElementException();

        IntPair pop = new IntPair(x_arr[first],y_arr[first]);
//        x_arr[first] = null;
//        y_arr[first] = null;

        if (++first == x_arr.length) first = 0;

        if (--total > 0 && total == x_arr.length / 4) {
            resize(x_arr.length / 2);
            resize(y_arr.length / 2);
        }
        return pop;
    }

}
