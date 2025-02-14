package com.season.example.dragview;

import android.view.MotionEvent;
import android.view.ViewGroup;

/**
 * 滑动监听器
 * 
 * @author SeasonAllan
 * 
 */
public interface IDragListener {
 
	/**
	 * 长按开启拖拽功能
	 *
	 */
	void onDragEnable();

	/**
	 * 返回键关闭拖拽功能
	 *
	 */
	void onDragDisable();

	/**
	 * 删除某一项
	 * 
	 * @param page
	 * @param position
	 */
	void onItemDelete(int page, int position);

	/**
	 * 删除某一项
	 * @param <T>
	 * @param <T>
	 * 
	 * @param page
	 * @param position
	 */
	<T> void onItemDelete(int totalPage, int page, int removePage, int position, T object);
	/**
	 * 长按创建浮窗
	 * 
	 * @param page
	 * @param itemView
	 * @param event
	 */
	void onDragViewCreate(int page, ViewGroup itemView, MotionEvent event);

	/**
	 * 放手销毁浮窗
	 * 
	 * @param page
	 * @param event
	 */
	void onDragViewDestroy(int page, MotionEvent event);

	/**
	 * 拖动浮窗
	 * 
	 * @param page
	 * @param event
	 */
	void onItemMove(int page, MotionEvent event);

	/**
	 * 滑动到其他页面【锁住浮窗】
	 * 
	 * @param lastPage
	 * @param currentPage
	 */
	void onPageChange(int lastPage, int currentPage);

	/**
	 * 页面切换数据交互一【老页面移除拖动数据项目通知】
	 * @param <T>
	 * 
	 * @param lastPage
	 * @param currentPage
	 * @param object
	 */
	<T> void onPageChangeRemoveDragItem(int lastPage, int currentPage,
                                               T object);

	/**
	 * 页面切换数据交互二【新页面添加拖动数据项目通知】
	 * @param <T>
	 * 
	 * @param lastPage
	 * @param currentPage
	 * @param object
	 */
	<T> void onPageChangeReplaceFirstItem(int lastPage, int currentPage,
                                                 T object);

	/**
	 * 页面切换操作完毕【通知浮窗解锁，可以继续滑动】
	 */
	void onPageChangeFinish();

}
