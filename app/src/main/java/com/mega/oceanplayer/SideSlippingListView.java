package com.mega.oceanplayer;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListView;

public class SideSlippingListView extends ListView {

    private String TAG = SideSlippingListView.class.getSimpleName();

    private int mExtraButtonWidth;          //删除组件的宽度
    private int mDownX;                     //手指初次按下的X坐标
    private int mDownY;                     //手指初次按下的Y坐标
    private int mPointPosition;             //手指按下位置所在的item位置

    private boolean isAllowItemClick;       //是否允许item点击
    private boolean isExtraButtonShow;      //删除组件是否显示

    private ViewGroup mPointChild;          //手指按下位置的item组件
    private boolean fixFirstRow;

    public SideSlippingListView(Context context) {
        super(context);
        LogUtil.i(TAG, "construct function 1");
        initView(context);
    }

    public SideSlippingListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LogUtil.i(TAG, "construct function 2");
        initView(context);
    }

    public SideSlippingListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LogUtil.i(TAG, "construct function 3");
        initView(context);
    }

    public void initView(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);

        float density = getContext().getResources().getDisplayMetrics().density;
        LogUtil.d(TAG, String.format("dp2px(4)=%f, dp2px(8)=%f, density=%f", dp2px(4), dp2px(8), density));
        mExtraButtonWidth = -1;
        fixFirstRow = false;
    }

    public void setFixFirstRow(boolean fixFirstRow) {
        this.fixFirstRow = fixFirstRow;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        LogUtil.i(TAG, "onInterceptTouchEvent: action=" + ev.toString());
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //记录按下的位置
                mDownX = (int) ev.getX();
                mDownY = (int) ev.getY();

                isAllowItemClick = true;

                int pointPosition = pointToPosition(mDownX, mDownY);
                int firstVisiblePosition = getFirstVisiblePosition();
                int lastVisiblePosition = getLastVisiblePosition();

                LogUtil.d(TAG, String.format("onInterceptTouchEvent: check position of child： %d (%d:%d)",
                        pointPosition,
                        firstVisiblePosition,
                        lastVisiblePosition));

                if (pointPosition < firstVisiblePosition || pointPosition > lastVisiblePosition) {
                    LogUtil.w(TAG, String.format("onInterceptTouchEvent: can not get child at by pos=%d", pointPosition));
                    return super.onInterceptTouchEvent(ev);
                }

                ViewGroup currentViewGroup = (ViewGroup) getChildAt(pointPosition - firstVisiblePosition);

                if (isExtraButtonShow) {
                    if (currentViewGroup == mPointChild) {
                        Rect listViewRect = new Rect();
                        getGlobalVisibleRect(listViewRect);
                        LogUtil.d(TAG, "LIST View: " + listViewRect.toString());
                        Rect firstChildViewGroupRect = new Rect();
                        mPointChild.getChildAt(0).getGlobalVisibleRect(firstChildViewGroupRect);
                        LogUtil.d(TAG, "VG's first Child: " + firstChildViewGroupRect.toString());

                        if (firstChildViewGroupRect.contains(mDownX + listViewRect.left, mDownY + listViewRect.top)) {
                            LogUtil.d(TAG, "isExtraButtonShow=true, clicked on the first child of slipping item, stop propagating touchEvent to child  ");
                            return true;
                        }
                    }
                    else {
                        LogUtil.d(TAG, "isExtraButtonShow=true, clicked on another item, stop propagating touchEvent to child");
                        return true;
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE: {
                int nowX = (int) ev.getX();
                int nowY = (int) ev.getY();
                int diffX = nowX - mDownX;

                //如果滑动的距离超过了12dp(36px), 就认为用户在滑动，在此阻止touchEvent传播到子view
                //如果滑动的距离小于12dp，那么touchEvent事件会传播到子view
                if (Math.abs(diffX) > dp2px(12) || Math.abs(nowY - mDownY) > dp2px(12)) {
                    LogUtil.d(TAG, String.format("onInterceptTouchEvent: User is slipping, diffX=%d, dp2px(12)=%f, set isAllowItemClick=false", Math.abs(diffX), dp2px(12)));
                    isAllowItemClick = false;
                    return true;
                }
                break;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return performActionDown(ev);
            case MotionEvent.ACTION_MOVE:
                return performActionMove(ev);
            case MotionEvent.ACTION_UP:
                return performActionUp(ev);
        }
        return super.onTouchEvent(ev);
    }

    private boolean performActionDown(MotionEvent ev) {

        LogUtil.d(TAG, String.format("performActionDown: x=%d, y=%d",mDownX, mDownY));

        //获取当前的item
        mPointPosition = pointToPosition(mDownX, mDownY);

        int firstVisiblePosition = getFirstVisiblePosition();
        int lastVisiblePosition = getLastVisiblePosition();

        LogUtil.d(TAG, String.format("performActionDown: check position of child： %d (%d:%d)",
                mPointPosition,
                firstVisiblePosition,
                lastVisiblePosition));

        if(mPointPosition < firstVisiblePosition || mPointPosition > lastVisiblePosition) {
            LogUtil.w(TAG, String.format("performActionDown: can not get child at by pos=%d, call super.onTouchEvent(ev)", mPointPosition));
            return super.onTouchEvent(ev);
        }

        ViewGroup currentViewGroup = (ViewGroup) getChildAt(mPointPosition - firstVisiblePosition);

        if(mPointChild != null && !mPointChild.equals(currentViewGroup) && isExtraButtonShow) {
            LogUtil.d(TAG, "performActionDown: clicked on another item, hide extra button on current item and set isAllowItemClick=false");
            animateHide();
            isAllowItemClick = false;
        }
        mPointChild = currentViewGroup;

        //获取删除组件的宽度
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mPointChild.getChildAt(1).getLayoutParams();
        if(mExtraButtonWidth == -1) {
            LogUtil.d(TAG,"performActionDown: get mExtraButtonWidth = " + mExtraButtonWidth);
            mExtraButtonWidth = layoutParams.width;
        }
        layoutParams.width = mExtraButtonWidth * 2;
        mPointChild.getChildAt(1).setLayoutParams(layoutParams);

        return super.onTouchEvent(ev);
    }

    private boolean performActionMove(MotionEvent ev) {

        int nowX = (int) ev.getX();
        int nowY = (int) ev.getY();

        int diffX = nowX - mDownX;
        int diffY = nowY - mDownY;

        LogUtil.d(TAG, String.format("performActionMove: x=%d, y=%d, dx=%d,dy=%d",nowX, nowY, diffX, diffY));

        if(mPointPosition == -1 || Math.abs(diffX) < Math.abs(diffY)) {
            LogUtil.d(TAG, "performActionMove: call super.onTouchEvent(ev)");
            return super.onTouchEvent(ev);
        }

        if(nowX < mDownX) {
            //向左滑
            if (isExtraButtonShow) {
                LogUtil.d(TAG, "performActionMove: left slipping, extra button already showing, do nothing");
            }
            else {
                if (-diffX >= mExtraButtonWidth) {
                    //如果滑动距离大于删除组件的宽度时, 进行偏移的最大处理
                    int offset = -diffX - mExtraButtonWidth;
                    offset = (int) (Math.log(offset) / Math.log(1.03));
                    LogUtil.d(TAG, String.format("performActionMove: Adjust diffX: %d:%d -- %d -- %d", diffX, mExtraButtonWidth, offset, -mExtraButtonWidth - offset));
                    diffX = -mExtraButtonWidth - offset;
                }
                moveLayout(diffX);
            }
        }
        else {
            //向右滑
            if (isExtraButtonShow) {
                if (diffX >= mExtraButtonWidth) {
                    int offset = diffX - mExtraButtonWidth;
                    offset = (int)(Math.log(offset)/Math.log(1.03));
                    LogUtil.d(TAG, String.format("performActionMove: Adjust diffX: %d:%d -- %d -- %d", diffX, mExtraButtonWidth, offset, -mExtraButtonWidth - offset));
                    diffX = mExtraButtonWidth + offset;
                }
                moveLayout(diffX - mExtraButtonWidth);
            }
            else {
                LogUtil.d(TAG, "performActionMove: right slipping, extra button not showing, do nothing");
            }
        }

        if (Math.abs(diffX) > dp2px(8) || Math.abs(nowY - mDownY) > dp2px(8)) {
            LogUtil.d(TAG, String.format("performActionMove: User is slipping, diffX=%d, dp2px(8)=%f, set isAllowItemClick=false", Math.abs(diffX), dp2px(8)));
            isAllowItemClick = false;
        }
        return super.onTouchEvent(ev);
    }

    private boolean performActionUp(MotionEvent ev) {

        LogUtil.d(TAG, "performActionUp");

        int nowX = (int) ev.getX();
        int nowY = (int) ev.getY();

        int diffX = nowX - mDownX;
        int diffY = nowY - mDownY;

        if(mPointPosition == -1) {
            LogUtil.d(TAG, "performActionUp: mPointPosition=-1, call super.onTouchEvent(ev)");
            return super.onTouchEvent(ev);
        }

        if(Math.abs(diffX) < Math.abs(diffY)) {
            LogUtil.d(TAG, "performActionUp: diffx < diffY, call super.onTouchEvent(ev)");
            turnNormal();
            return super.onTouchEvent(ev);
        }

        int currentOffset = (int)mPointChild.getChildAt(0).getTranslationX();
        //int currentOffset = (int)mPointChild.getTranslationX();
        if(nowX < mDownX) {
            //向左滑动, 如果之前没有显示按钮
            if(!isExtraButtonShow) {
                //滑动超过隐藏按钮宽度的一半, 则显示隐藏按钮
                if (-currentOffset >= mExtraButtonWidth / 2) {
                    LogUtil.i(TAG, String.format("performActionUp: show extra button since slipping long enough: offsetX=%d, diffX=%d", -currentOffset, -diffX));
                    animateShow();
                }
                //滑动没有超过隐藏按钮的一半，哪么就撤销这次滑动
                else {
                    LogUtil.d(TAG, String.format("performActionUp: revert the slipping since slipping too short: offsetX=%d, diffX=%d", -currentOffset, -diffX));
                    animateHide();
                    //滑动距离小于dp2px(8)，则等同于点击，需要调用父类的onTouchEvent
                    if (Math.abs(diffX) < dp2px(8)) {
                        return super.onTouchEvent(ev);
                    }
                }
            }
        }
        else {
            //向右滑动, 如果之前没有显示按钮
            if(isExtraButtonShow) {
                LogUtil.d(TAG, "performActionUp: hide the extra button on right slipping or click");
                animateHide();
            }
            else {
                if(isAllowItemClick) {
                    LogUtil.d(TAG, "performActionUp: extra button is not showing, the item is allowed to be clicked");
                    return super.onTouchEvent(ev);
                }
                else {
                    LogUtil.d(TAG, "performActionUp: extra button is not showing, somehow  the isAllowItemClick is false");
                }
                turnNormal();
            }
        }
        return true;
    }

    public void moveLayout(int offset) {
        LogUtil.d(TAG, String.format("moveLayout: offset=%d, mPointPosition", offset, mPointPosition));
        if(mPointPosition == 0 && fixFirstRow) {
            return;
        }
        if(mPointChild != null) {
            mPointChild.getChildAt(0).setTranslationX(offset);
            mPointChild.getChildAt(1).setTranslationX(offset);
        }
        //mPointChild.setTranslationX(offset);
    }

    public void animateShow() {
        isExtraButtonShow = true;

        if(mPointChild == null || mPointChild.getChildAt(0).getTranslationX() == 0) {
            isExtraButtonShow = false;
            return;
        }

        float offset = -mPointChild.getChildAt(0).getTranslationX() - mExtraButtonWidth;
        int duration = (int)(Math.abs(offset)/800f*1000);

        LogUtil.d(TAG, String.format("animateSlipping: offset=%f, duration=%d", offset, duration));
        ObjectAnimator animation = ObjectAnimator.ofFloat(mPointChild.getChildAt(0), "translationX", -mExtraButtonWidth);
        animation.setDuration(duration);
        animation.start();

        ObjectAnimator animation1 = ObjectAnimator.ofFloat(mPointChild.getChildAt(1), "translationX", -mExtraButtonWidth);
        animation1.setDuration(duration);
        animation1.start();
    }

    public void animateHide() {
        isExtraButtonShow = false;

        if(mPointChild == null || mPointChild.getChildAt(0).getTranslationX() == 0) {
            return;
        }

        int duration = (int)(Math.abs(mExtraButtonWidth)/800f*1000);

        LogUtil.d(TAG, String.format("animateHide: offset=%f, duration=%d", 0.0f, duration));

        ObjectAnimator animation = ObjectAnimator.ofFloat(mPointChild.getChildAt(0), "translationX", 0);
        animation.setDuration(duration);
        animation.start();

        ObjectAnimator animation1 = ObjectAnimator.ofFloat(mPointChild.getChildAt(1), "translationX", 0);
        animation1.setDuration(duration);
        animation1.start();
    }

    public void turnNormal() {
        LogUtil.d(TAG, "turnNormal");
        isExtraButtonShow = false;
        if(mPointChild == null || mPointChild.getChildAt(0).getTranslationX() == 0) {
            return;
        }
        //mPointChild.setTranslationX(0);
        mPointChild.getChildAt(0).setTranslationX(0);
        mPointChild.getChildAt(1).setTranslationX(0);
    }

    public float dp2px(int dp) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getContext().getResources().getDisplayMetrics()
        );
    }
}