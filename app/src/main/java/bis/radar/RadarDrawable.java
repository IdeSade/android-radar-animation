package bis.radar;

import android.animation.Animator;
import android.animation.AnimatorSet;
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

import java.util.ArrayList;
import java.util.List;

public class RadarDrawable extends Drawable implements Animatable {

    private static final int DURATION = 3000;

    private int mStartRadius;

    private AnimatorSet mAnimator;
    private boolean mAnimating;
    private List<Circle> mCircles = new ArrayList<>();

    public RadarDrawable() {
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
        for (Circle circle : mCircles) {
            circle.draw(canvas, rect);
        }
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

        mAnimator = new AnimatorSet();
        mAnimator.play(createCircleAnimation(0, rect))
                .with(createCircleAnimation(2000, rect));

        if (mAnimating) {
            mAnimator.start();
        }
    }

    private Animator createCircleAnimation(final int startDelay, Rect rect) {
        final Circle circle = new Circle();
        mCircles.add(circle);

        ValueAnimator animator = new ValueAnimator();

        animator.setValues(
                PropertyValuesHolder.ofInt("radius", mStartRadius, Math.min(rect.centerX(), rect.centerY())),
                PropertyValuesHolder.ofInt("alphaStroke", 255, 0),
                PropertyValuesHolder.ofInt("alphaFill", 40, 0)
        );

        animator.setStartDelay(startDelay);
        animator.setDuration(DURATION);
        animator.setRepeatCount(ValueAnimator.INFINITE);

        animator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                circle.mRadius = (int) animation.getAnimatedValue("radius");
                circle.mStrokePaint.setAlpha((int) animation.getAnimatedValue("alphaStroke"));
                circle.mFillPaint.setAlpha((int) animation.getAnimatedValue("alphaFill"));
                invalidateSelf();
            }
        });

        return animator;
    }

    private static class Circle {
        private Paint mStrokePaint;
        private Paint mFillPaint;
        private int mRadius;

        public Circle() {
            mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mStrokePaint.setStyle(Style.STROKE);
            mStrokePaint.setColor(Color.GREEN);
            mStrokePaint.setStrokeWidth(2);
            mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mFillPaint.setStyle(Style.FILL);
            mFillPaint.setColor(Color.GREEN);
        }

        public void draw(Canvas canvas, Rect rect) {
            canvas.drawCircle(rect.centerX(), rect.centerY(), mRadius, mFillPaint);
            canvas.drawCircle(rect.centerX(), rect.centerY(), mRadius, mStrokePaint);
        }
    }
}
