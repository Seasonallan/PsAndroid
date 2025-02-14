/*
 *          Copyright (C) 2016 jarlen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.season.ps.view.ps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ViewConfiguration;
import android.view.ViewParent;

import androidx.annotation.Nullable;

import com.season.ps.bean.LayerItem;
import com.season.lib.support.bitmap.BitmapUtil;
import com.season.lib.support.dimen.ColorUtil;
import com.season.ps.animation.AnimationProvider;
import com.season.lib.support.file.FileManager;
import com.season.lib.support.dimen.ScreenUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.github.rockerhieu.emojicon.EmojiconHandler;

/**
 * Disc: 文字图层，流程为1、measure>> text改变后getEmojis获取到每个字或表情的位置后calculateWidthHeight()获取宽高 后
 * 2、layout >> 用于控制添加文字的位置和文字改变后的位置矫正(通过位移达到中心点不变)
 * 3  draw   >> 绘制每个字或表情，有动画的时候进行画布矩阵调整
 * 注：动效的刷新原理都是外部ContainerView进行的统一刷新，线程循环调用invalidate，然后执行的TextStyleView的onDraw，
 * TextStyleView通过记录开始时间和当前onDraw时间获取每个字的位置进行绘制，达成动画效果
 * 所以TextStyleView单独提出去用的时候动效无法使用，需要配合ContainerView刷新或者自己在TextStyleView写刷新线程
 * User: SeasonAllan(451360508@qq.com)
 * Time: 2017-12-12 14:44
 */
public class CustomTextView extends CustomBaseView{
    private int offsetY;//由于Android系统的drawText中的y无法确定，这个值是调节的比较居中的值
    private boolean nullInput = false;
    public boolean isAudio = false;
    //背景图片
    private int backgroudRes = 0;
    private int preBackgroundInfo;
    private Bitmap backgroundBitmap;

    private String text;
    private Context context;
    public Paint paint = new Paint();
    public Paint strokepaint = new Paint();
    public String fontName = "";
    private int paddingLeft, paddingTop, textSpacing, lineSpacing;
    private int emojiWidth = 100;

    private String startColorStr;
    private String endColorStr;
    private float defaul_textColorSize=0.1f;

    public CustomTextView copy() {
        CustomTextView customTextView = new CustomTextView(context);
        customTextView.paint = new Paint();
        customTextView.paint.set(paint);
        customTextView.strokepaint = new Paint();
        customTextView.strokepaint.set(strokepaint);
        customTextView.backgroudRes = backgroudRes;
        customTextView.fontName = fontName;
        customTextView.text = text;
        customTextView.startColorStr=startColorStr;
        customTextView.endColorStr=endColorStr;
        customTextView.fixEmoji();
        customTextView.calculateWidthHeight();
        customTextView.setTextAnimationType(currentType);
        customTextView.resetAnimationPaint();
        customTextView.addEvent();
        return customTextView;
    }


    public boolean setTextEntry(LayerItem item, float width) {
        int opViewWidth = ScreenUtils.getScreenWidth();
        this.text = item.getText();
        fixEmoji();
        this.fontName = item.getTextFontName();

        paint.setTypeface(getTypeface(Typeface.DEFAULT));
        strokepaint.setTypeface(getTypeface(Typeface.DEFAULT));


        float maxLength = (float) (item.getSizeWidth() * item.getXScale() * opViewWidth / width);
        calculateWidthHeight();

        item.setXScale((maxLength / finalWidth) * width / opViewWidth);
        item.setYScale((maxLength / finalWidth) * width / opViewWidth);
        item.setSizeWidth(finalWidth);
        item.setSizeHeight(finalHeight);

        addEvent();
        return true;
    }


    /**
     * 在设置字体粗细的时候，描边的粗细要跟着变
     * 最小值
     */
    public boolean setPaintWidthByPercent(float paintwidthPercent) {
        float paintSizeParams = paintwidthPercent / 100;
        float oriParam = getPaintStrokeWidthParam();
        float newPaintWidth = getPaintStrokeWidth(paintSizeParams);
        float pw = paint.getStrokeWidth();
        if (pw == newPaintWidth) {
            return false;
        }
        paint.setStrokeWidth(newPaintWidth);
        strokepaint.setStrokeWidth(getStrokePaintStrokeWidth(paintSizeParams, getStrokePaintStrokeWidthParam(oriParam)));
        resetAnimationPaint();
        invalidate();
        return false;
    }

