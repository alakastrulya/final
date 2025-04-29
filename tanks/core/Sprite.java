package tanks.core;

import tanks.entities.Team;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class Sprite { // Abstract class Sprite for objects to map
    private Point center;
    private int sideLength;
    private Rectangle rectangle;
    private BufferedImage subImage;
    private Team team;
    private int elevation;

    public Sprite() {}

    public void setCenter(Point center) { // Function which save center
        this.center = center;
    }

    public Point getCenter() { // Function which get center position
        return center;
    }

    public void setSideLength(int sideLength) { // Function which save size side
        this.sideLength = sideLength;
    }

    public int getSideLength() { // Function which get size side
        return sideLength;
    }

    public void updateRectangle() { // Function which save rectangle collisions
        if (center != null) {
            rectangle = new Rectangle(
                    center.x - sideLength / 2,
                    center.y - sideLength / 2,
                    sideLength,
                    sideLength
            );
        }
    }

    public Rectangle getRectangle() { // Function which get rectangle collisions
        return rectangle;
    }

    public void setSubImage(BufferedImage subImage) { // Function which save image
        this.subImage = subImage;
    }

    public BufferedImage getSubImage() { // Function which get image
        return subImage;
    }

    public void setTeam(Team team) { // Function which save object's commands
        this.team = team;
    }

    public Team getTeam() { // Function which get object's command
        return team;
    }

    public void setElevation(int elevation) { // Function which save object's height
        this.elevation = elevation;
    }

    public int getElevation() { // Function which get object's height
        return elevation;
    }



    public abstract void move(); // Abstract function for moving
    public abstract void draw(Graphics g); // Abstract function for draw
}
