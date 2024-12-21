import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CatchMindTimer {
    private Timer timer;
    private int timeRemaining;
    private Runnable onTimeExpired; // 타이머 종료 시 실행할 콜백
    private Runnable onTimeUpdate;  // 매초 남은 시간을 업데이트하는 콜백

    public CatchMindTimer(int initialTime, Runnable onTimeExpired, Runnable onTimeUpdate) {
        this.timeRemaining = initialTime;
        this.onTimeExpired = onTimeExpired;
        this.onTimeUpdate = onTimeUpdate;

        // 타이머 초기화
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (timeRemaining > 0) {
                    timeRemaining--;
                    if (onTimeUpdate != null) {
                        onTimeUpdate.run();
                    }
                } else {
                    timer.stop();
                    if (onTimeExpired != null) {
                        onTimeExpired.run();
                    }
                }
            }
        });
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
        start();
    }

    // 남은 시간 반환
    public int getTimeRemaining() {
        return timeRemaining;
    }
}
