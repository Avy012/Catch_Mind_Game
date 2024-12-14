import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;

/// 게임 화면
public class CatchClientView extends JFrame {
    private JTextField txtInput;
    private String UserName;
    private JButton btnSend;
    private JTextArea textArea;
    private static final int BUF_LEN = 128; // Windows 처럼 BUF_LEN 을 정의
    private Socket socket; // 연결소켓
    private InputStream is;
    private OutputStream os;
    private DataInputStream dis;
    private DataOutputStream dos;

	
    private JPanel contentPane;
    private int people_num = 1; // 게임에 있는 사람 수
    
    //canvas
    private JPanel drawing;
    private Canvas createcanvas;
    //  pen
    private int lastX = -1, lastY = -1;
    private Color color = Color.black;
    private Color Redcolor = Color.red;
    private Color Bluecolor = Color.blue;
    private Color Blackcolor = Color.black;
    private Color Yellowcolor = Color.yellow;
    private Color Greencolor = Color.green;
    private JPanel color_space, Canvas_space;
    private JButton Red_button, Blue_button, Green_button, Yellow_button, Black_button, Clear_button;

    
    private CatchMindTimer catchmindtimer;
    private QuizWord Quizmanager;


    public CatchClientView(String username, String ip_addr, String port_no, String img_path) {
    	
    	try {
            socket = new Socket(ip_addr, Integer.parseInt(port_no));
            is = socket.getInputStream();
            dis = new DataInputStream(is);
            os = socket.getOutputStream();
            dos = new DataOutputStream(os);
            
            new Thread(this::listenToServer).start();
            System.out.println(username + " connected");
           
            sendUserInfo(img_path, username); // users: 하고 유저 정보 보냄
            
            
        } catch (NumberFormatException | IOException e) {
            e.printStackTrace();
        }
    	
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(250, 50, 1000, 750);
//        contentPane = new JPanel();
//        contentPane.setLayout(null);
//        setContentPane(contentPane);
        
        // Use JLayeredPane
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(null); // Use null layout for absolute positioning
        setContentPane(layeredPane);

        // Main content panel
        contentPane = new JPanel();
        contentPane.setBounds(0, 0, getWidth(), getHeight());
        contentPane.setLayout(null);
        layeredPane.add(contentPane, Integer.valueOf(0)); // Add as base layer

        

        //유저 공간
        JPanel leftSquares = new UserSquaresPanel();
        JPanel rightSquares = new UserSquaresPanel();
        leftSquares.setPreferredSize(new Dimension(150, 500));
        rightSquares.setPreferredSize(new Dimension(150, 500));
        leftSquares.setBounds(20,100,150,500);
        contentPane.add(leftSquares);
        rightSquares.setBounds(800,100,150,500);
        contentPane.add(rightSquares);
       
        //캔버스
        drawing = createDrawingPanel();
        drawing.setBounds(200, 100, 550, 500); 
        contentPane.add(drawing);
        
        
    }

