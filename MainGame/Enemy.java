package MainGame;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;

public class Enemy {
    GamePanle gp;
    
    public int worldX, worldY, speed;
    public boolean isDead = false; // สถานะการตาย
    private Random random = new Random();
    private int direction = 0; // 0=up, 1=down, 2=left, 3=right
    private int moveTimer = 0;
    private int moveInterval = 60; // เปลี่ยนทิศทางทุก 60 frames

    // Sprite
    public BufferedImage sprite;
    private int spriteVariant = 1; // 1..4 สำหรับไฟล์ pixil-enemy-*.png

    public Enemy(GamePanle gp) {
        this.gp = gp;
        setDefaultValues();
        // ค่าพื้นฐาน: เลือกสกินแบบสุ่มถ้ายังไม่ได้ตั้งจากภายนอก
        setSpriteVariant(1 + random.nextInt(4));
    }

    public void setDefaultValues() {
        worldX = gp.titlesize * 30;
        worldY = gp.titlesize * 30;
        speed = 1;
        direction = random.nextInt(4); // สุ่มทิศทางเริ่มต้น
    }

    public void setSpriteVariant(int variant) {
        this.spriteVariant = Math.max(1, Math.min(variant, 4));
        String path = "pixil-enemy-" + this.spriteVariant + ".png";
        try {
            sprite = ImageIO.read(getClass().getResourceAsStream(path));
        } catch (IOException | IllegalArgumentException e) {
            // ถ้าโหลดไม่ได้ ปล่อยให้ sprite เป็น null และใช้สี่เหลี่ยมแทน
            sprite = null;
            e.printStackTrace();
        }
    }

    public int getSpriteVariant() {
        return spriteVariant;
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
        
        // กันไม่ให้ออกนอกโลก (ไม่มี margin)
        worldX = Math.max(0, Math.min(worldX, gp.worldWidth - r));
        worldY = Math.max(0, Math.min(worldY, gp.worldHeight - r));
    }

    public void draw(Graphics2D g2) {
        // ถ้าตายแล้วไม่ต้องวาด
        if (isDead) return;
        
        // คำนวณตำแหน่งบนหน้าจอด้วยกล้องที่ clamp
        int camX = gp.player1.worldX - gp.player1.screenX;
        int camY = gp.player1.worldY - gp.player1.screenY;
        camX = Math.max(0, Math.min(camX, gp.worldWidth - gp.Widthscreen));
        camY = Math.max(0, Math.min(camY, gp.worldHeight - gp.Hightscreen));
        int screenX = worldX - camX;
        int screenY = worldY - camY;
        
        if (sprite != null) {
            g2.drawImage(sprite, screenX, screenY, gp.titlesize, gp.titlesize, null);
        } else {
            g2.setColor(Color.red);
            g2.fillRect(screenX, screenY, gp.titlesize, gp.titlesize);
        }
    }
}
