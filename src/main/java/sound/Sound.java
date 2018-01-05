//package sound;
//
//import javax.sound.sampled.*;
//import java.io.File;
//import java.io.IOException;
//
//public class Sound {
//    private boolean released = false;
//    private Clip clip = null;
//    private FloatControl volumControl = null;
//    private boolean playing = false;
//
//    public Sound(File file) {
//        try {
//            AudioInputStream stream = AudioSystem.getAudioInputStream(file);
//            clip = AudioSystem.getClip();
//            clip.open(stream);
//            clip.addLineListener(new Listener());
//            volumControl = (FloatControl) clip.getCo
//
//        } catch (UnsupportedAudioFileException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//}
