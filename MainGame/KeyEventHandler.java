package MainGame;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
public class KeyEventHandler implements KeyListener{

    public int upPressed = 0;
    public int downPressed = 0;
    public int leftPressed = 0;
    public int rightPressed = 0;
    public int spacePressed = 0;
    public int wep1 = 0;
    public int wep2 = 0;
    public int wep3 = 0;
    public int wep4 = 0;
    public int tabPressed = 0;
    public int enterPressed = 0;
    public int attackPressed = 0;
    public int escPressed = 0;


    
    @Override
    public void keyTyped(java.awt.event.KeyEvent e) {
    }

    @Override
    public void keyPressed(java.awt.event.KeyEvent e) {

        int ASCII = e.getKeyCode();
        if(ASCII == KeyEvent.VK_W){
            upPressed = 1;
        }
        if(ASCII == KeyEvent.VK_A){
            System.out.println("A");
            leftPressed = 1;
        }
        if(ASCII == KeyEvent.VK_S){
            System.out.println("S");
            downPressed = 1;
        }
        if(ASCII == KeyEvent.VK_D){
            System.out.println("D");
            rightPressed = 1;
        }
        if(ASCII == KeyEvent.VK_SPACE){
            System.out.println("SPACE PRESSED - attackPressed = 1");
            spacePressed = 1;
            attackPressed = 1;
        }

        // Card hotkeys: G/H/L/K
        if(ASCII == KeyEvent.VK_G){
            wep1 = 1; // STUN
        }
        if(ASCII == KeyEvent.VK_H){
            wep2 = 1; // POISON
        }
        if(ASCII == KeyEvent.VK_L){
            wep3 = 1; // SHIELD
        }
        if(ASCII == KeyEvent.VK_K){
            wep4 = 1; // HEAL
        }

        if (ASCII == KeyEvent.VK_P)   tabPressed = 1;    // ← เพิ่ม
        if (ASCII == KeyEvent.VK_ENTER) enterPressed = 1;  // ← เพิ่ม
        if (ASCII == KeyEvent.VK_ESCAPE) escPressed = 1;  // ESC key for pause
    }

    @Override
    public void keyReleased(java.awt.event.KeyEvent e) {
        int ASCII = e.getKeyCode();
        if(ASCII == KeyEvent.VK_W){
            upPressed = 0;
        }
        if(ASCII == KeyEvent.VK_A){
            System.out.println("A");
            leftPressed = 0;
        }
        if(ASCII == KeyEvent.VK_S){
            System.out.println("S");
            downPressed = 0;
        }
        if(ASCII == KeyEvent.VK_D){
            System.out.println("D");
            rightPressed = 0;
        }
        if(ASCII == KeyEvent.VK_SPACE){
            System.out.println("SPACE RELEASED - attackPressed = 0");
            spacePressed = 0;
            attackPressed = 0;
        }
        if(ASCII == KeyEvent.VK_G){
            wep1 = 0;
        }
        if(ASCII == KeyEvent.VK_H){
            wep2 = 0;
        }
        if(ASCII == KeyEvent.VK_L){
            wep3 = 0;
        }
        if(ASCII == KeyEvent.VK_K){
            wep4 = 0;
        }
        if (ASCII == KeyEvent.VK_P)   tabPressed = 0;     // ← เพิ่ม
        if (ASCII == KeyEvent.VK_ENTER) enterPressed = 0;   // ← เพิ่ม
        if (ASCII == KeyEvent.VK_ESCAPE) escPressed = 0;   // ESC key for pause
        
    }
}