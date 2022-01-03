package br.com.drinkwater;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button btnNotify;
    private EditText editMinutes;
    private TimePicker timePicker;

    private boolean activated = false;
    private final String keyPrefix = "@drink_water_";

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnNotify = findViewById(R.id.btn_notify);
        editMinutes = findViewById(R.id.edit_txt_number_interval);
        timePicker = findViewById(R.id.time_picker);

        timePicker.setIs24HourView(true);

        preferences = getSharedPreferences("db", Context.MODE_PRIVATE);

        loadValues();
    }

    public  void notifyClick(View view){
        String sInterval = editMinutes.getText().toString();

        if(sInterval.isEmpty()){
            Toast.makeText(this,R.string.error_msg, Toast.LENGTH_LONG).show();
            return;
        }

        int hour = timePicker.getCurrentHour();
        int minute = timePicker.getCurrentMinute();
        int interval = Integer.parseInt(sInterval);

        SharedPreferences.Editor editor = preferences.edit();


        if(!activated){
            btnNotify.setText(R.string.pause);
            btnNotify.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.black));
            activated = true;

            editor.putBoolean(keyPrefix+"activated", true);
            editor.putInt(keyPrefix+"interval", interval);
            editor.putInt(keyPrefix+"hour", hour);
            editor.putInt(keyPrefix+"minute", minute);


        }else {
            btnNotify.setText(R.string.notify);
            btnNotify.setBackgroundTintList(ContextCompat.getColorStateList(this,R.color.colorAccent));
            activated = false;

            editor.putBoolean(keyPrefix+"activated", false);
            editor.remove(keyPrefix+"interval");
            editor.remove(keyPrefix+"hour");
            editor.remove(keyPrefix+"minute");
        }
            editor.apply();

        Log.d("teste","hora: "+ hour +" minuto: "+ minute +" interval: "+ interval);


    }

    private void loadValues(){
        activated = preferences.getBoolean(keyPrefix+"activated", false);

        if(activated){
            btnNotify.setText(R.string.pause);
            btnNotify.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.black));

            int _interval = preferences.getInt(keyPrefix+"interval", 0);
            int _hour = preferences.getInt(keyPrefix+"hour", 0);
            int _minute = preferences.getInt(keyPrefix+"minute", 0);

            editMinutes.setText(String.valueOf(_interval));
            timePicker.setCurrentHour(_hour);
            timePicker.setCurrentMinute(_minute);
        }
    }
}