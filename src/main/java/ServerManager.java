import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.FrameworkMessage;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ServerManager {
    public ServerManager(Tab gameTab, int port, Server server){
        GameState gameState = new GameState();

        Utility.registerClasses(server.getKryo());

        Label connCountLabel = new Label("Number of Clients: 0");
        Button startButton = new Button("Start");

        startButton.setOnAction(e -> {
            startButton.setDisable(true);
            setupGame(server, gameState, startButton);
        });

        VBox vBox = new VBox();
        vBox.getChildren().addAll(connCountLabel, startButton);
        gameTab.setContent(vBox);

        server.addListener(listener(server, gameState, connCountLabel));
        try {
            server.bind(port, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.start();
    }

    private void setupGame(Server server, GameState gameState, Button startButton){
        gameState.state = GameState.State.IN_ROUND;
        gameState.init();
        server.sendToAllTCP(gameState.getClientPlayers());
        server.sendToAllTCP("log:The game has started.");
        checkForWin(server, gameState, startButton);
    }

    private void prepNextRound(Server server, GameState gameState, Button startButton){
        gameState.state = GameState.State.BETWEEN_ROUNDS;
        gameState.resetRewards();
        server.sendToAllTCP("sound:letsplay2.mp3");
        doTimer(6, server, () -> {
            server.sendToAllTCP("log:The round has begun!");
            gameState.state = GameState.State.IN_ROUND;
            pickSound(server, gameState, startButton);
        });
    }

    private void pickSound(Server server, GameState gameState, Button startButton){
        List<Path> files = Utility.getFiles("Units");
        Path racePath = files.get(Utility.rand(files.size() - 1));
        List<Path> units = Utility.getFiles(racePath);
        gameState.unitName = units.get(Utility.rand(units.size() - 1)).getFileName().toString();
        List<Path> unitFiles = Utility.getFiles("Units/" + racePath.getFileName() + "/" + gameState.unitName);
        gameState.soundPath = getRandSoundPath(unitFiles);
        server.sendToAllTCP("unitsound:" + gameState.soundPath);
        doTimerInRound(30, server, gameState, () -> {
            endRound(server, gameState, startButton);
        });
    }

    private void endRound(Server server, GameState gameState, Button startButton){
        doTimer(2, server, () -> {
            gameState.state = GameState.State.BETWEEN_ROUNDS;
            gameState.roundsLeft--;
            server.sendToAllTCP("log:And the unit was: " + gameState.unitName + "!");
            if(gameState.roundsLeft > 0){
                prepNextRound(server, gameState, startButton);
            }else{
                ServerPlayer loser = gameState.loser();
                if(loser == null)
                    initSuddenDeath(server, gameState, startButton);
                else
                    kill(loser, server, gameState, startButton);
            }
        });
    }

    private void kill(ServerPlayer loser, Server server, GameState gameState, Button startButton){
        server.sendToAllTCP("sound:whoisit.mp3");
        doTimer(6, server, () -> {
            server.sendToAllTCP("sound:goodbye2.mp3");
            doTimer(1, server, () -> {
                loser.hasLost = true;
                loser.place = gameState.playersLeft;
                gameState.playersLeft--;
                server.sendToAllTCP("log:" + loser.name + " lost and got place number: " + loser.place + "!");
                server.sendToAllTCP(gameState.getClientPlayers());
                checkForWin(server, gameState, startButton);
            });
        });
    }

    private void checkForWin(Server server, GameState gameState, Button startButton){
        doTimer(2, server, () -> {
            if(gameState.playersLeft <= 1){
                String winnerName = gameState.winner().name;
                gameState.winner().place = gameState.playersLeft;
                gameState.winner().hasLost = true;
                server.sendToAllTCP(gameState.getClientPlayers());
                server.sendToAllTCP("sound:finalend.mp3");
                doTimer(4, server, () -> {
                    server.sendToAllTCP("log:" + winnerName + " has won! Congratulations!!! :O");
                    server.sendToAllTCP("sound:joinus.mp3");
                    startButton.setDisable(false);
                });
            }else {
                gameState.resetRounds();
                prepNextRound(server, gameState, startButton);
            }
        });
    }

    private void initSuddenDeath(Server server, GameState gameState, Button startButton){
        server.sendToAllTCP("log:Last place is a tie so we go to the next round!");
        prepNextRound(server, gameState, startButton);
    }

    private String getRandSoundPath(List<Path> unitFiles) {
        int ind = Utility.rand(unitFiles.size() - 1);
        if(unitFiles.get(ind).getFileName().toString().endsWith(".wav"))
            return unitFiles.get(ind).toString();
        return getRandSoundPath(unitFiles);
    }

    private void doTimer(int seconds, Server server, Runnable endRunnable){
        Thread t = new Thread(() -> {
            for(int i = 0; i < seconds; i++){
                server.sendToAllTCP("timer:" + (seconds - i));
                Utility.sleep(1000);
            }
            Platform.runLater(endRunnable);
        });
        t.setDaemon(true);
        t.start();
    }

    private void doTimerInRound(int seconds, Server server, GameState gameState, Runnable endRunnable){
        Thread t = new Thread(() -> {
            for(int i = 0; i < seconds; i++){
                if(gameState.everyoneDone()){
                    server.sendToAllTCP("log:Everyone's done already?");
                    Platform.runLater(endRunnable);
                    return;
                }
                server.sendToAllTCP("timer:" + (seconds - i) + " Round: " + gameState.getRound());
                Utility.sleep(1000);
            }
            Platform.runLater(endRunnable);
        });
        t.setDaemon(true);
        t.start();
    }

    public Listener listener(Server server, GameState gameState, Label connCountLabel){
        return new Listener(){
            @Override
            public void connected(Connection connection) {
                Platform.runLater(() -> {
                    connCountLabel.setText("Number of Clients: " + server.getConnections().length);
                });
                connection.sendTCP("sound:welcome.mp3");
            }

            @Override
            public void received(Connection connection, Object object) {
                if(object instanceof FrameworkMessage.KeepAlive)
                    return;
                Platform.runLater(() -> {
                    parseMSG(object.toString(), server, connection, gameState);
                });
            }

            @Override
            public void disconnected(Connection connection) {
                Platform.runLater(() -> {
                    connCountLabel.setText("Number of Clients: " + server.getConnections().length);
                    String name = gameState.getPlayer(connection.getID()).name;
                    server.sendToAllTCP("log:" + name + " has left the game.. :C");
                    gameState.removePlayer(connection.getID());
                    server.sendToAllTCP(gameState.getClientPlayers());
                });
            }
        };
    }

    private void parseMSG(String msg, Server server, Connection connection, GameState gameState){
        if(msg.equals("repeat")){
            if(gameState.soundPath != null)
                connection.sendTCP("unitsound:" + gameState.soundPath);
            return;
        }
        String[] pair = msg.split(":", 2);
        switch (pair[0]){
            case "name":
                String name = pair[1];
                name = gameState.addPlayer(connection.getID(), name);
                server.sendToAllTCP("log:" + name + " has joined the game! :D");
                server.sendToAllTCP(gameState.getClientPlayers());
                break;
            case "unit":
                switch (gameState.state){
                    case NOT_STARTED:
                        //connection.sendTCP("sound:Error.wav");
                        break;
                    case IN_ROUND:
                        if(gameState.getPlayer(connection.getID()).hasLost)
                            return;
                        if(gameState.getPlayer(connection.getID()).guessedRight)
                            return;
                        if(gameState.getPlayer(connection.getID()).reward == 0){
                            connection.sendTCP("sound:Error.wav");
                            connection.sendTCP("log:Can't make any more guesses.");
                            return;
                        }
                        String unit = pair[1];
                        if(gameState.unitName.equals(unit)){
                            connection.sendTCP("sound:GoodJob.wav");
                            connection.sendTCP("log:You guessed it right!");
                            gameState.getPlayer(connection.getID()).guessedRight = true;
                            gameState.getPlayer(connection.getID()).totalPoints += gameState.getPlayer(connection.getID()).reward;
                            server.sendToAllTCP(gameState.getClientPlayers());
                        }else{
                            connection.sendTCP("sound:Error.wav");
                            gameState.getPlayer(connection.getID()).reward--;
                            if(gameState.getPlayer(connection.getID()).reward == 0)
                                connection.sendTCP("log:WRONG! Sorry, no more tries.");
                            else if(gameState.getPlayer(connection.getID()).reward == 1)
                                connection.sendTCP("log:WRONG! Careful, you have one try left.");
                            else
                                connection.sendTCP("log:WRONG! Two more tries left.");

                        }
                        break;
                    case BETWEEN_ROUNDS:
                        //connection.sendTCP("sound:Error.wav");
                        break;
                }
                break;
        }
    }
}
