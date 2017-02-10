package com.example.thomas.myapplication;

/**
 * Created by Sabastian on 2/9/17.
 */

public class CrystalQueue<T>  {

    private T[] x_arr, y_arr;

    private int total, first, next;

    public CrystalQueue()
    {
        x_arr = (T[]) new Object[2];
        y_arr = (T[]) new Object[2];
    }

    private void resize(int capacity)
    {
        T[] x_tmp = (T[]) new Object[capacity];
        T[] y_tmp = (T[]) new Object[capacity];

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
    public int enqueue(T[] elements)
    {
        if (x_arr.length == total) {
            resize(x_arr.length * 2);
            resize(y_arr.length * 2);

        }
        x_arr[next++] = elements[0];
        x_arr[next++] = elements[1];

        if (next == x_arr.length) next = 0;
        total++;
        return total;
    }

    //returns a pair of x,y coodinates
    public T[] dequeue()
    {
        if (total == 0) throw new java.util.NoSuchElementException();
        T[] pop = (T[]) new Object[2];

        pop[0] = x_arr[first];
        pop[0] = y_arr[first];
        x_arr[first] = null;
        y_arr[first] = null;

        if (++first == x_arr.length) first = 0;

        if (--total > 0 && total == x_arr.length / 4) {
            resize(x_arr.length / 2);
            resize(y_arr.length / 2);
        }
        return pop;
    }

}
