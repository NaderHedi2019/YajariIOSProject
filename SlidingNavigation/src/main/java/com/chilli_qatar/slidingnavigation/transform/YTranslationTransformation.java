package com.chilli_qatar.slidingnavigation.transform;

import android.view.View;

import com.chilli_qatar.slidingnavigation.util.SideNavUtils;


/**
 * Created by Ayaz Shekh on 16-8-2023
 */

public class YTranslationTransformation implements RootTransformation {

    private static final float START_TRANSLATION = 0f;

    private final float endTranslation;

    public YTranslationTransformation(float endTranslation) {
        this.endTranslation = endTranslation;
    }

    @Override
    public void transform(float dragProgress, View rootView) {
        float translation = SideNavUtils.evaluate(dragProgress, START_TRANSLATION, endTranslation);
        rootView.setTranslationY(translation);
    }
}
