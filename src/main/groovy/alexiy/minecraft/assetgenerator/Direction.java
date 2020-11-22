package alexiy.minecraft.assetgenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created on 9/4/17.
 */
public enum Direction {
    NORTH("north", 0, Axis.X), SOUTH("south", 180, Axis.Y), UP("up", 270, Axis.X), DOWN("down", 90, Axis.X), EAST("east", 90, Axis.Y), WEST("west", 270, Axis.Y);

    Direction(String name_, int modelRotation, Axis axis_) {
        name = name_;
        rotation = (short) modelRotation;
        around = axis_;
    }

    public static List<Direction> getHorizontals() {
        return new ArrayList<Direction>(Arrays.asList(NORTH, SOUTH, EAST, WEST));
    }

    public String name;
    public short rotation;
    public Axis around;
}

