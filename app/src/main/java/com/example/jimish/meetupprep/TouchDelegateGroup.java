package com.example.jimish.meetupprep;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by jimish on 25/3/15.
 */
public class TouchDelegateGroup extends TouchDelegate {
    private static final Rect USELESS_HACKY_RECT = new Rect();
    private final ArrayList<TouchDelegate> touchDelegates = new ArrayList<TouchDelegate>();
    private TouchDelegate currentTouchDelegate;
    private boolean enabled;

    public TouchDelegateGroup(View uselessHackyView) {
        super(USELESS_HACKY_RECT, uselessHackyView);
    }

    public void addTouchDelegate(@NonNull TouchDelegate touchDelegate) {
        touchDelegates.add(touchDelegate);
    }

    public void removeTouchDelegate(TouchDelegate touchDelegate) {
        touchDelegates.remove(touchDelegate);
        if (currentTouchDelegate == touchDelegate) {
            currentTouchDelegate = null;
        }
    }

    public void clearTouchDelegates() {
        touchDelegates.clear();
        currentTouchDelegate = null;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (!enabled) return false;

        TouchDelegate delegate = null;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                for (int i = 0; i < touchDelegates.size(); i++) {
                    TouchDelegate touchDelegate = touchDelegates.get(i);
                    if (touchDelegate.onTouchEvent(event)) {
                        currentTouchDelegate = touchDelegate;
                        return true;
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                delegate = currentTouchDelegate;
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                delegate = currentTouchDelegate;
                currentTouchDelegate = null;
                break;
        }

        return delegate != null && delegate.onTouchEvent(event);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}

