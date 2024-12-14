import java.util.Random;
import javax.swing.JLabel;

public class QuizWord {
    private String[] prompt;
    private JLabel promptLabel;

    public QuizWord(JLabel promptLabel) {
        this.promptLabel = promptLabel;
        this.prompt = new String[] { "상어", "집", "자동차", "고양이", "강아지", "사과", "산", "바다", "사람", "책","두부" };
    }

    public void setRandomword() {
        Random random = new Random();
        String randomPrompt = prompt[random.nextInt(prompt.length)];
        promptLabel.setText("제시어: " + randomPrompt);
    }
}
