package com.example.thomas.myapplication;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Comparator;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new My_Canvas(this));
    }
}
