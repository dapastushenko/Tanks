package ClientServer.game;

import ClientServer.IO.Input;

import java.awt.*;

public abstract class Entity {

    public final EntityType type;

    //местонахождение объектов
    protected float x;
    protected float y;
    protected float height;
    protected float width;

    protected Entity(EntityType type, float x, float y) {
        this.type = type;
        this.x = x;
        this.y = y;
    }

    public abstract void update(Input input);

    public abstract void render(Graphics2D g);

//    public boolean isInArea(int x, int y) {

//    }
}
