package ClientServer.game;

import ClientServer.IO.Input;
import ClientServer.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Bullet extends Entity{
    public static final int BULLET_SCALE = 16; //размер изображения 16 пикселей
    public static final int BULLETS_PER_HEADING = 1;
    public static final List BULLETS_QUEUE = null; //очередь пуль, пока что здесь
    private BufferedImage image;
/*
    public Bullet(BufferedImage image, int scale) {
        this.image = Utils.resize(image, image.getWidth() * scale, image.getHeight() * scale);
    }
*/

    protected Bullet(EntityType type, float x, float y) {
        super(type.Bullet, x, y);
    }

    @Override
    public void update(Input input) {

    }

    @Override
    public void render(Graphics2D g) {

    }

}
