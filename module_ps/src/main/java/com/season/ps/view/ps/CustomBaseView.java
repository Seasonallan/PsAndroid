package com.season.ps.view.ps;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * 图层基类
 */
public abstract class CustomBaseView extends View implements ILayer {

    private long mMovieStart;
    private int startTime = Integer.MIN_VALUE;
    private int endTime = Integer.MAX_VALUE;
    protected boolean autoPlay = false;
    private int currentTime = 0;
    private int maxTime = 0;

    public CustomBaseView(Context context) {
        super(context);
    }

    public CustomBaseView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomBaseView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void recordFrame(int time, int maxTime) {
        this.currentTime = time;
        this.maxTime = maxTime;
    }

    public void setAutoPlay(){
        autoPlay = true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (autoPlay){
            long now = System.currentTimeMillis();
            if (mMovieStart == 0) {
                mMovieStart = now;
            }
            int dur = getDuration();
            if (dur == 0) {
                dur = 3000;
            }
            currentTime = (int) ((now - mMovieStart) % dur);
            drawCanvasTime(canvas, currentTime);
            invalidate();
        }else{
            drawCanvas(canvas);
        }
    }

    @Override
    public void drawCanvas(Canvas canvas) {
        if (currentTime >= getStartTime() && currentTime <= getEndTime()) {
            canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
            int time = currentTime - getStartTime();
            int duration = getDuration();
            if (duration > 0 && time > duration){
                if (maxTime - (time/duration)*duration < duration){
                    time = getStayTime();
                }else{
                    time = time % duration;
                }
                if (isRepeat()){
                   // time = time % duration;
                }else{
                  //  time = getStayTime();
                }
            }
            drawCanvasTime(canvas, time);
        }
    }

    @Override
    public int getStayTime() {
        return getDuration();
    }


    @Override
    public boolean isRepeat(){
        return true;
    }

    /**
     * 绘制某个时间点的画布
     * @param canvas
     * @param time
     */
    public abstract void drawCanvasTime(Canvas canvas, int time);

    @Override
    public void onRelease() {
        autoPlay = false;
    }

    @Override
    public int getStartTime() {
        if (startTime == Integer.MIN_VALUE){
            return 0;
        }
        return startTime;
    }

    @Override
    public int getEndTime() {
        if (endTime == Integer.MAX_VALUE){
            return getDuration();
        }
        return endTime;
    }


    @Override
    public boolean setStartTime(int time) {
        startTime = time;
        return true;
    }

    @Override
    public boolean setEndTime(int time) {
        endTime = time;
        return true;
    }

}
