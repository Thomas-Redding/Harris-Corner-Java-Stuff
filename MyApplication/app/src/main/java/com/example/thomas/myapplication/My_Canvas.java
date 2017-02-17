package com.example.thomas.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.View;

import java.util.Set;

/**
 * Created by thomas on 1/25/17.
 */

public class My_Canvas extends View {
    private Paint paint;
    Bitmap imageMap;
    // HarrisCornerFinder.Array2D_List_Pair corner_info;
    // TableCornerFinder.IntPair[] table_corners;
    Set<IntPair> stuff;
    public My_Canvas(Context context) {
        super(context);
        paint = new Paint();
        imageMap = BitmapFactory.decodeResource( getResources() , R.drawable.horizontal);
        // imageMap = BitmapFactory.decodeResource( getResources() , R.drawable.vertical);
        // imageMap = BitmapFactory.decodeResource( getResources() , R.drawable.angleda);
        // imageMap = BitmapFactory.decodeResource( getResources() , R.drawable.angledb);
        imageMap = getResizedBitmap(imageMap, 800, 410);
        // HarrisCornerFinder hcf = new HarrisCornerFinder();
        // corner_info = hcf.findCorners(imageMap);
        // TableCornerFinder tcf = new TableCornerFinder();
        // table_corners = tcf.findTableCorners(imageMap, corner_info.lst, corner_info.arr);
        // System.out.println("MID");
        PaintMethod sm = new PaintMethod();
        long start = System.currentTimeMillis();
        stuff = sm.findCorners(imageMap);
        long end = System.currentTimeMillis();
        System.out.println("time: " + (end - start) + " ms");
    }

    // http://stackoverflow.com/questions/4837715/how-to-resize-a-bitmap-in-android
    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        if (imageMap != null)
            canvas.drawBitmap(imageMap, 0, 0, paint);
        else
            System.out.println("nope");

        paint.setColor(Color.parseColor("#FF0000"));
        for (IntPair pt : stuff) {
            canvas.drawCircle(pt.x, pt.y, 3, paint);
        }

        /*
        paint.setColor(Color.parseColor("#FF0000"));
        for (int i = 0; i < corner_info.lst.size(); ++i) {
            canvas.drawCircle(corner_info.lst.get(i).x, corner_info.lst.get(i).y, (float) Math.sqrt(corner_info.lst.get(i).n), paint);
        }

        paint.setColor(Color.parseColor("#0000FF"));
        for (int i = 0; i < table_corners.length; ++i) {
            canvas.drawCircle(table_corners[i].x, table_corners[i].y, 5, paint);
        }
        */
    }
}
