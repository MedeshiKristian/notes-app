package com.uzhnu.notesapp.callbacks;

import static androidx.core.content.ContextCompat.getSystemService;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.uzhnu.notesapp.R;
import com.uzhnu.notesapp.adapters.NotesAdapter;

abstract public class SwipeToDeleteCallback extends ItemTouchHelper.Callback {
    private static final float SWIPE_THRESHOLD = 0.45f;
    private Context context;
    private Paint clearPaint;
    private ColorDrawable backgroundDrawable;
    private int backgroundColor;
    private boolean vibrated;
    private Drawable deleteDrawable;
    private int intrinsicWidth;
    private int intrinsicHeight;
    private NotesAdapter adapter;

    public SwipeToDeleteCallback(Context context, NotesAdapter adapter) {
        this.context = context;
        this.adapter = adapter;
        backgroundDrawable = new ColorDrawable();
        backgroundColor = ContextCompat.getColor(context, R.color.primary);
        clearPaint = new Paint();
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        deleteDrawable = ContextCompat.getDrawable(this.context, R.drawable.ic_outline_delete_24);
        assert deleteDrawable != null;
        intrinsicWidth = deleteDrawable.getIntrinsicWidth();
        intrinsicHeight = deleteDrawable.getIntrinsicHeight();
        vibrated = false;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0, ItemTouchHelper.LEFT);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder viewHolder1) {
        return false;
    }

    @Override
    public void onChildDraw(@NonNull Canvas c,
                            @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        View itemView = viewHolder.itemView;
        int itemHeight = itemView.getHeight();

        boolean isCancelled = dX == 0 && !isCurrentlyActive;

        int left = itemView.getRight();
        int top = itemView.getTop();
        int right = itemView.getRight();
        int bottom = itemView.getBottom();

        if (isCancelled) {
            clearCanvas(c, (float) left + dX, (float) top, (float) right, (float) bottom);
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            return;
        }

        float swipeThreshold = viewHolder.itemView.getWidth() * getSwipeThreshold(viewHolder);
        if (Math.abs(dX) >= swipeThreshold) {
            if (!vibrated) {
                vibrated = true;
                vibrateOnce();
            }
            backgroundColor = ContextCompat.getColor(context, R.color.greyColor);
        } else if (Math.abs(dX) < swipeThreshold) {
            if (vibrated) {
                vibrated = false;
                vibrateOnce();
            }
            backgroundColor = ContextCompat.getColor(context, R.color.primary);
        }

        backgroundDrawable.setColor(backgroundColor);
        backgroundDrawable.setBounds(left + (int) dX - 100, top, right, bottom);
        backgroundDrawable.draw(c);

        int deleteIconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
        int deleteIconMargin = (itemHeight - intrinsicHeight) / 2;
        int deleteIconLeft = itemView.getRight() - deleteIconMargin - intrinsicWidth;
        int deleteIconRight = itemView.getRight() - deleteIconMargin;
        int deleteIconBottom = deleteIconTop + intrinsicHeight;

        deleteDrawable.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
        deleteDrawable.draw(c);
    }

    private void vibrateOnce() {
        long vibrationDuration = 75;
        Vibrator vibrator = (Vibrator) getSystemService(context, Vibrator.class);
        assert vibrator != null;
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(vibrationDuration);
        }
    }

    private void clearCanvas(@NonNull Canvas c, Float left, Float top, Float right, Float bottom) {
        c.drawRect(left, top, right, bottom, clearPaint);
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return SWIPE_THRESHOLD;
    }
}
