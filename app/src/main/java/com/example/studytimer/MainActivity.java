package com.example.studytimer;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.studytimer.R;

public class MainActivity extends AppCompatActivity {

    private Button timerButton;
    private TextView timerText;
    private TextView sessionHistoryText;

    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;
    private long timeLeftInMillis = 25 * 60 * 1000; // Default study time: 25 minutes
    private long studyInterval = 25 * 60 * 1000;
    private long breakInterval = 5 * 60 * 1000;
    private boolean isStudyTime = true;
    private int sessionCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timerButton = findViewById(R.id.timerButton);
        timerText = findViewById(R.id.timerText);
        sessionHistoryText = findViewById(R.id.sessionHistoryText);

        updateTimerText();

        timerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTimerRunning) {
                    pauseTimer();
                } else {
                    startTimer();
                }
            }
        });
    }

    private void startTimer() {
        isTimerRunning = true;
        timerButton.setText("Pause");
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                if (isStudyTime) {
                    timeLeftInMillis = breakInterval;
                    isStudyTime = false;
                    timerButton.setText("Start Break");
                    // TODO: Add code to trigger break notification
                } else {
                    timeLeftInMillis = studyInterval;
                    isStudyTime = true;
                    timerButton.setText("Start Study");
                    sessionCount++;
                    updateSessionHistory();
                    // TODO: Add code to trigger study notification
                }
                updateTimerText();
            }
        }.start();
    }

    private void pauseTimer() {
        isTimerRunning = false;
        timerButton.setText("Resume");
        countDownTimer.cancel();
    }

    private void updateTimerText() {
        long hours = (timeLeftInMillis / 1000) / 3600;
        long minutes = ((timeLeftInMillis / 1000) % 3600) / 60;
        long seconds = (timeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format("%02d : %02d : %02d", hours, minutes, seconds);
        timerText.setText(timeLeftFormatted);
    }

    private void updateSessionHistory() {
        sessionHistoryText.setText("Session History: (" + sessionCount + " completed)");
        // TODO: Implement more detailed session history display here (e.g., using a RecyclerView)
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}