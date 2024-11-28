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
    private JLabel lblUserName;
    private String ip_addr ;
    private String port;

	
    private JPanel contentPane;
    private String img_path;
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


    public CatchClientView(String username, String ip_addr, String port_no, String img_path) {
    	
    	try {
            socket = new Socket(ip_addr, Integer.parseInt(port_no));
            is = socket.getInputStream();
            dis = new DataInputStream(is);
            os = socket.getOutputStream();
            dos = new DataOutputStream(os);
            
            new Thread(this::listenToServer).start();
            System.out.println(username + " connected");
            
        } catch (NumberFormatException | IOException e) {
            e.printStackTrace();
        }
    	
    	this.ip_addr = ip_addr;
    	port = port_no;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(250, 50, 1000, 750);
        contentPane = new JPanel();
        contentPane.setLayout(null);
        setContentPane(contentPane);

        this.img_path = img_path;
        ImageIcon characterImageIcon = new ImageIcon(img_path); /// 캐릭터 이미지 
        Image img = characterImageIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);  //이미지 크기
        JLabel characterImageLabel = new JLabel(new ImageIcon(img));
        characterImageLabel.setBounds(30, 50, 100, 100);
        contentPane.add(characterImageLabel);

 
        JLabel usernameLabel = new JLabel(username); // 닉네임 
        usernameLabel.setFont(new Font("System", Font.BOLD, 20));
        usernameLabel.setBounds(30, 150, 200, 30);
        contentPane.add(usernameLabel);


//        UserSquaresPanel userSquaresPanel = new UserSquaresPanel();
//        userSquaresPanel.setBounds(50, 350, 900, 350); 
//        contentPane.add(userSquaresPanel);
        
        drawing = createDrawingPanel();
        drawing.setBounds(200, 100, 550, 500); 
        contentPane.add(drawing);
        
        
    }

    class UserSquaresPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.GRAY);
            
            if (people_num == 1) {
            	g.fillRect(50, 0, 150, 100); // Square 1
            	
            	Image icon = new ImageIcon(img_path).getImage();
            	//.getScaledInstance(100, 100, Image.SCALE_SMOOTH) //크기 
            	g.drawImage(icon, 50,0 ,this);
            }
            else if(people_num == 2) {
            	g.fillRect(50, 200, 150, 100); // Square 2
            }
            else if(people_num == 3) {
            	g.fillRect(500, 50, 150, 100); // Square 3
            }
            else if(people_num == 4) {
            	g.fillRect(500, 200, 150, 100); // Square 4
            }        
        }
    }
    
    private JPanel createDrawingPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Canvas Area
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
        });

        panel.add(Canvas_space, BorderLayout.CENTER);
        panel.add(color_space, BorderLayout.SOUTH);

        return panel;
    }
    
   
    
    private void sendDrawCommand(int x1, int y1, int x2, int y2) {
        try {
        	String colorCode = Integer.toHexString(color.getRGB());
            dos.writeUTF(x1 + " " + y1 + " " + x2 + " " + y2 + " " + colorCode);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    private void listenToServer() {
        try (DataInputStream input = new DataInputStream(socket.getInputStream())) {
            while (true) {
                String[] drawCommand = input.readUTF().split(" ");
                int x1 = Integer.parseInt(drawCommand[0]);
                int y1 = Integer.parseInt(drawCommand[1]);
                int x2 = Integer.parseInt(drawCommand[2]);
                int y2 = Integer.parseInt(drawCommand[3]);
                Color receivedColor = new Color(Integer.parseInt(drawCommand[4], 16));
                SwingUtilities.invokeLater(() -> {
                    Graphics g = createcanvas.getGraphics();
                    g.setColor(receivedColor);
                    g.drawLine(x1, y1, x2, y2);
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}