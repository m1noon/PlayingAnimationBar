package com.m1noon.playinganimationbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PlayingAnimationBar extends View {

    private static final int DEFAULT_FPS = 16;
    private static final int DEFAULT_DURATION = 3000;
    private static final int DEFAULT_BAR_COUNT = 3;
    private static final int DEFAULT_COLOR = Color.GRAY;

    private final int fps;
    private final float duration;
    private final int barCount;
    private final int margin;
    private final int topColor;
    private final int bottomColor;
    private final boolean isAlphaInflexionEnable;

    private Timer timer;
    private final Paint paint;
    private final List<Bar> bars;
    private final Handler handler;
    private final Runnable updateTask = new Runnable() {
        @Override
        public void run() {
            update();
        }
    };

    public PlayingAnimationBar(Context context) {
        this(context, null);
    }

    public PlayingAnimationBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayingAnimationBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray attr = context.obtainStyledAttributes(attrs, R.styleable.PlayingAnimationBar);
        fps = attr.getInteger(R.styleable.PlayingAnimationBar_pab_fps, DEFAULT_FPS);
        duration = attr.getInt(R.styleable.PlayingAnimationBar_pab_duration, DEFAULT_DURATION);
        margin = (int) attr.getDimension(R.styleable.PlayingAnimationBar_pab_margin, 0);
        barCount = attr.getInt(R.styleable.PlayingAnimationBar_pab_barCount, DEFAULT_BAR_COUNT);
        bottomColor = attr.getColor(R.styleable.PlayingAnimationBar_pab_bottomColor, DEFAULT_COLOR);
        topColor = attr.getColor(R.styleable.PlayingAnimationBar_pab_topColor, DEFAULT_COLOR);
        isAlphaInflexionEnable = attr.getBoolean(R.styleable.PlayingAnimationBar_pab_alphaInflexionEnable, true);

        paint = new Paint();
        paint.setColor(Color.RED);
        bars = new ArrayList<>();
        handler = new Handler();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(updateTask);
            }
        }, 0, 1000 / fps);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        timer.cancel();
        timer = null;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        final float velocity = (getMeasuredHeight() * 2) / duration;
        final int width = getWidth() / barCount;
        final int height = getHeight();
        bars.clear();
        for (int i = 0; i < barCount; i++) {
            bars.add(new Bar(i, width, height, (float) (velocity * (1 + (Math.random() - 0.5f) * 0.2f)), margin));
        }

        final Shader shader = new LinearGradient(0, 0, 0, h, topColor, bottomColor, Shader.TileMode.CLAMP);
        paint.setShader(shader);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Bar bar : bars) {
            bar.draw(canvas, paint, isAlphaInflexionEnable);
        }
    }

    private void update() {
        for (Bar bar : bars) {
            bar.update(1000 / fps);
        }
        invalidate();
    }

    private static class Bar {

        private final int height;
        private final int left;
        private final int right;
        private final float velocity;
        private final Rect rect;
        private float progress;

        public Bar(int number, int width, int height, float velocity, int margin) {
            this.height = height;
            this.velocity = velocity;
            left = width * number + (margin / 2);
            right = width * (number + 1) - (margin / 2);
            rect = new Rect();
            rect.set(left, 0, right, height);
            progress = (float) Math.random();
        }

        public void update(float duration) {
            progress += duration * velocity / height;
            progress -= Math.floor(progress);
        }

        public void draw(Canvas canvas, Paint paint, boolean isAlphaInflexionEnable) {
            final float p = (float) Math.sin(progress * Math.PI);
            rect.set(left, (int) (height * (1 - p)), right, height);
            if (isAlphaInflexionEnable) {
                paint.setAlpha(Math.min(255, (int) (255 * p + 128)));
            }
            canvas.drawRect(rect, paint);
        }
    }
}
