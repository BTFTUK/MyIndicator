package com.btftu.myindicator.indicator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;

/**H
 * 类似CircleIndicator的效果
 * yk改造 2017/7/4  17:53  仿造gitHub库magicIndicator
 */

public class ScaleCircleNavigator4 extends View implements IPagerNavigator, NavigatorHelper2.OnNavigatorScrollListener {
    private int mMinRadius;
    private int mMaxRadius;
    private int mMidRadius;
    private int mNormalCircleColor = Color.LTGRAY;
    private int mSelectedCircleColor = Color.GRAY;
    private int mCircleSpacing;
    private int mCircleCount;
    private float mScrollPivotX = 0.5f; // 滚动中心点 0.0f - 1.0f

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private List<CirclePoint> mCirclePoints = new ArrayList<>();
    private SparseArray<Float> mCircleRadiusArray = new SparseArray<>();

    // 事件回调
    private boolean mTouchable;
    private ScaleCircleNavigator4.OnCircleClickListener mCircleClickListener;
    private float mDownX;
    private float mDownY;
    private int mTouchSlop;

    private boolean mFollowTouch = true;    // 是否跟随手指滑动
    private NavigatorHelper2 mNavigatorHelper2 = new NavigatorHelper2();
    private Interpolator mStartInterpolator = new LinearInterpolator();
    private Context mContext;

    public ScaleCircleNavigator4(Context context) {
        super(context);
        init(context);
        this.mContext = context;
    }

