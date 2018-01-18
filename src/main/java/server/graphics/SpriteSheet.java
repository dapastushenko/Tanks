package server.graphics;

import java.awt.image.BufferedImage;

public class SpriteSheet {
    //изображение с несколькими фигурами
    private BufferedImage sheet;
    private int spriteCount; //колво изобр в sheet
    private int scale; //размер одного спрайта
    private int spritesInWidth; //кол-во спрайтов в ширину

    public SpriteSheet(BufferedImage sheet, int spriteCount, int scale) {
        this.sheet = sheet;
        this.spriteCount = spriteCount;
        this.scale = scale;
        this.spritesInWidth = sheet.getWidth() / scale;
    }

    public BufferedImage getSprite(int index) {
        //вычисляем танк
        index = index % spriteCount;
        int x = index % spritesInWidth * scale;
        int y = index / spritesInWidth * scale;
        return sheet.getSubimage(x, y, scale, scale);
    }
}
