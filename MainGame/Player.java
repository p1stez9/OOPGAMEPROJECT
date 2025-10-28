package MainGame;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Player extends Entity {
    GamePanle gp;
    KeyEventHandler keyH;

    public final int screenX;
    public final int screenY;
    
    public int x, y, speed;

    public Player(GamePanle gp, KeyEventHandler keyH){
        this.gp = gp;
        this.keyH = keyH;

        screenX = gp.Widthscreen / 2 - (gp.titlesize / 2);
        screenY = gp.Hightscreen / 2 - (gp.titlesize / 2);

        solidArea = new Rectangle(); // จำกัด hitbox
        solidArea.x = 8;
        solidArea.y = 16;
        solidArea.width = 32;
        solidArea.height = 32;

        setDefaultValues();
        getPlayerImage();
    }

    public void setDefaultValues(){
        worldX = gp.titlesize * 23;
        worldY = gp.titlesize * 21;
        speed = 4;
        direction = "down";
    }

    public void getPlayerImage() {
        
        try {
            up1 = ImageIO.read(getClass().getResourceAsStream("pixil-frame-3.png"));
            up2 = ImageIO.read(getClass().getResourceAsStream("pixil-frame-3.png"));
            down1 = ImageIO.read(getClass().getResourceAsStream("pixil-frame-4.png"));
            down2 = ImageIO.read(getClass().getResourceAsStream("pixil-frame-4.png"));
            left1 = ImageIO.read(getClass().getResourceAsStream("pixil-frame-2.png"));
            left2 = ImageIO.read(getClass().getResourceAsStream("pixil-frame-2.png"));
            right1 = ImageIO.read(getClass().getResourceAsStream("pixil-frame-1.png"));
            right2 = ImageIO.read(getClass().getResourceAsStream("pixil-frame-1.png"));
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update(){
        if (keyH.upPressed == 1 && (worldY - speed) >= 0) {
            direction = "up";
        } else if (keyH.downPressed == 1 && (worldY + speed) <= gp.worldHeight - gp.titlesize) {
            direction = "down";
        } else if (keyH.leftPressed == 1 && (worldX - speed) >= 0) {
            direction = "left";
        } else if (keyH.rightPressed == 1 && (worldX + speed) <= gp.worldWidth - gp.titlesize) {
            direction = "right";
        } else {
            return;
        }

        // Check tile collision
        collisionOn = false;
        gp.cChecker.checkTile(this);

        // if collision is false, player can move
        if(collisionOn == false) {
            switch(direction) {
            case "up":
                worldY -= speed;
                break;
            case "down":
                worldY += speed;
                break;
            case "left":
                worldX -= speed;
                break;
            case "right":
                worldX += speed;
                break;
            }
        }

        // Clamp ทั่วไปไม่ให้หลุดนอกโลก (ไม่มี margin)
        worldX = Math.max(0, Math.min(worldX, gp.worldWidth - gp.titlesize));
        worldY = Math.max(0, Math.min(worldY, gp.worldHeight - gp.titlesize));
    }

    public void draw(java.awt.Graphics2D g2){
        // g2.setColor(Color.blue);
        // g2.fillRect(x, y, gp.titlesize, gp.titlesize);
        
        BufferedImage image = null;

        switch(direction) {
        case "up":
            if(spriteNum == 1) {
                image = up1;
            }
            if(spriteNum == 2) {
                image = up2;
            }
            break;
        case "down":
            if(spriteNum == 1) {
                image = down1;
            }
            if(spriteNum == 2) {
                image = down2;
            }
            break;
        case "left":
            if(spriteNum == 1) {
                image = left1;
            }
            if(spriteNum == 2) {
                image = left2;
            }
            break;
        case "right":
            if(spriteNum == 1) {
                image = right1;
            }
            if(spriteNum == 2) {
                image = right2;
            }
            break;
        }
        g2.drawImage(image, screenX, screenY, gp.titlesize, gp.titlesize, null);
    }
}
