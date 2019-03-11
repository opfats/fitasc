package com.fitasc.fitascgame;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

public class settings extends AppCompatActivity {

    SeekBar seekBar;
    TextView textView;
    EditText editText;
    SharedPreferences.Editor editor;
    int i;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        seekBar = findViewById(R.id.seekBar);
        textView = findViewById(R.id.textView2);
        editText = findViewById(R.id.editText);
        sp = getApplicationContext().getSharedPreferences("game_prefs", Activity.MODE_PRIVATE);
        editor = sp.edit();

        int THValue = sp.getInt("thValue", 110);
        if (THValue < 1) {
            THValue = 110;
        }
        seekBar.setProgress(THValue);
        textView.setText(String.valueOf(THValue));
        int THTime = sp.getInt("thTime", 15);
        editText.setText(THTime + "");
        i = THValue;

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView.setText(String.valueOf(progress));

                i = progress;
                //editor.putInt("thValue",progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    public void done(View view) {
        saveThose();
        finish();
    }

    public void saveThose() {
        int j = 15;
        if (editText.getText() != null) {
            j = Integer.parseInt(editText.getText().toString());
        }
        //editor = sp.edit();
        editor.putInt("thValue", i);
        editor.putInt("thTime", j);
        editor.apply();
        //Log.d("pref", "saving on prefs...........i: " + i + " j: " + j);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveThose();
    }

    public void defaults(View view) {
        seekBar.setProgress(70);
        editText.setText( String.valueOf( 15));
    }
}
