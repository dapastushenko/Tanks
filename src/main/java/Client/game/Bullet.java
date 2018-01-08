package Client.game;

import ClientServer.utils.Utils;

import java.awt.image.BufferedImage;

public class Bullet {
    public static final int BULLET_SCALE = 16; //размер изображения 16 пикселей
    public static final int BULLETS_PER_HEADING = 1;
    private BufferedImage image;

    public Bullet(BufferedImage image, int scale) {
        this.image = Utils.resize(image, image.getWidth() * scale, image.getHeight() * scale);
    }

}
