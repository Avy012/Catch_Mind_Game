import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;

/// 게임 화면
public class CatchClientView extends JFrame {
    private JTextField txtInput;
    private String UserName;
    private JTextArea textArea;
    private static final int BUF_LEN = 128; // Windows 처럼 BUF_LEN 을 정의
    private Socket socket; // 연결소켓
    private InputStream is;
    private OutputStream os;
    private DataInputStream dis;
    private DataOutputStream dos;

	
    private JPanel contentPane;
    private int people_num = 1; // 게임에 있는 사람 수
    private String[] names = {"","","",""};
    private String[] pics = {"","","",""};
    
    
    //canvas
    private JPanel drawing;
    private Canvas createcanvas;
    private JPanel userpanel;
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

    
    private QuizWord Quizmanager;
    private JLabel timerLabel, quizLabel;
    private int QuizOk = 1;
    private String correct;
    private JButton btnSend, Quizmake;
    private int myScore = 0; // 현재 플레이어의 점수
    private Map<String, Integer> playerScores = new HashMap<>(); // 전체 스코어
    private JLabel scoreLabel;
    private Map<Integer, JLabel> userImageLabels = new HashMap<>(); // 사용자 이미지 레이블
    private Map<Integer, JLabel> userNameLabels = new HashMap<>(); // 사용자 이름 레이블
    
    public CatchClientView(String username, String ip_addr, String port_no, String img_path) {
    	
    	
    	
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(250, 50, 1000, 750);
        
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(null); 
        setContentPane(layeredPane);

        contentPane = new JPanel();
        contentPane.setBounds(0, 0, getWidth(), getHeight());
        contentPane.setLayout(null);
        layeredPane.add(contentPane, Integer.valueOf(0)); 

        UserName = username;
        
        //로고
        ImageIcon logo = new ImageIcon("images/logo2.png"); /// 캐릭터 이미지 
        Image img = logo.getImage().getScaledInstance(90, 90, Image.SCALE_SMOOTH);  //이미지 크기
        JLabel logolabel = new JLabel(new ImageIcon(img));
        logolabel.setBounds(220, 10, 150, 30);
       
        //캔버스
        drawing = createDrawingPanel();
        drawing.setBounds(200, 100, 550, 500); 
        contentPane.add(drawing);
        
        //채팅
  		JScrollPane scrollPane = new JScrollPane();
  		scrollPane.setBounds(200, 610, 550, 45);
  		contentPane.add(scrollPane);

  		textArea = new JTextArea();
  		textArea.setEditable(false);
  		scrollPane.setViewportView(textArea);
          //채팅 입력
  		txtInput = new JTextField();
        txtInput.setBounds(200, 660, 550, 30);
  		contentPane.add(txtInput);
  		txtInput.setColumns(10);
	    // 채팅 전송 버튼
	    btnSend = new JButton("전송");
	    btnSend.setBounds(880, 660, 70, 30);
	    btnSend.addActionListener(e -> sendMessage());
	    contentPane.add(btnSend);
	    // 퀴즈 단어
	    quizLabel = new JLabel("", SwingConstants.CENTER);
	    quizLabel.setBounds(675, 10, 250, 30);
	    quizLabel.setFont(new Font("System", Font.BOLD, 20));
	    quizLabel.setBorder(new LineBorder(Color.black));
	    contentPane.add(quizLabel);
	    // 퀴즈 매니저 초기화
	    Quizmanager = new QuizWord(quizLabel);
	    //시작버튼
	    Quizmake = new JButton("시작");
	    Quizmake.setBounds(880, 600, 70, 30);
	    Quizmake.addActionListener(e -> Quizgenerate());
	    contentPane.add(Quizmake);
	    
	    timerLabel = new JLabel("", SwingConstants.CENTER);
        timerLabel.setBounds(50, 10, 250, 30);
        timerLabel.setFont(new Font("System", Font.BOLD, 20));
        timerLabel.setBorder(new LineBorder(Color.black));
        contentPane.add(timerLabel);
        
	    initializeScorePanel();
	    
	    startDataCommunication(username, ip_addr, port_no, img_path); // 프레임 초기화 후 데이터 송수신 오픈
	    
    }
    
