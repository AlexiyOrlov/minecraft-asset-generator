package alexiy.minecraft.asset.generator;

public enum Axis {
    X, Y, Z;

    @Override
    public String toString() {
        return name().toLowerCase();
    }

}