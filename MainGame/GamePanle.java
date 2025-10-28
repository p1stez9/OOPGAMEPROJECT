package MainGame;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

public class GamePanle extends JPanel implements Runnable {
    // SCREEN SETTING
    final int OriginalTitlesize = 16;
    final int scale = 3;
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

    KeyEventHandler keyH = new KeyEventHandler();
    final int FPS = 60;
    Thread gameThread;

    TileManager tileM = new TileManager(this);
    CollisionChecker cChecker = new CollisionChecker(this);

    public Player player1 = new Player(this, keyH);
    Enemy[] enemies = new Enemy[5];
    TurnBase turnBase = new TurnBase(this);

    int currentEnemyIndex = -1;

    enum GameState {
        EXPLORE, BATTLE, TRANSITION
    }
    GameState state = GameState.EXPLORE;

    // Transition variables
    int transitionAlpha = 0;
    int transitionSpeed = 5;
    boolean showDialog = false;
    int dialogTimer = 0;
    final int DIALOG_DURATION = 120; // 2 seconds (60 FPS)

    boolean menuState = false;
    String[] menutext = { "item1", "item2" };
    int indexitem = 0;

    public GamePanle() {
        setPreferredSize(new Dimension(Widthscreen, Hightscreen));
        setBackground(Color.black);
        setDoubleBuffered(true);
        addKeyListener(keyH);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        // สร้าง enemies
        for (int i = 0; i < enemies.length; i++) {
            enemies[i] = new Enemy(this);
            enemies[i].worldX = titlesize * (25 + i * 5);
            enemies[i].worldY = titlesize * (25 + i * 3);
        }
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
        if (keyH.tabPressed == 1) {
            menuState = !menuState;
            keyH.tabPressed = 0;
        }

        if (menuState) return;

        if (state == GameState.EXPLORE) {
            player1.update();
            for (Enemy enemy : enemies) {
                if (!enemy.isDead) {
                    enemy.update();
                }
            }

            // ตรวจจับชนกับศัตรู
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
                    System.out.println("เริ่ม Transition ไปยังโหมด BATTLE กับ Enemy " + (i + 1) + "!");
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
                    turnBase.currentEnemy = enemies[currentEnemyIndex];
                    System.out.println("ตั้งค่า currentEnemy: " + (currentEnemyIndex + 1) + " - isDead: " + enemies[currentEnemyIndex].isDead);
                    // Reset TurnBase turn/dialog to ensure input works
                    turnBase.battleDialogVisible = false;
                    turnBase.battleDialogText = "";
                    turnBase.battleDialogTimer = 0;
                    turnBase.battleDialogStage = -1;
                    turnBase.turn = 0;
                    turnBase.enemyActionTimer = 0;
                    turnBase.playerPostAttackCooldown = 0;
                    state = GameState.BATTLE;
                    System.out.println("เปลี่ยนเป็นโหมด BATTLE - เริ่มการต่อสู้กับ Enemy " + (currentEnemyIndex + 1) + "!");
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
