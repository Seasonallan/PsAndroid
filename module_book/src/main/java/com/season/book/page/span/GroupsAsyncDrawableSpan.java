package com.season.book.page.span;

import android.graphics.Rect;
import android.graphics.RectF;

import com.season.book.page.paser.html.DataProvider;

public class GroupsAsyncDrawableSpan extends AsyncDrawableSpan implements ClickActionSpan{
	private String mImgs;

	public GroupsAsyncDrawableSpan(String src,String title,String imgs, boolean isFullScreen,float presetWidth, float presetHeight, DataProvider dataProvider) {
		super(src,title, isFullScreen, presetWidth, presetHeight, dataProvider);
		mImgs = imgs;
	}
	/**
	 * @return the mImgs
	 */
	public String getImgs() {
		return mImgs;
	}
	
	@Override
	public boolean isClickable() {
		return true;
	}
	
	@Override
	public void checkContentRect(RectF rect) {
		if(getDrawable() != null){
			Rect bounds = getDrawable().getBounds();
			if(bounds.width() > 0 && bounds.height() > 0){
				rect.set(bounds);
			}
		}
	}
}
