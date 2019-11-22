package org.kkteam.gapwatcher;

import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;


public class MainActivity extends AppCompatActivity {

    protected Button buttonSearch;
    protected EditText editTextHour;
    protected TextView textViewOutput;
    protected RoomStatus[] rs = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonSearch = findViewById(R.id.buttonSearch);
        editTextHour = findViewById(R.id.editTextHour);
        textViewOutput = findViewById(R.id.textViewOutput);

        Calendar rightNow = Calendar.getInstance();
        final int currentHourIn24Format = rightNow.get(Calendar.HOUR_OF_DAY);
        final int currentMin = rightNow.get(Calendar.MINUTE);
        editTextHour.setText(currentHourIn24Format + "." + currentMin);

        editTextHour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                        editTextHour.setText(hourOfDay + "." + minutes);
                    }
                }, currentHourIn24Format, currentMin, true);
                timePickerDialog.show();
            }
        });

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });
    }

    public void search() {
        if(rs == null) {
            buttonSearch.setEnabled(false);
            textViewOutput.setText("Sto cercando...");

            AsyncTask at = new AsyncTask<Object, Object, RoomStatus[]>() {

                @Override
                protected RoomStatus[] doInBackground(Object... objs) {
                    GapScraper gs = new GapScraper();
                    return gs.getRoomsStatus();
                }

                protected void onPostExecute(RoomStatus[] result) {
                    rs = result;
                    buttonSearch.setEnabled(true);
                    searchStage2();
                }

            };

            at.execute();
        } else {
            searchStage2();
        }
    }

    protected void searchStage2() {
        String[] splitted = editTextHour.getText().toString().split("\\.");
        textViewOutput.setText("");
        StringBuilder sb = new StringBuilder();
        int found = 0;
        for(RoomStatus status : rs) {
            String freeUntil = status.freeUntil(Integer.parseInt(splitted[0]), Integer.parseInt(splitted[1]));
            if(!freeUntil.equals("NO")) {
                String line = status.getName() + "\tlibera fino alle " + freeUntil + "\n";
                sb.append(line);
                ++found;
            }
        }
        if(found > 0)
            textViewOutput.setText(sb.toString());
        else
            textViewOutput.setText("Non sono riuscito a trovare aule libere :(");
    }
}
