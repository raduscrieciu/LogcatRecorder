package com.screechstudios.logcatrecorder;

/**
 * An interface used to notify the log recording status.
 */
public interface OnLogcatRecorderListener {
    /**
     * Called when recording has started.
     */
    void onStartRecording();

    /**
     * Called when recording has stopped.
     *
     * @param log Returns the recorded log.
     */
    void onStopRecording(String log);
}
