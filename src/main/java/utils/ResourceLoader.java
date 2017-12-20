package utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ResourceLoader {
    public static final String PATH = "resources/";

    public static BufferedImage loadImege(String fileName) {
        BufferedImage image = null;

        try {
            image = ImageIO.read(ResourceLoader.class.getClassLoader().getResourceAsStream("Battle City JPN.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }
}
