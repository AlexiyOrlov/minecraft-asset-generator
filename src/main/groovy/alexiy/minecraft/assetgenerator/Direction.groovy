package alexiy.minecraft.assetgenerator

/**
 * Created on 9/4/17.
 */
enum Direction {
    NORTH('north', 0, Axis.X),
    SOUTH('south', 180, Axis.Y),
    UP('up', 270, Axis.X),
    DOWN('down', 90, Axis.X),
    EAST('east', 90, Axis.Y),
    WEST('west', 270, Axis.Y)

    public String name
    public short rotation
    public Axis around

    Direction(String name_, int modelRotation, Axis axis_) {
        name = name_
        rotation = (short) modelRotation
        around = axis_
    }

    static List<Direction> getHorizontals() {
        return [NORTH, SOUTH, EAST, WEST]
    }
}

enum Axis
{
    X,
    Y,
    Z

    @Override
    String toString() {
        return name().toLowerCase()
    }
}