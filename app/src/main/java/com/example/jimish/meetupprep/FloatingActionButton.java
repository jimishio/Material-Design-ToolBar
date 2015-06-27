package com.example.jimish.meetupprep;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.ImageButton;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import justbe.mobile.R;
import justbe.mobile.sportMe.config.Constants;

/**
 * Created by jimish on 25/3/15.
 */
public class FloatingActionButton extends ImageButton {


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ Constants.SIZE_NORMAL, Constants.SIZE_MINI })
    public @interface FAB_SIZE {
    }

    int colorNormal;
    int colorPressed;
    int colorDisabled;
    String title;
    @DrawableRes
    private int icon;
    private Drawable iconDrawable;
    private int size;

    private float circleSize;
    private float shadowRadius;
    private float shadowOffset;
    private int drawableSize;
    boolean strokeVisible;

    public FloatingActionButton(Context context) {
        this(context, null);
    }

    public FloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FloatingActionButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    void init(Context context, AttributeSet attributeSet) {
        TypedArray attr = context.obtainStyledAttributes(attributeSet, R.styleable.FloatingActionButton, 0, 0);
        colorNormal = attr.getColor(R.styleable.FloatingActionButton_fab_colorNormal, getColor(android.R.color.holo_blue_dark));
        colorPressed = attr.getColor(R.styleable.FloatingActionButton_fab_colorPressed, getColor(android.R.color.holo_blue_light));
        colorDisabled = attr.getColor(R.styleable.FloatingActionButton_fab_colorDisabled, getColor(android.R.color.darker_gray));
        size = attr.getInt(R.styleable.FloatingActionButton_fab_size, Constants.SIZE_NORMAL);
        icon = attr.getResourceId(R.styleable.FloatingActionButton_fab_icon, 0);
        title = attr.getString(R.styleable.FloatingActionButton_fab_title);
        strokeVisible = attr.getBoolean(R.styleable.FloatingActionButton_fab_stroke_visible, true);
        attr.recycle();

        updateCircleSize();
        shadowRadius = getDimension(R.dimen.fab_shadow_radius);
        shadowOffset = getDimension(R.dimen.fab_shadow_offset);
        updateDrawableSize();

        updateBackground();
    }

    private void updateDrawableSize() {
        drawableSize = (int) (circleSize + 2 * shadowRadius);
    }

    private void updateCircleSize() {
        circleSize = getDimension(size == Constants.SIZE_NORMAL ? R.dimen.fab_size_normal : R.dimen.fab_size_mini);
    }

    public void setSize(@FAB_SIZE int size) {
        if (size != Constants.SIZE_MINI && size != Constants.SIZE_NORMAL) {
            throw new IllegalArgumentException("Use @FAB_SIZE constants only!");
        }

        if (this.size != size) {
            this.size = size;
            updateCircleSize();
            updateDrawableSize();
            updateBackground();
        }
    }

    @FAB_SIZE
    public int getSize() {
        return size;
    }

    public void setIcon(@DrawableRes int icon) {
        if (this.icon != icon) {
            this.icon = icon;
            iconDrawable = null;
            updateBackground();
        }
    }
    public void removeIcon(){
//        this.icon = null;
    }


    public void setIconDrawable(@NonNull Drawable iconDrawable) {
        if (this.iconDrawable != iconDrawable) {
            icon = 0;
            this.iconDrawable = iconDrawable;
            updateBackground();
        }
    }

    /**
     * @return the current Color for normal state.
     */
    public int getColorNormal() {
        return colorNormal;
    }

    public void setColorNormalResId(@ColorRes int colorNormal) {
        setColorNormal(getColor(colorNormal));
    }

    public void setColorNormal(int color) {
        if (colorNormal != color) {
            colorNormal = color;
            updateBackground();
        }
    }

    /**
     * @return the current color for pressed state.
     */
    public int getColorPressed() {
        return colorPressed;
    }

    public void setColorPressedResId(@ColorRes int colorPressed) {
        setColorPressed(getColor(colorPressed));
    }

    public void setColorPressed(int color) {
        if (colorPressed != color) {
            colorPressed = color;
            updateBackground();
        }
    }

    /**
     * @return the current color for disabled state.
     */
    public int getColorDisabled() {
        return colorDisabled;
    }

    public void setColorDisabledResId(@ColorRes int colorDisabled) {
        setColorDisabled(getColor(colorDisabled));
    }

    public void setColorDisabled(int color) {
        if (colorDisabled != color) {
            colorDisabled = color;
            updateBackground();
        }
    }

    public void setStrokeVisible(boolean visible) {
        if (strokeVisible != visible) {
            strokeVisible = visible;
            updateBackground();
        }
    }

    public boolean isStrokeVisible() {
        return strokeVisible;
    }

    int getColor(@ColorRes int id) {
        return getResources().getColor(id);
    }

    float getDimension(@DimenRes int id) {
        return getResources().getDimension(id);
    }

    public void setTitle(String title) {
        this.title = title;
        TextView label = getLabelView();
        if (label != null) {
            label.setText(title);
        }
    }

    TextView getLabelView() {
        return (TextView) getTag(R.id.fab_label);
    }

    public String getTitle() {
        return title;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(drawableSize, drawableSize);
    }

    void updateBackground() {
        final float strokeWidth = getDimension(R.dimen.fab_stroke_width);
        final float halfStrokeWidth = strokeWidth / 2f;

        LayerDrawable layerDrawable = new LayerDrawable(
                new Drawable[] {
                        getResources().getDrawable(size == Constants.SIZE_NORMAL ? R.drawable.fab_bg_normal : R.drawable.fab_bg_mini),
                        createFillDrawable(strokeWidth),
                        createOuterStrokeDrawable(strokeWidth),
                        getIconDrawable()
                });

        int iconOffset = (int) (circleSize - getDimension(R.dimen.fab_icon_size)) / 2;

        int circleInsetHorizontal = (int) (shadowRadius);
        int circleInsetTop = (int) (shadowRadius - shadowOffset);
        int circleInsetBottom = (int) (shadowRadius + shadowOffset);

        layerDrawable.setLayerInset(1,
                circleInsetHorizontal,
                circleInsetTop,
                circleInsetHorizontal,
                circleInsetBottom);

        layerDrawable.setLayerInset(2,
                (int) (circleInsetHorizontal - halfStrokeWidth),
                (int) (circleInsetTop - halfStrokeWidth),
                (int) (circleInsetHorizontal - halfStrokeWidth),
                (int) (circleInsetBottom - halfStrokeWidth));

        layerDrawable.setLayerInset(3,
                circleInsetHorizontal + iconOffset,
                circleInsetTop + iconOffset,
                circleInsetHorizontal + iconOffset,
                circleInsetBottom + iconOffset);

        setBackgroundCompat(layerDrawable);
    }

    Drawable getIconDrawable() {
        if (iconDrawable != null) {
            return iconDrawable;
        } else if (icon != 0) {
            return getResources().getDrawable(icon);
        } else {
            return new ColorDrawable(Color.TRANSPARENT);
        }
    }

    private StateListDrawable createFillDrawable(float strokeWidth) {
        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[] { -android.R.attr.state_enabled }, createCircleDrawable(colorDisabled, strokeWidth));
        drawable.addState(new int[] { android.R.attr.state_pressed }, createCircleDrawable(colorPressed, strokeWidth));
        drawable.addState(new int[] { }, createCircleDrawable(colorNormal, strokeWidth));
        return drawable;
    }

    private Drawable createCircleDrawable(int color, float strokeWidth) {
        int alpha = Color.alpha(color);
        int opaqueColor = opaque(color);

        ShapeDrawable fillDrawable = new ShapeDrawable(new OvalShape());

        final Paint paint = fillDrawable.getPaint();
        paint.setAntiAlias(true);
        paint.setColor(opaqueColor);

        Drawable[] layers = {
                fillDrawable,
                createInnerStrokesDrawable(opaqueColor, strokeWidth)
        };

        LayerDrawable drawable = alpha == 255 || !strokeVisible
                ? new LayerDrawable(layers)
                : new TranslucentLayerDrawable(alpha, layers);

        int halfStrokeWidth = (int) (strokeWidth / 2f);
        drawable.setLayerInset(1, halfStrokeWidth, halfStrokeWidth, halfStrokeWidth, halfStrokeWidth);

        return drawable;
    }

    private static class TranslucentLayerDrawable extends LayerDrawable {
        private final int mAlpha;

        public TranslucentLayerDrawable(int alpha, Drawable... layers) {
            super(layers);
            mAlpha = alpha;
        }

        @Override
        public void draw(Canvas canvas) {
            Rect bounds = getBounds();
            canvas.saveLayerAlpha(bounds.left, bounds.top, bounds.right, bounds.bottom, mAlpha, Canvas.ALL_SAVE_FLAG);
            super.draw(canvas);
            canvas.restore();
        }
    }

    private Drawable createOuterStrokeDrawable(float strokeWidth) {
        ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());

        final Paint paint = shapeDrawable.getPaint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        paint.setAlpha(opacityToAlpha(0.02f));

        return shapeDrawable;
    }

    private int opacityToAlpha(float opacity) {
        return (int) (255f * opacity);
    }

    private int darkenColor(int argb) {
        return adjustColorBrightness(argb, 0.9f);
    }

    private int lightenColor(int argb) {
        return adjustColorBrightness(argb, 1.1f);
    }

    private int adjustColorBrightness(int argb, float factor) {
        float[] hsv = new float[3];
        Color.colorToHSV(argb, hsv);

        hsv[2] = Math.min(hsv[2] * factor, 1f);

        return Color.HSVToColor(Color.alpha(argb), hsv);
    }

    private int halfTransparent(int argb) {
        return Color.argb(
                Color.alpha(argb) / 2,
                Color.red(argb),
                Color.green(argb),
                Color.blue(argb)
        );
    }

    private int opaque(int argb) {
        return Color.rgb(
                Color.red(argb),
                Color.green(argb),
                Color.blue(argb)
        );
    }

    private Drawable createInnerStrokesDrawable(final int color, float strokeWidth) {
        if (!strokeVisible) {
            return new ColorDrawable(Color.TRANSPARENT);
        }

        ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());

        final int bottomStrokeColor = darkenColor(color);
        final int bottomStrokeColorHalfTransparent = halfTransparent(bottomStrokeColor);
        final int topStrokeColor = lightenColor(color);
        final int topStrokeColorHalfTransparent = halfTransparent(topStrokeColor);

        final Paint paint = shapeDrawable.getPaint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(Paint.Style.STROKE);
        shapeDrawable.setShaderFactory(new ShapeDrawable.ShaderFactory() {
            @Override
            public Shader resize(int width, int height) {
                return new LinearGradient(width / 2, 0, width / 2, height,
                        new int[] { topStrokeColor, topStrokeColorHalfTransparent, color, bottomStrokeColorHalfTransparent, bottomStrokeColor },
                        new float[] { 0f, 0.2f, 0.5f, 0.8f, 1f },
                        Shader.TileMode.CLAMP
                );
            }
        });

        return shapeDrawable;
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private void setBackgroundCompat(Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(drawable);
        } else {
            setBackgroundDrawable(drawable);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        TextView label = getLabelView();
        if (label != null) {
            label.setVisibility(visibility);
        }

        super.setVisibility(visibility);
    }
}
