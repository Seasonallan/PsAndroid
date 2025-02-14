package com.season.ps.animation;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Disc: 文字动效 模型伪抽象工厂类
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-12-12 14:44
 */
public class AnimationProvider {


    /**
     * 显示的顺序， 可调整,"摇头晃脑","颜色闪变"被隐藏了
     */
    public static final String[] strsTextShow = {
            "无状态", "突然闪现", "从天而降", "底部升起","上下颠簸", "左右晃动", "放大缩小", "波浪跳动",
            "逐字放大", "逐字跳动","排队出现", "排队登场", "单行播放1", "单行播放2", "摇头晃脑", "颜色闪变"};

    /**
     * 标注AnimationProvider要使用的动画顺序，用于switch中的位置判断，不可调整,
     */
    public static final String[] strsText = {
            "无状态", "底部升起", "放大缩小", "波浪跳动",
            "上下颠簸", "突然闪现", "逐字放大", "逐字跳动",
            "排队出现", "排队登场", "摇头晃脑","颜色闪变",
            "从天而降", "左右晃动", "单行播放1", "单行播放2"};

    public static AnimationProvider getProvider(int type) {
        if (type < 0 || type >= strsText.length) {
            return null;
        }
        String clickText = strsTextShow[type];
        int position = 0;
        for (int i = 0; i < strsText.length; i++) {
            if (strsText[i].equals(clickText)) {
                position = i;
                break;
            }
        }
        switch (position) {
            case 0:
                break;
            case 1:
                return new TranslateShowUpProvider();
            case 2:
                return new ScaleProvider();
            case 3:
                return new WaveProvider();
            case 4:
                return new TranslateUpDownProvider();
            case 5:
                return new FlushProvider();
            case 6:
                return new LineUpScaleProvider();
            case 7:
                return new WaveOneProvider();
            case 8:
                return new LineUpShowProvider();
            case 9:
                return new LineUpScaleAlphaProvider();
            case 10:
                return new RotateProvider();
            case 11:
                return new ColorProvider();
            case 12:
                return new TranslateShowDownProvider();
            case 13:
                return new TranslateLRProvider();
            case 14:
                return new SingleLineProvider();
            case 15:
                return new SingleLineScaleProvider();
        }
        return null;
    }

    /**
     * 文字的颜色，暂时废弃
     *
     * @return
     */
    public int getColor() {
        return -1;
    }

    /**
     * 文字的透明度
     *
     * @return
     */
    public int getAlpha() {
        return -1;
    }

    Paint paintColor;

    public Paint getPaint(Paint paint) {
        if (getColor() != -1 || getAlpha() != -1) {
            if (paintColor == null) {
                paintAlpha = paint.getAlpha();
                paintColor = new Paint(paint);
            }
            if (getColor() != -1) {
                paintColor.setShader(null);
                paintColor.setColor(getColor());
            }
            if (getAlpha() != -1) {
                paintColor.setAlpha((int) (getAlpha() * paintAlpha * 1.0f / 255));
            }
            return paintColor;

        }
        return paint;
    }

    Paint paintStrokeColor;

    public Paint getStrokePaint(Paint paint) {
        if (getAlpha() != -1) {
            if (paintStrokeColor == null) {
                strokePaintAlpha = paint.getAlpha();
                paintStrokeColor = new Paint(paint);
            }
            if (getAlpha() != -1) {
                paintStrokeColor.setAlpha((int) (getAlpha() * strokePaintAlpha * 1.0f / 255));
            }
            return paintStrokeColor;

        }
        return paint;
    }

    int paintAlpha = 255;
    int strokePaintAlpha = 255;

    public void resetPaint(Paint paint, Paint strokePaint) {
        paintAlpha = paint.getAlpha();
        strokePaintAlpha = strokePaint.getAlpha();
        if (paintColor != null) {
            paintColor = new Paint(paint);
        }
        if (paintStrokeColor != null) {
            paintStrokeColor = new Paint(strokePaint);
        }
    }

    /**
     * 用于校验数据，保证文字动画时间跟视频时间成整数倍，以达到合成的时候完美连接
     */
    public void init() {
    }



    protected int delayDefault = 10;//默认的帧延迟
    protected int durationDefault = 1500;//默认的帧延迟

    protected float transMax = 24;
    protected int stayTime = 500;//动效最后保留时间
    protected int position;

    /**
     * 获取动效每一帧之间的延迟
     *
     * @return
     */
    public int getDelay() {
        return delayDefault;
    }

    /**
     * 获取动效显示一次的时长
     *
     * @return
     */
    public int getDuration() {
        return durationDefault;
    }

    public String getClassName() {
        return "";
    }


    /**
     * 设置当前动画的位置
     *
     * @param position
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * 画布操作
     *
     * @param canvas
     * @param centerX
     * @param centerY
     */
    public void preCanvas(Canvas canvas, int centerX, int centerY) {

    }

    /**
     * 画布重置
     *
     * @param canvas
     */
    public void proCanvas(Canvas canvas) {
        canvas.restore();
    }


    /**
     * 设置当前动画的时间，计算当前动效的数据坐标
     *
     * @param time   当前时间
     * @param record 是否是合成GIF
     * @return 显示的文字长度，0表示不显示文字
     */
    public int setTime(int time, boolean record) {
        return totalSize;
    }

    /**
     * 每个字有不同的动画
     *
     * @return
     */
    public boolean isWordSplit() {
        return false;
    }

    /**
     * 每一行有不同的动画
     *
     * @return
     */
    public boolean isRowSplited() {
        return false;
    }

    protected int rowCount = 1;
    public void setRowCount(int count) {
        if (count <= 0){
            count = 1;
        }
        this.rowCount = count;
    }

    public int getRowCount(){
        return rowCount;
    }

    public int getPerRowTime() {
        return getDuration()/rowCount;
    }

    //配置动画信息
    protected int totalTime = 3000;
    protected int totalSize = 3;
    /**
     * 设置文字数量
     *
     * @param size
     */
    public void setTextCount(int size) {
        this.totalSize = size;
    }

    protected int textWidth;
    protected int textHeight;

    /**
     * 设置文字宽高
     *
     * @param textWidth
     * @param textHeight
     */
    public void setTextWidthHeight(int textWidth, int textHeight) {
        this.textWidth = textWidth;
        this.textHeight = textHeight;
    }

    /**
     * 动效是否要重复
     *
     * @return
     */
    public boolean isRepeat() {
        return true;
    }

    public int getStayTime() {
        return getDuration();
    }
}
