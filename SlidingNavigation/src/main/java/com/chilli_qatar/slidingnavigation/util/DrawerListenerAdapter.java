package com.chilli_qatar.slidingnavigation.util;

import android.view.View;

import androidx.drawerlayout.widget.DrawerLayout;

import com.chilli_qatar.slidingnavigation.callback.DragListener;
import com.chilli_qatar.slidingnavigation.callback.DragStateListener;

/**
 * Created by Ayaz Shekh on 16-8-2023
 */

public class DrawerListenerAdapter implements DragListener, DragStateListener {

    private DrawerLayout.DrawerListener adaptee;
    private View drawer;

    public DrawerListenerAdapter(DrawerLayout.DrawerListener adaptee, View drawer) {
        this.adaptee = adaptee;
        this.drawer = drawer;
    }

    @Override
    public void onDrag(float progress) {
        adaptee.onDrawerSlide(drawer, progress);
    }

    @Override
    public void onDragStart() {
        adaptee.onDrawerStateChanged(DrawerLayout.STATE_DRAGGING);
    }

    @Override
    public void onDragEnd(boolean isMenuOpened) {
        if (isMenuOpened) {
            adaptee.onDrawerOpened(drawer);
        } else {
            adaptee.onDrawerClosed(drawer);
        }
        adaptee.onDrawerStateChanged(DrawerLayout.STATE_IDLE);
    }
}
