import java.util.ArrayList;

public class GameState {
    private ArrayList<ServerPlayer> players;
    public State state;
    public String soundPath;
    public String unitName;
    public int roundsLeft;
    public int playersLeft;

    private static final int ROUND_NUM = 5;

    public enum State{
        NOT_STARTED, IN_ROUND, BETWEEN_ROUNDS
    }

    public GameState(){
        players = new ArrayList<>();
        state = State.NOT_STARTED;
        soundPath = null;
        unitName = null;
    }

    public void init(){
        playersLeft = players.size();
        for(ServerPlayer p : players){
            p.hasLost = false;
            p.totalPoints = 0;
            p.guessedRight = false;
            p.reward = 3;
        }
    }

    public ServerPlayer winner(){
        if(playersLeft > 1)
            return null;
        for(ServerPlayer p : players){
            if(!p.hasLost)
                return p;
        }
        return null;
    }

    public ServerPlayer loser(){
        int ind = -1;
        int minPoints = -1;
        boolean isTie = false;
        for(int i = 0; i < players.size(); i++){
            if(!players.get(i).hasLost) {
                if (ind == -1 || players.get(i).totalPoints < minPoints) {
                    minPoints = players.get(i).totalPoints;
                    ind = i;
                    isTie = false;
                }else if (players.get(i).totalPoints == minPoints)
                    isTie = true;
            }
        }
        if(isTie)
            return null;
        return players.get(ind);
    }

    public String getRound(){
        return (ROUND_NUM - roundsLeft + 1) + " / " + ROUND_NUM;
    }

    public void resetRounds(){
        resetRewards();
        roundsLeft = ROUND_NUM;
    }

    public boolean everyoneDone() {
        for (ServerPlayer p : players)
            if (!p.hasLost && !p.guessedRight && p.reward > 0)
                return false;
        return true;
    }

    public void resetRewards(){
        for(ServerPlayer p : players){
            p.reward = 3;
            p.guessedRight = false;
        }
    }

    public ServerPlayer getPlayer(int connID){
        for(ServerPlayer p : players)
            if(p.connID == connID)
                return p;
        return null;
    }

    public void removePlayer(int connID){
        for(int i = 0; i < players.size(); i++){
            if(players.get(i).connID == connID) {
                players.remove(i);
                return;
            }
        }
    }

    public ServerPlayer getPlayer(String name){
        for(ServerPlayer p : players)
            if(p.name.equals(name))
                return p;
        return null;
    }

    public String addPlayer(int connId, String name){
        if(getPlayer(name) == null) {
            players.add(new ServerPlayer(connId, name));
            return name;
        }
        else
            return addPlayer(connId, name, 1);
    }

    private String addPlayer(int connId, String name, int num){
        if(getPlayer(name + " " + num) == null){
            players.add(new ServerPlayer(connId, name + " " + num));
            return name + " " + num;
        }
        return addPlayer(connId, name, num + 1);
    }

    public ClientPlayer[] getClientPlayers(){
        ClientPlayer[] cp = new ClientPlayer[players.size()];
        for(int i = 0; i < cp.length; i++){
            cp[i] = players.get(i).getClientPlayer();
        }
        return cp;
    }
}
