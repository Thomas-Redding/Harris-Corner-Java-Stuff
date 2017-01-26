package com.example.thomas.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by thomas on 1/25/17.
 */

public class My_Canvas extends View {
    private Paint paint;
    Bitmap imageMap;
    public My_Canvas(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        paint = new Paint();
        imageMap = BitmapFactory.decodeResource( getResources() , R.drawable.smallishfake);
        imageMap = getResizedBitmap(imageMap, 500, 500);

        HarrisCornerFinder hcf = new HarrisCornerFinder();
        ArrayList<HarrisCornerFinder.HarrisCorner> lst = hcf.findCorners(imageMap);
    }

    // http://stackoverflow.com/questions/4837715/how-to-resize-a-bitmap-in-android
    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        // Use Color.parseColor to define HTML colors
        paint.setColor(Color.parseColor("#FF0000"));
        canvas.drawCircle(80, 80, 40, paint);
//        imageMap.draw(canvas);
        if (imageMap != null)
            canvas.drawBitmap(imageMap, 0, 0, paint);
        else
            System.out.println("nope");

        // Drawable d = getResources().getDrawable(R.drawable.foobar);
//        d.setBounds(0, 0, 100, 100);
//        d.draw(canvas);
    }
}
