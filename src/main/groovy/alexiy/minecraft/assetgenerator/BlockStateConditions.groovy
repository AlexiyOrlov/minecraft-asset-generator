package alexiy.minecraft.assetgenerator

/**
 * Created on 12/28/17.
 */
enum BlockStateConditions {
    EAST_FACING('facing=east'),
    WEST_FACING('facing=west'),
    SOUTH_FACING('facing=south'),
    NORTH_FACING('facing=north'),
    UP_FACING('facing=up'),
    DOWN_FACING('facing=down'),
    DOOR_BOTTOM('half=lower'),
    DOOR_UPPER('half=upper'),
    DOOR_OPEN('open=true'),
    DOOR_CLOSED('open=false'),
    DOOR_HINGE_RIGHT('hinge=right'),
    DOOR_HINGE_LEFT('hinge=left'),
    POWERED('powered=true'),
    UNPOWERED('powered=false'),
    STAIRS_TOP('half=top'),
    STAIRS_BOTTOM('half=bottom'),
    STRAIGHT_SHAPE('shape=straight'),
    OUTER_RIGHT_SHAPE('shape=outer_right'),
    OUTER_LEFT_SHAPE('shape=outer_left'),
    INNER_RIGHT_SHAPE('shape=inner_right'),
    INNER_LEFT_SHAPE('shape=inner_left')

    String condition

    BlockStateConditions(String condition_) {
        condition = condition_
    }
}