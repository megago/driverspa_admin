package com.driverspa.ocr;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by Yerzhan Tanatov on 14/02/17.
 */
public class MyViewFinder extends View {

    Paint paint;
    Path path;
    Context context;
    int width,height;
    public MyViewFinder(Context context) {
        super(context);
        init();
        this.context = context;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        width = display.getWidth();  // deprecated
        height = display.getHeight();  // deprecated
    }

    public MyViewFinder(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyViewFinder(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init(){
        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(10);
        paint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);

//        Bitmap croppedBitmap = Bitmap.createBitmap(var1, var1.getWidth()-(var1.getWidth() - var1.getWidth()/6), // x
//                var1.getHeight()-((var1.getHeight()/5)*3), // y
//                var1.getWidth()- var1.getWidth()/6 - var1.getWidth()/6 , //width
//                var1.getHeight()/5, //height
//                matrix, true);
        float x = width-(width - width/3) - 50;
        float y = height-((height/2));
        float w = width- width/3 - width/3 + 100;
        float h = height/6;

        canvas.drawRect(x, //left
                        y-h/4, // top
                        x + w, // right
                        y + h/4, // bottom
                        paint);

//        canvas.drawRect(100, 100, 300, 400, paint);
        //drawRect(left, top, right, bottom, paint)

    }

}
