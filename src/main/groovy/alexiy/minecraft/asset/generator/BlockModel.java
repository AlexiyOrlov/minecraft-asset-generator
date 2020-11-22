package alexiy.minecraft.asset.generator;

public enum BlockModel {
    SINGLETEXTURE("block/cube_all", "All textures are same"),
    DIRECTIONAL("block/piston", "For rotatable blocks"),
    COLUMN("block/cube_column", "One texture for top and bottom, other texture for sides"),
    DIFFERENT_TOPANDBOTTOM("block/cube_bottom_top", "One texture for top, second texture for bottom, other texture for sides"),
    DIFFERENTSIDES("block/cube", "All sides have different textures"),
    ORIENTABLE("block/orientable", "One texture for top, second texture for front, other texture for sides"),
    CROSS("block/cross", "For flower-like blocks");

    BlockModel(String parentModel, String description_) {
        value = parentModel;
        description = description_;
    }

    public String value;
    public String description;
}
