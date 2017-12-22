package utils;

import java.awt.image.BufferedImage;

public class Utils {
    public static BufferedImage resize(BufferedImage image, int width, int height) {
        //функция изменяет размеры изображений, потому что т.к. было сделано раньше делать дорого по ресурсам
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        newImage.getGraphics().drawImage(image, 0, 0, width, height, null);

        return newImage;
    }
}
