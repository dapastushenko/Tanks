package main;

import display.Display;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class Main {

    public static void main(String[] args){
        //hex 0123456789abcdef - обозначение цветов. int - max 0x(прозр)ff(red)ff(green)ff(blue)ff
        Display.created(800,600,"Tanks",0xff00ff00,3);

        Timer t = new Timer(1000 / 60,new AbstractAction(){

            @Override
            public void actionPerformed(ActionEvent e) {
                Display.clear();
                //временно
                Display.render();
                Display.swapBuffers();
            }
        });
        t.setRepeats(true);
        t.start();
    }
}
