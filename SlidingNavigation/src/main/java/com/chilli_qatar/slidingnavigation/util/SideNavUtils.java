package com.chilli_qatar.slidingnavigation.util;

/**
 * Created by Ayaz Shekh on 16-8-2023
 */

public abstract class SideNavUtils {

    public static float evaluate(float fraction, float startValue, float endValue) {
        return startValue + fraction * (endValue - startValue);
    }
}
