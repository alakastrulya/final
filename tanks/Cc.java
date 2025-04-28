package tanks;

public class Cc { // Class Cc (Singleton), it manages all date from game

    private static final Cc instance = new Cc();
    CollisionOpList opsList = new CollisionOpList(); // Create array with operations
    private Tank tankPlayerOne;

    private Cc() {}

    public static Cc getInstance() { // Function which return object of this class (Singleton pattern)
        return instance;
    }

    private CollisionOpList getOpsList() { // Function which return list with operations
        return opsList;
    }

    public void initTankPlayerOne(Tank tank) { // Function which save first player's tank
        this.tankPlayerOne = tank;
    }

    public Tank getTankPlayerOne() { // Function which return first player's tank
        return tankPlayerOne;
    }

}
