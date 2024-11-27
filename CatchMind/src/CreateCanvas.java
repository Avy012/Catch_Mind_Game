import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class CreateCanvas extends Canvas {
    private int lastX = -1, lastY = -1;
    private Color color = Color.black;

    public CreateCanvas() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(700, 450)); // 기본 크기 설정

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                if (lastX != -1 && lastY != -1) {
                    Graphics g = getGraphics();
                    g.setColor(color);
                    g.drawLine(lastX, lastY, x, y);
                }
                lastX = x;
                lastY = y;
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                lastX = -1;
                lastY = -1;
            }
        });
    }

    public void set_color(Color color1) {
        color = color1;
    }

    public void clearCanvas() {
        Graphics g = getGraphics();
        g.setColor(Color.WHITE); 
        g.fillRect(0, 0, getWidth(), getHeight());
        g.dispose(); 
    }
}
