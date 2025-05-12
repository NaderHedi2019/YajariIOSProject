package com.chilli_qatar.slidingnavigation.transform;

import android.view.View;

import java.util.List;

/**
 * Created by Ayaz Shekh on 16-8-2023
 */

public class CompositeTransformation implements RootTransformation {

    private List<RootTransformation> transformations;

    public CompositeTransformation(List<RootTransformation> transformations) {
        this.transformations = transformations;
    }

    @Override
    public void transform(float dragProgress, View rootView) {
        for (RootTransformation t : transformations) {
            t.transform(dragProgress, rootView);
        }
    }
}
