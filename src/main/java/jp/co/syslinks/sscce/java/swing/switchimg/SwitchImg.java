package jp.co.syslinks.sscce.java.swing.switchimg;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

/**
 * 画像の切り替え。
 * ポイントは、切り替え処理はスレッドの中で行うこと。
 */
public final class SwitchImg extends JPanel {

    private static final long serialVersionUID = -5760171090762265645L;

    private SwitchImg() {
        super(new BorderLayout());
        setPreferredSize(new Dimension(800, 600));
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(SwitchImg::createAndShowGui);
    }

    private ImageIcon icon = null;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (icon == null) {
            return;
        }
        System.out.println("+++++++++++++++");
        Graphics2D g2 = (Graphics2D) g.create();
        g2.drawImage(icon.getImage(), 0, 0, this);
        g2.dispose();
    }

    /*@Override*/
    public void updateUI123(String file) {
        icon = new ImageIcon(this.getClass().getResource(file));
        super.updateUI();
    }

    private static void createAndShowGui() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
            Toolkit.getDefaultToolkit().beep();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        final SwitchImg comp = new SwitchImg();
        frame.getContentPane().add(comp);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        new Thread(() -> {
            try {
                for (;;) {
                    comp.updateUI123("doraemon.jpg");
                    TimeUnit.SECONDS.sleep(3);
                    comp.updateUI123("nobita.jpg");
                    TimeUnit.SECONDS.sleep(3);
                    comp.updateUI123("shizuka.jpg");
                    TimeUnit.SECONDS.sleep(3);
                    comp.updateUI123("takeshi.jpg");
                    TimeUnit.SECONDS.sleep(3);
                    comp.updateUI123("suneo.jpg");
                    TimeUnit.SECONDS.sleep(3);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        /*progress.scheduleWithFixedDelay(() -> {
            comp.updateUI123();
        }, 0, 3, TimeUnit.SECONDS);*/
    }
}