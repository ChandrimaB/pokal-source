package com.pocketapps.pockalendar.AppSigninPages;

import android.animation.Animator;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.pocketapps.pockalendar.R;


/**
 * Created by chandrima on 12/03/18.
 * This class shows a simple animation before first time signing in.
 * After animation ends, SigninActivity is shown.
 */

public class AnimatedLoadingActivity extends AppCompatActivity {

    private ImageView mPokalLogo;

    private static long DURATION = 1000;
    private static long DELAYMILLS = 1500;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_activity_layout);

        mPokalLogo = (ImageView)findViewById(R.id.pokal_logo);

        final AnimatorListenerImpl animatorListener = new AnimatorListenerImpl();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mPokalLogo.animate()
                        .rotation(360f)
                        .setDuration(DURATION)
                        .scaleX(0.1f)
                        .scaleY(0.1f)
                        .setListener(animatorListener);
            }
        },DELAYMILLS);
    }

    private class AnimatorListenerImpl implements Animator.AnimatorListener {

        @Override
        public void onAnimationStart(Animator animator) {

        }

        @Override
        public void onAnimationEnd(Animator animator) {
            Intent intent = new Intent(getApplicationContext(), SigninActivity.class);
            startActivity(intent);
            finish();
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    }
}
