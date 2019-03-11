package com.fitasc.fitascgame;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int REQ = 111;
    TextView text, shDetection, textTime, audioLevel;
    MediaRecorder mRecorder;
    Thread runner;
    private static double mEMA = 0.0;
    static final private double EMA_FILTER = 0.7;
    ProgressBar progressBar;
    SharedPreferences sp;
    int THValue;
    int THTime;
    int i, reset, anInt;
    AtomicInteger t;
    Button button;
    private int amp = 100;
    Vibrator v;

    Thread threadx;
    boolean shouldUpdate, color, Recording, threadOn, resumed;

    final Runnable updater = new Runnable() {

        public void run() {

            //if (shouldUpdate) {
            updateTv();

        }

    };
    final Handler mHandler = new Handler();
    private int vib;
    private boolean sys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        t = new AtomicInteger(0);
        text = findViewById(R.id.textHeader);
        progressBar = findViewById(R.id.progressBar);
        sp = this.getSharedPreferences("game_prefs", Activity.MODE_PRIVATE);
        shDetection = findViewById(R.id.textShot);
        textTime = findViewById(R.id.textTimer);
        audioLevel = findViewById(R.id.audioLevel);
        shouldUpdate = true;
        i = 0;
        sys = true;
        button = findViewById(R.id.gear);
        threadOn = false;
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        shDetection.setVisibility(View.INVISIBLE);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // If required we show an explanation here.

            /*
            if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
                    Manifest.permission.READ_CONTACTS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
            */

            text.setText("Allow audio record permission.");

            //ActivityCompat.requestPermissions(this,new String[] {""},100);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQ);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        } else {
            // Permission has already been granted

            startRecording();

            text.setVisibility(View.GONE);

        }
    }

    private void valuesetter() {
        THValue = sp.getInt("thValue", 30);

        THTime = sp.getInt("thTime", 15);

        reset = THTime + 10;
        vib = THTime + 1;
    }

    void ThreadMaker() {

        threadx = new Thread() {

            @Override
            public void run() {
                try {
                    while (threadx != null) {
                        Thread.sleep(1000);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                //t.getAndIncrement();
                                inc();

                                //if (anInt <= THTime) {
                                textTime.setText(String.valueOf(anInt));
                                //t.get()
                                if (anInt >= THTime) {
                                    textTime.setTextColor(Color.RED);

                                    if (anInt < vib) {

                                        sys = false;

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            v.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE));
                                        } else {
                                            //deprecated in API 26
                                            v.vibrate(400);
                                        }

                                    }


                                    if (anInt > vib) {

                                        if (!sys) {
                                            sys = true;
                                        }
                                    }

                                    if (anInt >= reset) {

                                        resetter();
                                    }
                                }
                            }
                        });
                    }
                } catch (
                        InterruptedException e)

                {
                }
            }
        }

        ;
    }

    private void resetter() {
        textTime.setTextColor(Color.WHITE);
        textTime.setText("count: 0");
        //t.set(0);
        anInt = 0;
        textTime.setVisibility(View.INVISIBLE);
        shDetection.setVisibility(View.INVISIBLE);

        if (threadx != null) {
            threadx.interrupt();
            try {
                threadx.join();
                //startRecording();
                threadOn = false;

                if (!Recording) {
                    if (resumed)
                        startRecorder();
                }
                shouldUpdate = true;
                button.setVisibility(View.VISIBLE);
                threadx = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    private void inc() {

        anInt++;

    }

    private void startRecording() {
        if (runner == null) {
            runner = new Thread() {
                public void run() {
                    while (runner != null) {
                        try {
                            Thread.sleep(100);
                            //Log.i("Noise", "Tock");
                        } catch (InterruptedException e) {
                        }
                        ;
                        mHandler.post(updater);
                    }
                }
            };
            runner.start();
            Log.d("Noise", "start runner()");
        }

    }


    public void onResume() {
        super.onResume();

        valuesetter();

        resumed = true;

        if (!Recording) {
            if (threadx == null)
                startRecorder();
        }
        if (!shouldUpdate)
            shouldUpdate = true;
    }

    public void onPause() {
        super.onPause();

        resumed = false;

        if (Recording) {
            stopRecorder();
        }

        if (shouldUpdate)
            shouldUpdate = false;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // BEGIN_INCLUDE(onRequestPermissionsResult)
        if (requestCode == REQ) {
            // Request for camera permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start camera preview Activity.
                Toast.makeText(this, R.string.permission_granted,
                        Toast.LENGTH_SHORT)
                        .show();
                //startCamera();
                startRecording();
            } else {
                // Permission request was denied.
                Toast.makeText(this, R.string.permission_denied,
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
        // END_INCLUDE(onRequestPermissionsResult)
    }


    public void startRecorder() {
        Recording = true;

        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            //mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setAudioSource(MediaRecorder.AudioSource.UNPROCESSED);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile("/dev/null");
            try {
                mRecorder.prepare();
            } catch (java.io.IOException ioe) {
                Log.e("[Monkey]", "IOException: " +
                        Log.getStackTraceString(ioe));

            } catch (SecurityException e) {
                Log.e("[Monkey]", "SecurityException: " +
                        Log.getStackTraceString(e));
            }
            try {
                mRecorder.start();
            } catch (SecurityException e) {
                Log.e("[Monkey]", "SecurityException: " +
                        Log.getStackTraceString(e));
            }

            //mEMA = 0.0;
        }

    }

    public void stopRecorder() {
        Recording = false;
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

    public void updateTv() {

        i = (int) getAmplitudeEMA() / amp;

        progressBar.setProgress(i);
        if (i>100)
        {
            audioLevel.setText(""+i);
        }


        if (sys) {
            if (i >= THValue) {
                shDetection.setVisibility(View.VISIBLE);
                textTime.setVisibility(View.VISIBLE);

                textTime.setText("0");
                textTime.setTextColor(Color.WHITE);

                anInt = 0;

                if (threadx != null) {
                    if (threadx.isAlive()) {
                        threadx.interrupt();
                        try {
                            threadx.join();
                            threadx = null;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            threadx = null;
                        }
                    }

                }
                ThreadMaker();


                threadx.start();

                button.setVisibility(View.INVISIBLE);

                threadOn = true;

                //Log.d("check", "here...i: " + i + threadx.getState());

            }
        }
    }

    public double getAmplitude() {
        if (mRecorder != null)
            return (mRecorder.getMaxAmplitude());
        else
            return 0;

    }

    public double getAmplitudeEMA() {
        double amp = getAmplitude();
        mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;
        return mEMA;
    }

    public void intent(View view) {
        Intent i = new Intent(this, settings.class);
        startActivity(i);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        runner = null;
    }
}
