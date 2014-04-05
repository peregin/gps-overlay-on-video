package peregin.tov;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import peregin.tov.gui.MigPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;

public class App extends JFrame {

    static Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String... args) {

        App app = new App();
        app.setTitle("Telemetry data overlay on videos");
        app.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        app.setIconImage(loadImage("images/video.png"));
        app.init();
        app.setSize(1024, 768);
        center(app);
        app.setVisible(true);
    }

    static void center(Window w) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle rc = new Rectangle(w.getBounds());
        rc.x = (screen.width - rc.width)/2;
        rc.y = (screen.height - rc.height)/2;
        w.setLocation(rc.x, rc.y);
    }

    static Image loadImage(String path) {
        try {
            logger.debug(String.format("loading %s", path));
            return ImageIO.read(App.class.getClassLoader().getResourceAsStream(path));
        } catch (Exception any) {
            logger.error(String.format("failed to load %s", path), any);
            return null;
        }
    }

    // setup layout and widgets
    void init() {
        JPanel top = new MigPanel("ins 5, fill", "[fill]", "[][fill]");

        JButton projectPanel = new JButton("project setup: new, open, save, export");
        top.add(projectPanel, "span 2, wrap");

        JButton videoPanel = new JButton("video");
        top.add(videoPanel, "pushy");

        JButton telemetryPanel = new JButton("telemetry");
        top.add(telemetryPanel, "pushy, wrap");

        // add container for widgets and templates

        JButton statusPanel = new JButton("Ready");
        top.add(statusPanel, "span 2");

        getContentPane().add(top);
    }
}
