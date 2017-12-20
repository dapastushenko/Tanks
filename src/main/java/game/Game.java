package game;

import IO.Input;
import display.Display;
import graphics.Sprite;
import graphics.SpriteSheet;
import graphics.TextureAtlas;
import utils.Time;

import java.awt.*;
import java.awt.event.KeyEvent;

public class Game implements Runnable{
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    public static final String TITLE = "Tanks";
    public static final int CLEAR_COLOR  = 0xff000000;
    public static final int NUM_BUFFERS = 3;


    public static final float UPDATE_RATE = 60.0f; //сколько раз в секунду идет просчет физики, ура танчики едут
    public static final float UPDATE_INTERVAL = Time.SECOND/UPDATE_RATE; //храним время между апдейтами
    public static final long IDLE_TIME = 1; //ожидание для threada время(млсек)

    private boolean running; //флаг запущена ли игра
    private Thread gameThread;
    private Graphics2D graphics;
    private Input input;
    private TextureAtlas atlas;
    private SpriteSheet sheet;
    private Sprite sprite;
    public static final String ATLAS_FILE_NAME= "Battle City JPN.png";

    // temp
    float x = 350;
    float y = 250;
    float delta = 0;
    float radius = 50;
    float speed = 3;
    //temp end

    public Game(){
        running=false;
        Display.created(WIDTH,HEIGHT,TITLE,CLEAR_COLOR,NUM_BUFFERS);
        graphics = Display.getGraphics();
        input = new Input();
        Display.addInputListener(input);
        atlas = new TextureAtlas(ATLAS_FILE_NAME);
        //сопстно передаем координаты чтобы порезать картинку
        sheet = new SpriteSheet(atlas.cut(1*16,9*16,16*2,16),2,16);
        sprite = new Sprite(sheet,1);

    }

    public synchronized void start(){
        //старт игры вызываем только одним потоком
        if(running)
            return;

        running=true;
        gameThread = new Thread(this);
        gameThread.start();

    }
    public synchronized void stop(){
        //конец игры
        if(!running)
            return;
        running=false;

        try {
            gameThread.join();
        }catch (InterruptedException e){
            //добавить логирование нафига только?

            e.printStackTrace();
        }
        cleanup();
    }
    private void update(){
        //физика игры
        //delta+=0.02f;
        if(input.getKey(KeyEvent.VK_UP))
            y-=speed;
        if(input.getKey(KeyEvent.VK_DOWN))
            y+=speed;
        if(input.getKey(KeyEvent.VK_LEFT))
            x-=speed;
        if(input.getKey(KeyEvent.VK_RIGHT))
            x+=speed;
    }

    private void render(){
        //прорисовка сцен вахаха, пульки пульки
        Display.clear();
        graphics.setColor(Color.white);
//        graphics.fillOval((int)(x+(Math.sin(delta)*200)),(int)y,(int)radius*2,(int)radius*2);
//        graphics.drawImage(atlas.cut(0,0,32,32),300,300,null);
        //заменили т.к. написали функцию которая вычисляет танк
        sprite.render(graphics, x, y);
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
        while (running){
            long now = Time.get(); //тек время
            //считаем сколько времени прошло с последнего запуска кода
            long elapsedTime = now - lasttime;
            lasttime = now;
            count+=elapsedTime;

            boolean render = false;
            //кол-во раз сколько должна бежать функция
            delta += (elapsedTime/UPDATE_INTERVAL); //каждая 1 означает что нужно сделать update
            while (delta>1){
                update();
                upd++;
                delta--;
                if(render){
                    updl++;
                }else {
                    render = true;
                }
            }
            if (render){
                //если что-то изменили перерисовываем сцену, а то пульки летать не будут
                render();
                fps++;
            }else{
                //стопим тред
                try {
                    Thread.sleep(IDLE_TIME);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            if(count>=Time.SECOND){
                Display.setTitle(TITLE+" || fps:" + fps + " | Upd:" + upd +" | Updl:" + updl);
                upd = 0;
                updl = 0;
                fps = 0;
                count = 0;
            }
        }

    }

    private void cleanup(){
        //удаляем окно
        Display.destroy();
    }
}
