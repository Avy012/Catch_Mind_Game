import java.awt.*;
import javax.swing.*;

public class CatchClientView extends JFrame {
    private JPanel contentPane;

    public CatchClientView(String username, String port_no, String img_path) {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(250, 50, 1000, 750);
        contentPane = new JPanel();
        contentPane.setLayout(null);
        setContentPane(contentPane);

        // Display Character Image
        ImageIcon characterImageIcon = new ImageIcon(img_path);
        Image img = characterImageIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        JLabel characterImageLabel = new JLabel(new ImageIcon(img));
        characterImageLabel.setBounds(200, 50, 200, 200);
        contentPane.add(characterImageLabel);

        // Display Username underneath
        JLabel usernameLabel = new JLabel("Username: " + username);
        usernameLabel.setFont(new Font("System", Font.BOLD, 20));
        usernameLabel.setBounds(200, 270, 200, 30);
        contentPane.add(usernameLabel);

        // Add a custom panel to draw the user squares
        UserSquaresPanel userSquaresPanel = new UserSquaresPanel();
        userSquaresPanel.setBounds(50, 350, 900, 350); // Adjust size and position as needed
        contentPane.add(userSquaresPanel);
    }

    // Custom JPanel for drawing squares
    class UserSquaresPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.setColor(Color.GRAY);

            // Draw squares at specified positions
            g.fillRect(50, 0, 100, 100); // Square 1
            g.fillRect(50, 200, 100, 100); // Square 2
            g.fillRect(500, 50, 100, 100); // Square 3
            g.fillRect(500, 200, 100, 100); // Square 4

            
        }
    }
}