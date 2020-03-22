package alexiy.minecraft.assetgenerator

/**
 * Created on 12/27/17.
 */
enum BlockState {
    DOOR('Door', 'Standard door'),
    SIMPLE_BLOCK('Simple block', 'Full block with single texture'),
    DIRECTIONAL('Directional block', 'Block that can be rotated in all directions'),
    COLUMN('Column', 'Block with one texture for top and bottom, and second texture for sides'),
    TOP_BOTTOM_SIDE('Column B', 'Block with one texture for top, second texture for bottom, third texture for sides'),
    HORIZONTAL('Horizontal', 'Block which can be rotated in horizontal directions; one texture for front, second texture for top,third texture for other sides'),
    CROSS('Cross', 'For flower-like or mushroom-like blocks'),
    DIFFERENT_SIDES('All sides different', 'Each side has different texture'),
    STAIRS('Stairs', 'Standard stairs'),
    PANE('Pane', 'Glass pane-like blocks')
//    SLAB('Slab','Standard slab'),
//    CABLE('Cable','')

    /** Type alias */
    String type
    String description
//    String parentModel

    BlockState(String type_, String description_) {
        type = type_
        description = description_
//        parentModel=parentModel_
    }

    @Override
    String toString() {
        return type
    }
}