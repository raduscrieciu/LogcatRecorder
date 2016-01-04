package com.screechstudios.logcatrecorder;

import android.app.ActivityManager;
import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Class used to capture the logcat output.
 * Contains a listener interface for the recording status
 * and a custom exception.
 */
public class LogcatRecorder {

    private Context context;
    private Process continuousLogging;
    private StringBuilder log;
    private Boolean recording;

    private OnLogcatRecorderListener onLogcatRecorderListener;

    /**
     * LogcatSpy constructor with a predefined OnLogcatSpyListener.
     *
     * @param context             Context. Cannot be null.
     * @param onLogcatRecorderListener OnLogcatSpyListener.
     */
    public LogcatRecorder(Context context, OnLogcatRecorderListener onLogcatRecorderListener) {

        this.context = context;
        this.recording = false;
        this.onLogcatRecorderListener = onLogcatRecorderListener;

    }

    /**
     * Get the Process ID (PID) for a specific package name.
     *
     * @param packageName The app's package name.
     * @return The PID / 0 if the packageName is invalid or app isn't running.
     */
    public int getPid(String packageName) {
        int result = 0;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        if (activityManager != null) {
            List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfo = activityManager.getRunningAppProcesses();

            if (runningAppProcessInfo != null && runningAppProcessInfo.size() > 0) {
                for (int i = 0; i < runningAppProcessInfo.size(); i++) {
                    try {
                        if (runningAppProcessInfo.get(i).processName.equals(packageName)) {
                            result = runningAppProcessInfo.get(i).pid;
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return result;
    }

    /**
     * Start recording the log output.
     *
     * @throws IllegalStateException when log recording is already in place.
     */
    public void start() throws IllegalStateException {
        start(null);
    }

    /**
     * Start recording the log output with a specific filter.
     *
     * @param filter Log filter.
     * @throws IllegalStateException when log recording is already in place.
     */
    public void start(final String filter) throws IllegalStateException {

        if (!recording) {
            recording = true;

            if (onLogcatRecorderListener != null) {
                onLogcatRecorderListener.onStartRecording();
            }

            Thread thread = new Thread() {
                public void run() {
                    log = new StringBuilder();
                    String line;
                    try {
                        continuousLogging = Runtime.getRuntime().exec("logcat -c");
                        if (filter != null && filter.length() > 0) {
                            continuousLogging = Runtime.getRuntime().exec("logcat | grep " + filter);
                        } else {
                            continuousLogging = Runtime.getRuntime().exec("logcat");
                        }

                        BufferedReader bufferedReader = new BufferedReader(
                                new InputStreamReader(continuousLogging.getInputStream()));

                        while ((line = bufferedReader.readLine()) != null) {
                            log.append(line).append("\n");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        recording = false;
                    }
                }

            };
            thread.start();
        } else {
            throw new IllegalStateException("Unable to call start(): Already recording.");
        }
    }


    /**
     * Stops recording the log output.
     *
     * @throws IllegalStateException when log recording has already been stopped.
     */
    public void stop() throws IllegalStateException {
        if (recording) {
            if (onLogcatRecorderListener != null) {
                if (log == null || log.toString().length() == 0) {
                    log = new StringBuilder("n/a");
                }
                onLogcatRecorderListener.onStopRecording(log.toString());
            }

            if (continuousLogging != null) {
                continuousLogging.destroy();
            }

            recording = false;
        } else {
            throw new IllegalStateException("Unable to call stop(): Currently not recording, start recording first.");
        }
    }
}