    public boolean setStrokeWidthByPercent(float strokeWidthPercent) {
        float newParams = strokeWidthPercent / 100;
        float paintParams = getPaintStrokeWidthParam();
        float oldParams = getStrokePaintStrokeWidthParam(paintParams);
        if (newParams == oldParams) {
            return false;
        }
        strokepaint.setStrokeWidth(getStrokePaintStrokeWidth(paintParams, newParams));
        resetAnimationPaint();
        invalidate();
        return false;
    }


    //外部描边百分比得到描边宽度
    private float getStrokePaintStrokeWidth(double paintSizeParams, double strokePaintSizeParams) {
        return (float) (strokePaintSizeParams * ToolPaint.getDefault().getPaintWidth() + getPaintStrokeWidth(paintSizeParams));
    }

    //描边宽度得到外部描边百分比
    public float getStrokePaintStrokeWidthParam(float paintSizeParams) {
        return (strokepaint.getStrokeWidth() - getPaintStrokeWidth(paintSizeParams)) / ToolPaint.getDefault().getPaintWidth();
    }

    //内部描边百分比得到描边宽度
    private float getPaintStrokeWidth(double params) {
        return (float) (params * ToolPaint.getDefault().getStrokeWidth());
    }

    //描边宽度得到内部描边百分比
    public float getPaintStrokeWidthParam() {
        return paint.getStrokeWidth() / ToolPaint.getDefault().getStrokeWidth();
    }


    public CustomTextView(Context context) {
        super(context);
        init(context);
    }

    public CustomTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    long drawingCacheSize;

    public void init(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//            this.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            this.setLayerType(LAYER_TYPE_SOFTWARE, null);
        }

        drawingCacheSize = ViewConfiguration.get(context).getScaledMaximumDrawingCacheSize();
        textSpacing = 4;
        lineSpacing = 16;
        paddingLeft =24;
        paddingTop = 24;
        offsetY = 10;

        this.context = context;
        paint.setDither(true);//防抖
        paint.setAntiAlias(true);
        paint.setTypeface(getTypeface(Typeface.DEFAULT));
        paint.setColor(Color.WHITE);

        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(getPaintStrokeWidth(defaul_textColorSize));
        paint.setAlpha(255);
        paint.setShader(null);
        //文字居中写,因为现在是的canva直接在具体的x,y坐标下点，进行居中绘制
        paint.setTextAlign(Paint.Align.CENTER);
        strokepaint.setTextAlign(Paint.Align.CENTER);

        strokepaint.setDither(true);
        strokepaint.setAntiAlias(true);
        strokepaint.setTypeface(getTypeface(Typeface.DEFAULT));
        strokepaint.setColor(Color.BLACK);

        strokepaint.setStyle(Paint.Style.FILL_AND_STROKE);
        strokepaint.setStrokeWidth(getStrokePaintStrokeWidth(0.2, 0.4));
        strokepaint.setAlpha(255);
        paint.setShader(null);

