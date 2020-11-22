package alexiy.minecraft.assetgenerator;

/**
 * Created on 12/24/17.
 */
public enum Recipes {
    SHAPED("Shaped"), SHAPELESS("Shapeless");

    Recipes(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;
}
