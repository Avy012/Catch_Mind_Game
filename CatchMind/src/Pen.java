import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

public class Pen extends JFrame implements ActionListener {
    private Color Redcolor = Color.red;
    private Color Bluecolor = Color.blue;
    private Color Blackcolor = Color.black;
    private Color Yellowcolor = Color.yellow;
    private Color Greencolor = Color.green;

    JPanel color_space, Canvas_space;
    JButton Red_button, Blue_button, Green_button, Yellow_button, Black_button, Clear_button;
    JLabel timerLabel, quizLabel;
    CreateCanvas createcanvas;
    CatchMindTimer catchmindtimer;
    QuizWord Quizmanager;

    Pen(CreateCanvas createcanvas) {
        setSize(1000, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        this.createcanvas = createcanvas;

        // 캔버스 공간
        Canvas_space = new JPanel();
        Canvas_space.setBounds(175, 75, 600, 500);
        Canvas_space.setBorder(new LineBorder(Color.black));
        Canvas_space.setLayout(null);
        createcanvas.setBounds(0, 0, 800, 500);
        Canvas_space.add(createcanvas);

        // 색상 버튼 공간
        color_space = new JPanel();
        color_space.setBounds(175, 600, 600, 50);
        color_space.setBorder(new LineBorder(Color.black));

        // 색상 버튼 생성
        Red_button = new JButton("빨강");
        Blue_button = new JButton("파랑");
        Green_button = new JButton("초록");
        Yellow_button = new JButton("노랑");
        Black_button = new JButton("검정");
        Clear_button = new JButton("지우기");

        // 버튼 추가
        color_space.add(Black_button);
        color_space.add(Red_button);
        color_space.add(Yellow_button);
        color_space.add(Blue_button);
        color_space.add(Green_button);
        color_space.add(Clear_button);

        // 이벤트 리스너 추가
        Red_button.addActionListener(this);
        Blue_button.addActionListener(this);
        Green_button.addActionListener(this);
        Yellow_button.addActionListener(this);
        Black_button.addActionListener(this);
        Clear_button.addActionListener(this);

        // 타이머 레이블 추가
        timerLabel = new JLabel("", SwingConstants.CENTER);
        timerLabel.setBounds(50, 10, 250, 30);
        timerLabel.setFont(new Font("System", Font.BOLD, 20));
        timerLabel.setBorder(new LineBorder(Color.black));
        add(timerLabel);

        // 제시어 레이블 추가
        quizLabel = new JLabel("", SwingConstants.CENTER);
        quizLabel.setBounds(675, 10, 250, 30);
        quizLabel.setFont(new Font("System", Font.BOLD, 20));
        quizLabel.setBorder(new LineBorder(Color.black));
        add(quizLabel);

        
        Quizmanager = new QuizWord(quizLabel);

        // 타이머 설정
        catchmindtimer = new CatchMindTimer(timerLabel, 10, () -> {
            JOptionPane.showMessageDialog(Canvas_space, "게임 종료");
            createcanvas.clearCanvas();
            catchmindtimer.reset(60);
            Quizmanager.setRandomword(); // 새로운 제시
        });

        catchmindtimer.start(); // 타이머 시작

        // 초기 제시어 설정
        Quizmanager.setRandomword();

        add(Canvas_space);
        add(color_space);
    }

    public void display() {
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == Red_button) {
            createcanvas.set_color(Redcolor);
        } else if (e.getSource() == Blue_button) {
            createcanvas.set_color(Bluecolor);
        } else if (e.getSource() == Green_button) {
            createcanvas.set_color(Greencolor);
        } else if (e.getSource() == Yellow_button) {
            createcanvas.set_color(Yellowcolor);
        } else if (e.getSource() == Black_button) {
            createcanvas.set_color(Blackcolor);
        } else if (e.getSource() == Clear_button) {
            createcanvas.clearCanvas();
            Quizmanager.setRandomword(); //test
        }
    }

    public static void main(String[] args) {
        CreateCanvas createcanvas = new CreateCanvas();
        Pen pen = new Pen(createcanvas);
        pen.display();
    }
}
