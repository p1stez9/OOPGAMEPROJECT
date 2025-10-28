package MainGame;

import java.awt.Color;
import java.awt.Graphics2D;

public class TurnBase {
    GamePanle gp;

    int playerHP = 100;
    int enemyHP = 50;
    int turn = 0; // 0 = Player, 1 = Enemy
    
    // Turn flow helpers
    int enemyActionTimer = 0;
    final int ENEMY_ACTION_DELAY = 45; // ~0.75s at 60 FPS
    int playerPostAttackCooldown = 0; // prevent double inputs the same frame

    // Dialog flow (แบบ Pokémon): แสดงข้อความ เช่น "End Turn", "Enemy Turn"
    boolean battleDialogVisible = false;
    String battleDialogText = "";
    int battleDialogTimer = 0;
    int battleDialogStage = -1; // -1 none, 0: show first message, 1: second message
    final int BATTLE_DIALOG_DURATION = 45;
    
    // Enemy reference
    Enemy currentEnemy;

    // Card/Magic dust system
    int magicDust = 5; // เริ่มต้น 5
    final int DUST_PER_TURN = 1; // เพิ่มเทิร์นละ 1
    final int CARD_COST = 2; // ใช้การ์ดใบละ 2
    boolean enemyStunned = false;
    int enemyStunTurns = 0;
    int enemyPoisonTurns = 0; // ศัตรูเสียเลือดปลายตา
    int playerShieldTurns = 0; // ลดดาเมจศัตรู

    public TurnBase(GamePanle gp) {
        this.gp = gp;
    }

