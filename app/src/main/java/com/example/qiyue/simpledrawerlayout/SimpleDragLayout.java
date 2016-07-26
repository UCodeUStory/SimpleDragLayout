package com.example.qiyue.simpledrawerlayout;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.nineoldandroids.view.ViewHelper;

/**
 * Created by Administrator on 2016/7/26 0026.
 */
public class SimpleDragLayout extends FrameLayout {

    /**手势处理类**/
    private GestureDetectorCompat gestureDetector;
    /**视图拖拽移动帮助类**/
    private ViewDragHelper dragHelper;

    /**滑动监听器**/
    private DragListener dragListener;
    /**页面状态 默认为关闭**/
    private Status status = Status.Close;

    private Context context;

    /**
     * 水平拖拽的距离
     */
    private int range;
    /**
     * 左侧布局宽度
     */
    private int width;
    /**
     * 左侧布局高度
     */
    private int height;
    /**main视图距离在ViewGroup距离左边的距离**/
    private int mainLeft;

    private ImageView iv_shadow;
    /**左侧布局**/
    private RelativeLayout vg_left;
    /**右侧(主界面布局)**/
    private CustomRelativeLayout vg_main;


    public SimpleDragLayout(Context context) {
        this(context, null);
        this.context = context;
    }

    public SimpleDragLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        this.context = context;

    }

    public SimpleDragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        gestureDetector = new GestureDetectorCompat(context, new YScrollDetector());
        dragHelper = ViewDragHelper.create(this, dragHelperCallback);
    }

    private ViewDragHelper.Callback dragHelperCallback = new ViewDragHelper.Callback() {
        /**
         * 捕获那些view
         */
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            /**
             * 直接 return true代表捕获所有
             */
            if (child == vg_main) {
                return true;
            }
            return false;
        }

        /**
         * 设置水平方向移动的范围
         */
        @Override
        public int getViewHorizontalDragRange(View child) {
            return range;
        }

        /**
         * 拖动过程中的回调
         * @param child 正在被拖拽的view
         * @param left 试图移动的距离, 返回的也就是每次移动的位置，最终位置还是由释放后决定
         * @param dx 本次滑动建议的距离
         * @return
         */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            Log.i("qiyue","left="+left+"dx="+dx);
            if (mainLeft + dx < 0) {
                return 0;
            } else if (mainLeft + dx > width) {
                return width;
            } else {
                return left;
            }
        }

        /**
         *  被拖动的view 返回新的边界值
         * @param changedView View whose position changed
         * @param left New X coordinate of the left edge of the view
         * @param top New Y coordinate of the top edge of the view
         * @param dx Change in X position from the last call
         * @param dy Change in Y position from the last call
         */
        @Override
        public void onViewPositionChanged(View changedView, int left, int top,
                                          int dx, int dy) {
            if (changedView == vg_main) {
                mainLeft = left;
            } else {
                mainLeft = mainLeft + left;
            }
            if (mainLeft < 0) {
                mainLeft = 0;
            } else if (mainLeft > range) {
                mainLeft = range;
            }

            dispatchDragEvent(mainLeft);
        }

        /**
         * 当拖拽的子View，手势释放的时候回调的方法， 然后根据左滑或者右滑的距离进行判断打开或者关闭
         * @param releasedChild
         * @param xvel   x 速度
         * @param yvel   y 速度
         */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            Log.i("qiyue","onViewReleased"+"xvel="+xvel+"yvel="+yvel);
           if (xvel > 0) {

                open();
            } else if (xvel < 0) {
                close();
            } else if (releasedChild == vg_main && mainLeft > range * 0.3) {
                Log.i("qiyue","releasedChild == vg_main");
                open();
            } else if (releasedChild == vg_left && mainLeft > range * 0.7) {
                Log.i("qiyue","releasedChild == vg_left");
                open();
            } else {
                close();
            }
        }
    };


    /**
     * 拦截触摸事件
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return dragHelper.shouldInterceptTouchEvent(ev) && gestureDetector.onTouchEvent(ev);
    }

    /**
     * 将拦截的到事件给ViewDragHelper进行处理
     * @param e
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        try {
            dragHelper.processTouchEvent(e);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }


    /**
     * 有加速度,当我们停止滑动的时候，该不会立即停止动画效果
     */
    @Override
    public void computeScroll() {
        if (dragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * 布局加载完成回调
     * 做一些初始化的操作
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        //左侧界面
        vg_left = (RelativeLayout) getChildAt(0);
        //右侧(主)界面
        vg_main = (CustomRelativeLayout) getChildAt(1);
        vg_main.setDragLayout(this);
        vg_left.setClickable(true);
        vg_main.setClickable(true);
    }

    /**
     * 布局size改变时回调，在onFini
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        width = vg_left.getMeasuredWidth();
        height = vg_left.getMeasuredHeight();
        //可以水平拖拽滑动的距离 一共为屏幕宽度的60%
        range = (int) (width * 0.6f);
        Log.i("qiyue","onSizeChanged="+"width="+width+"height="+height);
    }

    /**
     * 进行处理拖拽事件
     * @param mainLeft
     */
    private void dispatchDragEvent(int mainLeft) {
        if (dragListener == null) {
            return;
        }
        float percent = mainLeft / (float) range;
        //根据滑动的距离的比例,进行带有动画的缩小和放大View
        animateView(percent);
        //进行回调滑动的百分比
        dragListener.onDrag(percent);
        Status lastStatus = status;
        if (lastStatus != getStatus() && status == Status.Close) {
            Log.i("qiyue","onClose");
            dragListener.onClose();
        } else if (lastStatus != getStatus() && status == Status.Open) {
            Log.i("qiyue","onOpen");
            dragListener.onOpen();
        }
    }


    /**
     * 通过gestureDetector判断手势
     */
    class YScrollDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy) {
            Log.d("qiyue","dx="+dx+",dy="+dy+",isDrag:");
            return (Math.abs(dy) <= Math.abs(dx));
        }
    }
    /**
     * 滑动相关回调接口
     */
    public interface DragListener {
        /**界面打开**/
        public void onOpen();
        /**界面关闭**/
        public void onClose();
        /**界面滑动过程中**/
        public void onDrag(float percent);
    }

    /**
     * 页面状态(滑动,打开,关闭)
     */
    public enum Status {
        Drag, Open, Close
    }

    /**
     * 页面状态设置
     * @return
     */
    public Status getStatus() {
        if (mainLeft == 0) {
            status = Status.Close;
        } else if (mainLeft == range) {
            status = Status.Open;
        } else {
            status = Status.Drag;
        }
        return status;
    }

    /**open 和 close 方法**/
    public void open() {
        open(true);
    }

    public void open(boolean animate) {
        if (animate) {
            //继续滑动

            if (dragHelper.smoothSlideViewTo(vg_main, range, 0)) {
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            vg_main.layout(range, 0, range * 2, height);
          //  dispatchDragEvent(range);
        }
    }

    public void close() {
        close(true);
    }

    public void close(boolean animate) {
        if (animate) {
            //继续滑动
            if (dragHelper.smoothSlideViewTo(vg_main, 0, 0)) {
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            vg_main.layout(0, 0, width, height);
           // dispatchDragEvent(0);
        }
    }

    /**
     * 根据滑动的距离的比例,进行带有动画的缩小和放大View
     * @param percent
     */
    private void animateView(float percent) {
        float f1 = 1 - percent * 0.3f;
        //vg_main水平方向 根据百分比缩放
        ViewHelper.setScaleX(vg_main, f1);
        //vg_main垂直方向，根据百分比缩放
        ViewHelper.setScaleY(vg_main, f1);
        //沿着水平X轴平移
        ViewHelper.setTranslationX(vg_left, -vg_left.getWidth() / 2.3f + vg_left.getWidth() / 2.3f * percent);
        //vg_left水平方向 根据百分比缩放
        ViewHelper.setScaleX(vg_left, 0.5f + 0.5f * percent);
        //vg_left垂直方向 根据百分比缩放
        ViewHelper.setScaleY(vg_left, 0.5f + 0.5f * percent);
        //vg_left根据百分比进行设置透明度
        ViewHelper.setAlpha(vg_left, percent);
      /*  if (isShowShadow) {
            //阴影效果视图大小进行缩放
            ViewHelper.setScaleX(iv_shadow, f1 * 1.4f * (1 - percent * 0.12f));
            ViewHelper.setScaleY(iv_shadow, f1 * 1.85f * (1 - percent * 0.12f));
        }*/
        getBackground().setColorFilter(evaluate(percent, Color.BLACK, Color.TRANSPARENT), PorterDuff.Mode.SRC_OVER);
    }

    private Integer evaluate(float fraction, Object startValue, Integer endValue) {
        int startInt = (Integer) startValue;
        int startA = (startInt >> 24) & 0xff;
        int startR = (startInt >> 16) & 0xff;
        int startG = (startInt >> 8) & 0xff;
        int startB = startInt & 0xff;
        int endInt = (Integer) endValue;
        int endA = (endInt >> 24) & 0xff;
        int endR = (endInt >> 16) & 0xff;
        int endG = (endInt >> 8) & 0xff;
        int endB = endInt & 0xff;
        return (int) ((startA + (int) (fraction * (endA - startA))) << 24)
                | (int) ((startR + (int) (fraction * (endR - startR))) << 16)
                | (int) ((startG + (int) (fraction * (endG - startG))) << 8)
                | (int) ((startB + (int) (fraction * (endB - startB))));
    }

    public void setDragListener(DragListener dragListener) {
        this.dragListener = dragListener;
    }
}
