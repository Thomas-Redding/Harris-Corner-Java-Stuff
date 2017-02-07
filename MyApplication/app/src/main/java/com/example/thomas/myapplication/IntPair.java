package com.example.thomas.myapplication;

/**
 * Created by thomas on 2/7/17.
 */

public class IntPair implements Comparable<IntPair> {
    int x;
    int y;
    public IntPair(int x, int y) {
        super();
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof IntPair) && ((IntPair) o).x == this.x && ((IntPair) o).y == this.y;
    }

    @Override
    public int hashCode() {
        return this.x * 10000000 + this.y;
    }

    @Override
    public int compareTo(IntPair other) {
        return (1000000 * x + y) - (1000000 * other.x + other.y);
    }
}