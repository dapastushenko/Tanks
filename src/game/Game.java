package game;

import display.Display;
import utils.Time;

public class Game implements Runnable{
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    public static final String TITLE = "Tanks";
    public static final int CLEAR_COLOR  = 0xff000000;
    public static final int NUM_BUFFERS = 3;


    public static final float UPDATE_RATE = 60.0f; //сколько раз в секунду идет просчет физики, ура танчики едут
    public static final float UPDATE_INTERVAL = Time.SECOND/UPDATE_RATE; //ХРАНИМ ВРЕМЯ МЕЖДУ АПДЕЙТАМИ
    public static final long IDLE_TIME = 1; //ожидание для threada время(млсек)

    private boolean running; //флаг запущена ли игра
    private Thread gameThread;

    public Game(){
        running=false;
        Display.created(WIDTH,HEIGHT,TITLE,CLEAR_COLOR,NUM_BUFFERS);

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
            //добавить логирование
            e.printStackTrace();
        }
        cleanup();
    }
    private void update(){
        //физика игры
    }

    private void render(){
        //прорисовка сцен вахаха, пульки пульки
    }

    @Override
    public void run() {
        //синхронизированный код
        //ядро, луп
        float delta = 0;

        long lasttime = Time.get(); //прошлое время
        while (running){
            long now = Time.get(); //тек время
            //считаем сколько времени прошло с последнего запуска кода
            long elapsedTime = now - lasttime;
            lasttime = now;

            boolean render = false;
            //кол-во раз сколько должна бежать функция
            delta += (elapsedTime/UPDATE_INTERVAL); //каждая 1 означает что нужно сделать update
            while (delta>1){
                update();
                delta--;
                render = true;
            }
            if (render){
                //если что-то изменили перерисовываем сцену, а то пульки летать не будут
                render();
            }else{
                //стопим тред
                try {
                    Thread.sleep(IDLE_TIME);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }

        }

    }

    private void cleanup(){
        //удаляем окно
        Display.destroy();
    }
}