    class UserSquaresPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.GRAY);

            // 유저 창
            int squareWidth = 150;
            int squareHeight = 130;
            int spacing = 50;

            for (int i = 0; i < 2; i++) {
                int y = i * (squareHeight + spacing);
                g.fillRect(0, y, squareWidth, squareHeight);
            }
        }
    }
    
    private JPanel createDrawingPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        
        

        // 그림 창
        createcanvas = new Canvas();
        createcanvas.setBackground(Color.WHITE);
        createcanvas.setPreferredSize(new Dimension(600, 1000));
        
        createcanvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                if (lastX != -1 && lastY != -1) {
                	Graphics g = createcanvas.getGraphics();
                	g.setColor(color);
                    g.drawLine(lastX, lastY, x, y);
                    sendDrawCommand(lastX, lastY, x, y);
                }
                lastX = x;
                lastY = y;
            }
        });

        createcanvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                lastX = -1;
                lastY = -1;
            }
        });
        
        
        
        Canvas_space = new JPanel();
        Canvas_space.setPreferredSize(new Dimension(800, 1000));
        Canvas_space.setBorder(new LineBorder(Color.black));
        Canvas_space.setLayout(new BorderLayout());
        Canvas_space.add(createcanvas, BorderLayout.CENTER);
        


        // Color Buttons Area
        color_space = new JPanel();
        color_space.setBorder(new LineBorder(Color.black));
        color_space.setLayout(new FlowLayout());

        Red_button = new JButton("빨강");
        Blue_button = new JButton("파랑");
        Green_button = new JButton("초록");
        Yellow_button = new JButton("노랑");
        Black_button = new JButton("검정");
        Clear_button = new JButton("지우기");

        color_space.add(Black_button);
        color_space.add(Red_button);
        color_space.add(Yellow_button);
        color_space.add(Blue_button);
        color_space.add(Green_button);
        color_space.add(Clear_button);

        Red_button.addActionListener(e -> color = Redcolor);
        Blue_button.addActionListener(e -> color = Bluecolor);
        Green_button.addActionListener(e -> color = Greencolor);
        Yellow_button.addActionListener(e -> color = Yellowcolor);
        Black_button.addActionListener(e -> color = Blackcolor);
        Clear_button.addActionListener(e -> {
        	Graphics g = createcanvas.getGraphics();
        	g.setColor(Color.WHITE); 
            g.fillRect(0, 0, getWidth(), getHeight());
            g.dispose(); 
            Quizmanager.setRandomword(); ///테스트용
        });

        panel.add(Canvas_space, BorderLayout.CENTER);
        panel.add(color_space, BorderLayout.SOUTH);
        
        JLabel timerLabel, quizLabel;
        
        // 타이머 레이블 추가
        timerLabel = new JLabel("", SwingConstants.CENTER);
        timerLabel.setBounds(50, 10, 250, 30);
        timerLabel.setFont(new Font("System", Font.BOLD, 20));
        timerLabel.setBorder(new LineBorder(Color.black));
        contentPane.add(timerLabel);

        // 제시어 레이블 추가
        quizLabel = new JLabel("", SwingConstants.CENTER);
        quizLabel.setBounds(675, 10, 250, 30);
        quizLabel.setFont(new Font("System", Font.BOLD, 20));
        quizLabel.setBorder(new LineBorder(Color.black));
        contentPane.add(quizLabel);
        
        Quizmanager = new QuizWord(quizLabel);  

        // 타이머 설정
        catchmindtimer = new CatchMindTimer(timerLabel, 60, () -> {
            JOptionPane.showMessageDialog(Canvas_space, "게임 종료");
            catchmindtimer.reset(60);
            Quizmanager.setRandomword(); // 새로운 제시
            
            Graphics g = createcanvas.getGraphics();
        	g.setColor(Color.WHITE); 
            g.fillRect(0, 0, getWidth(), getHeight());
            g.dispose();
            
        });

        catchmindtimer.start(); // 타이머 시작

        // 초기 제시어 설정
        Quizmanager.setRandomword();

        return panel;
    }
    
    private void sendUserInfo (String img, String username) {
    	try {
			dos.writeUTF("users:" + img + " " + username);
			dos.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
   
    
    private void sendDrawCommand(int x1, int y1, int x2, int y2) {
        try {
        	String colorCode = Integer.toString(color.getRGB());
            dos.writeUTF("DRAW:" + x1 + " " + y1 + " " + x2 + " " + y2 + " " + colorCode);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    private void listenToServer() {
        try (DataInputStream input = new DataInputStream(socket.getInputStream())) {
            while (true) {
            	String message = input.readUTF();
            	if (message.startsWith("Your client number:")) { /// 내 유저 프로필 그리기
            		people_num = Integer.parseInt(message.split(":")[1].trim());
                    System.out.println("You are client #" + people_num );
                    
                    if(people_num < 0) { // 4명 꽉 찼을 때 
                    	JOptionPane.showMessageDialog(Canvas_space, "인원이 다 찼습니다");
                    	System.exit(0); // 프로그램 종료
                    }
                    
            	}
            	
            	if (message.startsWith("DRAW:")) { /////////그리기
            		 String[] drawCommand = message.replace("DRAW:", "").split(" ");
	                
	                if (drawCommand.length == 5) {	               
		            	int x1 = Integer.parseInt(drawCommand[0]);
		                int y1 = Integer.parseInt(drawCommand[1]);
		                int x2 = Integer.parseInt(drawCommand[2]);
		                int y2 = Integer.parseInt(drawCommand[3]);
		                Color receivedColor = new Color(Integer.parseInt(drawCommand[4]));
		                
		                SwingUtilities.invokeLater(() -> {
		                    Graphics g = createcanvas.getGraphics();
		                    g.setColor(receivedColor);
		                    g.drawLine(x1, y1, x2, y2);
		                    g.dispose();
		                });
	               }
               
                }
            	
            	if (message.startsWith("all userinfos:")) {
            		String[] msg = message.replace("all userinfos:", "").split("\\*"); // info랑 index 나눔
            		String[] index = msg[0].split(" ");
            		String[] info = msg[1].split(",");
            		
            		for (int i=0;i<index.length;i++) {
            			String[] both = info[i].split(" "); // both[0] -> pic, both[1] -> name
            			
                		// 각 유저 프로필 그림
                		user_place(Integer.parseInt(index[i]), both[0], both[1]);
            		}
            		
            	}
            	
            	
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void user_place(int ppl, String pic, String name) {//매개변수로 자리, 정보 받아서 배치
    	
	    JLayeredPane layeredPane = (JLayeredPane) getContentPane(); 
	    
		ImageIcon characterImageIcon = new ImageIcon(pic); /// 캐릭터 이미지 
        Image img = characterImageIcon.getImage().getScaledInstance(90, 90, Image.SCALE_SMOOTH);  //이미지 크기
        JLabel characterImageLabel = new JLabel(new ImageIcon(img));
            
        JLabel usernameLabel = new JLabel(name);
        usernameLabel.setText(name);
        usernameLabel.setFont(new Font("System", Font.BOLD, 20));
        usernameLabel.setForeground(Color.white);
        
        //받은 0,1 배열 통해서 비어있는 작은 수 인덱스부터 채워넣기
        
        if (ppl == 0) {
            characterImageLabel.setBounds(50, 110, 90, 90);
            usernameLabel.setBounds(30, 200, 150, 30);
            }
        else if(ppl ==1) {
        	characterImageLabel.setBounds(50, 290, 90, 90);
            usernameLabel.setBounds(30, 380, 150, 30);
        }
        else if(ppl == 2) {
        	characterImageLabel.setBounds(820, 110, 90, 90);
            usernameLabel.setBounds(810, 200, 150, 30);
        }
        else if(ppl == 3) {
        	characterImageLabel.setBounds(820, 290, 90, 90);
            usernameLabel.setBounds(810, 380, 150, 30);
        }
            layeredPane.add(characterImageLabel, Integer.valueOf(1)); // Add at higher layer
    	    layeredPane.add(usernameLabel, Integer.valueOf(1));
    	
    }
}