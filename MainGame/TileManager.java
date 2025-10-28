package MainGame;

import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.imageio.ImageIO;

public class TileManager{
    GamePanle gp;
    public Tile[] tile;
    public int mapTileNum[][];

    public TileManager(GamePanle gp) {
        this.gp = gp;
        
        tile = new Tile[11];
        mapTileNum = new int[gp.maxWorldCol][gp.maxWorldRow];

        getTileImage();
        loadMap("world01.txt"); // เผื่อทำหลาย map
    }

    public void getTileImage() {

        try {
            tile[0] = new Tile();
            tile[0].image = ImageIO.read(getClass().getResource("result_Grass.jpg"));

            tile[1] = new Tile();
            tile[1].image = ImageIO.read(getClass().getResource("result_stone.jpg"));

            tile[2] = new Tile();
            tile[2].image = ImageIO.read(getClass().getResource("result_water.jpg"));
            tile[2].collision = true;

            tile[3] = new Tile();
            tile[3].image = ImageIO.read(getClass().getResource("result_rock.jpg"));
            tile[3].collision = true;

            tile[4] = new Tile();
            tile[4].image = ImageIO.read(getClass().getResource("result_sand.jpg"));

            tile[5] = new Tile();
            tile[5].image = ImageIO.read(getClass().getResource("result_1.png")); // stone
            tile[5].collision = true;

            tile[6] = new Tile();
            tile[6].image = ImageIO.read(getClass().getResource("result_2.png")); // coffin
            tile[6].collision = true;

            tile[7] = new Tile();
            tile[7].image = ImageIO.read(getClass().getResource("result_3.png")); // rock grass black
            tile[7].collision = true;

            tile[8] = new Tile();
            tile[8].image = ImageIO.read(getClass().getResource("result_4.png")); // tree
            tile[8].collision = true;

            tile[9] = new Tile();
            tile[9].image = ImageIO.read(getClass().getResource("result_5.png")); // rock stone black
            tile[9].collision = true;

            tile[10] = new Tile();
            tile[10].image = ImageIO.read(getClass().getResource("result_dust.png")); // dust
            tile[10].collision = true;
            


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadMap(String filePath) {

        try {
            InputStream is = getClass().getResourceAsStream(filePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            int col = 0;
            int row = 0;

            while(col < gp.maxWorldCol && row < gp.maxWorldRow) {
                String line = br.readLine();
                
                while(col < gp.maxWorldCol) {

                    String numbers[] = line.split(" ");

                    int num = Integer.parseInt(numbers[col]);

                    mapTileNum[col][row] = num;
                    col++;
                }
                if(col == gp.maxWorldCol) {
                    col = 0;
                    row++;
                }
            }
            br.close();
            
        } catch (Exception e) {
            e.printStackTrace(); // แสดง error เพื่อ debug
        }
    }
    
        public void draw(Graphics2D g2) {
        // คำนวณกล้องแบบ free camera (ไม่ clamp)
        int camX = gp.player1.worldX - gp.player1.screenX;
        int camY = gp.player1.worldY - gp.player1.screenY;

        for (int row = 0; row < gp.maxWorldRow; row++) {
            for (int col = 0; col < gp.maxWorldCol; col++) {

                int worldX = col * gp.titlesize;
                int worldY = row * gp.titlesize;
                int screenX = worldX - camX;
                int screenY = worldY - camY;

                // วาด tile[0] เป็นพื้นหลัง
                if (screenX + gp.titlesize < 0 || screenX > gp.Widthscreen ||
                    screenY + gp.titlesize < 0 || screenY > gp.Hightscreen) {
                    continue; // ออกจากหน้าจอ
                }

                // ตรวจว่าพื้นที่อยู่นอก map → วาด tile[0] เป็นพื้น
                if (col < 0 || col >= gp.maxWorldCol || row < 0 || row >= gp.maxWorldRow) {
                    g2.drawImage(tile[0].image, screenX, screenY, gp.titlesize, gp.titlesize, null);
                } else {
                    g2.drawImage(tile[mapTileNum[col][row]].image, screenX, screenY, gp.titlesize, gp.titlesize, null);
                }
            }
        }
        }
}
