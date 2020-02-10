package alexiy.minecraft.assetgenerator

/**
 * Created on 12/24/17.
 */
enum Recipes {
    SHAPED('Shaped'),
    SHAPELESS('Shapeless')

    String name

    Recipes(String name) {
        this.name = name
    }
}