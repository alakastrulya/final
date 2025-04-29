package tanks.enums;

import tanks.core.Sprite;

public class CollisionOp { // Class CollisionOp which describe conflicts

    public enum Operation { // List of operation's type
        ADD,    // Add object to game
        REMOVE  // Delete object from game
    }

    private Sprite sprite;
    private Operation operation;

    public CollisionOp(Sprite sprite, Operation operation) { // Constructor for CollisionOp
        this.sprite = sprite;
        this.operation = operation;
    }

    public Sprite getSprite() { // Function which get object
        return sprite;
    }

    public Operation getOperation() { // Function which get operation's type
        return operation;
    }
}
