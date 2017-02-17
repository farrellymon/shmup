package shmup;

import java.awt.*;

import javax.swing.JFrame;

public class main extends JFrame {

	private main() {
		panel gamePanel = new panel();
		
		add(gamePanel);
        setTitle("Java pls");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        setResizable(false);
    }
	public static void main(String[] args) {
EventQueue.invokeLater(new Runnable() {
            
            @Override
            public void run() {                
                JFrame ex = new main();
                ex.setVisible(true);                
            }
        });
	}

}
