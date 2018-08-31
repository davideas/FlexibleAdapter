package eu.davidea.samples.flexibleadapter.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;

import eu.davidea.samples.flexibleadapter.R;

/**
 * Procedurally-drawn version of a horizontal indeterminate progress bar. Draws faster and more
 * frequently (by making use of the animation timer), requires minimal memory overhead, and allows
 * some configuration via attributes:
 * <ul>
 * <li>barColor (color attribute for the bar's solid color)
 * <li>barHeight (dimension attribute for the height of the solid progress bar)
 * <li>detentWidth (dimension attribute for the width of each transparent detent in the bar)
 * </ul>
 * <p>
 * This progress bar has no intrinsic height, so you must declare it with one explicitly. (It will
 * use the given height as the bar's shadow height.)
 */
public class ProgressBar extends View {

    private final ValueAnimator mAnimator;
    private final Paint mPaint = new Paint();
    private final int mBarColor;
    private final int mSolidBarHeight;
    private final int mSolidBarDetentWidth;
    private final float mDensity;
    private int mSegmentCount;
    private GradientDrawable mShadow;
    private boolean mUseShadow;


    /**
     * The baseline width that the other constants below are optimized for.
     */
    private static final int BASE_WIDTH_DP = 300;
    /**
     * A reasonable animation duration for the given width above. It will be weakly scaled up and
     * down for wider and narrower widths, respectively-- the goal is to provide a relatively
     * constant detent velocity.
     */
    private static final int BASE_DURATION_MS = 500;
    /**
     * A reasonable number of detents for the given width above. It will be weakly scaled up and
     * down for wider and narrower widths, respectively.
     */
    private static final int BASE_SEGMENT_COUNT = 5;
    private static final int DEFAULT_BAR_HEIGHT_DP = 4;
    private static final int DEFAULT_DETENT_WIDTH_DP = 3;

    public ProgressBar(Context c) {
        this(c, null);
    }

    public ProgressBar(Context c, AttributeSet attrs) {
        super(c, attrs);

        mDensity = c.getResources().getDisplayMetrics().density;

        final TypedArray ta = c.obtainStyledAttributes(attrs, R.styleable.ProgressBar);
        try {
            mBarColor = ta.getColor(
                    R.styleable.ProgressBar_barColor,
                    c.getResources().getColor(android.R.color.holo_blue_light));
            mSolidBarHeight = ta.getDimensionPixelSize(
                    R.styleable.ProgressBar_barHeight,
                    Math.round(DEFAULT_BAR_HEIGHT_DP * mDensity));
            mSolidBarDetentWidth = ta.getDimensionPixelSize(
                    R.styleable.ProgressBar_detentWidth,
                    Math.round(DEFAULT_DETENT_WIDTH_DP * mDensity));
            mUseShadow = ta.getBoolean(
                    R.styleable.ProgressBar_useShadow,
                    false);
        } finally {
            ta.recycle();
        }

        mAnimator = new ValueAnimator();
        mAnimator.setFloatValues(1.0f, 2.0f);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.setInterpolator(new ExponentialInterpolator());
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });

        mPaint.setColor(mBarColor);

        if (mUseShadow) {
            mShadow = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{(mBarColor & 0x00ffffff) | 0x22000000, 0});
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            final int w = getWidth();

            if (mUseShadow)
                mShadow.setBounds(0, mSolidBarHeight, w, getHeight() - mSolidBarHeight);

            final float widthMultiplier = w / mDensity / BASE_WIDTH_DP;
            // simple scaling by width is too aggressive, so dampen it first
            final float durationMult = 0.3f * (widthMultiplier - 1) + 1;
            final float segmentMult = 0.1f * (widthMultiplier - 1) + 1;
            mAnimator.setDuration((int) (BASE_DURATION_MS * durationMult));
            mSegmentCount = (int) (BASE_SEGMENT_COUNT * segmentMult);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!mAnimator.isStarted()) return;
        if (mUseShadow) mShadow.draw(canvas);

        final float val = (Float) mAnimator.getAnimatedValue();
        final int w = getWidth();
        // Because the left-most segment doesn't start all the way on the left, and because it moves
        // towards the right as it animates, we need to offset all drawing towards the left. This
        // ensures that the left-most detent starts at the left origin, and that the left portion
        // is never blank as the animation progresses towards the right.
        final int offset = w >> mSegmentCount - 1;
        // segments are spaced at half-width, quarter, eighth (powers-of-two). to maintain a smooth
        // transition between segments, we used a power-of-two interpolator.
        for (int i = 0; i < mSegmentCount; i++) {
            final float l = val * (w >> (i + 1));
            final float r = (i == 0) ? w + offset : l * 2;
            canvas.drawRect(l + mSolidBarDetentWidth - offset, 0, r - offset, mSolidBarHeight,
                    mPaint);
        }
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) start();
        else stop();
    }

    private void start() {
        if (mAnimator == null) return;
        mAnimator.start();
    }

    private void stop() {
        if (mAnimator == null) return;
        mAnimator.cancel();
    }

    private static class ExponentialInterpolator implements Interpolator {
        @Override
        public float getInterpolation(float input) {
            return (float) Math.pow(2.0, input) - 1;
        }
    }

}