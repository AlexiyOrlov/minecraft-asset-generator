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

    public static Function byName(String name) {
        for (Function function : Function.values()) {
            if (function.name.equals(name))
                return function;
        }
        return null;
    }
}
