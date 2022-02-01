package br.com.drinkwater;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

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

        btnNotify.setOnClickListener(this::notifyClick);

        loadValues();
    }

    private void alert(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_LONG).show();
    }

    private boolean intervalIsValid(String sInterval) {
        if (sInterval.isEmpty()) {
            alert(R.string.error_msg);
            return false;
        }
        if (sInterval.equals("0")) {
            alert(R.string.error_zero_value);
        }
        return true;
    }

    private void setupUI(boolean activated) {

        if (!activated) {
            btnNotify.setText(R.string.pause);
            btnNotify.setBackgroundResource(R.drawable.bg_button);
        } else {
            btnNotify.setText(R.string.notify);
            btnNotify.setBackgroundResource(R.drawable.bg_button_accent);
        }
    }

    private void updateStorage(boolean added, int interval, int hour, int minute) {
        SharedPreferences.Editor editor = preferences.edit();

        if (added) {
            editor.putBoolean(keyPrefix + "activated", true);
            editor.putInt(keyPrefix + "interval", interval);
            editor.putInt(keyPrefix + "hour", hour);
            editor.putInt(keyPrefix + "minute", minute);
        } else {
            editor.putBoolean(keyPrefix + "activated", false);
            editor.remove(keyPrefix + "interval");
            editor.remove(keyPrefix + "hour");
            editor.remove(keyPrefix + "minute");
        }
        editor.apply();

    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private void setupNotification(boolean added, int interval, int hour, int minute) {

        Intent notificationIntent = new Intent(MainActivity.this, NotificationPublisher.class);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (added) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);

            notificationIntent.putExtra(NotificationPublisher.KEY_NOTIFICATION_ID, 1);
            notificationIntent.putExtra(NotificationPublisher.KEY_NOTIFICATION, "Hora de beber água");
            PendingIntent broadcast = PendingIntent.getBroadcast(MainActivity.this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            // está sendo calculado para segundo devido aos testes
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), (long) interval * 1000, broadcast);

        } else {
            PendingIntent broadcast = PendingIntent.getBroadcast(MainActivity.this, 0, notificationIntent, 0);
            alarmManager.cancel(broadcast);
        }
    }

    public void notifyClick(View view) {
        String sInterval = editMinutes.getText().toString();
        int hour = timePicker.getCurrentHour();
        int minute = timePicker.getCurrentMinute();
        int interval = Integer.parseInt(sInterval);


        if (!intervalIsValid(sInterval)) return;


        if (!activated) {
            this.activated = true;
            setupUI(true);

            updateStorage(this.activated, interval, hour, minute);
            setupNotification(this.activated, interval, hour, minute);

        } else {
            this.activated = false;

            setupUI(false);
            updateStorage(this.activated, interval, hour, minute);
            setupNotification(this.activated, interval, hour, minute);

        }


    }

    private void loadValues() {
        this.activated = preferences.getBoolean(keyPrefix + "activated", false);

        if (this.activated) {
            btnNotify.setText(R.string.pause);
            btnNotify.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.black));

            int _interval = preferences.getInt(keyPrefix + "interval", 0);
            int _hour = preferences.getInt(keyPrefix + "hour", 0);
            int _minute = preferences.getInt(keyPrefix + "minute", 0);

            editMinutes.setText(String.valueOf(_interval));
            timePicker.setCurrentHour(_hour);
            timePicker.setCurrentMinute(_minute);
        }
    }
}