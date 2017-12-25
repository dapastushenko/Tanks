package utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ResourceLoader {
//    public static final String PATH = "resources/";

    public static BufferedImage loadImage(String fileName) {
        BufferedImage image = null;

        try {
            image = ImageIO.read(ResourceLoader.class.getClassLoader().getResourceAsStream("Battle City JPN.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    public static BufferedReader loadLvlTxt(String fileName) {
        BufferedReader lvlmap = null;
        InputStream in = BufferedReader.class.getClassLoader().getResourceAsStream("level.lvl");

        int[][] matrix = new int[50][37];
        return null;

    }
}
