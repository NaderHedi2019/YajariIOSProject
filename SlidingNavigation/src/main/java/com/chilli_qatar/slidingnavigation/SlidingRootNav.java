package com.chilli_qatar.slidingnavigation;

/**
 * Created by Ayaz Shekh 16-8-2023
 */

public interface SlidingRootNav {

    boolean isMenuClosed();

    boolean isMenuOpened();

    boolean isMenuLocked();

    void closeMenu();

    void closeMenu(boolean animated);

    void openMenu();

    void openMenu(boolean animated);

    void setMenuLocked(boolean locked);

    SlidingRootNavLayout getLayout();

}
