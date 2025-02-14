package com.season.book.page.span.media;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.season.book.R;
import com.season.lib.BaseContext;
import com.season.book.page.span.ReplacementSpan;
import com.season.book.page.span.ResourceSpan;

public class VoiceSpan extends ReplacementSpan implements ResourceSpan, IMediaSpan {
	private String mSrc;
	private float mSize;
	private int mWidth;
	private Drawable mPlayDrawable;
	private Drawable mPrepareingDrawable;
	private Drawable mPauseDrawable;
	public VoiceSpan(String src){
		mSrc = src;
		mSize = -1;
		ReaderMediaPlayer.getInstance().addPlayerListener(this);
	}
	
	public void setSrc(String src){
		mSrc = src;
	}
	
	@Override
	public boolean isClickable() {
		return !TextUtils.isEmpty(mSrc);
	}

	@Override
	public void release() {
		mPlayDrawable = null;
		mPauseDrawable = null;
		mPrepareingDrawable = null;
	}
	
	@Override
	public String getVoiceSrc(){
		return mSrc;
	}

	@Override
	public void draw(Canvas canvas, CharSequence text, int start, int end,
			int left, int top, int right, int bottom,int maxW,int maxH, Paint paint) {
		int state = ReaderMediaPlayer.getInstance().getState(mSrc);
		if(state == ReaderMediaPlayer.STATE_START){
			if(mPlayDrawable == null){
				mPlayDrawable = BaseContext.getInstance().getResources().getDrawable(R.drawable.ic_reader_voice_play);
			}
			mPlayDrawable.setBounds(left, top, right, bottom);
			mPlayDrawable.draw(canvas);
		}else{
			if(state != ReaderMediaPlayer.STATE_PREPAREING){
				if(mPauseDrawable == null){
					mPauseDrawable = BaseContext.getInstance().getResources().getDrawable(R.drawable.ic_reader_voice_pause);
				}
				mPauseDrawable.setBounds(left, top, right, bottom);
				mPauseDrawable.draw(canvas);
			}else{
				if(mPrepareingDrawable == null){
					mPrepareingDrawable = BaseContext.getInstance().getResources().getDrawable(R.drawable.ic_reader_voice_prepareing);
				}
				mPrepareingDrawable.setBounds(left, top, right, bottom);
				mPrepareingDrawable.draw(canvas);
			}
		}
	}
	
	@Override
	public boolean isPlay(){
		return ReaderMediaPlayer.getInstance().getState(mSrc) == ReaderMediaPlayer.STATE_START;
	}
	
	@Override
	public boolean contains(long position){
		return true;
	}
	
	@Override
	public void onPlayStateChange(int playState, String voiceSrc) {
		
	}

	@Override
	public void onProgressChange(long currentPosition, long maxPosition, String voiceSrc) {
		
	}

	@Override
	public long getStartPosition() {
		return ReaderMediaPlayer.getInstance().getLastPlayPosition(mSrc);
	}

	@Override
	public long computePositionByLocal(int x, int y) {
		return getStartPosition();
	}

	@Override
	public void getSize(Paint paint, CharSequence text, int start, int end,
			int maxW, int maxH, Rect container) {
		if(mSize != paint.getTextSize()){
			mWidth = (int) (paint.measureText("测") * 1.5);
			mSize = paint.getTextSize();
		}
		container.set(0, 0, mWidth, mWidth);
	}

	@Override
	public void checkContentRect(RectF rect) {
	}
}