import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Server;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Paths;

public class Main extends Application {

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        initStage(primaryStage);
    }

    private void initStage(Stage primaryStage){
        primaryStage.setTitle("Weakest Link");
        // primaryStage.getIcons().add(new Image("icon.png"));
        TabPane tabPane = new TabPane();
        Tab connTab = new Tab("Connection");
        Tab gameTab = new Tab("Game");
        initConnTab(connTab, gameTab, tabPane, primaryStage);
        initGameTab(gameTab);
        tabPane.getTabs().addAll(connTab, gameTab);
        int WIDTH = 1550;
        int HEIGHT = 550;
        primaryStage.setScene(new Scene(tabPane, WIDTH-50, HEIGHT-50));
        primaryStage.setMinWidth(WIDTH);
        primaryStage.setMinHeight(HEIGHT);

        primaryStage.show();
    }

    private void initConnTab(Tab connTab, Tab gameTab, TabPane tabPane, Stage primaryStage){
        connTab.setClosable(false);
        LabeledTextField nameField = new LabeledTextField("Name: ", "dumbo");
        LabeledTextField ipField = new LabeledTextField("IP: ", "localhost");
        LabeledTextField portField = new LabeledTextField("Port: ", "25565");
        Button joinButton = new Button("Join");
        Button hostButton = new Button("Host");

        Server server = new Server();
        Client client = new Client();

        primaryStage.setOnCloseRequest(e -> {
            try {
                server.stop();
                server.dispose();
            }catch (Exception ex){
                ex.printStackTrace();
            }

            try {
                client.stop();
                client.dispose();
            }catch (Exception ex){
                ex.printStackTrace();
            }
        });

        joinButton.setOnAction(e -> {
            connTab.setDisable(true);
            gameTab.setDisable(false);
            tabPane.getSelectionModel().select(gameTab);
            new ClientManager(gameTab, nameField.field.getText(), Integer.parseInt(portField.field.getText()), ipField.field.getText(), client);
        });

        hostButton.setOnAction(e -> {
            connTab.setDisable(true);
            gameTab.setDisable(false);
            tabPane.getSelectionModel().select(gameTab);
            new ServerManager(gameTab, Integer.parseInt(portField.field.getText()), server);
        });

        VBox vBox = new VBox();
        vBox.getChildren().addAll(nameField, ipField, portField, joinButton, hostButton);
        connTab.setContent(vBox);
    }

    private void initGameTab(Tab gameTab){
        gameTab.setClosable(false);
        gameTab.setDisable(true);
    }
}
