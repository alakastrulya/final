package tanks.factories;

import tanks.entities.Bullet;
import tanks.entities.Tank;
import tanks.entities.Team;
import tanks.entities.Terrain;

public class BattleCityEntityFactory implements EntityFactory {
    @Override
    public Tank createTank(Team team, int id) {
        return new Tank(team, id);
    }
    @Override
    public Bullet createBullet(Tank tank) {
        return new Bullet(tank);
    }
    @Override
    public Terrain createTerrain(String type, int row, int column, int convID) {
        return new Terrain(type, row, column, convID);
    }
}
