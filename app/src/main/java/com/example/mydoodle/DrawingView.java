package com.example.mydoodle;



import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class DrawingView extends View {

    private Paint currentPaint;
    private Path currentPath;
    private List<ColoredPath> paths;

    // Clear area-related variables
    private boolean isClearingArea = false;
    private float startX, startY, endX, endY;

    // Custom class to store a path along with its paint (color, size, opacity)
    private class ColoredPath {
        Path path;
        Paint paint;

        ColoredPath(Path path, Paint paint) {
            this.path = path;
            this.paint = paint;
        }
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paths = new ArrayList<>();

        currentPaint = new Paint();
        currentPaint.setColor(Color.BLACK);
        currentPaint.setAntiAlias(true);
        currentPaint.setStrokeWidth(10);
        currentPaint.setStyle(Paint.Style.STROKE);
        currentPaint.setStrokeJoin(Paint.Join.ROUND);
        currentPaint.setAlpha(255); // Default opacity
        currentPath = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Set the background color
        canvas.drawColor(Color.WHITE);

        // Draw all previous paths
        for (ColoredPath coloredPath : paths) {
            canvas.drawPath(coloredPath.path, coloredPath.paint);
        }

        // Draw current path
        if (currentPath != null) {
            canvas.drawPath(currentPath, currentPaint);
        }

        // Draw the selection rectangle if in clearing mode
        if (isClearingArea) {
            Paint clearRectPaint = new Paint();
            clearRectPaint.setStyle(Paint.Style.STROKE);
            clearRectPaint.setColor(Color.RED);
            clearRectPaint.setStrokeWidth(5);
            canvas.drawRect(startX, startY, endX, endY, clearRectPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        if (isClearingArea) {
            handleClearAreaTouchEvent(event);
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentPath.moveTo(x, y);
                return true;
            case MotionEvent.ACTION_MOVE:
                currentPath.lineTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
                paths.add(new ColoredPath(currentPath, new Paint(currentPaint)));
                currentPath = new Path();
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    public void setColor(int color) {
        currentPaint.setColor(color);
    }

    public void setBrushSize(float size) {
        currentPaint.setStrokeWidth(size);
    }

    public void setOpacity(int opacity) {
        currentPaint.setAlpha(opacity);
    }

    public void clearCanvas() {
        paths.clear();
        currentPath.reset();
        invalidate();
    }

    public void startClearingArea() {
        isClearingArea = true;
    }

    private void handleClearAreaTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                endX = startX;
                endY = startY;
                break;
            case MotionEvent.ACTION_MOVE:
                endX = event.getX();
                endY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                clearSpecificArea();
                isClearingArea = false;
                break;
        }

        invalidate();
    }

    private void clearSpecificArea() {
        RectF rect = new RectF(Math.min(startX, endX), Math.min(startY, endY), Math.max(startX, endX), Math.max(startY, endY));
        List<ColoredPath> remainingPaths = new ArrayList<>();

        for (ColoredPath coloredPath : paths) {
            Path path = coloredPath.path;
            RectF bounds = new RectF();
            path.computeBounds(bounds, true);

            if (!RectF.intersects(bounds, rect)) {
                remainingPaths.add(coloredPath);
            }
        }

        paths = remainingPaths;
        invalidate();
    }
}
