package com.btftu.myindicator.indicator;

import android.util.SparseArray;
import android.util.SparseBooleanArray;

/**
 * 方便扩展IPagerNavigator的帮助类，将ViewPager的3个回调方法转换成
 * onSelected、onDeselected、onEnter等回调，方便扩展
 * yk 2017/7/7更改
 */
public class NavigatorHelper2 {
    private SparseBooleanArray mDeselectedItems = new SparseBooleanArray();
    private SparseArray<Float> mLeavedPercents = new SparseArray<>();

    private int mTotalCount;
    private int mCurrentIndex;
    private int mLastIndex;
    private float mLastPositionOffsetSum;
    private int mScrollState;

    private boolean mSkimOver;
    private NavigatorHelper2.OnNavigatorScrollListener mNavigatorScrollListener;

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        float currentPositionOffsetSum = position + positionOffset;
        boolean leftToRight = true;//viewPager 从左往右滑？
        if (mLastPositionOffsetSum <= currentPositionOffsetSum) {
            leftToRight = false;
        }
//        System.out.println("========viewpager状态=============="+mScrollState);///
        if (mScrollState != ScrollState.SCROLL_STATE_IDLE) {//不禁止的状态 就是拖拽或拖拽一半自动回去的状态  1和2
            if (currentPositionOffsetSum == mLastPositionOffsetSum) {
                return;
            }
            int min2midPosition;
            int mid2maxPosition;
            int max2midPosition;
            int mid2minPosition;
//            System.out.println("========position=========" + position);
            if (leftToRight) {
                min2midPosition = position - 3;
                mid2maxPosition = position - 2;
                max2midPosition = position + 1;
                mid2minPosition = position + 2;
            } else {
                min2midPosition = position + 3;
                mid2maxPosition = position + 2;
                max2midPosition = position - 1;
                mid2minPosition = position - 2;
            }


//            if (positionOffset == 0.0f) {}


            for (int i = 0; i < mTotalCount; i++) {
                if (i == position) {
                    continue;
                }
                Float leavedPercent = mLeavedPercents.get(i, 0.0f);
                if (leavedPercent != 1.0f) {
//                    dispatchOnLeave(i, 1.0f, leftToRight, true);
                }
            }
            /*if (normalDispatch) {} else {
                dispatchOnLeave(min2midPosition, mid2maxPosition, positionOffset, true, false);
                dispatchOnEnter(max2midPosition, mid2minPosition, positionOffset, true, false);
                System.out.println("========normalDispatch 等于false的时候============");
            }*/
            if (leftToRight) {
                dispatchOnEnter(min2midPosition+1, mid2maxPosition+1, 1.0f - positionOffset, true, false);
                dispatchOnLeave(max2midPosition+1, mid2minPosition+1, 1.0f - positionOffset, true, false);
//                System.out.println("======左向右滑===positionOffset==============" + (1.0f - positionOffset));
            } else {
                dispatchOnEnter(min2midPosition, mid2maxPosition, positionOffset, false, false);
                dispatchOnLeave(max2midPosition, mid2minPosition, positionOffset, false, false);
//                System.out.println("=========右向左滑==1.0f - positionOffset=====================" + (positionOffset));
            }

        } else {
            for (int i = 0; i < mTotalCount; i++) {
                if (i == mCurrentIndex) {
                    continue;
                }
                boolean deselected = mDeselectedItems.get(i);
                if (!deselected) {
                    dispatchOnDeselected(i);
                }
//                Float leavedPercent = mLeavedPercents.get(i, 0.0f);
//                if (leavedPercent != 1.0f) {
//                    dispatchOnLeave(-1, -2, 1.0f, false, true);
//                }
            }
//            dispatchOnEnter(3, 2, 1.0f, false, true);
            dispatchOnSelected(mCurrentIndex);
        }
        mLastPositionOffsetSum = currentPositionOffsetSum;
    }

    //小变大
    private void dispatchOnEnter(int min2midPosition, int mid2maxPosition, float enterPercent, boolean leftToRight, boolean force) {
        if (mSkimOver || mScrollState == ScrollState.SCROLL_STATE_DRAGGING || force) {
            if (mNavigatorScrollListener != null) {
                mNavigatorScrollListener.onEnter(min2midPosition, mid2maxPosition, mTotalCount, enterPercent, leftToRight);
            }
            mLeavedPercents.put(min2midPosition, 1.0f - enterPercent);
            mLeavedPercents.put(mid2maxPosition, 1.0f - enterPercent);
        }
    }

    //大变小
    private void dispatchOnLeave(int max2midPosition, int mid2minPosition, float leavePercent, boolean leftToRight, boolean force) {
        if (mSkimOver || mScrollState == ScrollState.SCROLL_STATE_DRAGGING || force) {
            if (mNavigatorScrollListener != null) {
                mNavigatorScrollListener.onLeave(max2midPosition, mid2minPosition, mTotalCount, leavePercent, leftToRight);
            }
            mLeavedPercents.put(max2midPosition, leavePercent);
            mLeavedPercents.put(mid2minPosition, leavePercent);
        }
    }

    private void dispatchOnSelected(int index) {
        if (mNavigatorScrollListener != null) {
            mNavigatorScrollListener.onSelected(index, mTotalCount);
        }
        mDeselectedItems.put(index, false);
    }

    private void dispatchOnDeselected(int index) {
        if (mNavigatorScrollListener != null) {
            mNavigatorScrollListener.onDeselected(index, mTotalCount);
        }
        mDeselectedItems.put(index, true);
    }

    public void onPageSelected(int position) {
        mLastIndex = mCurrentIndex;
        mCurrentIndex = position;
        dispatchOnSelected(mCurrentIndex);
        for (int i = 0; i < mTotalCount; i++) {
            if (i == mCurrentIndex) {
                continue;
            }
            boolean deselected = mDeselectedItems.get(i);
            if (!deselected) {
                dispatchOnDeselected(i);
            }
        }
    }

    public void onPageScrollStateChanged(int state) {
        mScrollState = state;
    }

    public void setNavigatorScrollListener(NavigatorHelper2.OnNavigatorScrollListener navigatorScrollListener) {
        mNavigatorScrollListener = navigatorScrollListener;
    }

    public void setSkimOver(boolean skimOver) {
        mSkimOver = skimOver;
    }

    public int getTotalCount() {
        return mTotalCount;
    }

    public void setTotalCount(int totalCount) {
        mTotalCount = totalCount;
        mDeselectedItems.clear();
        mLeavedPercents.clear();
    }

    public int getCurrentIndex() {
        return mCurrentIndex;
    }

    public int getScrollState() {
        return mScrollState;
    }

    public interface OnNavigatorScrollListener {
        void onEnter(int min2midPosition, int mid2maxPosition, int totalCount, float enterPercent, boolean leftToRight);

        void onLeave(int max2midPosition, int mid2minPosition, int totalCount, float leavePercent, boolean leftToRight);

        void onSelected(int index, int totalCount);

        void onDeselected(int index, int totalCount);
    }
}
