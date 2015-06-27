package com.example.jimish.meetupprep;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import justbe.mobile.R;
import justbe.mobile.sportMe.config.Constants;

/**
 * Created by jimish on 25/3/15.
 */
public class FloatingActionMenu extends ViewGroup {

    //TODO(jimish): define as statics
    private int addButtonPlusColor;
    private int addButtonColorNormal;
    private int addButtonColorPressed;
    private int addButtonSize;
    private boolean addButtonStrokeVisible;
    private int expandDirection;

    private int buttonSpacing;
    private int labelsMargin;
    private int labelsVerticalOffset;

    private boolean expanded;

    private AnimatorSet expandAnimation = new AnimatorSet().setDuration(Constants.ANIMATION_DURATION);
    private AnimatorSet collapseAnimation = new AnimatorSet().setDuration(Constants.ANIMATION_DURATION);
    private AddFloatingActionButton addButton;
    private RotatingDrawable rotatingDrawable;
    private int maxButtonWidth;
    private int maxButtonHeight;
    private int labelsStyle;
    private int labelsPosition;
    private int buttonsCount;

    private TouchDelegateGroup touchDelegateGroup;

    private OnFloatingActionsMenuUpdateListener listener;

    public interface OnFloatingActionsMenuUpdateListener {
        void onMenuExpanded();
        void onMenuCollapsed();
    }

    public FloatingActionMenu(Context context) {
        this(context, null);
    }

