package peregin.tov;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Created by peregin on 03/04/14.
 */
public class App extends JFrame {

    public static void main(String... args) {

        App app = new App();
        app.setTitle("Telemetry data overlay on videos");
        app.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        app.setSize(1024, 768);
        app.setIconImage(loadImage("video.png"));
        center(app);
        app.setVisible(true);
    }

    static void center(Window w) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle rc = new Rectangle(w.getBounds());
        rc.x = (screen.width-rc.width)/2;
        rc.y = (screen.height-rc.height)/2;
        w.setLocation(rc.x, rc.y);
    }

    static Image loadImage(String path) {
        try {
            return ImageIO.read(App.class.getClassLoader().getResourceAsStream(path));
        } catch (IOException any) {
            return null;
        }
    }
}
