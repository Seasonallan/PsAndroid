package com.season.book.page.layout;

import java.lang.ref.SoftReference;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.util.SparseArray;
import android.view.View;

import com.season.book.page.span.ColorSpan;
import com.season.book.page.Constant;
import com.season.book.page.PatchParent;
import com.season.book.page.SettingParam;
import com.season.book.page.StyleText;
import com.season.book.page.Util;
import com.season.book.page.span.AlignSpan;
import com.season.book.page.span.AsyncDrawableSpan;
import com.season.book.page.span.ClickActionSpan;
import com.season.book.page.span.FloatSpan;
import com.season.book.page.span.ReplacementSpan;
import com.season.book.page.span.ResourceSpan;
import com.season.lib.support.dimen.DimenUtil;
import com.season.lib.util.LogUtil;

public class Line extends Patch{
	private static final String TAG = Line.class.getSimpleName();;
	private SparseArray<RectF> mTextSizeMap;
	private SoftReference<SparseArray<RectF>> mCacheTextSizeMap;
	private TextPaint mTextPaint;
	private Paint mWhiteDotPaint;
	private RectF mTempBgRect;
	private int mDefaultFontSize;
	private int mIndentSize;
	
	Line(SettingParam settingParam) {
		super(settingParam);
		mCacheTextSizeMap = new SoftReference<SparseArray<RectF>>(null);
		mTextPaint = new TextPaint();
		mTempBgRect = new RectF();
		mWhiteDotPaint = new Paint();
		mWhiteDotPaint.setColor(Color.WHITE);
		mDefaultFontSize = (int) getSourcePaint().measureText("测");
		mIndentSize = settingParam.getIndentSize();
	}

	public void setIndentSize(int indentSize) {
		mIndentSize = indentSize;
	}

	@Override
	public void init() {
		super.init();
		mIndentSize = 2;
	}


	@Override
	public void setLayoutType(int layoutType) {
		super.setLayoutType(layoutType);
		if(getLayoutType() != Constant.LAYOUT_TYPE_NOTHING){
			mIndentSize = 0;
		}
	}


	@Override
	void setBoundary(int width, int height) {
		if(height < mContentHeight){
			height = mContentHeight;
		}
		super.setBoundary(width, height);
	}

	/**
	 * 计算当前Line所包含的所有字符的位置信息，就是内部对文字进行排版布局
	 * @return
	 */
	private SparseArray<RectF> measureLine(){
		SparseArray<RectF> textSizeMap = new SparseArray<RectF>();
		Rect rect = null;
		RectF newRect = null;
		CharacterStyle [] characterStyles = null;
		int startLeft = measureStartLeft();
		boolean isSingleParagraph = getStart() == getEnd() && isParagraphStart();
		if(isSingleParagraph){
			startLeft -= getIndentWidth();
		}
		float endBottom = measureEndBottom() + offsetY;
		for(int i = getStart();i <= getEnd();i++){
			StyleText styleText = mStyleText.findStyleText(i);
			characterStyles = styleText.getTotalSpans();
			//过滤包含浮动部分，因为已经抽出
			if(getLayoutType() == Constant.LAYOUT_TYPE_NOTHING){
				Object filterStyle = Util.findLastSpans(characterStyles, FloatSpan.class);
				if(filterStyle != null){
					continue;
				}
			}
			rect = measureText(i, styleText);
			newRect = checkBounds(rect);
			newRect.offset(startLeft,endBottom - newRect.height());
			startLeft += newRect.width();
			textSizeMap.put(i, new RectF(newRect));
		}
		return optimizationsLayout(textSizeMap);
	}
	
