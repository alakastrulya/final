package tanks.enums;

import tanks.core.Sprite;

import java.util.LinkedList;
import java.util.Queue;

public class CollisionOpList { // Class CollisionOpList which save operation's queue conflict

    private Queue<CollisionOp> opsQueue = new LinkedList<>();

    public void enqueue(Sprite sprite, CollisionOp.Operation operation) { // Function which add operation to queue
        opsQueue.add(new CollisionOp(sprite, operation));
    }

    public Queue<CollisionOp> getOpsQueue() { // Function which return queue
        return opsQueue;
    }
}
