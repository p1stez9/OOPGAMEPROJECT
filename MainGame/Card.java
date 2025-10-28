package MainGame;

public class Card {
    public enum Type { STUN, POISON, SHIELD }

    public final Type type;
    public final int costDust;

    public Card(Type type, int costDust) {
        this.type = type;
        this.costDust = costDust;
    }
}


