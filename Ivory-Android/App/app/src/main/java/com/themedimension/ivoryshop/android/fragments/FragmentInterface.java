/*
 * Copyright © 2018-present, MNK Group. All rights reserved.
 */

package com.themedimension.ivoryshop.android.fragments;

import com.themedimension.ivoryshop.android.activities.fragmentActivities.DrawerActivity;
import com.themedimension.ivoryshop.android.activities.fragmentActivities.FavoritesActivity;

/**
 * This interface enables the communication and data persistence
 * between sibling fragments in a <class> ViewPager </class>.
 * <p>
 * The interface is necessary as a result of the
 * fragments lifecycle {@link android.support.v4.app.Fragment}.
 * For further details about their lifecycle,
 *
 * @see <a href="https://developer.android.com/guide/components/fragments.html"> Fragments </a>
 * <p>
 * For app usage of the interface,
 * @see DrawerActivity
 * @see FavoritesActivity
 */

public interface FragmentInterface {
    //  The interface will declare that the fragments will be constraint to do
    // some kind of action when they become visible to the user.
    void fragmentIsVisible();

    void onFilterButtonClicked();

    void onSearchViewOpened(String string);
    void onSearchViewClicked();
    void onSearchViewClosed();
    void onRefreshFragment();
}
