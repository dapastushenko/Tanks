package ClientServer.graphics;

import ClientServer.utils.ResourceLoader;

import java.awt.image.BufferedImage;
import java.io.Serializable;

public class TextureAtlas implements Serializable {
    BufferedImage image;

    public TextureAtlas(String imageName) {
        image = ResourceLoader.loadImage(imageName);
    }

    public BufferedImage cut(int x, int y, int w, int h) {
        //режем изображение на куски, от гиморой а
        return image.getSubimage(x, y, w, h);

    }
}
