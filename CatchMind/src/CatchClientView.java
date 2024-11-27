import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;

import javax.imageio.ImageIO;
import javax.swing.*;

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
    private String ip_addr = "127.0.0.1";

	
    private JPanel contentPane;
    private String img_path;
    private int people_num = 1; // 게임에 있는 사람 수

    public CatchClientView(String username, String port_no, String img_path) {
    	
    	try {
            socket = new Socket(ip_addr, Integer.parseInt(port_no));
            is = socket.getInputStream();
            dis = new DataInputStream(is);
            os = socket.getOutputStream();
            dos = new DataOutputStream(os);
            
            System.out.println(username + " connected");
            
        } catch (NumberFormatException | IOException e) {
            e.printStackTrace();
        }
    	
    	this.img_path = img_path;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(250, 50, 1000, 750);
        contentPane = new JPanel();
        contentPane.setLayout(null);
        setContentPane(contentPane);


        ImageIcon characterImageIcon = new ImageIcon(img_path); /// 캐릭터 이미지 
        Image img = characterImageIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);  //이미지 크기
        JLabel characterImageLabel = new JLabel(new ImageIcon(img));
        characterImageLabel.setBounds(200, 50, 200, 200);
        contentPane.add(characterImageLabel);

 
        JLabel usernameLabel = new JLabel(username); // 닉네임 
        usernameLabel.setFont(new Font("System", Font.BOLD, 20));
        usernameLabel.setBounds(200, 270, 200, 30);
        contentPane.add(usernameLabel);


        UserSquaresPanel userSquaresPanel = new UserSquaresPanel();
        userSquaresPanel.setBounds(50, 350, 900, 350); 
        contentPane.add(userSquaresPanel);
        
        
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
}