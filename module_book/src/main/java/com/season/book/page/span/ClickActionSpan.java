package com.season.book.page.span;

import android.graphics.RectF;


public interface ClickActionSpan{
	public boolean isClickable();
	public void checkContentRect(RectF rect);
}