    public void update() {
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
            // ใช้การ์ด: 1=STUN, 2=POISON, 3=SHIELD (หักค่า Magic Dust)
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
                    playerShieldTurns = Math.min(playerShieldTurns + 2, 5); // โล่ 2 เทิร์น สะสมได้จำกัด
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
                gp.keyH.wep1 = gp.keyH.wep2 = gp.keyH.wep3 = 0;
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
            // หากกำลังแสดง dialog: ดำเนิน stage ของ dialog ก่อน
            if (battleDialogVisible) {
                battleDialogTimer++;
                if (battleDialogStage == 0 && battleDialogTimer >= BATTLE_DIALOG_DURATION) {
                    battleDialogText = "Enemy Turn";
                    battleDialogTimer = 0;
                    battleDialogStage = 1;
                } else if (battleDialogStage == 1 && battleDialogTimer >= BATTLE_DIALOG_DURATION) {
                    // ปิด dialog และเริ่มนับเวลาโจมตีของศัตรู
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

                    int damage = 5;
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
            // บันทึกผลการต่อสู้ก่อนรีเซ็ตค่า HP
            boolean enemyKilled = enemyHP <= 0;

            // ออกจากโหมด Battle กลับไป Explore ทันที
            gp.state = GamePanle.GameState.EXPLORE;

            // ถ้า enemy ตาย ให้ฆ่า enemy ก่อนรีเซ็ตค่า
            if (enemyKilled && currentEnemy != null) {
                System.out.println("กำลังฆ่า Enemy - isDead: " + currentEnemy.isDead);
                currentEnemy.isDead = true;
                System.out.println("Enemy ถูกฆ่าแล้ว! isDead: " + currentEnemy.isDead);
            }

            // รีเซ็ต transition variables
            gp.transitionAlpha = 0;
            gp.showDialog = false;
            gp.dialogTimer = 0;

            // รีเซ็ต HP กลับเป็นค่าเริ่มต้น
            playerHP = 100;
            enemyHP = 50;
            turn = 0;
            enemyActionTimer = 0;
            playerPostAttackCooldown = 0;

            // รีเซ็ต dialog และ currentEnemy
            battleDialogVisible = false;
            battleDialogText = "";
            battleDialogTimer = 0;
            battleDialogStage = -1;
            enemyStunned = false;
            enemyStunTurns = 0;
            enemyPoisonTurns = 0;
            playerShieldTurns = 0;
            // รีเซ็ต currentEnemyIndex และ currentEnemy
            gp.currentEnemyIndex = -1;
            currentEnemy = null;

            System.out.println("การต่อสู้จบแล้ว! กลับไป Explore mode");
        }
    }

    public void draw(Graphics2D g2) {
        int W = gp.getWidth();
        int H = gp.getHeight();

        // พื้นหลังสีดำแบบ Pokémon
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, W, H);

        // แพลตฟอร์มพื้น (วงรีเงาใต้เท้า)
        g2.setColor(new Color(255, 255, 255, 35));
        // Enemy platform (ด้านบนขวา)
        int ePlatW = W / 4; int ePlatH = H / 16;
        int ePlatX = (int)(W * 0.65);
        int ePlatY = (int)(H * 0.25);
        g2.fillOval(ePlatX, ePlatY, ePlatW, ePlatH);
        // Player platform (ด้านล่างซ้าย)
        int pPlatW = W / 3; int pPlatH = H / 14;
        int pPlatX = (int)(W * 0.12);
        int pPlatY = (int)(H * 0.66);
        g2.fillOval(pPlatX, pPlatY, pPlatW, pPlatH);

        // ศัตรู (ด้านบนขวา - มองซ้าย) ใช้สี่เหลี่ยมเป็น placeholder sprite
        int enemySize = gp.titlesize * 3;
        int enemyX = ePlatX + ePlatW/2 - enemySize/2 + gp.titlesize/2;
        int enemyY = ePlatY - enemySize + gp.titlesize/2;
        g2.setColor(Color.RED);
        g2.fillRoundRect(enemyX, enemyY, enemySize, enemySize, 12, 12);

        // ผู้เล่น (ด้านล่างซ้าย - เห็นด้านหลัง) ใช้สี่เหลี่ยมเป็น placeholder sprite
        int playerSize = gp.titlesize * 4;
        int playerX = pPlatX + pPlatW/2 - playerSize/2 - gp.titlesize/2;
        int playerY = pPlatY - playerSize + gp.titlesize/2;
        g2.setColor(new Color(80, 160, 255));
        g2.fillRoundRect(playerX, playerY, playerSize, playerSize, 16, 16);

        // กล่อง HP (ย้ายวาดตอนท้ายเพื่อไม่ให้ถูก UI อื่นทับ)
        int boxW = W / 3; int boxH = H / 9;
        int eBoxX = gp.titlesize; int eBoxY = gp.titlesize;
        int pBoxW = W / 3; int pBoxH = H / 9;
        int pBoxX = W - pBoxW - gp.titlesize;
        int pBoxY = H - pBoxH - gp.titlesize * 2;

        // เมนูคำสั่งด้านล่างแบบ Pokémon
        int menuH = H / 4;
        g2.setColor(new Color(25, 25, 25));
        g2.fillRoundRect(gp.titlesize, H - menuH - gp.titlesize, W - gp.titlesize * 2, menuH, 12, 12);
        g2.setColor(Color.WHITE);
        g2.drawRoundRect(gp.titlesize, H - menuH - gp.titlesize, W - gp.titlesize * 2, menuH, 12, 12);

        // ตัวเลือกพื้นฐาน
        int textX = gp.titlesize * 2;
        int textY = H - menuH - gp.titlesize + gp.titlesize * 2;
        g2.drawString("What will PLAYER do?", textX, textY);
        textY += gp.titlesize * 2;
        g2.setColor(Color.YELLOW);
        g2.drawString("> ATTACK (SPACE)", textX, textY);
        g2.setColor(Color.WHITE);
        g2.drawString("STUN(G)", textX + 180, textY);
        g2.drawString("POISON(H)", textX + 260, textY);
        g2.drawString("SHIELD(L)", textX + 360, textY);
        g2.drawString("Magic Dust: " + magicDust, textX + 480, textY);

        // Dialog overlay: End Turn / Enemy Turn (สไตล์ Pokémon)
        if (battleDialogVisible) {
            int dW = W - gp.titlesize * 2;
            int dH = gp.titlesize * 3;
            int dX = gp.titlesize;
            int dY = H - menuH - gp.titlesize - dH - gp.titlesize/2;
            g2.setColor(new Color(25, 25, 25));
            g2.fillRoundRect(dX, dY, dW, dH, 12, 12);
            g2.setColor(Color.WHITE);
            g2.drawRoundRect(dX, dY, dW, dH, 12, 12);
            g2.drawString(battleDialogText, dX + gp.titlesize, dY + dH/2 + gp.titlesize/4);
        }

        // วาด HP Boxes ด้านบนสุด (หลังสุดเพื่อให้ทับทุกอย่าง)
        // Enemy box
        g2.setColor(new Color(30, 30, 30));
        g2.fillRoundRect(eBoxX, eBoxY, boxW, boxH, 12, 12);
        g2.setColor(Color.WHITE);
        g2.drawRoundRect(eBoxX, eBoxY, boxW, boxH, 12, 12);
        g2.drawString("ENEMY", eBoxX + gp.titlesize/2, eBoxY + gp.titlesize);
        int eHpMaxW = boxW - gp.titlesize * 2;
        int eHpX = eBoxX + gp.titlesize/2;
        int eHpY = eBoxY + boxH - gp.titlesize;
        g2.setColor(new Color(60, 60, 60));
        g2.fillRoundRect(eHpX, eHpY, eHpMaxW, gp.titlesize/3 + 2, 8, 8);
        int eHpW = (int)(eHpMaxW * Math.max(0, Math.min(1.0, enemyHP / 50.0)));
        g2.setColor(new Color(255, 80, 80));
        g2.fillRoundRect(eHpX, eHpY, eHpW, gp.titlesize/3 + 2, 8, 8);

        // Player box
        g2.setColor(new Color(30, 30, 30));
        g2.fillRoundRect(pBoxX, pBoxY, pBoxW, pBoxH, 12, 12);
        g2.setColor(Color.WHITE);
        g2.drawRoundRect(pBoxX, pBoxY, pBoxW, pBoxH, 12, 12);
        g2.drawString("PLAYER", pBoxX + gp.titlesize/2, pBoxY + gp.titlesize);
        int pHpMaxW = pBoxW - gp.titlesize * 2;
        int pHpX = pBoxX + gp.titlesize/2;
        int pHpY = pBoxY + pBoxH - gp.titlesize;
        g2.setColor(new Color(60, 60, 60));
        g2.fillRoundRect(pHpX, pHpY, pHpMaxW, gp.titlesize/3 + 2, 8, 8);
        int pHpW = (int)(pHpMaxW * Math.max(0, Math.min(1.0, playerHP / 100.0)));
        g2.setColor(new Color(120, 220, 120));
        g2.fillRoundRect(pHpX, pHpY, pHpW, gp.titlesize/3 + 2, 8, 8);
        g2.setColor(Color.WHITE);
        String pHpText = playerHP + "/100";
        int pHpTextW = g2.getFontMetrics().stringWidth(pHpText);
        g2.drawString(pHpText, pBoxX + pBoxW - pHpTextW - gp.titlesize/2, pHpY - gp.titlesize/4);

        // Shield bar under player's HP showing remaining shield turns
        if (playerShieldTurns > 0) {
            int shieldBarY = pHpY + gp.titlesize/2 + 4;
            int shieldMaxTurns = 5;
            int shieldMaxW = pHpMaxW;
            g2.setColor(new Color(80, 80, 40));
            g2.fillRoundRect(pHpX, shieldBarY, shieldMaxW, gp.titlesize/4 + 2, 8, 8);
            int shieldW = (int)(shieldMaxW * Math.max(0, Math.min(1.0, playerShieldTurns / (double)shieldMaxTurns)));
            g2.setColor(new Color(255, 220, 70));
            g2.fillRoundRect(pHpX, shieldBarY, shieldW, gp.titlesize/4 + 2, 8, 8);
            g2.setColor(Color.WHITE);
            String shieldText = "Shield: " + playerShieldTurns + "T";
            g2.drawString(shieldText, pHpX, shieldBarY - 2);
        }

        // Enemy status texts under enemy HP: STUN/POISON remaining turns
        int statusY = eHpY + gp.titlesize/2 + 4;
        int statusX = eHpX;
        if (enemyStunTurns > 0) {
            g2.setColor(new Color(255, 220, 0));
            g2.drawString("STUN: " + enemyStunTurns + "T", statusX, statusY);
            statusX += gp.titlesize * 3;
        }
        if (enemyPoisonTurns > 0) {
            g2.setColor(new Color(120, 255, 120));
            g2.drawString("POISON: " + enemyPoisonTurns + "T", statusX, statusY);
        }
    }
}
