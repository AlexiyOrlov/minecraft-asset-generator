package alexiy.minecraft.assetgenerator

/**
 * Created on 12/27/17.
 */
enum BlockState {
    DOOR('Door', 'Standard door', null),
    SIMPLE_BLOCK('Simple block', 'Full block with single texture', BlockModel.SINGLETEXTURE),
    DIRECTIONAL('Directional block', 'Block that can be rotated in all directions', BlockModel.DIRECTIONAL),
    COLUMN('Column', 'Block with one texture for top and bottom, and second texture for sides', BlockModel.COLUMN),
    TOP_BOTTOM_SIDE('Column B', 'Block with one texture for top, second texture for bottom, third texture for sides', BlockModel.DIFFERENT_TOPANDBOTTOM),
    HORIZONTAL('Horizontal', 'Block which can be rotated in horizontal directions; one texture for front, second texture for top,third texture for other sides', BlockModel.ORIENTABLE),
    CROSS('Cross', 'For flower-like or mushroom-like blocks', BlockModel.CROSS),
    DIFFERENT_SIDES('All sides different', 'Each side has different texture', BlockModel.DIFFERENTSIDES),
    STAIRS('Stairs', 'Standard stairs', null),
    PANE('Pane', 'Glass pane-like blocks', null)
//    SLAB('Slab','Standard slab'),
//    CABLE('Cable','')

    /** Type alias */
    String type
    String description
    BlockModel defaultModel

    BlockState(String type_, String description_, BlockModel defaultModel_) {
        type = type_
        description = description_
        this.defaultModel = defaultModel_
    }

    @Override
    String toString() {
        return type
    }
}