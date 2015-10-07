package bis.radar;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;

public class RadarDrawable extends Drawable implements Animatable {

    private static final int DURATION = 2000;

    private Paint mStrokePaint;
    private Paint mFillPaint;
    private int mRadius;
    private int mStartRadius;

    private ValueAnimator mAnimator;
    private boolean mAnimating;

    public RadarDrawable() {
        mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mStrokePaint.setStyle(Style.STROKE);
        mStrokePaint.setColor(Color.GREEN);
        mStrokePaint.setStrokeWidth(2);
        mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFillPaint.setStyle(Style.FILL);
        mFillPaint.setColor(Color.GREEN);
    }

    public void setStartRadius(int startRadius) {
        mStartRadius = startRadius;
        if (mAnimating) {
            initAnimator();
        }
    }

    // Drawable

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        if (mAnimating) {
            initAnimator();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        Rect rect = getBounds();
        canvas.drawCircle(rect.centerX(), rect.centerY(), mRadius, mFillPaint);
        canvas.drawCircle(rect.centerX(), rect.centerY(), mRadius, mStrokePaint);
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
    }

    @Override
    public int getOpacity() {
        return 0;
    }

    // Animatable

    @Override
    public void start() {
        mAnimating = true;
        if (!isRunning()) {
            if (mAnimator == null) {
                initAnimator();
            }
        }
    }

    @Override
    public void stop() {
        mAnimating = false;
        if (isRunning()) {
            mAnimator.cancel();
        }
    }

    @Override
    public boolean isRunning() {
        return mAnimator != null && mAnimator.isRunning();
    }

    private void initAnimator() {
        if (isRunning()) {
            mAnimator.cancel();
        }

        Rect rect = getBounds();
        if (rect.isEmpty()) {
            return;
        }

        mAnimator = new ValueAnimator();
        mAnimator.setValues(
                PropertyValuesHolder.ofInt("radius", mStartRadius, Math.min(rect.centerX(), rect.centerY())),
                PropertyValuesHolder.ofInt("alphaStroke", 255, 0),
                PropertyValuesHolder.ofInt("alphaFill", 30, 0)
        );
        mAnimator.setDuration(DURATION);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mRadius = (int) animation.getAnimatedValue("radius");
                mStrokePaint.setAlpha((int) animation.getAnimatedValue("alphaStroke"));
                mFillPaint.setAlpha((int) animation.getAnimatedValue("alphaFill"));
                invalidateSelf();
            }
        });

        if (mAnimating) {
            mAnimator.start();
        }
    }
}
