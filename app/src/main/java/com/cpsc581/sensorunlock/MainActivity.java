package com.cpsc581.sensorunlock;

import android.animation.Animator;
import android.animation.FloatEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.renderscript.Sampler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.devs.vectorchildfinder.VectorChildFinder;
import com.devs.vectorchildfinder.VectorDrawableCompat;

/*
* Referenced from https://www.javacodegeeks.com/2013/09/android-compass-code-example.html
* */

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "SensorUnlock";
    private SensorManager sensorManager;
    private Sensor oSensor;
    private float check = 0f;
    private static float startDegree = 0f;
    RotateAnimation rotateAnimation;
    float currentAlpha;
    float targetDotDegree;

    float comboCode1 = 100f;
    float comboCode2 = 359f;
    float comboCode3 = 90f;

    boolean isFirst = true;
    ImageView userDotRed;
    ImageView userDotBlue;
    ImageView userDotWhite;
    ImageView targetDot1;
    ImageView targetDot2;
    TextView degreeText;
    View background;

    ImageView lock;

    Vibrator vibrator;

    Drawable dotDrawable;
    float currentDegree = 0f;
    int[] posXY = new int[2];
    int userX;
    int userY;
    int statusFlag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        oSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        userDotRed = (ImageView) findViewById(R.id.userView);
        userDotBlue = (ImageView) findViewById(R.id.userViewBlue);
        userDotWhite = (ImageView) findViewById(R.id.userViewWhite);
        targetDot1 = (ImageView) findViewById(R.id.targetDot1);
        targetDot2 = (ImageView) findViewById(R.id.targetDot2);
        degreeText = (TextView) findViewById(R.id.degreeTextView);
        background = (View) findViewById(R.id.background);
        background.setBackgroundColor(Color.BLACK);
        degreeText.setTextColor(Color.WHITE);

        lock = findViewById(R.id.lockView);
        lock.setColorFilter(Color.RED);

        dotDrawable = getResources().getDrawable(R.drawable.userdot_drawable);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        currentAlpha = Math.abs(comboCode1-90) * 0.011111f;
        userDotRed.setAlpha(currentAlpha);
        userDotBlue.setAlpha(0f);
        userDotWhite.setAlpha(1f);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        isFirst = true;
        // for the system's orientation sensor registered listeners
        sensorManager.registerListener(this, oSensor,
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        if(event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            getOrientation(event);
        }
    }

    public int modulo( float m, int n ){
        int mod =  (int) m % n ;
        return ( mod < 0 ) ? mod + n : mod;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int value){
        return;
    }

    private void getOrientation(SensorEvent event){
        //TODO Use this value to rotate something on the screen
        if (statusFlag == 0){
            targetDotDegree = comboCode1;
        }
        else if (statusFlag == 1){
            targetDotDegree = comboCode2;
        } else {
            targetDotDegree = comboCode3;
        }
        //timeView.setText(Float.toString(Math.round(event.values[0])));
        if(isFirst == true){
            startDegree = Math.round(event.values[0]);
            isFirst = false;
        }
        float actualDegree = Math.round(event.values[0]);
        float degree = modulo(((actualDegree) - startDegree),360);

        rotateAnimation = new RotateAnimation(currentDegree, degree, Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT, 0.25f);
        /*
        rotateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                userDot.getLocationInWindow(posXY);
                userX = posXY[0];
                userY = posXY[1];
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                userDot.getLocationInWindow(posXY);
                userX = posXY[0];
                userY = posXY[1];
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                userDot.getLocationInWindow(posXY);
                userX = posXY[0];
                userY = posXY[1];
            }
        });
        */

        rotateAnimation.setDuration(200);

        rotateAnimation.setFillAfter(true);

        if(modulo(targetDotDegree - degree,360) < 90){
            currentAlpha = userDotRed.getAlpha();
            userDotBlue.setAlpha(0f);
            float alphaNumber = 0.0111111f * Math.abs(modulo((targetDotDegree - degree), 360) - 90);
            Log.v(TAG, "Alpha Number"+  Float.toString(alphaNumber));
            userDotRed.setAlpha(alphaNumber);
        }
        if(modulo(targetDotDegree - degree,360) > 90 &&modulo(targetDotDegree - degree,360) < 180){
            userDotRed.setAlpha(0f);
            currentAlpha = userDotBlue.getAlpha();
            userDotBlue.setAlpha(1- (0.0111f * Math.abs(modulo((degree - targetDotDegree), 360) - 180)));
        }
        if(modulo(targetDotDegree - degree,360) > 180 &&modulo(targetDotDegree - degree,360) < 270){
            userDotRed.setAlpha(0f);
            currentAlpha = userDotBlue.getAlpha();
            userDotBlue.setAlpha(1- (0.0111f * Math.abs(modulo((degree - targetDotDegree), 360) - 180)));
        }
        if(modulo(targetDotDegree - degree,360) > 270 &&modulo(targetDotDegree - degree,360) < 360){
            currentAlpha = userDotRed.getAlpha();
            userDotBlue.setAlpha(0f);
            float alphaNumber = 0.011111111f * Math.abs(modulo((degree - targetDotDegree), 360) - 90);
            Log.v(TAG, "Alpha Number"+  Float.toString(alphaNumber));
            userDotRed.setAlpha(alphaNumber);
        }

        /*
        // Distance between target dot and user dot is increasing
        if(Math.abs(180 - degree) < Math.abs(180 - currentDegree)){
            userDotRed.setAlpha(0.00555556f * Math.abs(180-degree));
            //userDotRed.setAlpha(currentAlpha - 0.00555556f);
        }
        if(Math.abs(180 - degree) > Math.abs(180 - currentDegree)){
            userDotRed.setAlpha(0.00555556f * Math.abs(180-degree));
            //userDotRed.setAlpha(currentAlpha + 0.00555556f);
        }
        */



        userDotRed.startAnimation(rotateAnimation);
        userDotBlue.startAnimation(rotateAnimation);
        userDotWhite.startAnimation(rotateAnimation);
        if(check != degree);
        {
            check = degree;
            Log.v(TAG, "Check Degrees: " + Float.toString(check));
            Log.v(TAG, "Start Degrees: " + Float.toString(startDegree));
            Log.v(TAG, "Actual Degree: " + Float.toString(actualDegree));
            Log.v(TAG, "Previous Degree: " + Float.toString(currentDegree));
            Log.v(TAG, "Status Flag: " + Integer.toString(statusFlag));

            degreeText.setText(Float.toString(check) + "°");
            //changeBackgroundColour(check);
            statusFlag = setTargets(check, statusFlag);
        }
        currentDegree = degree;
    }

    public void changeBackgroundColour(float check){
        int dotColour = Color.WHITE;
        if(check >= 0 && check < 90){
            dotColour = Color.BLUE;
            dotDrawable.setColorFilter(new PorterDuffColorFilter(0xffff00, PorterDuff.Mode.SRC_IN));
            //background.setBackgroundColor(Color.BLUE);
            //degreeText.setTextColor(Color.WHITE);
        } else if(check >= 90 && check < 180){
            dotColour = Color.GREEN;
            dotDrawable.setColorFilter(new PorterDuffColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY));
            //background.setBackgroundColor(Color.GREEN);
            //degreeText.setTextColor(Color.WHITE);
        } else if(check >= 180 && check < 270){
            dotColour = Color.MAGENTA;
            dotDrawable.setColorFilter(new PorterDuffColorFilter(Color.MAGENTA, PorterDuff.Mode.MULTIPLY));
            //background.setBackgroundColor(Color.MAGENTA);
            //degreeText.setTextColor(Color.BLACK);
        } else if(check >= 270 && check <= 360){
            dotColour = Color.YELLOW;
            dotDrawable.setColorFilter(new PorterDuffColorFilter(Color.YELLOW, PorterDuff.Mode.MULTIPLY));
           // background.setBackgroundColor(Color.YELLOW);
            //degreeText.setTextColor(Color.BLACK);
        }

        dotDrawable.setColorFilter(dotColour, PorterDuff.Mode.MULTIPLY);

        userDotRed.setImageDrawable(dotDrawable);
        userDotBlue.setImageDrawable(dotDrawable);
        userDotWhite.setImageDrawable(dotDrawable);
    }

    public int setTargets(float check, int statusFlag){
       targetDot1.setVisibility(View.INVISIBLE);
       targetDot2.setVisibility(View.INVISIBLE);
        //First Target at comboCode1 degrees
        //Second Target at comboCode2 degrees
        if(check == 0 && statusFlag == 0){
            //targetDot1.setVisibility(View.VISIBLE);
            //targetDot2.setVisibility(View.INVISIBLE);
        } else if(check == comboCode1 && statusFlag == 0){
            statusFlag = statusFlag + 1;
            pulseLock();
            //targetDot1.setVisibility(View.INVISIBLE);
            //targetDot2.setVisibility(View.VISIBLE);
        } else if((check == comboCode2) && (statusFlag == 1)){
            statusFlag = statusFlag + 1;
            pulseLock();
        } else if((check == comboCode3) && (statusFlag == 2)){
            //targetDot2.setVisibility(View.INVISIBLE);
            unlockPhone();

            statusFlag = statusFlag + 1;
            sensorManager.unregisterListener(this);
        }
        return statusFlag;
    }

    private Animator pulseLock() {
        ValueAnimator anim = ValueAnimator.ofObject(new FloatEvaluator(), 1f, 1.5f, 1f);
        anim.setDuration(300);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                lock.setScaleX((float)animation.getAnimatedValue());
                lock.setScaleY((float)animation.getAnimatedValue());
            }
        });
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        anim.start();

        return anim;
    }

    private void unlockPhone() {
        //degreeText.setText("Unlocked!");

        final VectorDrawableCompat.VFullPath unlockbar = new VectorChildFinder(this, R.drawable.ic_lock, lock).findPathByName("ubar");

        final ValueAnimator anim = ValueAnimator.ofObject(new FloatEvaluator(), 1f, 0.75f);
        anim.setDuration(250);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                unlockbar.setTrimPathEnd((float)animation.getAnimatedValue());
                lock.invalidate();
            }
        });
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                lock.setColorFilter(Color.GREEN);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        finish();
                    }
                }, 250);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        pulseLock().addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                anim.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }
}
