package MainGame;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class TurnBase {
    GamePanle gp;

    int playerHP = 100;
    int enemyHP = 50;
    int enemyMaxHP = 50;
    int enemyAttack = 5;
    int turn = 0; // 0 = Player, 1 = Enemy
    
    // คุมเทิร์น
    int enemyActionTimer = 0;
    final int ENEMY_ACTION_DELAY = 45; // หน่วงเวลา
    int playerPostAttackCooldown = 0; // cooldown player

    // Dialog "End Turn", "Enemy Turn"
    boolean battleDialogVisible = false;
    String battleDialogText = "";
    int battleDialogTimer = 0;
    int battleDialogStage = -1; // -1 ไม่มี 0 ข้อความแรก 1 ข้อความสอง
    final int BATTLE_DIALOG_DURATION = 45;
    
    Enemy currentEnemy;

    // Victory
    boolean victorySequence = false;
    int victoryTimer = 0;
    final int VICTORY_TEXT_DURATION = 90; // 1.5s
    int fadeAlpha = 0;
    final int FADE_SPEED = 10;

    // Defeat
    boolean defeatSequence = false;
    int defeatTimer = 0;
    final int DEFEAT_TEXT_DURATION = 90; // 1.5s

    // กำหนดค่าความเก่งตามสี (1=ฟ้า, 2=เหลือง, 3=ส้ม, 4=แดง)
    public void initForEnemy(Enemy enemy) {
        this.currentEnemy = enemy;
        int v = (enemy != null) ? enemy.getSpriteVariant() : 1;
        switch (v) {
            case 1: // ฟ้า 
                enemyMaxHP = 10; enemyAttack = 4; break;
            case 2: // เหลือง
                enemyMaxHP = 10; enemyAttack = 6; break;
            case 3: // ส้ม
                enemyMaxHP = 10; enemyAttack = 8; break;
            case 4: // แดง
                enemyMaxHP = 10; enemyAttack = 10; break;
            default:
                enemyMaxHP = 10; enemyAttack = 5; break;
        }
        enemyHP = enemyMaxHP;
        // ใช้ค่า HP ผู้เล่นที่คงอยู่ข้ามการต่อสู้
        if (gp != null) {
            playerHP = Math.max(0, Math.min(100, gp.persistentPlayerHP));
        }
    }

    // Card/Magic dust system
    int magicDust = 5; // เริ่มต้น 5
    final int DUST_PER_TURN = 1; // เพิ่มเทิร์นละ 1
    final int CARD_COST = 2; // ใช้การ์ดใบละ 2
    final int HEAL_AMOUNT = 20; // การ์ดฮีลเพิ่มเลือด
    boolean enemyStunned = false;
    int enemyStunTurns = 0;
    int enemyPoisonTurns = 0; // ศัตรูเสียเลือดปลายตา
    int playerShieldTurns = 0; // ลดดาเมจศัตรู

    public TurnBase(GamePanle gp) {
        this.gp = gp;
    }

    public void update() {
        // ตอนชนะแสดงข้อความและค่อยๆดำก่อนกลับสู่ Explore
        if (victorySequence) {
            // แสดงข้อความช่วงแรก
            if (victoryTimer < VICTORY_TEXT_DURATION) {
                victoryTimer++;
                return;
            }
            // จากนั้นค่อยๆเฟดดำ
            if (fadeAlpha < 255) {
                fadeAlpha = Math.min(255, fadeAlpha + FADE_SPEED);
                return;
            }
            // เฟดเต็มแล้ว กลับสู่โหมด Explore และรีเซ็ตสถานะการต่อสู้
            gp.state = GamePanle.GameState.EXPLORE;
            // อัปเดต HP ผู้เล่นให้คงอยู่ข้ามการต่อสู้
            gp.persistentPlayerHP = playerHP;
            // รีเซ็ต transition variables
            gp.transitionAlpha = 0;
            gp.showDialog = false;
            gp.dialogTimer = 0;
            // รีเซ็ตสถานะการต่อสู้ภายใน TurnBase
            enemyHP = 50;
            turn = 0;
            enemyActionTimer = 0;
            playerPostAttackCooldown = 0;
            battleDialogVisible = false;
            battleDialogText = "";
            battleDialogTimer = 0;
            battleDialogStage = -1;
            enemyStunned = false;
            enemyStunTurns = 0;
            enemyPoisonTurns = 0;
            playerShieldTurns = 0;
            gp.currentEnemyIndex = -1;
            currentEnemy = null;
            // รีเซ็ตตัวแปรฉากชัยชนะ
            victorySequence = false;
            victoryTimer = 0;
            fadeAlpha = 0;
            System.out.println("การต่อสู้จบแล้ว! กลับไป Explore mode");
            return;
        }

        // ตอนแพ้แสดงข้อความแล้วค่อยๆดำก่อนกลับ Title
        if (defeatSequence) {
            if (defeatTimer < DEFEAT_TEXT_DURATION) {
                defeatTimer++;
                return;
            }
            if (fadeAlpha < 255) {
                fadeAlpha = Math.min(255, fadeAlpha + FADE_SPEED);
                return;
            }
            // ไปหน้า Title และรีเซ็ตการต่อสู้
            gp.state = GamePanle.GameState.TITLE;
            gp.persistentPlayerHP = 100; // เริ่มใหม่ที่ Title
            gp.transitionAlpha = 0;
            gp.showDialog = false;
            gp.dialogTimer = 0;
            enemyHP = 50;
            turn = 0;
            enemyActionTimer = 0;
            playerPostAttackCooldown = 0;
            battleDialogVisible = false;
            battleDialogText = "";
            battleDialogTimer = 0;
            battleDialogStage = -1;
            enemyStunned = false;
            enemyStunTurns = 0;
            enemyPoisonTurns = 0;
            playerShieldTurns = 0;
            gp.currentEnemyIndex = -1;
            currentEnemy = null;
            defeatSequence = false;
            defeatTimer = 0;
            fadeAlpha = 0;
            System.out.println("ผู้เล่นพ่ายแพ้ กลับไปหน้า Title");
            return;
        }

        if (turn == 0) {
            // จัดการ dialog ฝั่งผู้เล่น (เคสกลับจากศัตรูโจมตี: End Turn -> Player Turn)
            if (battleDialogVisible) {
                battleDialogTimer++;
                if (battleDialogStage == 0 && battleDialogTimer >= BATTLE_DIALOG_DURATION) {
                    // จาก End Turn ไป Player Turn
                    battleDialogText = "Player Turn";
                    battleDialogTimer = 0;
                    battleDialogStage = 1;
                } else if (battleDialogStage == 1 && battleDialogTimer >= BATTLE_DIALOG_DURATION) {
                    // ปิด dialog และพร้อมรับอินพุตผู้เล่น
                    battleDialogVisible = false;
                    battleDialogText = "";
                    battleDialogTimer = 0;
                    battleDialogStage = -1;
                }
                // ยังไม่รับอินพุตจนกว่าจะปิด dialog
                return;
            }

            // รอให้ Player ออกคำสั่ง (กดปุ่ม) เฉพาะตอนคูลดาวน์หมดแล้ว
            if (playerPostAttackCooldown > 0) {
                playerPostAttackCooldown--;
            }
            // ใช้การ์ด: 1=STUN, 2=POISON, 3=SHIELD, 4=HEAL (หักค่า Magic Dust)
            boolean usedCard = false;
            if (magicDust >= CARD_COST) {
                if (gp.keyH.wep1 == 1) { // ใช้ปุ่มเดิมเป็นทางลัด card1
                    enemyStunned = true;
                    enemyStunTurns = 1;
                    usedCard = true;
                } else if (gp.keyH.wep2 == 1) {
                    enemyPoisonTurns = Math.min(enemyPoisonTurns + 3, 9); // พิษ 3 เทิร์น สะสมได้
                    usedCard = true;
                } else if (gp.keyH.wep3 == 1) {
                    playerShieldTurns = Math.min(playerShieldTurns + 3, 5); // โล่ 3 เทิร์น สะสมได้จำกัด
                    usedCard = true;
                } else if (gp.keyH.wep4 == 1) {
                    int before = playerHP;
                    playerHP = Math.min(100, playerHP + HEAL_AMOUNT);
                    System.out.println("HEAL card used! Player HP: " + before + " -> " + playerHP);
                    usedCard = true;
                }
            } 
            if (usedCard) {
                magicDust -= CARD_COST;
                // แสดง dialog ใช้การ์ดแล้วจบตา
                battleDialogVisible = true;
                battleDialogText = "End Turn";
                battleDialogTimer = 0;
                battleDialogStage = 0;
                turn = 1;
                enemyActionTimer = 0;
                gp.keyH.wep1 = gp.keyH.wep2 = gp.keyH.wep3 = gp.keyH.wep4 = 0;
                return;
            }

            if (gp.keyH.attackPressed == 1 && playerPostAttackCooldown == 0) {
                int nextHp = Math.max(0, enemyHP - 10);
                System.out.println("Player attacks! Enemy HP: " + enemyHP + " -> " + nextHp);
                enemyHP = nextHp;
                // เริ่มแสดง dialog: End Turn -> Enemy Turn
                battleDialogVisible = true;
                battleDialogText = "End Turn";
                battleDialogTimer = 0;
                battleDialogStage = 0;
                // ส่งตาให้ Enemy แต่รอ dialog จบก่อนค่อยโจมตี
                turn = 1;
                enemyActionTimer = 0;
                gp.keyH.attackPressed = 0;
                playerPostAttackCooldown = 8; // กันกดติดซ้ำ
            }
        } else if (turn == 1) {
            // Enemy โจมตีอัตโนมัติหลังหน่วงเวลาเล็กน้อย
            // ทำ dialog ก่อน
            if (battleDialogVisible) {
                battleDialogTimer++;
                if (battleDialogStage == 0 && battleDialogTimer >= BATTLE_DIALOG_DURATION) {
                    battleDialogText = "Enemy Turn";
                    battleDialogTimer = 0;
                    battleDialogStage = 1;
                } else if (battleDialogStage == 1 && battleDialogTimer >= BATTLE_DIALOG_DURATION) {
                    // ปิด dialog
                    battleDialogVisible = false;
                    battleDialogText = "";
                    battleDialogTimer = 0;
                    battleDialogStage = -1;
                }
            } else {
                enemyActionTimer++;
                if (enemyActionTimer >= ENEMY_ACTION_DELAY) {
                    // ศัตรูถ้าติด Stun จะข้ามตา
                    if (enemyStunned || enemyStunTurns > 0) {
                        enemyStunned = false;
                        if (enemyStunTurns > 0) enemyStunTurns--;
                        // แสดง End Turn แล้วกลับ Player
                        battleDialogVisible = true;
                        battleDialogText = "End Turn";
                        battleDialogTimer = 0;
                        battleDialogStage = 0;
                        turn = 0;
                        enemyActionTimer = 0;
                        return;
                    }

                    int damage = enemyAttack;
                    if (playerShieldTurns > 0) {
                        damage = Math.max(1, damage - 3); // โล่ลดความเสียหาย 3
                        playerShieldTurns--;
                    }
                    int nextHp = Math.max(0, playerHP - damage);
                    System.out.println("Enemy attacks! Player HP: " + playerHP + " -> " + nextHp);
                    playerHP = nextHp;
                    // พิษ โดนปลายเทิร์นของศัตรู
                    if (enemyPoisonTurns > 0) {
                        int poisonDmg = 3;
                        int pHp = Math.max(0, enemyHP - poisonDmg);
                        System.out.println("Poison ticks! Enemy HP: " + enemyHP + " -> " + pHp);
                        enemyHP = pHp;
                        enemyPoisonTurns--;
                    }
                    // หลังศัตรูโจมตี: แสดง dialog Enemy End Turn -> Player Turn
                    battleDialogVisible = true;
                    battleDialogText = "End Turn";
                    battleDialogTimer = 0;
                    battleDialogStage = 0;
                    // เปลี่ยนตาเป็นผู้เล่น แต่อยู๋ในช่วง dialog ยังรับอินพุตไม่ได้จน dialog จบ
                    turn = 0;
                    enemyActionTimer = 0;
                    magicDust += DUST_PER_TURN; // จบตาศัตรู เติม Magic Dust 1
                }
            }
        }

        // ตรวจสอบว่าใครแพ้
        if (playerHP <= 0 || enemyHP <= 0) {
            boolean enemyKilled = enemyHP <= 0;
            if (enemyKilled) {
                // กำจัดศัตรู แล้วเริ่มฉากชัยชนะ
                if (currentEnemy != null) {
                    System.out.println("กำลังฆ่า Enemy - isDead: " + currentEnemy.isDead);
                    currentEnemy.isDead = true;
                    System.out.println("Enemy ถูกฆ่าแล้ว! isDead: " + currentEnemy.isDead);
                }
                // เริ่มลำดับฉากชัยชนะ
                victorySequence = true;
                victoryTimer = 0;
                fadeAlpha = 0;
                battleDialogVisible = false;
                battleDialogText = "";
                battleDialogTimer = 0;
                battleDialogStage = -1;
                return;
            } else {
                // เริ่มลำดับฉากพ่ายแพ้
                defeatSequence = true;
                defeatTimer = 0;
                fadeAlpha = 0;
                battleDialogVisible = false;
                battleDialogText = "";
                battleDialogTimer = 0;
                battleDialogStage = -1;
                return;
            }
        }
    }

    public void draw(Graphics2D g2) {
    int W = gp.getWidth();
    int H = gp.getHeight();

    // กำหนด font ตามขนาดหน้าจอ
    int fontSize = Math.max(12, W / 50);
    g2.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, fontSize));

    // พื้นหลังสีดำ
    g2.setColor(Color.BLACK);
    g2.fillRect(0, 0, W, H);

    // แพลตฟอร์มพื้น (วงรีเงาใต้เท้า)
    g2.setColor(new Color(255, 255, 255, 35));
    // Enemy platform
    int ePlatW = (int)(W * 0.25);
    int ePlatH = (int)(H * 0.06);
    int ePlatX = (int)(W * 0.65);
    int ePlatY = (int)(H * 0.25);
    g2.fillOval(ePlatX, ePlatY, ePlatW, ePlatH);
    // Player platform
    int pPlatW = (int)(W * 0.33);
    int pPlatH = (int)(H * 0.07);
    int pPlatX = (int)(W * 0.12);
    int pPlatY = (int)(H * 0.66);
    g2.fillOval(pPlatX, pPlatY, pPlatW, pPlatH);

    // ศัตรู
    int enemySize = (int)(W * 0.15); // ปรับตามหน้าจอ
    int enemyX = ePlatX + ePlatW/2 - enemySize/2;
    int enemyY = ePlatY - enemySize + ePlatH/2;
    BufferedImage enemySprite = (currentEnemy != null) ? currentEnemy.sprite : null;
    if (enemySprite != null) {
        g2.drawImage(enemySprite, enemyX, enemyY, enemySize, enemySize, null);
    } else {
        g2.setColor(Color.RED);
        g2.fillRoundRect(enemyX, enemyY, enemySize, enemySize, 12, 12);
    }

    // ผู้เล่น
    int playerSize = (int)(W * 0.2);
    int playerX = pPlatX + pPlatW/2 - playerSize/2;
    int playerY = pPlatY - playerSize + pPlatH/2;
    BufferedImage playerSprite = gp.player1 != null ? gp.player1.up1 : null;
    if (playerSprite != null) {
        g2.drawImage(playerSprite, playerX, playerY, playerSize, playerSize, null);
    } else {
        g2.setColor(new Color(80, 160, 255));
        g2.fillRoundRect(playerX, playerY, playerSize, playerSize, 16, 16);
    }

    // เมนูด้านล่าง
    int menuH = (int)(H * 0.25);
    g2.setColor(new Color(25, 25, 25));
    g2.fillRoundRect((int)(W*0.02), H - menuH - (int)(H*0.02), W - (int)(W*0.04), menuH, 12, 12);
    g2.setColor(Color.WHITE);
    g2.drawRoundRect((int)(W*0.02), H - menuH - (int)(H*0.02), W - (int)(W*0.04), menuH, 12, 12);

    // ข้อความเมนู
    int textX = (int)(W * 0.05);
    int textY = H - menuH - (int)(H*0.02) + fontSize * 2;
    g2.drawString("What will PLAYER do?", textX, textY);
    textY += fontSize * 2;
    g2.setColor(Color.YELLOW);
    g2.drawString("> ATTACK (SPACE)", textX, textY);
    g2.setColor(Color.WHITE);
    g2.drawString("STUN(G)", textX + fontSize * 12, textY);
    g2.drawString("POISON(H)", textX + fontSize * 18, textY);
    g2.drawString("SHIELD(L)", textX + fontSize * 25, textY);
    g2.drawString("HEAL(K)", textX + fontSize * 32, textY);
    g2.drawString("Magic Dust: " + magicDust, textX + fontSize * 40, textY);

    // Dialog overlay
    if (battleDialogVisible) {
        int dW = W - (int)(W*0.04);
        int dH = fontSize * 4;
        int dX = (int)(W*0.02);
        int dY = H - menuH - (int)(H*0.02) - dH - fontSize;
        g2.setColor(new Color(25, 25, 25));
        g2.fillRoundRect(dX, dY, dW, dH, 12, 12);
        g2.setColor(Color.WHITE);
        g2.drawRoundRect(dX, dY, dW, dH, 12, 12);
        g2.drawString(battleDialogText, dX + fontSize, dY + dH/2 + fontSize/2);
    }

    // Victory / Defeat overlay
    if (victorySequence || defeatSequence) {
        String msg = victorySequence ? "BATTLE COMPLETE" : "THE ENEMY WAS TOO STRONG...";
        g2.setColor(Color.WHITE);
        int msgW = g2.getFontMetrics().stringWidth(msg);
        g2.drawString(msg, (W - msgW)/2, H/2);
        if (fadeAlpha > 0) {
            g2.setColor(new Color(0, 0, 0, Math.min(255, fadeAlpha)));
            g2.fillRect(0, 0, W, H);
        }
    }

        // HP boxes
        int boxW = (int)(W * 0.3);
        int boxH = (int)(H * 0.25 / 2);
        // Enemy box
        int eBoxX = (int)(W*0.02);
        int eBoxY = (int)(H*0.02);
        g2.setColor(new Color(30, 30, 30));
        g2.fillRoundRect(eBoxX, eBoxY, boxW, boxH, 12, 12);
        g2.setColor(Color.WHITE);
        g2.drawRoundRect(eBoxX, eBoxY, boxW, boxH, 12, 12);
        g2.drawString("ENEMY", eBoxX + fontSize/2, eBoxY + fontSize);
        int eHpMaxW = boxW - fontSize * 2;
        int eHpX = eBoxX + fontSize/2;
        int eHpY = eBoxY + boxH - fontSize;
        g2.setColor(new Color(60, 60, 60));
        g2.fillRoundRect(eHpX, eHpY, eHpMaxW, fontSize/2 + 2, 8, 8);
        int eHpW = (int)(eHpMaxW * Math.max(0, Math.min(1.0, enemyHP / (double)Math.max(1, enemyMaxHP))));
        g2.setColor(new Color(255, 80, 80));
        g2.fillRoundRect(eHpX, eHpY, eHpW, fontSize/2 + 2, 8, 8);

        // Player box
        int pBoxW = boxW;
        int pBoxH = boxH;
        int pBoxX = W - pBoxW - (int)(W*0.02);
        int pBoxY = H - pBoxH - (int)(H*0.03);
        g2.setColor(new Color(30, 30, 30));
        g2.fillRoundRect(pBoxX, pBoxY, pBoxW, pBoxH, 12, 12);
        g2.setColor(Color.WHITE);
        g2.drawRoundRect(pBoxX, pBoxY, pBoxW, pBoxH, 12, 12);
        g2.drawString("PLAYER", pBoxX + fontSize/2, pBoxY + fontSize);
        int pHpMaxW = pBoxW - fontSize * 2;
        int pHpX = pBoxX + fontSize/2;
        int pHpY = pBoxY + pBoxH - fontSize;

        // HP bar
        g2.setColor(new Color(60, 60, 60));
        g2.fillRoundRect(pHpX, pHpY, pHpMaxW, fontSize/2 + 2, 8, 8);
        int pHpW = (int)(pHpMaxW * Math.max(0, Math.min(1.0, playerHP / 100.0)));
        g2.setColor(new Color(120, 220, 120));
        g2.fillRoundRect(pHpX, pHpY, pHpW, fontSize/2 + 2, 8, 8);
        g2.setColor(Color.WHITE);
        String pHpText = playerHP + "/100";
        int pHpTextW = g2.getFontMetrics().stringWidth(pHpText);
        g2.drawString(pHpText, pBoxX + pBoxW - pHpTextW - fontSize/2, pHpY - fontSize/4);

        // Shield bar (ปรับให้ห่างจาก HP bar)
        if (playerShieldTurns > 0) {
            int shieldGap = fontSize; // ระยะห่างจาก HP bar
            int shieldBarY = pHpY - (fontSize/2 + 8) - shieldGap; // ขยับขึ้นด้านบน
            int shieldMaxTurns = 5;
            int shieldMaxW = pHpMaxW;
            g2.setColor(new Color(80, 80, 40));
            g2.fillRoundRect(pHpX, shieldBarY, shieldMaxW, fontSize/2, 8, 8);
            int shieldW = (int)(shieldMaxW * Math.max(0, Math.min(1.0, playerShieldTurns / (double)shieldMaxTurns)));
            g2.setColor(new Color(255, 220, 70));
            g2.fillRoundRect(pHpX, shieldBarY, shieldW, fontSize/2, 8, 8);
            // เขียนข้อความ Shield พร้อมจำนวน turn ให้ชัด
            g2.setColor(Color.WHITE);
            String shieldText = "Shield: " + playerShieldTurns + "T";
            int shieldTextW = g2.getFontMetrics().stringWidth(shieldText);
            g2.drawString(shieldText, pHpX + shieldMaxW - shieldTextW, shieldBarY - fontSize/4);
        }

        // Enemy status texts (STUN/POISON) ปรับไม่ทับกัน
        int statusY = eHpY + boxH + fontSize/2; // ขยับลงจาก enemy box
        int statusX = eHpX;
        if (enemyStunTurns > 0) {
            g2.setColor(new Color(255, 220, 0));
            String stunText = "STUN: " + enemyStunTurns + "T";
            g2.drawString(stunText, statusX, statusY);
            statusX += g2.getFontMetrics().stringWidth(stunText) + fontSize; // เว้นระยะให้ POISON
        }
        if (enemyPoisonTurns > 0) {
            g2.setColor(new Color(120, 255, 120));
            String poisonText = "POISON: " + enemyPoisonTurns + "T";
            g2.drawString(poisonText, statusX, statusY);
        }
    }

}
