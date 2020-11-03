package alexiy.minecraft.asset.generator.eventhandlers;

public enum Function {
    SET_COUNT("set_count");
    public String name;

    Function(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
