
package com.fitasc.fitascgame;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.wear.widget.WearableLinearLayoutManager;
import android.support.wear.widget.WearableRecyclerView;
import android.support.wearable.activity.WearableActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.wearable.Wearable;

public class settings extends WearableActivity {


    TextView textView;
    EditText timeoutValue, thresholdValue;
    SharedPreferences.Editor editor;
    int i, DefaultValue;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

    //        seekBar = findViewById(R.id.seekBar);
    //        textView = findViewById(R.id.textView2);
        timeoutValue = findViewById(R.id.timeEntry);
        thresholdValue = findViewById(R.id.decibelEntry);

        DefaultValue = getResources().getInteger(R.integer.DefaultThresholdValue);

        sp = getApplicationContext().getSharedPreferences("game_prefs", Activity.MODE_PRIVATE);
        editor = sp.edit();


        int THValue = sp.getInt("thValue",DefaultValue);

        int THTime = sp.getInt("thTime", this.getResources().getInteger(R.integer.DefaultCountDownTime));


        thresholdValue.setText(String.valueOf(THValue));

        timeoutValue.setText(Integer.toString(THTime));
        i = THValue;


        thresholdValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {


            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {


            }

            @Override
            public void afterTextChanged(Editable editable) {


                if (TextUtils.isEmpty(thresholdValue.getText())) {

                    // ThresholdValue.setText(DefaultValue);

                    //according to test result, direct setting value will require less data access.

                    thresholdValue.setText(getResources().getInteger(R.integer.DefaultThresholdValue));

                }

                //following features were removed.

                /*else {
                    if (ThresholdValue.getText() != null) {
                //  seekBar.setProgress(Integer.parseInt(ThresholdValue.getText().toString()));

                        if (Integer.parseInt(ThresholdValue.getText().toString()) < 70) {

                            ThresholdValue.setText("70");

                         // ThresholdValue.setText(DefaultValue);

                            Toast.makeText(getApplicationContext(), "expected min value: 70", Toast.LENGTH_SHORT).show();

                        }

                        if (Integer.parseInt(ThresholdValue.getText().toString()) > 150) {

                            ThresholdValue.setText("150");

                            Toast.makeText(getApplicationContext(), "range:70-150", Toast.LENGTH_SHORT).show();
                        }
                    }
                }*/

            }
        });

        //following features were removed.
        /*
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ThresholdValue.setText(String.valueOf(progress));

                i = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        */

    }

    //UI button function.
    public void done(View view) {
        finish();
    }

    //Saving values
    public void saveThose() {
        int j = 15;

        if (TextUtils.isEmpty(timeoutValue.getText())) {

            j = 15;

            Toast.makeText(getApplicationContext(), "Default timeout has been set", Toast.LENGTH_SHORT).show();

        } else if (timeoutValue.getText() != null) {
            j = Integer.parseInt(timeoutValue.getText().toString());
            if (j < 1) {

                j = 15;
            }
        }

        if (thresholdValue.getText() != null) {

            i = Integer.parseInt(thresholdValue.getText().toString());

            if (Integer.parseInt(thresholdValue.getText().toString()) < getResources().getInteger(R.integer.DefaultThresholdValue)) {

                i = getResources().getInteger(R.integer.DefaultThresholdValue);

                Toast.makeText(getApplicationContext(), "expected min value: 135", Toast.LENGTH_SHORT).show();

            }


        }


        editor.putInt("thValue", i);
        editor.putInt("thTime", j);
        editor.apply();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveThose();
    }

    public void defaults(View view) {

        timeoutValue.setText(String.valueOf(15));
    }

}
