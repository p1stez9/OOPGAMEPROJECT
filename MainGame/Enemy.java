package MainGame;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

public class Enemy {
    GamePanle gp;
    
    public int worldX, worldY, speed;
    public boolean isDead = false; // สถานะการตาย
    private Random random = new Random();
    private int direction = 0; // 0=up, 1=down, 2=left, 3=right
    private int moveTimer = 0;
    private int moveInterval = 60; // เปลี่ยนทิศทางทุก 60 frames

    public Enemy(GamePanle gp) {
        this.gp = gp;
        setDefaultValues();
    }

    public void setDefaultValues() {
        worldX = gp.titlesize * 25;
        worldY = gp.titlesize * 25;
        speed = 1;
        direction = random.nextInt(4); // สุ่มทิศทางเริ่มต้น
    }

    public void update() {
        // ถ้าตายแล้วไม่ต้องอัปเดต
        if (isDead) return;
        
        moveTimer++;
        
        // เปลี่ยนทิศทางแบบสุ่ม
        if (moveTimer >= moveInterval) {
            direction = random.nextInt(4);
            moveTimer = 0;
            moveInterval = 30 + random.nextInt(60); // สุ่มช่วงเวลา 30-90 frames
        }
        
        int r = gp.titlesize;
        
        // เคลื่อนที่ตามทิศทางที่สุ่ม
        switch (direction) {
            case 0: // up
                if (worldY - speed >= 0) worldY -= speed;
                break;
            case 1: // down
                if (worldY + speed <= gp.worldHeight - r) worldY += speed;
                break;
            case 2: // left
                if (worldX - speed >= 0) worldX -= speed;
                break;
            case 3: // right
                if (worldX + speed <= gp.worldWidth - r) worldX += speed;
                break;
        }
        
        // กันไม่ให้ออกนอกโลก
        worldX = Math.max(0, Math.min(worldX, gp.worldWidth - r));
        worldY = Math.max(0, Math.min(worldY, gp.worldHeight - r));
    }

    public void draw(Graphics2D g2) {
        // ถ้าตายแล้วไม่ต้องวาด
        if (isDead) return;
        
        // คำนวณตำแหน่งบนหน้าจอ
        int screenX = worldX - gp.player1.worldX + gp.player1.screenX;
        int screenY = worldY - gp.player1.worldY + gp.player1.screenY;
        
        g2.setColor(Color.red);
        g2.fillRect(screenX, screenY, gp.titlesize, gp.titlesize);
    }
}
