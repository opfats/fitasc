package com.fitasc.fitascgame;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.gesture.Gesture;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.wear.ambient.AmbientModeSupport;
import android.support.wear.widget.drawer.WearableNavigationDrawerView;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.DismissOverlayView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

public class MainActivity extends WearableActivity {


    private TextView mTextView;

    private static final String TAG = "MainActivity";
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final long COUNT_DOWN_MS = TimeUnit.SECONDS.toMillis(10);
    private static final long MILLIS_IN_SECOND = TimeUnit.SECONDS.toMillis(1);

    private ProgressBar mProgressBar;
    private CountDownTimer mCountDownTimer;
    private int amp = 100;
    private Vibrator v;
    private TextView shotDetection, TimerText;
    private int vib, reset;
    private static volatile boolean startedOrNot;
    private int testingMaxAmp;
    private Thread TimerThread;
    private Thread runner;
    private int anInt;

    private SharedPreferences sp;
    private int THValue;
    private int THTime;


    private MediaRecorder mRecorder;
    private static double mEMA = 0.0;
    static final private double EMA_FILTER = 0.7;

    private AmbientModeSupport.AmbientController mAmbientController;

    final Runnable updater = new Runnable() {

        public void run() {

            //if (shouldUpdate) {
            updateTv();

        }

    };
    final Handler mHandler = new Handler();
    private int j;
    private volatile boolean sys;
    private volatile boolean started;
    private int bias;

    private GestureDetector mDetector;
    private String DEBUG_TAG = "see";
    private boolean ShouldUpdate;
    //private WearableNavigationDrawerView mWearableNavigationDrawer;

    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mTextView = (TextView) findViewById(R.id.text);

        sp = this.getSharedPreferences("game_prefs", Activity.MODE_PRIVATE);

        anInt = 0;

        // Enables Always-on
        setAmbientEnabled();

        ShouldUpdate = true;

        mTextView = (TextView) findViewById(R.id.text);
        shotDetection = (TextView) findViewById(R.id.shotFiredText);
        TimerText = (TextView) findViewById(R.id.countUpTimer);

        viewUpdater();
        // Enables Always-on
        setAmbientEnabled();

        mProgressBar = findViewById(R.id.progressBar);

        checkPermissions();

        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


        // Configure a gesture detector
        mDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public void onLongPress(MotionEvent event) {

                Log.d(DEBUG_TAG, " onLongPress: " + event.toString());
            }

            @Override
            public boolean onDown(MotionEvent event) {

                Log.d(DEBUG_TAG, " onDown: " + event.toString());
                return true;
            }

            @Override
            public boolean onFling(MotionEvent event1, MotionEvent event2,
                                   float velocityX, float velocityY) {
                Intent intent = new Intent(MainActivity.this, settings.class);
                startActivity(intent);
                Log.d(DEBUG_TAG, " onFling: " + event1.toString() + event2.toString());
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                                    float distanceY) {
                Log.d(DEBUG_TAG, " onScroll: " + e1.toString() + e2.toString());
                return true;
            }

            @Override
            public void onShowPress(MotionEvent event) {
                Log.d(DEBUG_TAG, " onShowPress: " + event.toString());
            }

