import com.esotericsoftware.kryonet.Client;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class Icon extends ImageView {
    public Icon(Path path, Client client, String name){
        super();
        setImageToUnitIcon(path);
        setOnMouseClicked(e -> {
            Utility.playSound("Click.wav");
            client.sendTCP("unit:" + name);
        });
    }

    private void setImageToUnitIcon(Path path){
        try {
            List<Path> files = Files.list(path).collect(Collectors.toList());
            for(Path p : files){
                if(p.getFileName().toString().endsWith(".JPG")){
                    setImage(new Image("file:" + p.toString()));
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
