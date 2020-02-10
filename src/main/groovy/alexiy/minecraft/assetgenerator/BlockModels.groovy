package alexiy.minecraft.assetgenerator

enum BlockModels {


    BLOCKSINGLETEXTURE('block/cube_all', 'All textures are same'),
    BLOCKDIRECTIONAL('block/piston', 'For rotatable blocks'),
    BLOCKCOLUMN('block/cube_column', 'One texture for top and bottom, other texture for sides'),
    BLOCKDIFFERENTTOPANDBOTTOM('block/cube_bottom_top', 'One texture for top, second texture for bottom, other texture for sides'),
    BLOCKDIFFERENTSIDES('block/cube', 'All sides have different textures'),
    BLOCK_ORIENTABLE('block/orientable', 'One texture for top, second texture for front, other texture for sides'),
    CROSS('block/cross', 'For flower-like blocks'),


//    public SimpleStringProperty desc
    public String value, description

    BlockModels(String value_, String description_) {
        value = value_
        description = description_
    }

}