            @Override
            public boolean onSingleTapUp(MotionEvent event) {


                Log.d(DEBUG_TAG, " onSingleTapUp: " + event.toString());
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent event) {
                Log.d(DEBUG_TAG, " onDoubleTap: " /*+ event.toString()*/);
                return true;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent event) {
                Log.d(DEBUG_TAG, " onDoubleTapEvent: " /*+ event.toString()*/);
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent event) {
                Log.d(DEBUG_TAG, " onSingleTapConfirmed: " /*+ event.toString()*/);
                return true;
            }
        });
    }

    // Capture long presses
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mDetector.onTouchEvent(ev) || super.onTouchEvent(ev);
    }


    private void viewUpdater() {

        shotDetection.setVisibility(View.INVISIBLE);
        TimerText.setVisibility(View.INVISIBLE);

        sys = true;
        started = true;

        bias = 10;

        THValue = sp.getInt("thValue", this.getResources().getInteger(R.integer.DefaultThresholdValue));

        THTime = sp.getInt("thTime", this.getResources().getInteger(R.integer.DefaultCountDownTime));

        reset = THTime + 10;
        vib = THTime + 1;

    }


    private void startRecording() {
        if (runner == null) {
            runner = new Thread() {
                public void run() {
                    while (runner != null) {
                        try {
                            Thread.sleep(100);
                            Log.d("Noise", "running runner()");

                        } catch (InterruptedException e) {
                        }
                        mHandler.post(updater);
                    }
                }
            };
            runner.start();
            Log.d("Noise", "start runner()");
        }

    }

    public void startRecorder() {

        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.UNPROCESSED);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile("/dev/null");
            try {
                mRecorder.prepare();
            } catch (java.io.IOException ioe) {
                android.util.Log.e("[Monkey]", "IOException: " +
                        android.util.Log.getStackTraceString(ioe));

            } catch (java.lang.SecurityException e) {
                android.util.Log.e("[Monkey]", "SecurityException: " +
                        android.util.Log.getStackTraceString(e));
            }
            try {
                mRecorder.start();
            } catch (java.lang.SecurityException e) {
                android.util.Log.e("[Monkey]", "SecurityException: " +
                        android.util.Log.getStackTraceString(e));
            }

            //mEMA = 0.0;
        }
    }


    void ThreadMaker() {

        TimerThread = new Thread() {

            @Override
            public void run() {
                try {
                    while (TimerThread != null) {
                        Thread.sleep(1000);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // update here!

                                increment();

                                TimerText.setText(String.valueOf(anInt));

                                if (anInt >= THTime) {
                                    TimerText.setTextColor(Color.RED);

                                    if (anInt < vib) {

                                        sys = false;

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            v.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
                                        } else {
                                            //deprecated in API 26 - wear starts also
                                            v.vibrate(300);
                                        }

                                    }


                                    if (anInt >= vib) {

                                        if (!sys) {
                                            sys = true;
                                        }
                                    }

                                    if (anInt > reset) {

                                        resetter();
                                    }
                                }

                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

    }

    private void resetter() {
        anInt = 0;
        sys = true;
        shotDetection.setVisibility(View.INVISIBLE);
        TimerText.setVisibility(View.INVISIBLE);
        if (TimerThread != null) {
            TimerThread.interrupt();
            try {
                TimerThread.join();
                TimerThread = null;

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    private void increment() {
        anInt++;
    }


    /**
     * Checks the permission that this app needs and if it has not been granted, it will
     * prompt the user to grant it, otherwise it shuts down the app.
     */
    private void checkPermissions() {
        boolean recordAudioPermissionGranted =
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        == PackageManager.PERMISSION_GRANTED;

        if (recordAudioPermissionGranted) {
            start();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSIONS_REQUEST_CODE);

        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                start();
            } else {
                // Permission has been denied before. At this point we should show a dialog to
                // user and explain why this permission is needed and direct him to go to the
                // Permissions settings for the app in the System settings. For this sample, we
                // simply exit to get to the important part.
                Toast.makeText(this, R.string.exiting_for_permissions, Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }


    /**
     * Starts the main flow of the application.
     */
    private void start() {

        startedOrNot = true;

        startRecording();

        startRecorder();


    }

    public double getAmplitude() {
       if (mRecorder != null) {
            int max =mRecorder.getMaxAmplitude();
            testingMaxAmp=max;
            return max;
        }
        else
            return 0;

    }

    public double getAmplitudeEMA() {
        double amp = getAmplitude();
        mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;
        return mEMA;
    }


    public void updateTv() {

        j = (int) getAmplitudeEMA() / amp;

        if (started) {
            if (j >= 100) {
                mProgressBar.setProgress(j);
                ((TextView) findViewById(R.id.currDecibel)).setText("f:" + j + " r:" + testingMaxAmp);
            }
        }

        if (sys) {
            if (j >= THValue) {
                shotDetection.setVisibility(View.VISIBLE);
                TimerText.setVisibility(View.VISIBLE);

                TimerText.setText("0");
                TimerText.setTextColor(Color.WHITE);

                j = 0;
                anInt = 0;

                if (TimerThread != null) {
//                    sys = true;

                    if (TimerThread.isAlive()) {
                        TimerThread.interrupt();
                        try {
                            TimerThread.join();
                            TimerThread = null;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            TimerThread = null;
                        }

                    } else {
                        TimerThread = null;
                    }

                }


                ThreadMaker();

                TimerThread.start();

            }
        }
    }

    public void ValuesUpdater() {

        THValue = sp.getInt("thValue", this.getResources().getInteger(R.integer.DefaultThresholdValue));

        THTime = sp.getInt("thTime", this.getResources().getInteger(R.integer.DefaultCountDownTime));

        reset = THTime + 10;
        vib = THTime + 1;

    }


    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
        runner = null;
        TimerThread = null;
        startedOrNot = false;

        if (TimerText.getVisibility() == View.VISIBLE) {
            TimerText.setVisibility(View.INVISIBLE);
            shotDetection.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        started = false;
        mProgressBar.setProgress(0);
        //mProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
        started = true;
        //mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        ValuesUpdater();

        if (!startedOrNot)
            start();

    }

}