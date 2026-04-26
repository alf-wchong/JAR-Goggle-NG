package chongwm.utility.java.jargoggle;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import chongwm.utility.java.jargoggle.ui.MainFrame;

public final class MainApp {
    private MainApp() {
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
