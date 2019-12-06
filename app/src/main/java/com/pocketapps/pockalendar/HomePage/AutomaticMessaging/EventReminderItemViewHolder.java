package com.pocketapps.pockalendar.HomePage.AutomaticMessaging;

import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Created by chandrima on 11/04/18.
 */

public class EventReminderItemViewHolder extends RecyclerView.ViewHolder {
    public ConstraintLayout eventConstraintLayout;
    public ImageButton settingImage;
    public TextView settingsText;
    public ImageButton sendBirthdayImage;
    public TextView sendBirthdayText;

    public EventReminderItemViewHolder(View itemView) {
        super(itemView);
        eventConstraintLayout = (ConstraintLayout) itemView;
        sendBirthdayImage = (ImageButton) eventConstraintLayout.getChildAt(0);
        sendBirthdayText = (TextView) eventConstraintLayout.getChildAt(1);
        settingImage = (ImageButton) eventConstraintLayout.getChildAt(2);
        settingsText = (TextView) eventConstraintLayout.getChildAt(3);
    }
}
