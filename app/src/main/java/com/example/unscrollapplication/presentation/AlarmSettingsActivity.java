package com.example.unscrollapplication.presentation;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.unscrollapplication.R;
import com.example.unscrollapplication.data.DoomscrollStore;

public class AlarmSettingsActivity extends AppCompatActivity {

    private EditText etDoomMinutes;
    private SwitchCompat swDoomEnable;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_settings);

        etDoomMinutes = findViewById(R.id.etDoomMinutes);
        swDoomEnable = findViewById(R.id.swDoomEnable);
        btnSave = findViewById(R.id.btnSaveDoom);

        int minutes = DoomscrollStore.getMinutes(this);
        etDoomMinutes.setText(String.valueOf(minutes));
        swDoomEnable.setChecked(DoomscrollStore.isEnabled(this));

        btnSave.setOnClickListener(v -> save());
    }

    private void save() {
        String minutesText = etDoomMinutes.getText() == null ? "" : etDoomMinutes.getText().toString().trim();
        if (TextUtils.isEmpty(minutesText)) {
            Toast.makeText(this, "Enter minutes", Toast.LENGTH_SHORT).show();
            return;
        }

        int minutes;
        try {
            minutes = Integer.parseInt(minutesText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid minutes", Toast.LENGTH_SHORT).show();
            return;
        }

        if (minutes < 1 || minutes > 240) {
            Toast.makeText(this, "Minutes must be 1-240", Toast.LENGTH_SHORT).show();
            return;
        }

        DoomscrollStore.setMinutes(this, minutes);
        DoomscrollStore.setEnabled(this, swDoomEnable.isChecked());
        Toast.makeText(this, "Alarm settings saved", Toast.LENGTH_SHORT).show();
        finish();
    }
}
