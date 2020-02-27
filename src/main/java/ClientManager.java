import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.FrameworkMessage;
import com.esotericsoftware.kryonet.Listener;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.Double.MAX_VALUE;

public class ClientManager {
    public ClientManager(Tab gameTab, String name, int port, String ip, Client client){
        ListView<LogLabel> log = new ListView<>();
        log.getItems().add(new LogLabel("LOG", Color.GOLD));

        ListView<LogLabel> playerList = new ListView<>();
        playerList.getItems().add(new LogLabel("PLAYER POINTS", Color.GOLD));

        Label timer = new Label("Timer: ");

        client = new Client();
        setupGame(gameTab, log, playerList, timer, client);
        Utility.registerClasses(client.getKryo());
        client.addListener(listener(log, playerList, timer));
        client.start();
        try {
            client.connect(5000, ip, port, port);
            client.sendTCP("name:" + name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupGame(Tab gameTab, ListView<LogLabel> log, ListView playerList, Label timer, Client client){
        Button soundButton = new Button("Repeat Sound");
        soundButton.setOnAction(e -> {
            client.sendTCP("repeat");
        });
        soundButton.setMaxWidth(MAX_VALUE);

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(new ScrollPane(getIconTable(client)));
        borderPane.setLeft(log);
        borderPane.setRight(playerList);
        borderPane.setBottom(soundButton);

        LabeledSlider volumeSlider = new LabeledSlider("Volume: ", Utility.VOLUME);
        volumeSlider.slider.valueProperty().addListener(e -> {
            Utility.VOLUME = volumeSlider.slider.getValue();
        });

        BorderPane topPane = new BorderPane();
        topPane.setLeft(timer);
        topPane.setRight(volumeSlider);

        borderPane.setTop(topPane);
        gameTab.setContent(borderPane);
    }

    private VBox getIconTable(Client client){
        Path[][] units = getUnitPaths();

        Node[] rows = new Node[units.length * 2];
        for(int i = 0; i < units.length; i++){
            rows[i*2] = new LogLabel(units[i][0].getParent().getFileName().toString() + ":", Color.BLACK);
            rows[i*2+1] = new HBox();
            for(Path p : units[i]){
                ((HBox)rows[i*2+1]).getChildren().add(new Icon(p, client, p.getFileName().toString()));
            }
        }

        VBox vBox = new VBox();
        vBox.getChildren().addAll(rows);
        return vBox;
    }

    private Path[][] getUnitPaths(){
        Path[] races = Utility.getFilesArr("Units");
        Path[][] ret = new Path[races.length][];
        for(int i = 0; i < races.length; i++)
            ret[i] = Utility.getFilesArr("Units/" + races[i].getFileName());
        return ret;
    }

    private Listener listener(ListView<LogLabel> log, ListView<LogLabel> playerList, Label timer){
        return new Listener(){
            @Override
            public void connected(Connection connection) {

            }

            @Override
            public void received(Connection connection, Object object) {
                if(object instanceof FrameworkMessage.KeepAlive)
                    return;
                Platform.runLater(() -> {
                    if(object instanceof ClientPlayer[]){
                        updatePlayerList(playerList, (ClientPlayer[])object);
                    }else
                        parseMSG(object.toString(), log, timer);
                });
            }

            @Override
            public void disconnected(Connection connection) {

            }
        };
    }

    private void updatePlayerList(ListView<LogLabel> playerList, ClientPlayer[] players){
        playerList.getItems().clear();
        playerList.getItems().add(new LogLabel("PLAYER POINTS", Color.GOLD));
        for(ClientPlayer p : players){
            if(p.hasLost)
                playerList.getItems().add(new LogLabel(p.name +  " - " + p.totalPoints + " - PLACE: " + p.place, Color.GRAY));
            else
                playerList.getItems().add(new LogLabel(p.name + " - " + p.totalPoints, Color.BLACK));
        }
    }

    private void parseMSG(String msg, ListView<LogLabel> log, Label timer){
        String[] pair = msg.split(":", 2);
        switch (pair[0]){
            case "log":
                log.getItems().add(new LogLabel(pair[1], Color.BLACK));
                break;
            case "sound":
                Utility.playSound(pair[1]);
                break;
            case "unitsound":
                Utility.playSound(Paths.get(pair[1]));
                break;
            case "timer":
                timer.setText("Timer: " + pair[1]);
                break;
        }
    }
}