    public FloatingActionMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FloatingActionMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attributeSet) {
        buttonSpacing = (int) (getResources().getDimension(R.dimen.fab_actions_spacing) - getResources().getDimension(R.dimen.fab_shadow_radius) - getResources().getDimension(R.dimen.fab_shadow_offset));
        labelsMargin = getResources().getDimensionPixelSize(R.dimen.fab_labels_margin);
        labelsVerticalOffset = getResources().getDimensionPixelSize(R.dimen.fab_shadow_offset);

        touchDelegateGroup = new TouchDelegateGroup(this);
        setTouchDelegate(touchDelegateGroup);

        TypedArray attr = context.obtainStyledAttributes(attributeSet, R.styleable.FloatingActionsMenu, 0, 0);
        addButtonPlusColor = attr.getColor(R.styleable.FloatingActionsMenu_fab_addButtonPlusIconColor, getColor(android.R.color.white));
        addButtonColorNormal = attr.getColor(R.styleable.FloatingActionsMenu_fab_addButtonColorNormal, getColor(android.R.color.holo_blue_dark));
        addButtonColorPressed = attr.getColor(R.styleable.FloatingActionsMenu_fab_addButtonColorPressed, getColor(android.R.color.holo_blue_light));
        addButtonSize = attr.getInt(R.styleable.FloatingActionsMenu_fab_addButtonSize, Constants.SIZE_NORMAL);
        addButtonStrokeVisible = attr.getBoolean(R.styleable.FloatingActionsMenu_fab_addButtonStrokeVisible, true);
        expandDirection = attr.getInt(R.styleable.FloatingActionsMenu_fab_expandDirection, Constants.EXPAND_UP);
        labelsStyle = attr.getResourceId(R.styleable.FloatingActionsMenu_fab_labelStyle, 0);
        labelsPosition = attr.getInt(R.styleable.FloatingActionsMenu_fab_labelsPosition, Constants.LABELS_ON_LEFT_SIDE);
        attr.recycle();

        if (labelsStyle != 0 && expandsHorizontally()) {
            throw new IllegalStateException("Action labels in horizontal expand orientation is not supported.");
        }

        createAddButton(context);
    }

    public void setOnFloatingActionsMenuUpdateListener(OnFloatingActionsMenuUpdateListener listener) {
        this.listener = listener;
    }

    private boolean expandsHorizontally() {
        return expandDirection == Constants.EXPAND_LEFT || expandDirection == Constants.EXPAND_RIGHT;
    }

    private static class RotatingDrawable extends LayerDrawable {
        public RotatingDrawable(Drawable drawable) {
            super(new Drawable[] { drawable });
        }

        private float rotation;

        @SuppressWarnings("UnusedDeclaration")
        public float getRotation() {
            return rotation;
        }

        @SuppressWarnings("UnusedDeclaration")
        public void setRotation(float rotation) {
            this.rotation = rotation;
            invalidateSelf();
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.save();
            canvas.rotate(rotation, getBounds().centerX(), getBounds().centerY());
            super.draw(canvas);
            canvas.restore();
        }
    }

    private void createAddButton(Context context) {
        addButton = new AddFloatingActionButton(context) {
            @Override
            void updateBackground() {
                plusColor = addButtonPlusColor;
                colorNormal = addButtonColorNormal;
                colorPressed = addButtonColorPressed;
                strokeVisible = addButtonStrokeVisible;
                super.updateBackground();
            }

            @Override
            Drawable getIconDrawable() {
                final RotatingDrawable rotatingDrawable = new RotatingDrawable(super.getIconDrawable());
                FloatingActionMenu.this.rotatingDrawable = rotatingDrawable;

                final OvershootInterpolator interpolator = new OvershootInterpolator();

                final ObjectAnimator collapseAnimator = ObjectAnimator.ofFloat(rotatingDrawable, "rotation", Constants.EXPANDED_PLUS_ROTATION, Constants.COLLAPSED_PLUS_ROTATION);
                final ObjectAnimator expandAnimator = ObjectAnimator.ofFloat(rotatingDrawable, "rotation", Constants.COLLAPSED_PLUS_ROTATION, Constants.EXPANDED_PLUS_ROTATION);

                collapseAnimator.setInterpolator(interpolator);
                expandAnimator.setInterpolator(interpolator);

                expandAnimation.play(expandAnimator);
                collapseAnimation.play(collapseAnimator);

                return rotatingDrawable;
            }
        };

        addButton.setId(R.id.fab_expand_menu_button);
        addButton.setSize(addButtonSize);
        addButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle();
            }
        });

        addView(addButton, super.generateDefaultLayoutParams());
    }

    public void addButton(FloatingActionButton button) {
        addView(button, buttonsCount - 1);
        buttonsCount++;

        if (labelsStyle != 0) {
            createLabels();
        }
    }

    public void removeButton(FloatingActionButton button) {
        removeView(button.getLabelView());
        removeView(button);
        buttonsCount--;
    }

    private int getColor(@ColorRes int id) {
        return getResources().getColor(id);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int width = 0;
        int height = 0;

        maxButtonWidth = 0;
        maxButtonHeight = 0;
        int maxLabelWidth = 0;

        for (int i = 0; i < buttonsCount; i++) {
            View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            switch (expandDirection) {
                case Constants.EXPAND_UP:
                case Constants.EXPAND_DOWN:
                    maxButtonWidth = Math.max(maxButtonWidth, child.getMeasuredWidth());
                    height += child.getMeasuredHeight();
                    break;
                case Constants.EXPAND_LEFT:
                case Constants.EXPAND_RIGHT:
                    width += child.getMeasuredWidth();
                    maxButtonHeight = Math.max(maxButtonHeight, child.getMeasuredHeight());
                    break;
            }

            if (!expandsHorizontally()) {
                TextView label = (TextView) child.getTag(R.id.fab_label);
                if (label != null) {
                    maxLabelWidth = Math.max(maxLabelWidth, label.getMeasuredWidth());
                }
            }
        }

        if (!expandsHorizontally()) {
            width = maxButtonWidth + (maxLabelWidth > 0 ? maxLabelWidth + labelsMargin : 0);
        } else {
            height = maxButtonHeight;
        }

        switch (expandDirection) {
            case Constants.EXPAND_UP:
            case Constants.EXPAND_DOWN:
                height += buttonSpacing * (getChildCount() - 1);
                height = adjustForOvershoot(height);
                break;
            case Constants.EXPAND_LEFT:
            case Constants.EXPAND_RIGHT:
                width += buttonSpacing * (getChildCount() - 1);
                width = adjustForOvershoot(width);
                break;
        }

        setMeasuredDimension(width, height);
    }

    private int adjustForOvershoot(int dimension) {
        return dimension * 12 / 10;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        switch (expandDirection) {
            case Constants.EXPAND_UP:
            case Constants.EXPAND_DOWN:
                boolean expandUp = expandDirection == Constants.EXPAND_UP;

                if (changed) {
                    touchDelegateGroup.clearTouchDelegates();
                }

                int addButtonY = expandUp ? b - t - addButton.getMeasuredHeight() : 0;
                // Ensure addButton is centered on the line where the buttons should be
                int buttonsHorizontalCenter = labelsPosition == Constants.LABELS_ON_LEFT_SIDE
                        ? r - l - maxButtonWidth / 2
                        : maxButtonWidth / 2;
                int addButtonLeft = buttonsHorizontalCenter - addButton.getMeasuredWidth() / 2;
                addButton.layout(addButtonLeft, addButtonY, addButtonLeft + addButton.getMeasuredWidth(), addButtonY + addButton.getMeasuredHeight());

                int labelsOffset = maxButtonWidth / 2 + labelsMargin;
                int labelsXNearButton = labelsPosition == Constants.LABELS_ON_LEFT_SIDE
                        ? buttonsHorizontalCenter - labelsOffset
                        : buttonsHorizontalCenter + labelsOffset;

                int nextY = expandUp ?
                        addButtonY - buttonSpacing :
                        addButtonY + addButton.getMeasuredHeight() + buttonSpacing;

                for (int i = buttonsCount - 1; i >= 0; i--) {
                    final View child = getChildAt(i);

                    if (child == addButton || child.getVisibility() == GONE) continue;

                    int childX = buttonsHorizontalCenter - child.getMeasuredWidth() / 2;
                    int childY = expandUp ? nextY - child.getMeasuredHeight() : nextY;
                    child.layout(childX, childY, childX + child.getMeasuredWidth(), childY + child.getMeasuredHeight());

                    float collapsedTranslation = addButtonY - childY;
                    float expandedTranslation = 0f;

                    child.setTranslationY(expanded ? expandedTranslation : collapsedTranslation);
                    child.setAlpha(expanded ? 1f : 0f);

                    LayoutParams params = (LayoutParams) child.getLayoutParams();
                    params.collapseDir.setFloatValues(expandedTranslation, collapsedTranslation);
                    params.expandDir.setFloatValues(collapsedTranslation, expandedTranslation);
                    params.setAnimationsTarget(child);

                    View label = (View) child.getTag(R.id.fab_label);
                    if (label != null) {
                        int labelXAwayFromButton = labelsPosition == Constants.LABELS_ON_LEFT_SIDE
                                ? labelsXNearButton - label.getMeasuredWidth()
                                : labelsXNearButton + label.getMeasuredWidth();

                        int labelLeft = labelsPosition == Constants.LABELS_ON_LEFT_SIDE
                                ? labelXAwayFromButton
                                : labelsXNearButton;

                        int labelRight = labelsPosition == Constants.LABELS_ON_LEFT_SIDE
                                ? labelsXNearButton
                                : labelXAwayFromButton;

                        int labelTop = childY - labelsVerticalOffset + (child.getMeasuredHeight() - label.getMeasuredHeight()) / 2;

                        label.layout(labelLeft, labelTop, labelRight, labelTop + label.getMeasuredHeight());

                        Rect touchArea = new Rect(
                                Math.min(childX, labelLeft),
                                childY - buttonSpacing / 2,
                                Math.max(childX + child.getMeasuredWidth(), labelRight),
                                childY + child.getMeasuredHeight() + buttonSpacing / 2);
                        touchDelegateGroup.addTouchDelegate(new TouchDelegate(touchArea, child));

                        label.setTranslationY(expanded ? expandedTranslation : collapsedTranslation);
                        label.setAlpha(expanded ? 1f : 0f);

                        LayoutParams labelParams = (LayoutParams) label.getLayoutParams();
                        labelParams.collapseDir.setFloatValues(expandedTranslation, collapsedTranslation);
                        labelParams.expandDir.setFloatValues(collapsedTranslation, expandedTranslation);
                        labelParams.setAnimationsTarget(label);
                    }

                    nextY = expandUp ?
                            childY - buttonSpacing :
                            childY + child.getMeasuredHeight() + buttonSpacing;
                }
                break;

            case Constants.EXPAND_LEFT:
            case Constants.EXPAND_RIGHT:
                boolean expandLeft = expandDirection == Constants.EXPAND_LEFT;

                int addButtonX = expandLeft ? r - l - addButton.getMeasuredWidth() : 0;
                // Ensure addButton is centered on the line where the buttons should be
                int addButtonTop = b - t - maxButtonHeight + (maxButtonHeight - addButton.getMeasuredHeight()) / 2;
                addButton.layout(addButtonX, addButtonTop, addButtonX + addButton.getMeasuredWidth(), addButtonTop + addButton.getMeasuredHeight());

                int nextX = expandLeft ?
                        addButtonX - buttonSpacing :
                        addButtonX + addButton.getMeasuredWidth() + buttonSpacing;

                for (int i = buttonsCount - 1; i >= 0; i--) {
                    final View child = getChildAt(i);

                    if (child == addButton || child.getVisibility() == GONE) continue;

                    int childX = expandLeft ? nextX - child.getMeasuredWidth() : nextX;
                    int childY = addButtonTop + (addButton.getMeasuredHeight() - child.getMeasuredHeight()) / 2;
                    child.layout(childX, childY, childX + child.getMeasuredWidth(), childY + child.getMeasuredHeight());

                    float collapsedTranslation = addButtonX - childX;
                    float expandedTranslation = 0f;

                    child.setTranslationX(expanded ? expandedTranslation : collapsedTranslation);
                    child.setAlpha(expanded ? 1f : 0f);

                    LayoutParams params = (LayoutParams) child.getLayoutParams();
                    params.collapseDir.setFloatValues(expandedTranslation, collapsedTranslation);
                    params.expandDir.setFloatValues(collapsedTranslation, expandedTranslation);
                    params.setAnimationsTarget(child);

                    nextX = expandLeft ?
                            childX - buttonSpacing :
                            childX + child.getMeasuredWidth() + buttonSpacing;
                }

                break;
        }
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(super.generateDefaultLayoutParams());
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(super.generateLayoutParams(attrs));
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(super.generateLayoutParams(p));
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return super.checkLayoutParams(p);
    }

    private static Interpolator sExpandInterpolator = new OvershootInterpolator();
    private static Interpolator sCollapseInterpolator = new DecelerateInterpolator(3f);
    private static Interpolator sAlphaExpandInterpolator = new DecelerateInterpolator();

    private class LayoutParams extends ViewGroup.LayoutParams {

        private ObjectAnimator expandDir = new ObjectAnimator();
        private ObjectAnimator expandAlpha = new ObjectAnimator();
        private ObjectAnimator collapseDir = new ObjectAnimator();
        private ObjectAnimator collapseAlpha = new ObjectAnimator();
        private boolean animationsSetToPlay;

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);

            expandDir.setInterpolator(sExpandInterpolator);
            expandAlpha.setInterpolator(sAlphaExpandInterpolator);
            collapseDir.setInterpolator(sCollapseInterpolator);
            collapseAlpha.setInterpolator(sCollapseInterpolator);

            collapseAlpha.setProperty(View.ALPHA);
            collapseAlpha.setFloatValues(1f, 0f);

            expandAlpha.setProperty(View.ALPHA);
            expandAlpha.setFloatValues(0f, 1f);

            switch (expandDirection) {
                case Constants.EXPAND_UP:
                case Constants.EXPAND_DOWN:
                    collapseDir.setProperty(View.TRANSLATION_Y);
                    expandDir.setProperty(View.TRANSLATION_Y);
                    break;
                case Constants.EXPAND_LEFT:
                case Constants.EXPAND_RIGHT:
                    collapseDir.setProperty(View.TRANSLATION_X);
                    expandDir.setProperty(View.TRANSLATION_X);
                    break;
            }
        }

        public void setAnimationsTarget(View view) {
            collapseAlpha.setTarget(view);
            collapseDir.setTarget(view);
            expandAlpha.setTarget(view);
            expandDir.setTarget(view);

            // Now that the animations have targets, set them to be played
            if (!animationsSetToPlay) {
                collapseAnimation.play(collapseAlpha);
                collapseAnimation.play(collapseDir);
                expandAnimation.play(expandAlpha);
                expandAnimation.play(expandDir);
                animationsSetToPlay = true;
            }
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        bringChildToFront(addButton);
        buttonsCount = getChildCount();

        if (labelsStyle != 0) {
            createLabels();
        }
    }

    private void createLabels() {
        Context context = new ContextThemeWrapper(getContext(), labelsStyle);

        for (int i = 0; i < buttonsCount; i++) {
            FloatingActionButton button = (FloatingActionButton) getChildAt(i);
            String title = button.getTitle();

            if (button == addButton || title == null ||
                    button.getTag(R.id.fab_label) != null) continue;

            TextView label = new TextView(context);
            label.setTextAppearance(getContext(), labelsStyle);
            label.setText(button.getTitle());
            addView(label);

            button.setTag(R.id.fab_label, label);
        }
    }

    public void collapse() {
        if (expanded) {
            expanded = false;
            touchDelegateGroup.setEnabled(false);
            collapseAnimation.start();
            expandAnimation.cancel();

            if (listener != null) {
                listener.onMenuCollapsed();
            }
        }
    }

    public void toggle() {

        if (expanded) {
            collapse();

        } else {

            expand();
        }
    }

    public void expand() {
        if (!expanded) {
            expanded = true;
            touchDelegateGroup.setEnabled(true);
            collapseAnimation.cancel();
            expandAnimation.start();

            if (listener != null) {
                listener.onMenuExpanded();
            }
        }
    }

    public boolean isExpanded() {
        return expanded;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.expanded = expanded;

        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState savedState = (SavedState) state;
            expanded = savedState.expanded;
            touchDelegateGroup.setEnabled(expanded);

            if (rotatingDrawable != null) {
                rotatingDrawable.setRotation(expanded ? Constants.EXPANDED_PLUS_ROTATION : Constants.COLLAPSED_PLUS_ROTATION);
            }

            super.onRestoreInstanceState(savedState.getSuperState());
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    public static class SavedState extends BaseSavedState {
        public boolean expanded;

        public SavedState(Parcelable parcel) {
            super(parcel);
        }

        private SavedState(Parcel in) {
            super(in);
            expanded = in.readInt() == 1;
        }

        @Override
        public void writeToParcel(@NonNull Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(expanded ? 1 : 0);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}