    private void init(Context context) {

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mMinRadius = UIUtil.dip2px(context, 2);
        mMidRadius = UIUtil.dip2px(context, 3);
        mMaxRadius = UIUtil.dip2px(context, 4);
        mCircleSpacing = UIUtil.dip2px(context, 6);
        mNavigatorHelper2.setNavigatorScrollListener(this);
        mNavigatorHelper2.setSkimOver(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    private int measureWidth(int widthMeasureSpec) {
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int result = 0;
        switch (mode) {
            case MeasureSpec.EXACTLY:
                result = width;
                break;
            case MeasureSpec.AT_MOST:
            case MeasureSpec.UNSPECIFIED:
                //最多设计7个的效果
                result = mMinRadius * 2 * 2 + mMaxRadius * 2 * 3 + mMidRadius * 2 * 2 + 8 * mCircleSpacing + getPaddingLeft() + getPaddingRight();
                break;
            default:
                break;
        }
        return result;
    }

    private int measureHeight(int heightMeasureSpec) {
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int result = 0;
        switch (mode) {
            case MeasureSpec.EXACTLY:
                result = height;
                break;
            case MeasureSpec.AT_MOST:
            case MeasureSpec.UNSPECIFIED:
                result = mMaxRadius * 2 + getPaddingTop() + getPaddingBottom();
                break;
            default:
                break;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0, j = mCirclePoints.size(); i < j; i++) {
            CirclePoint point = mCirclePoints.get(i);

            float radius = mCircleRadiusArray.get(i, (float) mMinRadius);

            if (Math.abs(point.x - getWidth() / 2) < (mMaxRadius * 2 + mCircleSpacing)) {
                mPaint.setColor(ArgbEvaluatorHolder.eval(Math.abs(point.x - getWidth() / 2) /
                        (mMaxRadius * 2 + mCircleSpacing), mSelectedCircleColor, mNormalCircleColor));
            } else {
                mPaint.setColor(mNormalCircleColor);
            }
            canvas.drawCircle(point.x, getHeight() / 2.0f, radius, mPaint);
        }
    }

    private void prepareCirclePoints() {
        mCirclePoints.clear();
        if (mCircleCount > 0) {
            int y = Math.round(getHeight() / 2.0f);
            int centerSpacing = mMaxRadius * 2 + mCircleSpacing;
            int startX = getWidth() / 2;
            for (int i = 0; i < mCircleCount; i++) {
                CirclePoint pointF = new CirclePoint(startX, y);
                mCircleRadiusArray.put(0, (float) mMaxRadius);
                mCircleRadiusArray.put(1, (float) mMaxRadius);
                mCircleRadiusArray.put(2, (float) mMidRadius);
                mCircleRadiusArray.put(3, (float) mMinRadius);
                mCirclePoints.add(pointF);
                startX += centerSpacing;
            }
        }
    }

    private void notifyCirclePoints(int mx) {
        if (mCircleCount > 0 && mCircleCount == mCirclePoints.size()) {
            for (int i = 0; i < mCircleCount; i++) {
                CirclePoint point = mCirclePoints.get(i);
                point.x += -mx;
            }
        }
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mTouchable) {
                    mDownX = x;
                    mDownY = y;
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mCircleClickListener != null) {
                    if (Math.abs(x - mDownX) <= mTouchSlop && Math.abs(y - mDownY) <= mTouchSlop) {
                        float max = Float.MAX_VALUE;
                        int index = 0;
                        for (int i = 0; i < mCirclePoints.size(); i++) {
                            CirclePoint pointF = mCirclePoints.get(i);
                            float offset = Math.abs(pointF.x - x);
                            if (offset < max) {
                                max = offset;
                                index = i;
                            }
                        }
                        mCircleClickListener.onClick(index);
                    }
                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mNavigatorHelper2.onPageScrolled(position, positionOffset, positionOffsetPixels);
        // 手指跟随滚动
        if (mCirclePoints.size() > 0 && position >= 0 && position < mCirclePoints.size()) {
            if (mFollowTouch) {
                int currentPosition = Math.min(mCirclePoints.size() - 1, position);
                int nextPosition = Math.min(mCirclePoints.size() - 1, position + 1);
                CirclePoint current = mCirclePoints.get(currentPosition);
                CirclePoint next = mCirclePoints.get(nextPosition);
                float scrollTo = current.x - getWidth() * mScrollPivotX;
                float nextScrollTo = next.x - getWidth() * mScrollPivotX;
                int num = (int) (scrollTo + (nextScrollTo - scrollTo) * positionOffset);
                notifyCirclePoints(num);
            }
        }
    }

    @Override
    public void onPageSelected(int position) {
        mNavigatorHelper2.onPageSelected(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        mNavigatorHelper2.onPageScrollStateChanged(state);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        prepareCirclePoints();
    }

    @Override
    public void notifyDataSetChanged() {
        invalidate();
    }

    @Override
    public void onAttachToMagicIndicator() {
    }

    @Override
    public void onDetachFromMagicIndicator() {
    }

    public void setMinRadius(int minRadius) {
        mMinRadius = minRadius;
    }

    public void setMaxRadius(int maxRadius) {
        mMaxRadius = maxRadius;
    }

    public void setNormalCircleColor(int normalCircleColor) {
        mNormalCircleColor = normalCircleColor;
    }

    public void setSelectedCircleColor(int selectedCircleColor) {
        mSelectedCircleColor = selectedCircleColor;
    }

    public void setCircleSpacing(int circleSpacing) {
        mCircleSpacing = circleSpacing;
    }

    public void setStartInterpolator(Interpolator startInterpolator) {
        mStartInterpolator = startInterpolator;
        if (mStartInterpolator == null) {
            mStartInterpolator = new LinearInterpolator();
        }
    }

    public void setCircleCount(int count) {
        mCircleCount = count;  // 此处不调用invalidate，让外部调用notifyDataSetChanged
        mNavigatorHelper2.setTotalCount(mCircleCount);
    }

    public void setTouchable(boolean touchable) {
        mTouchable = touchable;
    }

    public void setFollowTouch(boolean followTouch) {
        mFollowTouch = followTouch;
    }

    public void setSkimOver(boolean skimOver) {
        mNavigatorHelper2.setSkimOver(skimOver);
    }

    public void setCircleClickListener(OnCircleClickListener circleClickListener) {
        if (!mTouchable) {
            mTouchable = true;
        }
        mCircleClickListener = circleClickListener;
    }

    @Override
    public void onEnter(int min2midPosition, int mid2maxPosition, int totalCount, float enterPercent, boolean leftToRight) {
        if (mFollowTouch) {
            float min2midRadius = mMinRadius + (mMidRadius - mMinRadius) * mStartInterpolator.getInterpolation(enterPercent);
            float mid2maxRadius = mMidRadius + (mMaxRadius - mMidRadius) * mStartInterpolator.getInterpolation(enterPercent);
            mCircleRadiusArray.put(min2midPosition, min2midRadius);
            mCircleRadiusArray.put(mid2maxPosition, mid2maxRadius);
            invalidate();
        }
    }

    @Override
    public void onLeave(int max2midPosition, int mid2minPosition, int totalCount, float leavePercent, boolean leftToRight) {
        if (mFollowTouch) {
            float max2midRadius = mMaxRadius + (mMidRadius - mMaxRadius) * mStartInterpolator.getInterpolation(leavePercent);
            float mid2minRadius = mMidRadius + (mMinRadius - mMidRadius) * mStartInterpolator.getInterpolation(leavePercent);
            mCircleRadiusArray.put(max2midPosition, max2midRadius);
            mCircleRadiusArray.put(mid2minPosition, mid2minRadius);
            invalidate();

        }
    }

    @Override
    public void onSelected(int index, int totalCount) {
        if (!mFollowTouch) {
//            mCircleRadiusArray.put(index, (float) mMaxRadius);
//            mCircleRadiusArray.put(index - 1, (float) mMaxRadius);
//            mCircleRadiusArray.put(index + 1, (float) mMaxRadius);
//            centerNum = index;
            invalidate();
        }
    }

    @Override
    public void onDeselected(int index, int totalCount) {
//        if (!mFollowTouch) {
//        }
    }

    public interface OnCircleClickListener {
        void onClick(int index);
    }
}
