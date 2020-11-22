package alexiy.minecraft.assetgenerator;

/**
 * Created on 12/27/17.
 */
public enum BlockState {
    SIMPLE_BLOCK("Simple block", "Full block with single texture", BlockModel.SINGLETEXTURE), DIRECTIONAL("Directional block", "Block that can be rotated in all directions", BlockModel.COLUMN), COLUMN("Column", "Block with one texture for top and bottom, and second texture for sides", BlockModel.COLUMN), TOP_BOTTOM_SIDE("Column B", "Block with one texture for top, second texture for bottom, third texture for sides", BlockModel.DIFFERENT_TOPANDBOTTOM), HORIZONTAL("Horizontal", "Block which can be rotated in horizontal directions; one texture for front, second texture for top,third texture for other sides", BlockModel.ORIENTABLE), CROSS("Cross", "For flower-like or mushroom-like blocks", BlockModel.CROSS), DIFFERENT_SIDES("All sides different", "Each side has different texture", BlockModel.DIFFERENTSIDES);

    /**
     * Type alias
     */
    private String type;
    private String description;
    private BlockModel defaultModel;

    BlockState(String type_, String description_, BlockModel defaultModel_) {
        type = type_;
        description = description_;
        this.defaultModel = defaultModel_;
    }

    @Override
    public String toString() {
        return type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }


    public BlockModel getDefaultModel() {
        return defaultModel;
    }

}
