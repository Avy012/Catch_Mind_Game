import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class CatchClientMain extends JFrame {

	private JPanel contentPane;
	private JTextField txtUserName;
	private JTextField txtPortNumber;
	private JLabel characterImageLabel;
	private String chosenChar;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					CatchClientMain frame = new CatchClientMain();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	
	public CatchClientMain() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(250, 50, 1000, 750); //4:3 윈도우 크기, 위치 지정
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		ImageIcon imageIcon = new ImageIcon("images/logo.png"); 
		Image img = imageIcon.getImage().getScaledInstance(130, 130, Image.SCALE_SMOOTH); // 이미지 크기 조정
		ImageIcon scaledIcon = new ImageIcon(img);
		JLabel imageLabel = new JLabel(scaledIcon);
		//JLabel imageLabel = new JLabel(imageIcon);
		imageLabel.setBounds(400, 50, 200, 130);
		contentPane.add(imageLabel);
		
		JLabel ChooseChar = new JLabel("캐릭터 선택");
		ChooseChar.setBounds(430, 200, 200, 33);
		ChooseChar.setFont(new Font("System", Font.BOLD, 25));
		contentPane.add(ChooseChar);
		
		
        JPanel radioPanel = new JPanel();  ///라디오 버튼
        radioPanel.setLayout(new GridLayout(1, 7));
        radioPanel.setBounds(250, 250, 500, 20);
        contentPane.add(radioPanel);

        String[] characterNames = {"양파", "양배추", "피망", "주먹밥", "샐러드"};
        String[] characterImagePaths = {"images/onion.png", "images/cabbage.png","images/pepper.png",
        		"images/rice.png","images/salad.png"};
        ButtonGroup characterGroup = new ButtonGroup();

        for (int i = 0; i < characterNames.length; i++) {
            JRadioButton radioButton = new JRadioButton(characterNames[i]);
            radioButton.setActionCommand(characterImagePaths[i]); 
            radioPanel.add(radioButton);
            characterGroup.add(radioButton);
            
            radioButton.addActionListener(new CharacterSelectionListener());
        }
        
        //라디오 버튼 이미지
        characterImageLabel = new JLabel();
        characterImageLabel.setBounds(450, 300, 200, 100); 
        contentPane.add(characterImageLabel);

        if (radioPanel.getComponentCount() > 0) {
            JRadioButton firstButton = (JRadioButton) radioPanel.getComponent(0);
            firstButton.setSelected(true);
            updateCharacterImage(characterImagePaths[0]); 
        }
        
        
        
		
		JLabel lblNewLabel = new JLabel("닉네임");
		lblNewLabel.setBounds(270, 450, 82, 33);
		lblNewLabel.setFont(new Font("System", Font.BOLD, 25));
		contentPane.add(lblNewLabel);
		
		
		txtUserName = new JTextField(); // 닉네임 입력 창
		txtUserName.setHorizontalAlignment(SwingConstants.CENTER);
		txtUserName.setBounds(450, 450, 116, 33);
		contentPane.add(txtUserName);
		txtUserName.setColumns(10);
		
		
		JLabel lblPortNumber = new JLabel("Port Number");
		lblPortNumber.setFont(new Font("System", Font.BOLD, 20));
		lblPortNumber.setBounds(270, 520, 150, 33);
		contentPane.add(lblPortNumber);
		
		txtPortNumber = new JTextField(); // 포트넘버 입력 창 
		txtPortNumber.setText("30000");
		txtPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
		txtPortNumber.setColumns(10);
		txtPortNumber.setBounds(450, 520, 116, 33);
		contentPane.add(txtPortNumber);
		
		JButton btnConnect = new JButton("연결하기");  //연결 버튼
		btnConnect.setBounds(400, 600, 205, 38);
		contentPane.add(btnConnect);
		Myaction action = new Myaction();
		btnConnect.addActionListener(action);
		txtUserName.addActionListener(action);
		txtPortNumber.addActionListener(action);
		
	}
	
	private void updateCharacterImage(String imagePath) {
        ImageIcon imageIcon = new ImageIcon(imagePath); 
        Image img = imageIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH); //크기
        characterImageLabel.setIcon(new ImageIcon(img)); 
        chosenChar = imagePath;
    }
	
	class Myaction implements ActionListener 
	{
		@Override
		public void actionPerformed(ActionEvent e) {
			String username = txtUserName.getText().trim();
			String port_no = txtPortNumber.getText().trim();
			String ip_addr = "127.0.0.1";
			CatchClientView view = new CatchClientView(username, ip_addr, port_no, chosenChar);
			setVisible(false);
			view.setVisible(true);
		}
	}
	

    class CharacterSelectionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String selectedImagePath = e.getActionCommand(); 
            updateCharacterImage(selectedImagePath); 
            
        }
    }
	
}