package alexiy.minecraft.asset.generator.eventhandlers

/**
 * Created on 2/13/20.
 */
enum MinecraftVersion {

    V1_12('1.12'),
    V1_15('1.15')
    final String version

    MinecraftVersion(String version) {
        this.version = version
    }
}