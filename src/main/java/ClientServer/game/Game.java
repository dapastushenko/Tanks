package ClientServer.game;

import ClientServer.IO.Input;
import ClientServer.display.Display;
import ClientServer.game.level.Level;
import ClientServer.graphics.TextureAtlas;
import ClientServer.utils.Time;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Game implements Runnable {
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    public static final String TITLE = "Tanks";
    public static final int CLEAR_COLOR = 0xff000000;
    public static final int NUM_BUFFERS = 3;


    public static final float UPDATE_RATE = 60.0f; //сколько раз в секунду идет просчет физики, ура танчики едут
    public static final float UPDATE_INTERVAL = Time.SECOND / UPDATE_RATE; //храним время между апдейтами
    public static final long IDLE_TIME = 1; //ожидание для threada время(млсек)
    private static Map<EntityType, List<Bullet>> bullets;

    private boolean running; //флаг запущена ли игра
    private Thread gameThread;
    private Graphics2D graphics;
    private Input input;
    private TextureAtlas atlas;
    private Player player;
    private Level lvl;

    public static final String ATLAS_FILE_NAME = "Battle City JPN.png";

    public Game() {
        running = false;
        Display.created(WIDTH, HEIGHT, TITLE, CLEAR_COLOR, NUM_BUFFERS);
        graphics = Display.getGraphics();
        input = new Input();
        Display.addInputListener(input);
        atlas = new TextureAtlas(ATLAS_FILE_NAME);
        //сопстно передаем координаты чтобы порезать картинку
        bullets = new HashMap<>();
        bullets.put(EntityType.Player, new LinkedList<Bullet>());
        player = new Player(300, 300, 2, 3, atlas, lvl);
        lvl = new Level(atlas);
    }

    public synchronized void start() {
        //старт игры вызываем только одним потоком
        if (running)
            return;

        running = true;
        gameThread = new Thread(this);
        gameThread.start();

    }

    public synchronized void stop() {
        //конец игры
        if (!running)
            return;
        running = false;

        try {
            gameThread.join();
        } catch (InterruptedException e) {
            //добавить логирование нафига только?

            e.printStackTrace();
        }
        cleanup();
    }

    private void update() {
        //физика игры
        player.update(input);
        lvl.update();

        for (int i = 0; i < bullets.get(EntityType.Player).size(); i++)
            bullets.get(EntityType.Player).get(i).update();
        // send to all clients: write to ObjectOutputStream
    }

    private void render() {
        //прорисовка сцен
        Display.clear();
        lvl.render(graphics);
        player.render(graphics);
        for (Bullet bullet : getBullets(EntityType.Player))
            bullet.render(graphics);
        lvl.renderGrass(graphics);

        Display.swapBuffers();


    }

    @Override
    public void run() {
        //синхронизированный код
        //ядро, луп
        int fps = 0;
        int upd = 0;
        int updl = 0;

        long count = 0;


        float delta = 0;

        long lasttime = Time.get(); //прошлое время
        while (running) {
            long now = Time.get(); //тек время
            //считаем сколько времени прошло с последнего запуска кода
            long elapsedTime = now - lasttime;
            lasttime = now;
            count += elapsedTime;

            boolean render = false;
            //кол-во раз сколько должна бежать функция
            delta += (elapsedTime / UPDATE_INTERVAL); //каждая 1 означает что нужно сделать update
            while (delta > 1) {
                update();
                upd++;
                delta--;
                if (render) {
                    updl++;
                } else {
                    render = true;
                }
            }
            if (render) {
                //если что-то изменили перерисовываем сцену, а то пульки летать не будут
                render();
                fps++;
            } else {
                //стопим тред
                try {
                    Thread.sleep(IDLE_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (count >= Time.SECOND) {
                Display.setTitle(TITLE + " || fps:" + fps + " | Upd:" + upd + " | Updl:" + updl);
                upd = 0;
                updl = 0;
                fps = 0;
                count = 0;
            }
        }

    }

    private void cleanup() {
        //удаляем окно
        Display.destroy();
    }
    public static void registerBullet(EntityType type, Bullet bullet) {
        bullets.get(type).add(bullet);
    }
    public static void unregisterBullet(EntityType type, Bullet bullet) {
        if (bullets.get(type).size() > 0) {
            bullets.get(type).remove(bullet);
        }
    }
    public static List<Bullet> getBullets(EntityType type) {
        return bullets.get(type);
    }
}
