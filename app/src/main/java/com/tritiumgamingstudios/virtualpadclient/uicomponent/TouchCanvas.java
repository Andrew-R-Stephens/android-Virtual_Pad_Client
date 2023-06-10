package com.tritiumgamingstudios.virtualpadclient.uicomponent;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.tritiumgamingstudios.virtualpadclient.ui.main.MainViewModel;

import java.util.ArrayList;

public class TouchCanvas extends View {

    private MainViewModel viewModel;

    private float ox, oy, tx, ty;
    private Paint brush, eraser, pen;

    private ArrayList<InputPair> input = new ArrayList<InputPair>(1000);

    public TouchCanvas(Context context) {
        super(context);
    }

    public TouchCanvas(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchCanvas(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TouchCanvas(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void init(MainViewModel viewModel) {
        this.viewModel = viewModel;

        eraser = new Paint();
        eraser.setColor(Color.BLACK);
        eraser.setStrokeCap(Paint.Cap.ROUND);
        eraser.setStrokeWidth(100f);

        pen = new Paint();
        pen.setColor(Color.WHITE);
        pen.setStrokeCap(Paint.Cap.ROUND);
        pen.setStrokeWidth(5f);

        brush = pen;
    }

    public void setBrushToEraser() {
        brush = eraser;
    }

    public void setBrushToPen() {
        brush = pen;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);
        canvas.drawBitmap(viewModel.bmp, 0, 0, brush);

    }

    @Override
    protected void onSizeChanged(int width, int height,
                                 int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        // Create bitmap, create canvas with bitmap, fill canvas with color.
        viewModel.bmp = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        viewModel.canvas = new Canvas(viewModel.bmp);
        // Fill the Bitmap with the background color.
        viewModel.canvas.drawColor(Color.BLACK);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                ox = event.getX();
                oy = event.getY();

                tx = ox;
                ty = oy;

                input.add(new InputPair(ox, oy, tx, ty));

                viewModel.canvas.drawLine(ox, oy, tx, ty, brush);

                return true;
            }
            case MotionEvent.ACTION_UP: {
                // Connect the points
                ox = event.getX();
                oy = event.getY();

                ox = tx;
                oy = ty;

                input.add(new InputPair(ox, oy, tx, ty));

                break;
            }
            case MotionEvent.ACTION_MOVE: {
                // Connect the points
                tx = event.getX();
                ty = event.getY();

                input.add(new InputPair(ox, oy, tx, ty));

                viewModel.canvas.drawLine(ox, oy, tx, ty, brush);

                ox = tx;
                oy = ty;

                invalidate();
                return true;
            }
            default:
                return false;
        }
        invalidate();

        return super.onTouchEvent(event);
    }

    public boolean hasRegisteredInput() {
        return input != null && input.size() > 0;
    }

    public String getNextRegisteredInput() {
        InputPair i = input.remove(0);
        if(i != null)
            return getWidth() + "," + getHeight() + "," + 1
                    + "," + i.ox + "," + i.oy + "," + i.ex + "," + i.ey
                    + "," + brush.getStrokeWidth() + "," + brush.getColor();
        else
            return null;
    }

    private static class InputPair {
        public int ox, oy, ex, ey;

        public InputPair(float ox, float oy, float ex, float ey){
            this.ox = (int)ox;
            this.oy = (int)oy;
            this.ex = (int)ex;
            this.ey = (int)ey;
        }
    }
}