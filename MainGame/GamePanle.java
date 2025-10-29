package MainGame;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

public class GamePanle extends JPanel implements Runnable {
    // SCREEN SETTING
    public final int OriginalTitlesize = 16;
    public final int scale = 3;
    public int titlesize = OriginalTitlesize * scale;
    public final int maxCol = 16;
    public final int maxRow = 12;
    public final int Widthscreen = titlesize * maxCol;
    public final int Hightscreen = titlesize * maxRow;

    // WORLD SETTING
    public final int maxWorldCol = 50;
    public final int maxWorldRow = 50;
    public final int worldWidth = titlesize * maxWorldCol;
    public final int worldHeight = titlesize * maxWorldRow;

    public KeyEventHandler keyH = new KeyEventHandler();
    final int FPS = 60;
    Thread gameThread;

    public TileManager tileM = new TileManager(this);
    public CollisionChecker cChecker = new CollisionChecker(this);

    public Player player1 = new Player(this, keyH);
    public Enemy[] enemies = new Enemy[5];
    public TurnBase turnBase = new TurnBase(this);

    int currentEnemyIndex = -1;

    // Hp player หลังสู็ก็ยังอยู่เท่าเดิม
    public int persistentPlayerHP = 100;

    enum GameState {
        TITLE, EXPLORE, BATTLE, TRANSITION, VICTORY, PAUSED
    }
    GameState state = GameState.TITLE;

    // Transition variables
    int transitionAlpha = 0;
    int transitionSpeed = 5;
    boolean showDialog = false;
    int dialogTimer = 0;
    final int DIALOG_DURATION = 120; // 2 seconds (60 FPS)

    boolean menuState = false;
    String[] menutext = { "item1", "item2" };
    int indexitem = 0;

    // Pause variables
    GameState previousState;

    // Victory screen variables
    int victoryAlpha = 0;
    int victoryTimer = 0;
    final int VICTORY_FADE_SPEED = 8;
    final int VICTORY_DISPLAY_DURATION = 240; // 4 seconds at 60 FPS

    // check collision
    public boolean isValidPosition(int worldX, int worldY) {
        if (worldX < 0 || worldY < 0 || 
            worldX >= worldWidth || worldY >= worldHeight) {
            return false;
        }

        int col = worldX / titlesize;
        int row = worldY / titlesize;

        if (col < 0 || col >= maxWorldCol || row < 0 || row >= maxWorldRow) {
            return false;
        }

        int tileNum = tileM.mapTileNum[col][row];
        return !tileM.tile[tileNum].collision;
    }

    // Check position enemy ว่าขยับได้มั้ย
    public boolean canMoveTo(int worldX, int worldY) {
        return isValidPosition(worldX, worldY);
    }

    private int[] findRandomValidPosition() {
        int attempts = 0;
        int maxAttempts = 100;
        
        while (attempts < maxAttempts) {
            int worldX = (int)(Math.random() * worldWidth);
            int worldY = (int)(Math.random() * worldHeight);
            
            if (isValidPosition(worldX, worldY)) {
                // Round to tile position
                int col = worldX / titlesize;
                int row = worldY / titlesize;
                int tileX = col * titlesize;
                int tileY = row * titlesize;
                
                return new int[]{tileX, tileY};
            }
            attempts++;
        }
        
        return new int[]{titlesize * 25, titlesize * 25};
    }

    public GamePanle() {
        setPreferredSize(new Dimension(Widthscreen, Hightscreen));
        setBackground(Color.black);
        setDoubleBuffered(true);
        addKeyListener(keyH);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        // สร้าง enemy
        for (int i = 0; i < enemies.length; i++) {
            enemies[i] = new Enemy(this);
            
            // สุ่มเกิด
            int[] pos = findRandomValidPosition();
            enemies[i].worldX = pos[0];
            enemies[i].worldY = pos[1];
            
            enemies[i].setSpriteVariant((i % 4) + 1);
        }
    }

