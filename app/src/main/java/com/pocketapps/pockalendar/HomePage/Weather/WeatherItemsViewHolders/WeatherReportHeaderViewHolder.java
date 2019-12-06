package com.pocketapps.pockalendar.HomePage.Weather.WeatherItemsViewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by chandrima on 23/03/18.
 */

public class WeatherReportHeaderViewHolder extends RecyclerView.ViewHolder {
    public LinearLayout linearLayout;
    public TextView headerText;
    public Button allowAccessButton;

    public WeatherReportHeaderViewHolder(View itemView) {
        super(itemView);
        linearLayout = (LinearLayout) itemView;
        headerText = (TextView) linearLayout.getChildAt(0);
        allowAccessButton = (Button) linearLayout.getChildAt(1);
    }
}
