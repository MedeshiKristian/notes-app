package com.uzhnu.notesapp.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrListener;
import com.uzhnu.notesapp.R;
import com.uzhnu.notesapp.events.LockSlidrEvent;
import com.uzhnu.notesapp.events.UnlockSlidrEvent;
import com.uzhnu.notesapp.utilities.Constants;
import com.uzhnu.notesapp.utilities.PreferencesManager;
import com.uzhnu.notesapp.utilities.ThemeUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class SlidrActivity extends AppCompatActivity {
    private AppCompatActivity activity;
    protected SlidrInterface slidrInterface;
    protected View backgroundView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);

        SlidrConfig config = new SlidrConfig.Builder()
                .listener(new SlidrListener() {
                    @Override
                    public void onSlideStateChanged(int state) {
                    }

                    @Override
                    public void onSlideChange(float percent) {
                        float coefficient = 0.25f;
                        if (MainActivity.isRunning && backgroundView == null) {
                            getBackground();
                        }
                        if (backgroundView != null) {
                            float moveFactor = backgroundView.getWidth() * percent * coefficient;
                            backgroundView.setTranslationX(-moveFactor);
                        }
                    }

                    @Override
                    public void onSlideOpened() {
                    }

                    @Override
                    public boolean onSlideClosed() {
                        return false;
                    }
                })
                .build();

        slidrInterface = Slidr.attach(SlidrActivity.this, config);

    }

    private void getBackground() {
        activity = (AppCompatActivity) PreferencesManager
                .getInstance().get(Constants.KEY_MAIN_ACTIVITY);
        if (activity != null) {
            backgroundView = activity.findViewById(R.id.coordinatorContent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Subscribe
    public void onLockSlidrEvent(LockSlidrEvent event) {
        getWindow().setStatusBarColor(ThemeUtil.getPrimary(getApplicationContext()));
        slidrInterface.lock();
    }

    @Subscribe
    public void onUnLockSlidrEvent(UnlockSlidrEvent event) {
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        slidrInterface.unlock();
    }

    @Override
    public void onBackPressed() {
        if (MainActivity.isRunning) {
            if (backgroundView != null) {
                backgroundView.setTranslationX(0);
            }
            super.onBackPressed();
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}