import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CatchMindTimer {
    private Timer timer;
    private int timeRemaining;
    private JLabel timerLabel;
    private Runnable onTimeExpired; // 타이머 종료

    public CatchMindTimer(JLabel timerLabel, int initialTime, Runnable onTimeExpired) {
        this.timerLabel = timerLabel;
        this.timeRemaining = initialTime;
        this.onTimeExpired = onTimeExpired;

        // 타이머 초기화
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (timeRemaining > 0) {
                    timeRemaining--;
                    updateTimerLabel();
                } else {
                    timer.stop();
                    if (onTimeExpired != null) {
                        onTimeExpired.run();
                    }
                }
            }
        });
        updateTimerLabel();
    }

    // 타이머 시작
    public void start() {
        timer.start();
    }

    // 타이머 멈춤
    public void stop() {
        timer.stop();
    }

    // 타이머 재설정
    public void reset(int newTime) {
        stop();
        timeRemaining = newTime;
        updateTimerLabel();
        start();
    }

    // 남은 시간 업데이트
    private void updateTimerLabel() {
        timerLabel.setText("남은 시간: " + timeRemaining + "초");
    }
}
