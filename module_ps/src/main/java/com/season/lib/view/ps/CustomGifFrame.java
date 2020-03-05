package com.season.lib.view.ps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.season.lib.bitmap.BitmapUtil;
import com.season.lib.gif.frame.GifDecoder;
import com.season.lib.gif.frame.GifFrame;
import com.season.lib.gif.movie.FrameDecoder;

import java.util.List;


/**
 * Disc: 使用解码器GifDecoder解析出每一帧，然后逐帧显示刷新
 *
 * @see GifDecoder
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-12-12 18:37
 */
public class CustomGifFrame extends CustomBaseView{
    private GifDecoder gifDecoder = null;
    private Paint mPaint;
    public String url;
    private boolean isGifEditMode;
    private float mScale = 1;
    private int mMeasuredMovieWidth;
    private int mMeasuredMovieHeight;
    private float mLeft;
    private float mTop;

    public CustomGifFrame(Context context) {
        super(context);
        init();
    }

    public CustomGifFrame(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomGifFrame(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    public void setMovieResource(int resId) {
        gifDecoder = new GifDecoder();
        gifDecoder.setGifImage(getContext().getResources(), resId);
        gifDecoder.start();
    }

    public void setFrameList(List<GifFrame> frameList) {
        gifDecoder.setFrameList(frameList);
    }

    public Bitmap firstFrame;
    public String file;

    public void setMovieResource(String strFileName) {
        if (gifDecoder != null) {
            destroy();
        }
        this.file = strFileName;
        firstFrame = new FrameDecoder(file).getFrame();
        gifDecoder = new GifDecoder();
        gifDecoder.setGifImage(strFileName);
        gifDecoder.start();
    }

    public void destroy() {
        stopDecodeThread();
        if (gifDecoder != null) {
            gifDecoder.destroy();
            gifDecoder = null;
        }
        if (firstFrame != null) BitmapUtil.recycleBitmaps(firstFrame);
    }


    @Override
    public void onRelease() {
        super.onRelease();
        destroy();
    }

    /**
     */
    private void stopDecodeThread() {
        if (gifDecoder != null && gifDecoder.getState() != Thread.State.TERMINATED) {
            gifDecoder.interrupt();
            gifDecoder.destroy();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (firstFrame != null) {
            int movieWidth = firstFrame.getWidth();
            int movieHeight = firstFrame.getHeight();
            int maximumWidth = MeasureSpec.getSize(widthMeasureSpec);
//			if (!isFullScreen){
//				maximumWidth = movieWidth;
//			}
            float scaleW = (float) movieWidth / (float) maximumWidth;
            mScale = 1f / scaleW;
            mMeasuredMovieWidth = maximumWidth;
            mMeasuredMovieHeight = (int) (movieHeight * mScale);
            setMeasuredDimension(mMeasuredMovieWidth, mMeasuredMovieHeight);
        } else {
            setMeasuredDimension(0, 0);
        }
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mLeft = (getWidth() - mMeasuredMovieWidth) / 2f;
        mTop = (getHeight() - mMeasuredMovieHeight) / 2f;
    }

    @Override
    public void drawCanvasTime(Canvas canvas, int time) {
        if (gifDecoder==null){
            return;
        }
        GifFrame gifFrame = gifDecoder.getFrame(time);
        if (gifFrame != null) {
            if (gifFrame.image != null && gifFrame.image.isRecycled() == false) {
                if (isGifEditMode){
                    canvas.save();
                    canvas.scale(mScale, mScale);
                }
                canvas.drawBitmap(gifFrame.image, mLeft / mScale, mTop / mScale, mPaint);
                if (isGifEditMode)
                    canvas.restore();
            }
        }
    }


    @Override
    public int getViewWidth() {
        try {
            return firstFrame.getWidth();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int getViewHeight() {
        try {
            return firstFrame.getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int getDuration() {
        return gifDecoder.getDuration();
    }

    @Override
    public int getDelay() {
        return gifDecoder.getDelay();
    }


    public CustomGifFrame copy() {
        CustomGifFrame gifView = new CustomGifFrame(getContext());
        if (!TextUtils.isEmpty(file)) {
            gifView.setMovieResource(file);
        } else {
        }
        gifView.url = url;
        gifView.file = file;
        return gifView;
    }

    public void setisGifEditMode(boolean isGifEditMode) {
        this.isGifEditMode = isGifEditMode;
    }
}