    private void startDataCommunication(String username, String ip_addr, String port_no, String img_path) {
    	
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
            SendMessage("erase");
        });

        panel.add(Canvas_space, BorderLayout.CENTER);
        panel.add(color_space, BorderLayout.SOUTH);
        
        
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
    
    private void Quizgenerate() {
    	correct=Quizmanager.setRandomword(correct);
    	System.out.println("정답보내기");
    	myScore -= 1;
    	sendCorrect();
    	Quizmake.setEnabled(false);
    	try {
			dos.writeUTF("start");
			dos.flush();
		} catch (IOException e) {
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
    
    private void sendCorrect() {
    	btnSend.setEnabled(false);
    	QuizOk=0;
    	myScore += 1; // 정답 점수 추가
        sendUpdatedScore();// 점수 보내기
        String msg = String.format("CORRECT:"+ correct);
        SendMessage(msg);
    }
    
    private void sendMessage() {
    	if(correct.equals(txtInput.getText())&& QuizOk!=0) {
    		correct=Quizmanager.setRandomword(correct);
    		sendCorrect();
    		Graphics g = createcanvas.getGraphics();
        	g.setColor(Color.WHITE); 
            g.fillRect(0, 0, getWidth(), getHeight());
            g.dispose(); 
    	}
        String msg = String.format("CHAT: [%s] %s\n", UserName, txtInput.getText());
        String chatMessage = msg.replace("CHAT:", "");
        textArea.append(chatMessage);
        SendMessage(msg);
        txtInput.setText(""); // 메세지 입력창을 비운다
        txtInput.requestFocus(); // 텍스트 필드로 커서를 다시 위치시킨다
        if (msg.contains("/exit")) { // 종료 처리
            System.exit(0);
        }
    }
    
    public void SendMessage(String msg) {
        try {
            // Use writeUTF to send messages
            dos.writeUTF(msg);
        } catch (IOException e) {
            try {
                dos.close();
                dis.close();
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
                System.exit(0);
            }
        }
    }
    
    private void sendUpdatedScore() { // 닉네임이랑 점수 보냄
        try {
            dos.writeUTF("SCORE:" + UserName + ":" + myScore);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void initializeScorePanel() {
        scoreLabel = new JLabel("점수판");
        scoreLabel.setBounds(10, 600, 170, 100);
        scoreLabel.setVerticalAlignment(SwingConstants.TOP);
        scoreLabel.setBorder(new LineBorder(Color.BLACK));
        contentPane.add(scoreLabel);
    }

    private void updateScorePanel() {
    	if (playerScores!=null) {
	        StringBuilder scoreText = new StringBuilder("<html>");
	        scoreText.append("점수판<br>");
	        for (Map.Entry<String, Integer> entry : playerScores.entrySet()) {
	            scoreText.append(entry.getKey()).append(": ").append(entry.getValue()).append("점<br>");
	        }
	        scoreText.append("</html>");
	        scoreLabel.setText(scoreText.toString());
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
            	if (message.startsWith("CORRECT:")) { // 출제자 아닌 사람들한테 문제 바뀔 때 정답알려줌 , 캔버스 초기화
            		if(btnSend!=null)
            			btnSend.setEnabled(true);
                    String Message = message.replace("CORRECT:", "");
                    correct=Message;
                    if(Quizmanager!=null) { 
                    	Quizmake.setEnabled(false);
                    	Graphics g = createcanvas.getGraphics();
                    	g.setColor(Color.WHITE); 
                        g.fillRect(0, 0, getWidth(), getHeight());
                        g.dispose(); 
                    	Quizmanager.blindword();
                    	QuizOk=1;
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
            		
            		resetUserPanels(); //유저 패널 리셋
            		
            		for (int i=0;i<4;i++) {
            			names[i] = "";
            			pics[i] = "";
            		}
            		
            		//유저 공간
                    JPanel leftSquares = new UserSquaresPanel();
                    JPanel rightSquares = new UserSquaresPanel();
                    leftSquares.setPreferredSize(new Dimension(150, 500));
                    rightSquares.setPreferredSize(new Dimension(150, 500));
                    leftSquares.setBounds(20,100,150,500);
                    contentPane.add(leftSquares);
                    rightSquares.setBounds(800,100,150,500);
                    contentPane.add(rightSquares);

            	    
                    
            		for (int i=0;i<index.length;i++) {
            			String[] both = info[i].split(" "); // both[0] -> pic, both[1] -> name
                		// 각 유저 프로필 그림
                		user_place(Integer.parseInt(index[i]), both[0], both[1]);
            		}
            	}
            	
            	if (message.startsWith("CHAT:")) {
                    String chatMessage = message.replace("CHAT:", "");
                    textArea.append(chatMessage);
                }
            	if (message.startsWith("TIMER:")) {
            		String msg = message.replace("TIMER:", "");
            		if (msg.equals("expired")) {///// 시간 만료 -> 정답 공개, 다음 문제
            			JOptionPane.showMessageDialog(Canvas_space, "정답은 " + correct + "입니다.");
            			if (QuizOk == 0) {
            				btnSend.setEnabled(false);
            				QuizOk=0;
            				correct=Quizmanager.setRandomword(correct);
                            String msg1 = String.format("CORRECT:"+ correct);
                            SendMessage(msg1);
            			}
            			
                    	Graphics g = createcanvas.getGraphics();
                    	g.setColor(Color.WHITE); 
                        g.fillRect(0, 0, getWidth(), getHeight());
                        g.dispose(); 
                        
            		}
            		else
            			timerLabel.setText("남은시간: "+ msg + "초");
            	}
            	if (message.startsWith("SCORE_UPDATE:")) {
            	    String[] scoreUpdate = message.split(":");
            	    String playerName = scoreUpdate[1];
            	    int score = Integer.parseInt(scoreUpdate[2]);
            	    playerScores.put(playerName, score);
            	    System.out.println(playerName+ score);
            	    // 점수를 화면에 갱신
            	    if (playerScores != null ) {
            	    	updateScorePanel();
            	    }
            	    
            	}
            	if (message.startsWith("erase")) {
            		Graphics g = createcanvas.getGraphics();
                	g.setColor(Color.WHITE); 
                    g.fillRect(0, 0, getWidth(), getHeight());
                    g.dispose(); 
            	}
            	
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetUserPanels() {
        // 기존 유저레이블 제거
        JLayeredPane layeredPane = (JLayeredPane) getContentPane();
        for (JLabel label : userImageLabels.values()) {
            layeredPane.remove(label);
        }
        for (JLabel label : userNameLabels.values()) {
            layeredPane.remove(label);
        }
        userImageLabels.clear();
        userNameLabels.clear();
        layeredPane.repaint();
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
        layeredPane.add(characterImageLabel, Integer.valueOf(2)); 
        layeredPane.add(usernameLabel, Integer.valueOf(2)); 
        
        userImageLabels.put(ppl, characterImageLabel);  //맵에 이미지 저장
        userNameLabels.put(ppl, usernameLabel);  //맵에 이름 저장

        layeredPane.revalidate();
        layeredPane.repaint();
    }
}