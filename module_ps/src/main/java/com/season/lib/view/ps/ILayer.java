package com.season.lib.view.ps;

import android.graphics.Canvas;



/**
 * Disc: 用于合成的统一操作
 *
 * 继承这个接口的类有：
 * @see CustomTextView 文字图层
 * @see CustomImageView 静图图层，包含涂鸦
 * @see CustomGifMovie gif动图图层
 * @see CustomGifFrame gif动图图层，只有在CustomGifMovie解析失败的情况下会用
 *
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-12-12 14:44
 */
public interface ILayer {
    /**
     * 获取显示的宽度
     * @return
     */
    int getViewWidth();

    /**
     * 湖区显示的高度
     * @return
     */
    int getViewHeight();

    /**
     * 获取动画开始时间点
     * @return
     */
    int getStartTime();

    /**
     * 获取动画结束时间点
     * @return
     */
    int getEndTime();

    /**
     * 设置动画开始时间点
     * @param time
     * @return
     */
    boolean setStartTime(int time);

    /**
     * 设置动画结束时间点
     * @param time
     * @return
     */
    boolean setEndTime(int time);

    /**
     * 获取动画的时长,单位毫秒
     * @return
     */
    int getDuration();

    /**
     * 获取动画每一帧的延迟
     * @return
     */
    int getDelay();

    /**
     * 合成第几帧，通过时间算出显示的是第几帧
     * @param time
     */
    void recordFrame(int time);

    /**
     * 释放内存
     */
    void onRelease();

    /**
     * 合成的时候绘制，之前是使用draw然后调用View的onDraw，由于锯齿问题，现在的合成是直接调用onDraw。
     * @param canvas
     */
    void drawCanvas(Canvas canvas);
}
