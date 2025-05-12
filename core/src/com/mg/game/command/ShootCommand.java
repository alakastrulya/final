package com.mg.game.command;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.mg.game.bullet.Bullet;
import com.mg.game.tank.Tank;

import java.util.List;

public class ShootCommand implements Command {
    private final Tank tank;
    private final boolean useEnter; // true если использовать Enter, false если пробел
    private final List<Bullet> bullets;
    private float shootCooldown = 0f;

    private static final float SHOOT_COOLDOWN_TIME = 0.3f;

    public ShootCommand(Tank tank, boolean useEnter, List<Bullet> bullets) {
        this.tank = tank;
        this.useEnter = useEnter;
        this.bullets = bullets;
    }

    public void update(float delta) {
        if (shootCooldown > 0f) {
            shootCooldown -= delta;
            if (shootCooldown < 0f) shootCooldown = 0f;
        }
    }

    @Override
    public boolean canExecute() {
        int shootKey = useEnter ? Input.Keys.ENTER : Input.Keys.SPACE;
        return tank.isAlive() && Gdx.input.isKeyJustPressed(shootKey) && shootCooldown <= 0f;
    }

    @Override
    public void execute() {
        Bullet bullet = tank.shoot();
        if (bullet != null) {
            bullets.add(bullet);
            shootCooldown = SHOOT_COOLDOWN_TIME;
            Gdx.app.log("ShootCommand", "Shot a bullet for tank at " + tank.positionX + ", " + tank.positionY);
        }
    }
}