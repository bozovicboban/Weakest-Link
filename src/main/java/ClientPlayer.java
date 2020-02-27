public class ClientPlayer {
    public String name;
    public int totalPoints, place;
    public boolean hasLost;

    public ClientPlayer(){ // FOR KRYO

    }

    public ClientPlayer(String name) {
        this.name = name;
        hasLost = false;
        totalPoints = 0;
    }
}