package com.cpsc581.sensorunlock;

import android.graphics.Color;
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

/*
* Referenced from https://www.javacodegeeks.com/2013/09/android-compass-code-example.html
* */

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "SensorUnlock";
    private SensorManager sensorManager;
    private Sensor oSensor;
    private float check = 0f;
    private static float startDegree = 0f;

    boolean isFirst = true;
    ImageView userDot;
    TextView degreeText;
    View background;
    float currentDegree = 0f;
    int[] posXY = new int[2];
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
        userDot = (ImageView) findViewById(R.id.userView);
        degreeText = (TextView) findViewById(R.id.degreeTextView);
        background = (View) findViewById(R.id.background);
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

        //timeView.setText(Float.toString(Math.round(event.values[0])));
        if(isFirst == true){
            startDegree = Math.round(event.values[0]);
            isFirst = false;
        }
        float random = Math.round(event.values[0]);
        float degree = modulo(((random) - startDegree),360);

        RotateAnimation rotateAnimation = new RotateAnimation(currentDegree, degree, Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT, 0.25f);

        rotateAnimation.setDuration(200);

        rotateAnimation.setFillAfter(true);

        userDot.startAnimation(rotateAnimation);
        currentDegree = degree;
        if(check != degree);
        {
            userDot.getLocationOnScreen(posXY);
            int userDotX = posXY[0];
            int userDotY = posXY[1];
            check = degree;
            Log.v(TAG, "Check Degrees:" + Float.toString(check));
            Log.v(TAG, "Start Degrees:" + Float.toString(startDegree));
            Log.v(TAG, "Actual Degree:" + Float.toString(random));
            Log.v(TAG, "User X: " + Integer.toString(userDotX));
            Log.v(TAG, "User Y: " + Integer.toString(userDotY));
            degreeText.setText(Float.toString(check) + "°");
            if(check >= 0 && check < 90){
                background.setBackgroundColor(Color.BLUE);
                degreeText.setTextColor(Color.WHITE);
            } else if(check >= 90 && check < 180){
                background.setBackgroundColor(Color.GREEN);
                degreeText.setTextColor(Color.WHITE);
            } else if(check >= 180 && check < 270){
                background.setBackgroundColor(Color.MAGENTA);
                degreeText.setTextColor(Color.BLACK);
            } else if(check >= 270 && check <= 360){
                background.setBackgroundColor(Color.YELLOW);
                degreeText.setTextColor(Color.BLACK);
            }
        }
    }
}
