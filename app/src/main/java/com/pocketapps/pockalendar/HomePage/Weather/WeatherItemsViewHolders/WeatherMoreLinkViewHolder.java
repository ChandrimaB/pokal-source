package com.pocketapps.pockalendar.HomePage.Weather.WeatherItemsViewHolders;

import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.widget.TextView;

/**
 * Created by chandrima on 14/04/18.
 */

public class WeatherMoreLinkViewHolder extends RecyclerView.ViewHolder {
    public TextView link;
    public WeatherMoreLinkViewHolder(View itemView) {
        super(itemView);
        link = (TextView)itemView;
        link.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
