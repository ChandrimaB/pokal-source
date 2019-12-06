package com.pocketapps.pockalendar.SchedulePage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.pocketapps.pockalendar.R;

/**
 * Created by chandrima on 18/04/18.
 */

public class SwipeToDeleteController extends ItemTouchHelper.Callback {
    private FullCalendarRecyclerViewAdapter mAdapter;
    private Context mContext;
    private Paint mPaint;
    public SwipeToDeleteController(FullCalendarRecyclerViewAdapter adapter, Context context) {
        mAdapter = adapter;
        mContext = context;
        mPaint = new Paint();
        mPaint.setColor(Color.parseColor("#FF4081"));
    }
    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        if (!mAdapter.shouldSwipe(viewHolder.getAdapterPosition()))
            return 0;
        return makeMovementFlags(0, ItemTouchHelper.RIGHT);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        mAdapter.notifyDelete(viewHolder.getAdapterPosition());
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            RectF background = new RectF((float) viewHolder.itemView.getLeft(), (float) viewHolder.itemView.getTop(), (float) viewHolder.itemView.getRight(), (float) viewHolder.itemView.getBottom());
            c.drawRect(background,mPaint);
            Bitmap icon = getBitmapFromVectorDrawable(mContext, R.drawable.ic_delete_24dp);
            RectF iconDest = new RectF(new RectF((float) viewHolder.itemView.getLeft(), (float) viewHolder.itemView.getTop(), (float) viewHolder.itemView.getRight()/4, (float) viewHolder.itemView.getBottom()));

            c.drawBitmap(icon,null,iconDest,mPaint);
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
