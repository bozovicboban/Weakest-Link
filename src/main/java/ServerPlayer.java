public class ServerPlayer extends ClientPlayer{
    public int connID, reward;
    public boolean guessedRight;

    public ServerPlayer(int connID, String name){
        super(name);
        this.connID = connID;
    }

    public ClientPlayer getClientPlayer(){
        ClientPlayer cp = new ClientPlayer(name);
        cp.totalPoints = totalPoints;
        cp.place = place;
        cp.hasLost = hasLost;
        return cp;
    }
}
