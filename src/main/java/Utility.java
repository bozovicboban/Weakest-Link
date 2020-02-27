import com.esotericsoftware.kryo.Kryo;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class Utility {
    private static MediaPlayer mediaPlayer;
    public static double VOLUME = 0.25;

    public static void registerClasses(Kryo kryo){
        for(Class c : new Class[]{boolean.class, int.class, String.class, ClientPlayer.class, ClientPlayer[].class}){
            kryo.register(c);
        }
    }

    public static void playSound(String name){
        Media sound = new Media(new File("Sounds/" + name).toURI().toString());
        mediaPlayer = new MediaPlayer(sound);
        mediaPlayer.setVolume(VOLUME);
        mediaPlayer.play();
    }

    public static void playSound(Path path){
        Media sound = new Media(path.toFile().toURI().toString());
        mediaPlayer = new MediaPlayer(sound);
        mediaPlayer.setVolume(VOLUME);
        mediaPlayer.play();
    }

    public static void sleep(int millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static int rand(int max){
        return (int)(Math.random() * 1000000000) % (max + 1);
    }

    public static List<Path> getFiles(String path){
        return getFiles(Paths.get(path));
    }

    public static Path[] getFilesArr(String path){
        return getFiles(Paths.get(path)).toArray(new Path[]{});
    }

    public static List<Path> getFiles(Path path){
        try {
            return Files.list(path).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
