package com.example.artech.geoapi.hellogeospatial.helpers;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import java.util.function.Consumer;


/*
    This code defines a custom view called maptouchwrapper, which is a FrameLayout that intercepts touch events
    and notifies a listener if the user has tapped the screen within a certain threshold distance.

    The maptouchwrapper constructor initializes the touch slop, which is the minimum distance a user must move their
    finger before it is considered a scroll instead of a tap. The setup method takes a Consumer<Point> listener as an
    argument and sets it as the instance variable listener, which is called when a tap is detected.

    The distance method calculates the Euclidean distance between two points, which is used to determine if the user has
    moved their finger more than the touch slop.

    The onInterceptTouchEvent method intercepts touch events and determines if a tap has occurred. If a tap is detected
    (i.e. the user's finger moves less than the touch slop distance), the listener is notified with the location of the tap as a
    Point object, and the method returns true to indicate that the touch event has been consumed. If the user's finger moves more
    than the touch slop, the method returns false to allow scrolling or other touch events to occur.
*/


public class maptouchwrapper extends FrameLayout {

    private int touchSlop = 0;
    private Point down = null;
    private Consumer<Point> listener = null;

    public maptouchwrapper(Context context) {
        super(context);
        setup(context);
    }

    public maptouchwrapper(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setup(context);
    }

    private void setup(Context context) {
        final ViewConfiguration vc = ViewConfiguration.get(context);
        touchSlop = vc.getScaledTouchSlop();
    }

    public void setup(Consumer<Point> listener) {
        this.listener = listener;
    }

    private double distance(Point p1, Point p2) {
        double xDiff = (p1.x - p2.x);
        double yDiff = (p1.y - p2.y);
        return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (listener == null) {
            return false;
        }
        final int x = (int) event.getX();
        final int y = (int) event.getY();
        final Point tapped = new Point(x, y);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                down = tapped;
                break;
            case MotionEvent.ACTION_MOVE:
                if (down != null && distance(down, tapped) >= touchSlop) {
                    down = null;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (down != null && distance(down, tapped) < touchSlop) {
                    if (listener != null) {
                        listener.accept(tapped);
                        return true;
                    }
                }
                break;
            default:
                break;
        }
        return false;
    }


}
