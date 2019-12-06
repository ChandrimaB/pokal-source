package com.pocketapps.pockalendar.HomePage.Calendar;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by chandrima on 30/03/18.
 */

public class CalendarHeaderViewHolder extends RecyclerView.ViewHolder {
    public TextView mTextView;
    public CalendarHeaderViewHolder(View itemView) {
        super(itemView);
        mTextView = (TextView)itemView;
    }
}
