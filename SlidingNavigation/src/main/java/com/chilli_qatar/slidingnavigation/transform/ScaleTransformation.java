package com.chilli_qatar.slidingnavigation.transform;

import android.view.View;

import com.chilli_qatar.slidingnavigation.util.SideNavUtils;


/**
 * Created by Ayaz Shekh on 16-8-2023
 */

public class ScaleTransformation implements RootTransformation {

    private static final float START_SCALE = 1f;

    private final float endScale;

    public ScaleTransformation(float endScale) {
        this.endScale = endScale;
    }

    @Override
    public void transform(float dragProgress, View rootView) {
        float scale = SideNavUtils.evaluate(dragProgress, START_SCALE, endScale);
        rootView.setScaleX(scale);
        rootView.setScaleY(scale);
    }
}
