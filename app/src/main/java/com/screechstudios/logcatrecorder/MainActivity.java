package com.screechstudios.logcatrecorder;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

    private LogcatRecorder logcatRecorder;
    private TextView outputText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        outputText = (TextView) findViewById(R.id.outputTextView);

        //Initialize the LogcatRecorder
        logcatRecorder = new LogcatRecorder(new OnLogcatRecorderListener() {
            @Override
            public void onStartRecording() {
                outputText.setText("");
                Toast.makeText(MainActivity.this, "Recording logcat...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNewLogEntry(final String logEntry) {

                //Run on the main UI thread, since the call is made from a separate thread.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        outputText.append(logEntry);
                    }
                });
            }

            @Override
            public void onStopRecording(final String log) {
                outputText.setText(log);
                Toast.makeText(MainActivity.this, "Recording stopped...", Toast.LENGTH_SHORT).show();
            }
        });

        //Record button
        ((ToggleButton) findViewById(R.id.toggleButton)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    try {
                        //Start recording the logcat and filter it by the app's own PID.
                        logcatRecorder.start(String.valueOf(logcatRecorder.getPid(getPackageName(), getApplicationContext())));
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Already recording...", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        //Stop recording the logcat.
                        logcatRecorder.stop();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Already stopped...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        //Add some random output to the logcat.
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Some output: " + Math.random());
                Log.d("LogcatRecorder", "Some log entry: " + Math.random());
            }
        });
    }
}