	private final RectF checkBounds(Rect rect){
		float w = rect.width();
		float h = rect.height();
		int maxW = getMaxWidth() - measureStartLeft() + mLeft - mRightPadding;
		int maxH = getMaxHeight() - mTopPadding - mBottomPadding;
		if(isFullScreen()){
			if(w > maxW){
				w = maxW;
			}
			if(h > maxH){
				h = maxH;
			}
		}else{
			if(w > maxW || h > maxH){
				float gapW = w - maxW;
				float gapH = h - maxH;
				if(gapW > gapH){
					h = h * maxW / w;
					w = maxW;
				}else{
					w = w * maxH / h;
					h = maxH;
				}
			}
		}
		return new RectF(rect.left, rect.top,rect.left + w, rect.top + h);
	}
	/**
	 * <p>计算行内容结束的位置，用于控制底部对齐</p>
	 * <p>内容结束位置的计算，注意每一个字的大小可能都不一样</p>
	 * @return
	 */
	private int measureEndBottom(){
		return mTop + getHeight() - mBottomPadding;
	}
	/**
	 * 缩进宽度
	 * @return
	 */
	private final int getIndentWidth(){
		if(mIndentSize == 0 || mLeft - mSettingParam.getPageRect().left > mDefaultFontSize){
			return 0;
		}
		return mDefaultFontSize * mIndentSize;
	}
	/**
	 * <p>计算行的第一个字符开始绘制的位置</p>
	 * <p>如果是段的开始是否缩进的计算</p>
	 * @return
	 */
	private final int measureStartLeft(){
		if(isParagraphStart()){
			return mLeftPadding + mLeft + getIndentWidth();
		}else{
			return mLeftPadding + mLeft;
		}
	}
	
	@Override
	public int getWidth() {
		if(isParagraphStart()){
			return super.getWidth() + getIndentWidth();
		}
		return super.getWidth();
	}

	@Override
	public int getBottom() {
		int bottomSpace = mSettingParam.getLineSpace();
		if(isParagraphEnd()){
			bottomSpace = mSettingParam.getParagraphSpace();
		}else if(isTitleEnd()){
			bottomSpace = mSettingParam.getParagraphSpace() * 2;
		}
		return mBottom + bottomSpace;
	}
	
	public int getSuperBottom(){
		return super.getBottom();
	}
	/**
	 * 是否是段落的开始位置
	 */
	public boolean isParagraphStart(){
		if(mPanleType == StyleText.PANLE_TYPE_P
				|| mPanleType == StyleText.PANLE_TYPE_H){
			return isPanleStart();
		}
		return false;
	}
	/**
	 * 是否是段落的结束位置
	 */
	public boolean isParagraphEnd(){
		if(mPanleType == StyleText.PANLE_TYPE_P
				|| mPanleType == StyleText.PANLE_TYPE_H){
			return isPanleEnd();
		}
		return false;
	}
	
	/**
	 * 是否是段落的结束位置
	 */
	public boolean isTitleEnd(){
		if(mPanleType == StyleText.PANLE_TYPE_TITLE){
			return isPanleEnd();
		}
		return false;
	}
	/**
	 * 计算一个字符的宽高
	 * @param index
	 * @param styleText 当前字符所包含的样式
	 * @return
	 */
	private Rect measureText(int index,StyleText styleText){
		Rect pageRect = mSettingParam.getPageRect();
		Rect container = new Rect();
		mTextPaint.set(getSourcePaint());
		Util.measureText(styleText, mTextPaint, index, pageRect.width(),pageRect.height(), container);
		return container;
	}
	/**
	 * 微调美化已经计算好的排版,处理左右对齐
	 * @param textSizeMap
	 * @return
	 */
	private SparseArray<RectF> optimizationsLayout(SparseArray<RectF> textSizeMap){
		int textSize = textSizeMap.size();
		float surplusWidth = getMaxWidth() - getWidth();
		boolean isSingleParagraph = getStart() == getEnd();
		if(isSingleParagraph && isParagraphStart()){
			surplusWidth += getIndentWidth();
		}
		
		if(surplusWidth <= mDefaultFontSize){
			if(textSize > 1){
				surplusWidth = surplusWidth / (textSize - 1);
				for(int i = 0 ;i < textSizeMap.size();i++){
					RectF rect = textSizeMap.get(textSizeMap.keyAt(i));
					rect.set(rect.left + i * surplusWidth,rect.top,rect.right + ( i + 1 ) * surplusWidth ,rect.bottom);
				}
			}else{
				for(int i = 0 ;i < textSizeMap.size();i++){
					RectF rect = textSizeMap.get(textSizeMap.keyAt(i));
					rect.set(rect.left + surplusWidth / 2,rect.top,rect.right + surplusWidth / 2,rect.bottom);
				}
			}
		}else if(getLayoutType() == Constant.LAYOUT_TYPE_NOTHING){
			AlignSpan alignSpan = null;
			CharacterStyle[] characterStyles = null;
			if(mStyleText != null){
				StyleText styleText = mStyleText.findStyleText(getStart());
				characterStyles = styleText.getTotalSpans();
			}
			if(characterStyles != null){
				CharacterStyle characterStyle = null;
				for(int i = characterStyles.length - 1;i >= 0;i--){
					characterStyle = characterStyles[i];
					if(characterStyle instanceof AlignSpan){
						alignSpan = (AlignSpan) characterStyle;
						break;
					}
				}
			}
			if(alignSpan != null && alignSpan.getType() != AlignSpan.LEFT_ALIGN){
				if(alignSpan.getType() == AlignSpan.CENTER_ALIGN){
					surplusWidth = surplusWidth / 2;
					if(isParagraphStart() && !isSingleParagraph){
						surplusWidth -= getIndentWidth() / 2;
					}
				}
				for(int i = 0 ;i < textSizeMap.size();i++){
					RectF rect = textSizeMap.get(textSizeMap.keyAt(i));
					rect.set(rect.left + surplusWidth,rect.top,rect.right + surplusWidth,rect.bottom);
				}
			}
		}
		return textSizeMap;
	}

