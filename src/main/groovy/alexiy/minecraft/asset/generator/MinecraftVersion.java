package alexiy.minecraft.asset.generator;

/**
 * Created on 2/13/20.
 */
public enum MinecraftVersion {
    V1_12("1.12"), V1_15("1.15");

    MinecraftVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return version;
    }

    public final String getVersion() {
        return version;
    }

    private final String version;
}