    public void resetGame() {
        // enemy reset
        for (int i = 0; i < enemies.length; i++) {
            enemies[i] = new Enemy(this);
            
            int[] pos = findRandomValidPosition();
            enemies[i].worldX = pos[0];
            enemies[i].worldY = pos[1];
            
            enemies[i].setSpriteVariant((i % 4) + 1);
            enemies[i].isDead = false;
        }

        // player reset
        player1.setDefaultValues();
        persistentPlayerHP = 100;

        // game reset
        state = GameState.TITLE;
        currentEnemyIndex = -1;
        victoryAlpha = 0;
        victoryTimer = 0;
        transitionAlpha = 0;
        showDialog = false;
        dialogTimer = 0;
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000.0 / FPS;
        double nextDrawTime = System.nanoTime() + drawInterval;

        while (gameThread != null) {
            update();
            repaint();

            try {
                double remainingTime = nextDrawTime - System.nanoTime();
                remainingTime /= 1_000_000.0;
                if (remainingTime < 0) remainingTime = 0;
                Thread.sleep((long) remainingTime);
                nextDrawTime += drawInterval;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void update() {
        // pause
        if (state == GameState.PAUSED) {
            if (keyH.escPressed == 1) {
                state = previousState;
                keyH.escPressed = 0;
            }
            return;
        }

        // victory
        if (state == GameState.VICTORY) {
            victoryTimer++;
            if (victoryAlpha < 255 && victoryTimer > 60) {
                victoryAlpha = Math.min(255, victoryAlpha + VICTORY_FADE_SPEED);
            }
            if (victoryTimer >= VICTORY_DISPLAY_DURATION) {
                if (keyH.enterPressed == 1 || keyH.spacePressed == 1) {
                    // เวลาชนะมันจะกลับไปหน้า title
                    state = GameState.TITLE;
                    keyH.enterPressed = 0;
                    keyH.spacePressed = 0;
                    resetGame();
                }
            }
            return;
        }

        // pause key
        if (keyH.escPressed == 1 && (state == GameState.EXPLORE || state == GameState.BATTLE)) {
            previousState = state;
            state = GameState.PAUSED;
            keyH.escPressed = 0;
            return;
        }

        if (keyH.tabPressed == 1) {
            menuState = !menuState;
            keyH.tabPressed = 0;
        }

        if (menuState) return;

        // title screen
        if (state == GameState.TITLE) {
            if (keyH.enterPressed == 1 || keyH.spacePressed == 1) {
                state = GameState.EXPLORE;
                keyH.enterPressed = 0;
                keyH.spacePressed = 0;
            }
            return;
        }

        if (state == GameState.EXPLORE) {
            player1.update();
            
            // เช็คว่าตายหมดมั้ย
            boolean allEnemiesDead = true;
            for (Enemy enemy : enemies) {
                if (!enemy.isDead) {
                    allEnemiesDead = false;
                    enemy.update();
                }
            }

            // ถ้าตายหมดก็จะ victory
            if (allEnemiesDead && enemies.length > 0) {
                state = GameState.VICTORY;
                victoryAlpha = 0;
                victoryTimer = 0;
                return;
            }

            // เช็คว่า player ชน enemy มั้ย
            for (int i = 0; i < enemies.length; i++) {
                Enemy e = enemies[i];
                if (!e.isDead &&
                    player1.worldX < e.worldX + titlesize &&
                    player1.worldX + titlesize > e.worldX &&
                    player1.worldY < e.worldY + titlesize &&
                    player1.worldY + titlesize > e.worldY) {
                    currentEnemyIndex = i;
                    state = GameState.TRANSITION;
                    transitionAlpha = 0;
                    showDialog = false;
                    dialogTimer = 0;
                    break;
                }
            }
        } else if (state == GameState.TRANSITION) {
            transitionAlpha += transitionSpeed;
            if (transitionAlpha >= 255) {
                transitionAlpha = 255;
                if (!showDialog) {
                    showDialog = true;
                    dialogTimer = 0;
                }
            }

            if (showDialog) {
                dialogTimer++;
                if (dialogTimer >= DIALOG_DURATION) {
                    turnBase.initForEnemy(enemies[currentEnemyIndex]);
                    turnBase.battleDialogVisible = false;
                    turnBase.battleDialogText = "";
                    turnBase.battleDialogTimer = 0;
                    turnBase.battleDialogStage = -1;
                    turnBase.turn = 0;
                    turnBase.enemyActionTimer = 0;
                    turnBase.playerPostAttackCooldown = 0;
                    state = GameState.BATTLE;
                }
            }
        } else if (state == GameState.BATTLE) {
            turnBase.update();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        if (state == GameState.TITLE) {
            // วาดหน้า Title
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, getWidth(), getHeight());
            // ชื่อเกมใหญ่
            String title = "Dust & Magic";
            g2.setColor(Color.WHITE);
            g2.setFont(g2.getFont().deriveFont(48f));
            int tW = g2.getFontMetrics().stringWidth(title);
            int tX = (getWidth() - tW) / 2;
            int tY = getHeight() / 3;
            g2.drawString(title, tX, tY);
            // ปุ่มเริ่ม
            String prompt = "PRESS ENTER TO START";
            g2.setFont(g2.getFont().deriveFont(20f));
            int pW = g2.getFontMetrics().stringWidth(prompt);
            int pX = (getWidth() - pW) / 2;
            int pY = tY + 80;
            g2.drawString(prompt, pX, pY);
            g2.dispose();
            return;
        }

        if (state == GameState.EXPLORE) {
            tileM.draw(g2);
            player1.draw(g2);
            for (Enemy enemy : enemies) {
                if (!enemy.isDead) enemy.draw(g2);
            }
        }

        if (state == GameState.TRANSITION) {
            tileM.draw(g2);
            player1.draw(g2);
            for (Enemy enemy : enemies) {
                if (!enemy.isDead) enemy.draw(g2);
            }

            g2.setColor(new Color(0, 0, 0, transitionAlpha));
            g2.fillRect(0, 0, getWidth(), getHeight());

            if (showDialog) {
                int dialogWidth = 400;
                int dialogHeight = 150;
                int dialogX = (getWidth() - dialogWidth) / 2;
                int dialogY = (getHeight() - dialogHeight) / 2;

                g2.setColor(new Color(50, 50, 50, 200));
                g2.fillRoundRect(dialogX, dialogY, dialogWidth, dialogHeight, 20, 20);

                g2.setColor(Color.WHITE);
                g2.drawRoundRect(dialogX, dialogY, dialogWidth, dialogHeight, 20, 20);

                g2.setFont(g2.getFont().deriveFont(24f));
                String message = "BATTLE HAS BEGUN!";
                int textWidth = g2.getFontMetrics().stringWidth(message);
                int textX = dialogX + (dialogWidth - textWidth) / 2;
                int textY = dialogY + dialogHeight / 2 + 10;

                g2.setColor(Color.YELLOW);
                g2.drawString(message, textX, textY);
            }
        } else if (state == GameState.BATTLE) {
            turnBase.draw(g2);
        } else if (state == GameState.VICTORY) {
            // Victory screen background
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Victory message
            String victoryText = "VICTORY!";
            String subText = "All Enemies Defeated";
            g2.setColor(new Color(255, 215, 0)); // Gold color
            g2.setFont(g2.getFont().deriveFont(64f));
            int vW = g2.getFontMetrics().stringWidth(victoryText);
            int vX = (getWidth() - vW) / 2;
            int vY = getHeight() / 2 - 40;
            g2.drawString(victoryText, vX, vY);

            g2.setColor(Color.WHITE);
            g2.setFont(g2.getFont().deriveFont(24f));
            int sW = g2.getFontMetrics().stringWidth(subText);
            int sX = (getWidth() - sW) / 2;
            int sY = vY + 60;
            g2.drawString(subText, sX, sY);

            // Prompt to continue
            if (victoryTimer >= VICTORY_DISPLAY_DURATION) {
                String prompt = "Press ENTER to return to title";
                g2.setFont(g2.getFont().deriveFont(20f));
                int pW = g2.getFontMetrics().stringWidth(prompt);
                int pX = (getWidth() - pW) / 2;
                int pY = sY + 60;
                g2.setColor(Color.LIGHT_GRAY);
                g2.drawString(prompt, pX, pY);
            }

            // Fade overlay
            if (victoryAlpha > 0) {
                g2.setColor(new Color(0, 0, 0, Math.min(255, victoryAlpha)));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        } else if (state == GameState.PAUSED) {
            // Draw the previous state in the background
            if (previousState == GameState.EXPLORE) {
                tileM.draw(g2);
                player1.draw(g2);
                for (Enemy enemy : enemies) {
                    if (!enemy.isDead) enemy.draw(g2);
                }
            } else if (previousState == GameState.BATTLE) {
                turnBase.draw(g2);
            }

            // Semi-transparent overlay
            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Pause text
            String pauseText = "PAUSED";
            g2.setColor(Color.WHITE);
            g2.setFont(g2.getFont().deriveFont(48f));
            int pW = g2.getFontMetrics().stringWidth(pauseText);
            int pX = (getWidth() - pW) / 2;
            int pY = getHeight() / 2;

            // Shadow effect
            g2.setColor(new Color(0, 0, 0, 150));
            g2.drawString(pauseText, pX + 3, pY + 3);
            g2.setColor(Color.WHITE);
            g2.drawString(pauseText, pX, pY);

            // Instruction
            String instruction = "Press ESC to resume";
            g2.setFont(g2.getFont().deriveFont(18f));
            int iW = g2.getFontMetrics().stringWidth(instruction);
            int iX = (getWidth() - iW) / 2;
            int iY = pY + 50;
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawString(instruction, iX, iY);
        }

        if (menuState) {
            g2.setColor(Color.WHITE);
            g2.fillRect(50, 50, 300, 200);
            for (int i = 0; i < menutext.length; i++) {
                g2.setColor(Color.BLACK);
                g2.drawString(menutext[i], 150, 150 + i * 30);
            }
        }

        g2.dispose();
    }
}