	/**
	 * 获取当前字符所包含的样式，有缓存读缓存，没缓存重新获取
	 * @param index
	 * @return
	 */
	private CharacterStyle [] getCharacterStyle(int index){
		if(mStyleText != null){
			StyleText styleText = mStyleText.findStyleText(index);
			if(styleText != null){
				return styleText.getTotalSpans();
			}
		}
		return new CharacterStyle[0];
	}
	/**
	 * 获取当前line所有字符的位置信息
	 * @return
	 */
	private SparseArray<RectF> getTextSizeMap(){
		if(mTextSizeMap == null){
			mTextSizeMap = measureLine();
		}
		return mTextSizeMap;
	}


	@Override
	public void draw(Canvas canvas) {
		if(mParent == null){
			LogUtil.e(TAG, "draw unBind");
			return;
		}
		SparseArray<RectF> textSizeMap = getTextSizeMap();
		mTextPaint.set(getSourcePaint());
		
		for(int i = getStart();i < getEnd();i++){
			RectF rect = textSizeMap.get(i);
			handlerDraw(canvas, rect, i, mTextPaint);
			mTextPaint.set(getSourcePaint());
		}
		if(mStyleText.charAt(getEnd()) != '\n' && mStyleText.charAt(getEnd()) != '\u2029'){
			RectF rect = textSizeMap.get(getEnd());
			handlerDraw(canvas, rect, getEnd(), mTextPaint);
		}
//		mTextPaint.setColor(Color.RED);
//		canvas.drawLine(getLeft(),getTop(), getRight(), getTop(), mTextPaint);
//		canvas.drawLine(getLeft(),getTop(), getLeft(),getBottom(), mTextPaint);
	}
	/**
	 * 绘制具体字符图片内容
	 * @param canvas
	 * @param rect
	 * @param index
	 * @param paint
	 */
	private void handlerDraw(Canvas canvas,RectF rect,int index,TextPaint paint){
		if(rect == null){
			LogUtil.e(TAG, "handlerDraw rect isNull char="+mStyleText.charAt(index));
			return;
		}
		StyleText styleText = mStyleText.findStyleText(index);
		CharacterStyle [] characterStyles = styleText.getTotalSpans();
		CharacterStyle [] extraStyles = styleText.getDataSource().getSpans(index, index + 1, CharacterStyle.class);
		if(extraStyles.length > 0){
			if(characterStyles.length == 0){
				characterStyles = extraStyles;
			}else{
				CharacterStyle[] tempStyles = new CharacterStyle[characterStyles.length + extraStyles.length];
				System.arraycopy(characterStyles, 0, tempStyles, 0, characterStyles.length);
				System.arraycopy(extraStyles, 0, tempStyles,characterStyles.length,extraStyles.length);
				characterStyles = tempStyles;
			}
		}
		//过滤包含浮动部分，因为已经抽出
		if(getLayoutType() == Constant.LAYOUT_TYPE_NOTHING){
			Object filterStyle = Util.findLastSpans(characterStyles, FloatSpan.class);
			if(filterStyle != null){
				return;
			}
		}
		FontMetricsInt fm = paint.getFontMetricsInt();
		boolean isDigestEnd = false;
		if(rect != null){
			if(characterStyles != null && characterStyles.length > 0){
				for(CharacterStyle characterStyle : characterStyles){
					characterStyle.updateDrawState(paint);
					if (paint.bgColor != 0)
						drawBgColor(canvas, rect, paint);
					if (characterStyle instanceof ColorSpan){
						isDigestEnd = index + 1 == ((ColorSpan) characterStyle).getEnd();
					}
				}
			}else{
				if (paint.bgColor != 0) //不是透明
					drawBgColor(canvas, rect, paint);
			}
			ReplacementSpan replacementSpan = Util.findFirstSpans(characterStyles, ReplacementSpan.class);
			if(replacementSpan != null){
				if(replacementSpan instanceof AsyncDrawableSpan && ((AsyncDrawableSpan)replacementSpan).isFullScreen()){
					Rect fullPageRect = mSettingParam.getFullPageRect();
					replacementSpan.draw(canvas,mStyleText.getDataSource(), index, index + 1
							, fullPageRect.left , fullPageRect.top , fullPageRect.right,fullPageRect.bottom 
							,fullPageRect.width(),fullPageRect.height(), paint);
				}else{
					replacementSpan.draw(canvas, mStyleText.getDataSource(), index, index + 1
							, (int)rect.left , (int)rect.top , (int)rect.right ,(int)rect.bottom 
							,getMaxWidth(),getMaxHeight(), paint);
				}
			}else{
				boolean isUnderlineText = paint.isUnderlineText();
				paint.setUnderlineText(false);
				paint.setTextAlign(Align.CENTER);
				int startX = (int)rect.centerX();
				int startY = (int) rect.bottom - fm.bottom / 2;
				canvas.drawText(mStyleText.getDataSource(), index, index + 1, startX , startY , paint);
				if(isUnderlineText){
					paint.setColor(paint.linkColor);
					canvas.drawRect(rect.left, rect.bottom, rect.right, rect.bottom + 4, paint);
					//canvas.drawLine(rect.left, rect.bottom, rect.right, rect.bottom + 5, paint);
					if (isDigestEnd){//绘制笔记圆点
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
							float left = rect.right - 1;
							float right = rect.right + (rect.right - rect.left)*2/3;
							float top = rect.bottom - (rect.bottom - rect.top)/4 + 2;
							float bottom = rect.bottom + (rect.bottom - rect.top)/4 + 2;
							canvas.drawOval(left,top ,right, bottom , paint);
							float percentX = (right - left)/4;
							float cy = top + (bottom - top)/2;
							canvas.drawCircle(left + percentX,cy,1, mWhiteDotPaint);
							canvas.drawCircle(left + percentX * 2,cy,1, mWhiteDotPaint);
							canvas.drawCircle(left + percentX * 3,cy,1, mWhiteDotPaint);
						}
					}
				}
			}
//			canvas.drawLine(rect.left, rect.top, rect.right, rect.top, paint);
//			canvas.drawLine(rect.left, rect.top, rect.left, rect.bottom, paint);
		}
	}
	
	private void drawBgColor(Canvas canvas,RectF rect,TextPaint paint){
		int oldColor = paint.getColor();
		paint.setColor(paint.bgColor);
		int height = (int) rect.height();
		int lineHeight = getHeight();
		if(height > lineHeight / 3 * 2f && height < lineHeight){
			mTempBgRect.set(rect.left, rect.bottom - lineHeight, rect.right, rect.bottom);
		}else{
			mTempBgRect.set(rect.left, rect.top, rect.right, rect.bottom);
		}
		canvas.drawRect(mTempBgRect, paint);
		paint.setColor(oldColor);
	}
	
	@Override
	public void bindPatchParent(PatchParent parent, StyleText styleText) {
		super.bindPatchParent(parent, styleText);
		mTextSizeMap = mCacheTextSizeMap.get();
		if(mTextSizeMap == null){
			mTextSizeMap = measureLine();
		}
		mCacheTextSizeMap.clear();
	}

	@Override
	public void unBindPatchParent() {
		for(int i = getStart();i <= getEnd();i++){
			StyleText styleText = mStyleText.findStyleText(i);
			CharacterStyle [] characterStyles = styleText.getTotalSpans();
			for (CharacterStyle characterStyle : characterStyles) {
				if(characterStyle instanceof ResourceSpan){
					((ResourceSpan) characterStyle).release();
				}
			}
		}
		if(mTextSizeMap != null){
			mCacheTextSizeMap = new SoftReference<SparseArray<RectF>>(mTextSizeMap);
			mTextSizeMap = null;
		}
		super.unBindPatchParent();
	}

	@Override
	public boolean isFullScreen() {
		if(mParent != null){
			CharacterStyle [] characterStyles = getCharacterStyle(getStart());
			for(CharacterStyle characterStyle : characterStyles){
				if(characterStyle instanceof AsyncDrawableSpan && ((AsyncDrawableSpan)characterStyle).isFullScreen()){
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean dispatchClick(View v, int x, int y) {
		if(mParent != null){
			SparseArray<RectF> textSizeMap = getTextSizeMap();
			RectF clickRectF = new RectF();
			for(int i = getStart();i <= getEnd();i++){
				RectF rect = textSizeMap.get(i);
				int errorBand = DimenUtil.DIPToPX(5);
				int left = (int) (rect.left - errorBand);
				int right = (int) (rect.right + errorBand);
				if(rect != null && left <= x && right >= x){
					StyleText styleText = mStyleText.findStyleText(i);
					CharacterStyle [] characterStyles = styleText.getTotalSpans();
					ClickActionSpan clickableSpan = Util.findLastSpans(characterStyles, ClickActionSpan.class);
					if(clickableSpan != null){
						clickRectF.set(rect);
						clickableSpan.checkContentRect(clickRectF);
						if(mSettingParam.getClickSpanHandler().onClickSpan(clickableSpan,clickRectF,x,y)){
							return true;
						}
					}
					return mSettingParam.getClickSpanHandler().checkDigestSpan(i);
				}
			}
		}
		return false;
	}

	@Override
	public boolean isFinish() {
		boolean isFullScreen = false;
		boolean isFinish = false;
		if(mParent != null){
			CharacterStyle [] characterStyles = getCharacterStyle(getStart());
			for(CharacterStyle characterStyle : characterStyles){
				if(characterStyle instanceof AsyncDrawableSpan && ((AsyncDrawableSpan)characterStyle).isFullScreen()){
					isFullScreen = true;
					isFinish = ((AsyncDrawableSpan)characterStyle).getDrawable() != null;
					break;
				}
			}
		}
		if(isFullScreen){
			return isFinish;
		}
		return true;
	}
	
	@Override
	public Rect getFullScreenContentRect(){
		CharacterStyle [] characterStyles = getCharacterStyle(getStart());
		for(CharacterStyle characterStyle : characterStyles){
			if(characterStyle instanceof AsyncDrawableSpan && ((AsyncDrawableSpan)characterStyle).isFullScreen()){
				Rect rect = ((AsyncDrawableSpan)characterStyle).getImageRect();
				return new Rect(rect.left,rect.top,rect.right,rect.bottom);
			}
		}
		return null;
	}
	
	@Override
	public int findIndexByLocation(int x, int y, boolean isAccurate) {
		if(mParent != null){
			SparseArray<RectF> textSizeMap = getTextSizeMap();
			int oldIndex = getStart();
			for(int i = getStart();i <= getEnd();i++){
				RectF rect = textSizeMap.get(i);
				if(rect != null){
					if(rect.left > x){
						if(!isAccurate){
							return oldIndex;
						}else{
							return -1;
						}
					}else if(rect.right >= x){
						return i;
					}else{
						oldIndex = i;
					}
				}
			}
			if(!isAccurate){
				return oldIndex;
			}
		}
		return -1;
	}
	
	@Override
	public Rect findRectByPosition(int position) {
		if(mParent != null){
			SparseArray<RectF> textSizeMap = getTextSizeMap();
			RectF rectF = textSizeMap.get(position);
			if(rectF != null){
				return new Rect((int)rectF.left, (int)rectF.top, (int)rectF.right, (int)rectF.bottom);
			}
		}
		return null;
	}
}
