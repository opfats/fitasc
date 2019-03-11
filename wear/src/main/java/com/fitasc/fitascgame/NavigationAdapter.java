package com.fitasc.fitascgame;

/*
This is not used in current implementation.
*/

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.wear.widget.drawer.WearableNavigationDrawerView;

class NavigationAdapter
        extends WearableNavigationDrawerView.WearableNavigationDrawerAdapter {

    private final Context mContext;

    public NavigationAdapter(Context context) {
        mContext = context;
    }


    //setting values only for two items :-> settings, home.

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public String getItemText(int pos) {
         if(pos == 1){
             return "settings";
        }else {
             return "Home";
         }

    }

    @Override
    public Drawable getItemDrawable(int pos) {

        if(pos == 1) {

            return mContext.getDrawable(R.drawable.ic_settings_applications_black_24dp);
        }
        else {
            return mContext.getDrawable(R.drawable.ic_home_black_24dp);
        }
    }
}
