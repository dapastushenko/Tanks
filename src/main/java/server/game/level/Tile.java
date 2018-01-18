package server.game.level;

import server.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Tile {
    //таблица для вставки объектов
    private BufferedImage image;
    private TileType type;

    public Tile(BufferedImage image, int scale, TileType type) {
        this.type = type;
        this.image = Utils.resize(image, image.getWidth() * scale, image.getHeight() * scale);
    }

    protected void render(Graphics2D g, int x, int y) {
        g.drawImage(image, x, y, null);
    }

    protected TileType type() {
        return type;
    }
}
