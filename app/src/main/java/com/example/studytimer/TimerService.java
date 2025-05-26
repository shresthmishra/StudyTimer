package com.example.studytimer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import java.util.Locale;
import com.example.studytimer.R;
public class TimerService extends Service {

    private static final String CHANNEL_ID = "TimerServiceChannel";
    private static final int NOTIFICATION_ID = 1;

    private CountDownTimer countDownTimer;
    private long timeRemainingInMillis;
    private long totalTimeInMillis; // To know the total time for percentage calculation
    private boolean isTimerRunning = false;

    private final IBinder binder = new LocalBinder(); // For potential communication with Activity

    public class LocalBinder extends Binder {
        TimerService getService() {
            return TimerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case "START":
                        long timeInMillis = intent.getLongExtra("TIME_IN_MILLIS", 0);
                        startTimer(timeInMillis);
                        break;
                    case "PAUSE":
                        pauseTimer();
                        break;
                    case "RESUME":
                        resumeTimer();
                        break;
                    case "STOP":
                        stopTimer();
                        stopSelf(); // Stop the service
                        break;
                }
            }
        }
        return START_STICKY; // Restart service if killed by system
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Timer Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void startTimer(long timeInMillis) {
        totalTimeInMillis = timeInMillis;
        timeRemainingInMillis = timeInMillis;
        isTimerRunning = true;

        countDownTimer = new CountDownTimer(timeInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemainingInMillis = millisUntilFinished;
                updateNotification();
            }

            @Override
            public void onFinish() {
                timeRemainingInMillis = 0;
                isTimerRunning = false;
                updateNotification();
                //  Send broadcast or callback to activity if needed for UI update or actions
                stopSelf(); // Stop the service when timer finishes
            }
        }.start();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE); // Use FLAG_IMMUTABLE or FLAG_MUTABLE

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Study Timer")
                .setContentText("Timer running...")
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your icon
                .setContentIntent(pendingIntent)
                .setOngoing(true) // Make it a foreground notification
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    private void pauseTimer() {
        if (isTimerRunning) {
            countDownTimer.cancel();
            isTimerRunning = false;
            updateNotification(); // Update notification to show "Paused"
        }
    }

    private void resumeTimer() {
        if (!isTimerRunning && timeRemainingInMillis > 0) {
            isTimerRunning = true;
            countDownTimer = new CountDownTimer(timeRemainingInMillis, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    timeRemainingInMillis = millisUntilFinished;
                    updateNotification();
                }

                @Override
                public void onFinish() {
                    timeRemainingInMillis = 0;
                    isTimerRunning = false;
                    updateNotification();
                    stopSelf();
                }
            }.start();
        }
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
        timeRemainingInMillis = 0;
        stopForeground(true); // Remove the notification
        stopSelf();
    }

    private void updateNotification() {
        if (isTimerRunning) {
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE);

            String timeLeftFormatted = getFormattedTime(timeRemainingInMillis);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Study Timer")
                    .setContentText(timeLeftFormatted)
                    .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your icon
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .build();

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID, notification);
        } else {
            // Show a different notification when paused or finished
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Study Timer")
                    .setContentText(timeRemainingInMillis == 0 ? "Timer Finished" : "Timer Paused")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentIntent(pendingIntent)
                    .setOngoing(true) // Keep it for pause/finished state
                    .build();

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    private String getFormattedTime(long millis) {
        long hours = (millis / 1000) / 3600;
        long minutes = ((millis / 1000) % 3600) / 60;
        long seconds = (millis / 1000) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}