        //当前做法是字体尽量设计大一点，然后通过scaleView对其进行缩小，以达成放大文字不会锯齿的效果
        paint.setTextSize(ToolPaint.getDefault().getPaintSize());
        strokepaint.setTextSize(ToolPaint.getDefault().getPaintSize());
    }

    //获取最大放大倍数，超过的话文字会显示锯齿
    public float getMaxScale() {
        if (paint == null) {
            paint = new Paint();
            paint.setTextSize(ToolPaint.getDefault().getPaintSize());
        }
        int width = (int) paint.measureText("情");
        return ScreenUtils.getScreenWidth() / 2 * 1.0f / width;
    }

    private int finalWidth = 0, finalHeight = 0;
    int row = 0;
    int xoffsetCenter = 0;

    /**
     * 计算每个字的长度，最后算出要显示的宽高
     */
    //为了做行动画，我们先可以在这里确定每一个字符的行数。
    //先确认"\n"字符是否占位置，我们先标记出换行符的位置，然后判断位置，确定每一个字符是第几行
    private void calculateWidthHeight() {
        if (textEmojiList == null) {
            setMeasuredDimension(0, 0);
            return;
        }
        emojiWidth = (int) paint.measureText("情");
        finalWidth = 0;
        row = 0;//emoji的行号，从0开始
        int offsetX = paddingLeft;
        //******为了让文字居中start
        xoffsetCenter = 0;
        String longestRow = "";
        //int longestCount = 0;
        int widest = 0;
        int currentRowleng = 0;
        int offset = 0;
        //外部判断了,text一定不为null
        String[] split = text.split("\n");
        if (split != null && split.length > 0) {
            for (int i = 0; i < split.length; i++) {
                if (!TextUtils.isEmpty(split[i])) {
                    //emoji算一个字符
                    List<EmojiconHandler.TextEmoji> longestList = EmojiconHandler.getEmojis(context, longestRow);
                    List<EmojiconHandler.TextEmoji> currentlist = EmojiconHandler.getEmojis(context, split[i]);
                    if (longestList.size() < currentlist.size()) {
                        longestRow = split[i];
                    }
                }
            }
            //数字和文字的大小不同
//           widest = longestRow.length() * (emojiWidth + textSpacing);//固定
            widest = (int) paint.measureText(longestRow) + 1;//固定
            if (longestRow.length() > 0) {
                widest += (longestRow.length() - 1) * textSpacing;
            }
            String currentrow = split[row];
            if (!TextUtils.isEmpty(currentrow)) {
//                currentRowleng = currentrow.length() * (emojiWidth + textSpacing);
                currentRowleng = (int) paint.measureText(currentrow);
                if (currentrow.length() > 0) {
                    offset = (currentrow.length() - 1) * textSpacing;
                    currentRowleng += offset;
                }
                if (widest >= currentRowleng) {
                    xoffsetCenter = (widest - currentRowleng) / 2;
                    offsetX += xoffsetCenter;//校正居中偏移量
                }
            }
        }
        finalHeight = paddingTop;
        int size = textEmojiList.size();
        for (int i = 0; i < size; i++) {
            EmojiconHandler.TextEmoji emoji = textEmojiList.get(i);
            if (emoji.icon != null) {
                //emoji
                emoji.row = row;//设置行
                emoji.offsetX = offsetX;
                emoji.offsetY = finalHeight;
                emoji.setSize(emojiWidth);
                offsetX += (emoji.width + textSpacing);//x坐标加一个字符宽度和文件间隙
            } else {
                //文字
                String itemText = emoji.text;
                if (itemText.equals("\n")) {
                    //加一个判断 if (split.length <= row)
                    //如果最后一行是空换行，可能就下标越界了，for example:啊哈哈\n哈哈\n 实际上我们拆分==>用"\n"来拆分{啊哈哈,n哈哈}
                    row++;
                    if (split.length - 1 >= row) {
                        finalWidth = Math.max(finalWidth, offsetX);
                        offsetX = paddingLeft;//换行的时候重置一下offsetX
                        //这里做换行
                        if (animationProvider == null || !animationProvider.isRowSplited()) {
                            //如果没有动画或者不是单行显示
                            finalHeight += (emojiWidth + lineSpacing);
                        }
                        String currentrow = split[row];
                        if (!TextUtils.isEmpty(currentrow)) {
                            currentRowleng = (int) paint.measureText(currentrow);
                            if (currentrow.length() > 0) {
                                offset = (currentrow.length() - 1) * textSpacing;
                                currentRowleng += offset;
                            }
                            if (widest >= currentRowleng) {
                                xoffsetCenter = (widest - currentRowleng) / 2;
                                offsetX += xoffsetCenter;//校正居中偏移量
                            }
                        }
                    }
                } else {
                    emoji.row = row;//设置行
                    int fontTotalWidth = (int) paint.measureText(itemText);
                    emoji.offsetX = offsetX;
                    emoji.offsetY = finalHeight;
                    emoji.fontTotalWidth = (int) paint.measureText(itemText);
                    emoji.fontTotalHeight = emojiWidth;
                    offsetX += fontTotalWidth + textSpacing;//x坐标加一个字符宽度和文件间隙
                }
            }
            emoji.ready = true;
        }
        finalHeight += (emojiWidth + lineSpacing);
        finalWidth = Math.max(finalWidth, offsetX);
        finalWidth += paddingLeft;
        finalHeight += paddingTop;
        resetAnimationPaint();

        ViewParent parent = getParent();
        if (parent != null && parent instanceof PSLayer) {
            ((PSLayer) parent).disableHardWareWhenText2Long(this);
        }
    }

    //重置动画画笔，用于带有透明度的动画
    public void resetAnimationPaint() {
        if (animationProvider != null) {
            animationProvider.resetPaint(paint, strokepaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(finalWidth, finalHeight);
    }


    public boolean resetPosition = false; // 由于初始位置没有确定，不矫正位置
    int preWidth, preHeight;

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (getParent() instanceof PSLayer) {
            int width = right - left;
            int height = bottom - top;
            if (preWidth == width && preHeight == height) {
                ((PSLayer) getParent()).rebindOpView();
                return;
            }
            preWidth = width;
            preHeight = height;
            if (width > 0 && height > 0) {
                if (nullInput || isAudio) {
                    resetPosition = true;
                    int offsetY = 0;
                    float scale = ((PSLayer) getParent()).showBottomCenter(width, height, offsetY, isAudio ? getText() : null);
                    ((PSLayer) getParent()).rebindOpView(height, scale);
                    isAudio = false;
                } else {
                    float[] offset = ((PSLayer) getParent()).rebindOpView();
                    if (offset != null && offset.length == 2) {
                        if (offset[0] == 0 && offset[1] == 0) {
                        } else {
                            //文字长度变化的时候，对位置进行矫正
                            if (resetPosition) {
                                ((PSLayer) getParent()).changeOffset(getText(), offset[0], offset[1]);
                            }
                            resetPosition = true;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void drawCanvasTime(Canvas canvas, int time) {
        if (textEmojiList == null || textEmojiList.size() == 0) {
            return;
        }
        drawBackground(canvas);
        if (animationProvider == null) {
            //没有动画绘制文字
            drawText(canvas, textEmojiList.size(), paint, strokepaint, startColorStr, endColorStr, lineSpacing);
        } else {
            if (animationProvider.isRowSplited()) {
                //设置行号
                if (!TextUtils.isEmpty(text)) {
                    int drawTextRow;
                    int perRowTime = animationProvider.getPerRowTime();
                    drawTextRow = (time / perRowTime);
                    if (drawTextRow > animationProvider.getRowCount() - 1){
                        drawTextRow = animationProvider.getRowCount() - 1;
                    }
                    for (EmojiconHandler.TextEmoji emoji : textEmojiList) {
                        if (emoji.row == drawTextRow) {
                            animationProvider.setTime(time, true);
                            animationProvider.preCanvas(canvas, emoji.offsetX + emojiWidth / 2, emoji.offsetY + emojiWidth / 2);
                            emoji.onDraw(canvas, animationProvider.getPaint(paint), animationProvider.getStrokePaint(strokepaint),
                                    offsetY, startColorStr, endColorStr, lineSpacing);
                            animationProvider.proCanvas(canvas);
                        }
                    }
                }
            } else {
                if (animationProvider.isWordSplit()) {//文字如果每个字的动画不一样的话，需要对每个字进行动画
                    int i = 0;
                    for (EmojiconHandler.TextEmoji emoji : textEmojiList) {
                        i++;
                        animationProvider.setPosition(i - 1);//设置positon,进而影响setTime（）产生的效果。来达到对每个字动画的控制。
                        int drawTextCount = animationProvider.setTime(time, true);
                        if (i > drawTextCount) {
                            break;
                        }
                        animationProvider.preCanvas(canvas, emoji.offsetX + emojiWidth / 2, emoji.offsetY + emojiWidth / 2);
                        emoji.onDraw(canvas, animationProvider.getPaint(paint), animationProvider.getStrokePaint(strokepaint),
                                offsetY, startColorStr, endColorStr, lineSpacing);

                        animationProvider.proCanvas(canvas);
                    }
                } else {
                    //文字动画统一处理
                    int showTextCount = animationProvider.setTime(time, true);
                    animationProvider.preCanvas(canvas, getViewWidth() / 2, getViewHeight() / 2);
                    drawText(canvas, showTextCount, animationProvider.getPaint(paint), animationProvider.getStrokePaint(strokepaint)
                            , startColorStr, endColorStr, lineSpacing);
                    animationProvider.proCanvas(canvas);
                }
            }
        }
    }

    void drawText(Canvas canvas, int drawTextCount, Paint paint, Paint strokePaint, String startColorStr, String endColorStr, int
            lineSpacing) {
        int i = 0;
        for (EmojiconHandler.TextEmoji emoji : textEmojiList) {
            i++;
            if (i > drawTextCount) {
                break;
            }
            emoji.onDraw(canvas, paint, strokePaint, offsetY, startColorStr, endColorStr, lineSpacing);
        }
    }


    void drawBackground(Canvas canvas) {
        if (backgroudRes != 0) {
            if (preBackgroundInfo != backgroudRes) {
                BitmapUtil.recycleBitmaps(backgroundBitmap);
                backgroundBitmap = BitmapFactory.decodeResource(getResources(), backgroudRes);
            }
            if (backgroundBitmap != null && !backgroundBitmap.isRecycled()) {
                canvas.drawBitmap(backgroundBitmap, null, new Rect(0, 0, getWidth(), getHeight()), paint);
            }
        }
        preBackgroundInfo = backgroudRes;
    }

    public AnimationProvider animationProvider;
    public int currentType = 0;

    public boolean setTextAnimationType(int type) {
        if (currentType == type) {
            return false;
        }
        setTextAnimationType(type,true);
        return true;
    }

    private void setTextAnimationType(int type, boolean addEvent) {
        currentType = type;
        animationProvider = AnimationProvider.getProvider(type);
        calculateWidthHeight();
        resetAnimationParams();
        requestLayout();
        if (addEvent) {
            addEvent();
        }
    }


    private void resetAnimationParams() {
        if (animationProvider != null) {
            animationProvider.setTextWidthHeight(finalWidth, finalHeight);
            animationProvider.setTextCount(textEmojiList.size());
            if (animationProvider.isRowSplited()) {
                String[] split = text.split("\n");
                animationProvider.setRowCount(split == null?1:split.length);
            }
            animationProvider.init();
        }
    }

    @Override
    public boolean isRepeat() {
        if (animationProvider != null) {
            return animationProvider.isRepeat();
        }
        return true;
    }


    @Override
    public int getStayTime() {
        if (animationProvider != null) {
            return animationProvider.getStayTime();
        }
        return getDuration();
    }

    @Override
    public int getDuration() {
        if (animationProvider == null) {
            return 0;
        }
        return animationProvider.getDuration();
    }


    @Override
    public int getDelay() {
        if (animationProvider == null) {
            return 0;
        }
        return animationProvider.getDelay();
    }


    public Typeface getTypeface(Typeface typefaceDefault) {
        if (!TextUtils.isEmpty(fontName)) {
            File fontfile = FileManager.getPsFile(fontName, "ttf");
            if (fontfile != null && fontfile.exists()) {
                try {
                    Typeface typeface = Typeface.createFromFile(fontfile);
                    if (typeface != null) {
                        return typeface;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //把错误的字体文件删除，以便重新下载
                    boolean delete = fontfile.delete();
                }
            }
        }
        return typefaceDefault;
    }

    @Override
    public void onRelease() {
        super.onRelease();
        BitmapUtil.recycleBitmaps(backgroundBitmap);
    }

    public float getTextSize() {
        return paint.getTextSize();
    }

    public String getText() {
        return text;
    }

    public boolean setTexttypeface(Typeface texttypeface) {
        Typeface typeface = paint.getTypeface();
        if (texttypeface.equals(typeface)) {
            return false;
        }
        fontName = null;
        paint.setTypeface(getTypeface(texttypeface));
        strokepaint.setTypeface(getTypeface(texttypeface));
        calculateWidthHeight();
        addEvent();
        requestLayout();
        invalidate();
        return true;
    }


    List<EmojiconHandler.TextEmoji> textEmojiList;

    private void fixEmoji() {
        // \n的算一个字符，emoji算两个字符，但是这里做了fix
        textEmojiList = EmojiconHandler.getEmojis(getContext(), text);
    }


    public void setPaintColorReverse(int color, int strokeColor) {
        paint.setColor(color);
        strokepaint.setColor(strokeColor);
        resetAnimationPaint();
    }

    public boolean setStrokecolor(String strokecolor) {
        int colorNew = ColorUtil.getColor(strokecolor, paint.getColor());
        int color = strokepaint.getColor();
        if (color == colorNew) {
            return false;
        }

        //setColor之后透明度会重置为255, 需要重新设置
        int alpha = strokepaint.getAlpha();
        strokepaint.setColor(colorNew);
        strokepaint.setAlpha(alpha);

        resetAnimationPaint();
        invalidate();
        return false;
    }

    public boolean isText2Long() {
        return finalWidth * finalHeight * 4 > drawingCacheSize;
    }

    public int setText(String text) {
        this.text = text;
        fixEmoji();
        calculateWidthHeight();

        addEvent();
        return finalWidth;
    }

    public int editText(String text) {
        if (text != null && text.equals(this.text)) {
            return finalWidth;
        }
        this.text = text;
        fixEmoji();
        calculateWidthHeight();
        resetAnimationParams();
        if (getDuration() > 0){
            setStartTime(0);
            setEndTime(getDuration());
        }

        addEvent();
        requestLayout();
        invalidate();
        return finalWidth;
    }

    public boolean setStrokealpha(int strokealpha) {
        int alpha = strokepaint.getAlpha();
        if (alpha == strokealpha) {
            return false;
        }
        strokepaint.setAlpha(strokealpha);
        resetAnimationPaint();
        invalidate();
        return false;
    }

    public boolean setStrokecolor(int strokecolor) {
        int color = strokepaint.getColor();
        if (color == strokecolor) {
            return false;
        }
        //setColor之后透明度会重置为255, 需要重新设置
        int alpha = strokepaint.getAlpha();
        strokepaint.setColor(strokecolor);
        strokepaint.setAlpha(alpha);
        resetAnimationPaint();
        invalidate();
        return false;
    }


    public boolean setTextcolor(int textcolor) {
        if (paint.getShader() != null) {
            startColorStr="";
            endColorStr="";
            paint.setColor(textcolor);
            resetAnimationPaint();
            invalidate();
            return true;
        }
        int color = paint.getColor();
        if (color != textcolor) {
            //setColor之后透明度会重置为255, 需要重新设置
            int alpha = paint.getAlpha();
            paint.setColor(textcolor);
            paint.setAlpha(alpha);
            resetAnimationPaint();
            invalidate();
            //TODO
            requestLayout();
            return false;
        }
        return false;
    }

    public boolean setTextalpha(int textalpha) {
        if (paint.getAlpha() == textalpha) {
            return false;
        }
        paint.setAlpha(textalpha);
        resetAnimationPaint();
        invalidate();
        return false;
    }

    public boolean setTextcolor(String textcolor) {
        startColorStr = "";
        endColorStr = "";
        int color = paint.getColor();
        if (!TextUtils.isEmpty(textcolor)) {
            int colorNew = ColorUtil.getColor(textcolor, paint.getColor());
            if (paint.getShader() != null) {
                paint.setShader(null);
                paint.setColor(colorNew);
                resetAnimationPaint();
                invalidate();
                return true;
            }
            if (color != colorNew) {
                //setColor之后透明度会重置为255, 需要重新设置
                int alpha = paint.getAlpha();
                paint.setColor(colorNew);
                paint.setAlpha(alpha);
                resetAnimationPaint();
                invalidate();
                return false;
            }
        }
        return false;
    }


    @Override
    public int getViewWidth() {
        return finalWidth;
    }

    @Override
    public int getViewHeight() {
        return finalHeight;
    }


    int position = -1;
    List<TextOp> list = new ArrayList<>();

    /**
     * 记录文字的历史记录
     */
    private void addEvent() {
        if (position < list.size() - 1) {
            for (int i = list.size() - 1; i > position; i--) {
                list.remove(i);
            }
        }
        list.add(new TextOp(getText(), paint, strokepaint, backgroudRes, fontName, currentType, startColorStr, endColorStr));
        position = list.size() - 1;
    }

    public void pre() {
        position--;
        if (position < 0) {
            position = 0;
        }
        TextOp op = list.get(position);
        reset(op);
    }

    public void pro() {
        position++;
        if (position > list.size() - 1) {
            position = list.size() - 1;
        }
        TextOp op = list.get(position);
        reset(op);
    }

    private void reset(TextOp op) {
        this.paint = new Paint();
        this.paint.set(op.paint);
        this.strokepaint = new Paint();
        this.strokepaint.set(op.strokePaint);
        this.backgroudRes = op.background;
        this.fontName = op.fontName;
        this.text = op.text;
        this.startColorStr = op.startColorStr;
        this.endColorStr = op.endColorStr;
        fixEmoji();
        calculateWidthHeight();
        this.currentType = op.animationType;
        setTextAnimationType(currentType, false);
        requestLayout();
        invalidate();
    }

    //文字历史记录类
    class TextOp {
        public Paint paint;
        public Paint strokePaint;
        public int background;
        public String text;
        public String fontName;
        public int animationType;
        public String startColorStr;
        public String endColorStr;

        TextOp(String text, Paint p, Paint sp, int bg, String fontName, int animationType, String startColorStr, String endColorStr) {
            this.text = text;
            this.fontName = fontName;
            this.paint = new Paint();
            this.paint.set(p);
            this.strokePaint = new Paint();
            this.strokePaint.set(sp);
            this.background = bg;
            this.animationType = animationType;
            this.startColorStr = startColorStr;
            this.endColorStr = endColorStr;
        }
    }


    public boolean getNullInput() {
        return nullInput;
    }


    public void setNullInput(boolean nullInput) {
        if (nullInput == this.nullInput) {
            return;
        }
        init(getContext());
        this.nullInput = nullInput;
        paint.setColor(Color.WHITE);
        if (nullInput) {
            strokepaint.setColor(Color.parseColor("#474747"));
            paint.setStrokeWidth(getPaintStrokeWidth(0.4f));
            strokepaint.setStrokeWidth(getStrokePaintStrokeWidth(0.4f, 0.2f));
            paint.setAlpha(100);
            strokepaint.setAlpha(108);
        } else {
            strokepaint.setColor(Color.BLACK);
            paint.setStrokeWidth(getPaintStrokeWidth(0.4f));
            strokepaint.setStrokeWidth(getStrokePaintStrokeWidth(0.4f, 0.2f));
            paint.setAlpha(255);
            strokepaint.setAlpha(255);
        }
    }

    public void setIsAudio(boolean isAudio) {
        init(getContext());
        this.isAudio = isAudio;
        paint.setColor(Color.WHITE);
        strokepaint.setColor(Color.BLACK);
        if (isAudio) {
            paint.setStrokeWidth(getPaintStrokeWidth(0.2f));
            strokepaint.setStrokeWidth(getStrokePaintStrokeWidth(0.2f, 0.4f));
        }
        paint.setAlpha(255);
        strokepaint.setAlpha(255);
    }

    //TODO 进阶，对每个字Shader
    public void setLinearGradient(String startcolorStr, String endcolorStr) {
        //字体颜色是双色
        int startcolor = Color.parseColor("#" + startcolorStr);
        int endcolor = Color.parseColor("#" + endcolorStr);
        int[] colrs = {startcolor, startcolor, endcolor};
        float[] positions = {};
        //更适合多行文字的效果
        LinearGradient linearGradient = new LinearGradient(getViewWidth() / 2, 0, getViewWidth() / 2, getViewHeight(), startcolor,
                endcolor, Shader.TileMode.REPEAT);
//        LinearGradient linearGradient = new LinearGradient(getViewWidth() / 2, 0, getViewWidth() / 2, lineHeight, startcolor,
// endcolor,
//                Shader.TileMode.REPEAT);
        paint.setShader(linearGradient);
    }

}
