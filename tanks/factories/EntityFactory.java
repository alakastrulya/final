package tanks.factories;

import tanks.entities.Bullet;
import tanks.entities.Tank;
import tanks.entities.Team;
import tanks.entities.Terrain;

public interface EntityFactory {
    Tank createTank(Team team, int id);
    Bullet createBullet(Tank tank);
    Terrain createTerrain(String type, int row, int col, int convID);
}
