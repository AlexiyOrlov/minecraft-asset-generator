package alexiy.minecraft.assetgenerator.eventhandlers

import alexiy.minecraft.assetgenerator.*
import com.google.common.collect.Sets
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.*
import javafx.scene.layout.Pane

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Created on 12/28/17.
 */
class BlockStateGenerator implements EventHandler<ActionEvent> {

    ChoiceBox<String> generateLootTable, blocksstates
    TextField modidentifier, objectidentifier, directionname, singleTexture
    Tab blockstatetab
    CheckBox generateBlockstate, generateWorldModel
    ChoiceBox<String> minecraftVersion
    private Pane contentPane

    BlockStateGenerator(ChoiceBox<String> states, TextField modid, TextField blockId, TextField direction,
                        TextField optionalSingleTexture, Tab blockStateTab, ChoiceBox<String> minecraftVersions, Pane container) {
        blocksstates = states
        modidentifier = modid
        objectidentifier = blockId
        directionname = direction
//        resourceDirectory=resourceFolder
        blockstatetab = blockStateTab
        singleTexture = optionalSingleTexture
        minecraftVersion = minecraftVersions
        generateLootTable = new ChoiceBox<>(FXCollections.observableArrayList('Generate standard loot table', 'Generate NBT copy loot table'));
        generateLootTable.getSelectionModel().select(0);
        contentPane = container;
        contentPane.getChildren().add(generateLootTable)
        generateBlockstate = new CheckBox('Generate block state')
        generateBlockstate.setSelected(true)
        contentPane.getChildren().add(generateBlockstate)
        generateWorldModel = new CheckBox('Generate world model')
        generateWorldModel.setSelected(true)
        contentPane.getChildren().add(generateWorldModel)
    }

    @Override
    void handle(ActionEvent event) {
        String blockstate = blocksstates.value
        String modid = modidentifier.text
        String blockidentifier = objectidentifier.text
        JAG.lastModid = modid
        if (modid && blockidentifier && JAG.resourceDirectory) {
            boolean generateModels = generateWorldModel.isSelected()
            boolean generateState = generateBlockstate.isSelected()
            String version = minecraftVersion.selectionModel.getSelectedItem();
            File resourceDirectory = JAG.resourceDirectory
            File modelWorld = Paths.get(resourceDirectory.absolutePath, AssetConstants.ASSETS.value, modid, AssetConstants.MODELS_LITERAL.value, AssetConstants.BLOCKMODEL.value, blockidentifier + '.json').toFile()
            if (modelWorld.parentFile)
                modelWorld.parentFile.mkdirs()
            File blockstatefile = Paths.get(resourceDirectory.absolutePath, AssetConstants.ASSETS.value, modid, AssetConstants.BLOCKSTATES.value, blockidentifier + '.json').toFile()
            if (generateState) {
                if (blockstatefile.parentFile)
                    blockstatefile.parentFile.mkdirs()
                blockstatefile.createNewFile()
            }
            File inventoryblockmodel = Paths.get(resourceDirectory.absolutePath, AssetConstants.ASSETS.value, modid, AssetConstants.MODELS_LITERAL.value, AssetConstants.BLOCKMODEL.value, blockidentifier + '_inventory.json').toFile()
            if (inventoryblockmodel.parentFile)
                inventoryblockmodel.parentFile.mkdirs()
            File itemModel = null
            if (version == '1.12')
                itemModel = Paths.get(resourceDirectory.absolutePath, AssetConstants.ASSETS.value, modid, AssetConstants.MODELS_LITERAL.value, AssetConstants.ITEMMODEL.value, blockidentifier + '.json').toFile()
            else if (version == '1.14')
                itemModel = Paths.get(resourceDirectory.absolutePath, AssetConstants.ASSETS.value, modid, AssetConstants.MODELS_LITERAL.value, AssetConstants.ITEM_LITERAL.value, blockidentifier + '.json').toFile()
            if (itemModel.parentFile)
                itemModel.parentFile.mkdirs()
            String textureprefix
            if (version == '1.12')
                textureprefix = "$modid:$AssetConstants.BLOCKTEXTURE.value/$blockidentifier"
            else if (version == '1.14')
                textureprefix = "$modid:$AssetConstants.BLOCK_LITERAL.value/$blockidentifier"

            if (generateModels && blockstate != BlockState.DOOR.type && blockstate != BlockState.PANE.type)
                modelWorld.createNewFile()
            if (blockstate != BlockState.DOOR.type && generateModels)
                itemModel.createNewFile()
            switch (blockstate) {
                case BlockState.SIMPLE_BLOCK.type:
                    if (generateState) {
                        blockstatefile.setText(Utilities.createSimpleBlockState(modid, blockidentifier, version))
                    }
                    if (generateModels) {
                        String string = Utilities.formatJson([parent: BlockModel.SINGLETEXTURE.value, textures: [all: textureprefix]])
                        modelWorld.setText(string)

                    }
                    break
                case BlockState.CROSS.type:
                    if (generateModels) {
                        String json = Utilities.formatJson([parent: BlockModel.CROSS.value, textures: [cross: textureprefix]])
                        modelWorld.setText(json)
                    }
                    if (generateState)
                        blockstatefile.setText(Utilities.createSimpleBlockState(modid, blockidentifier, version))
                    break
                case BlockState.DIFFERENT_SIDES.type:
                    if (generateModels) {
                        String string = Utilities.formatJson([parent: BlockModel.DIFFERENTSIDES.value, textures: [particle: textureprefix + '_up', east: textureprefix + '_east', west: textureprefix + '_west', north: textureprefix + '_north', south: textureprefix + '_south', up: textureprefix + '_up', down: textureprefix + '_down']])
                        modelWorld.setText(string)
                    }
                    if (generateState)
                        blockstatefile.setText(Utilities.createSimpleBlockState(modid, blockidentifier, version))
                    break
                case BlockState.COLUMN.type:
                    if (generateModels) {
                        String string = Utilities.formatJson([parent: BlockModel.COLUMN.value, textures: [side: textureprefix, end: textureprefix + '_end']])
                        modelWorld.setText(string)
                    }
                    if (generateState)
                        blockstatefile.setText(Utilities.createSimpleBlockState(modid, blockidentifier, version))
                    break
                case BlockState.TOP_BOTTOM_SIDE.type:
                    if (generateModels) {
                        String str = Utilities.formatJson([parent: BlockModel.DIFFERENT_TOPANDBOTTOM.value, textures: [top: textureprefix + '_top', bottom: textureprefix + '_bottom', side: textureprefix]])
                        modelWorld.setText(str)
                    }
                    if (generateState)
                        blockstatefile.setText(Utilities.createSimpleBlockState(modid, blockidentifier, version))
                    break
                case BlockState.DIRECTIONAL.type:
                    if (generateState) {
                        def map = [:]
                        if (directionname) {
                            Direction.values().each {
                                String direction = directionname.getText() + '=' + it.name
                                String axis = it.around.toString()
                                if (version == '1.12')
                                    map.put(direction, [model: "$modid:$blockidentifier", (axis): it.rotation])
                                else if (version == '1.14')
                                    map.put(direction, [model: "$modid:$AssetConstants.BLOCK_LITERAL.value/$blockidentifier", (axis): it.rotation])
                            }
                        }
                        map = [variants: map]
                        String vars = Utilities.formatJson(map)
                        blockstatefile.setText(vars)
                    }
                    if (generateModels) {
                        String worldmodel = '{}'
                        if (version == '1.12') {
                            worldmodel = Utilities.formatJson([parent: BlockModel.DIRECTIONAL.value, textures: [bottom: "$modid:$AssetConstants.BLOCKTEXTURE.value/$blockidentifier" + '_bottom', side: "$modid:$AssetConstants.BLOCKTEXTURE.value/$blockidentifier" + '_side', platform: "$modid:$AssetConstants.BLOCKTEXTURE.value/$blockidentifier"]])
                        } else if (version == '1.14') {
                            worldmodel = Utilities.formatJson([parent: BlockModel.DIRECTIONAL.value, textures: [bottom: "$modid:$AssetConstants.BLOCK_LITERAL.value/$blockidentifier" + '_bottom', side: "$modid:$AssetConstants.BLOCK_LITERAL.value/$blockidentifier" + '_side', platform: "$modid:$AssetConstants.BLOCK_LITERAL.value/$blockidentifier"]])
                        }
                        modelWorld.setText(worldmodel)

                        String inventorymodel = Utilities.createDirectionalBlockModel(modid, blockidentifier, version)
                        if (blockstate != BlockState.DOOR.type)
                            inventoryblockmodel.createNewFile()
                        inventoryblockmodel.setText(inventorymodel)
                    }
                    break
                case BlockState.HORIZONTAL.type:
                    if (generateState) {
                        def path = ''
                        if (version == '1.12')
                            path = "$modid:$blockidentifier"
                        else if (version == '1.14')
                            path = "$modid:$AssetConstants.BLOCK_LITERAL.value/$blockidentifier"
                        def state = Utilities.formatJson([variants: [('facing=north'): [model: path],
                                                                     ('facing=south'): [model: path, y: 180],
                                                                     ('facing=west') : [model: path, y: 270],
                                                                     ('facing=east') : [model: path, y: 90]]])
                        blockstatefile.setText(state)
                    }
                    if (generateModels) {
                        String texturePrefix = null
                        if (version == '1.12')
                            texturePrefix = AssetConstants.TEXTURES.value + '/' + AssetConstants.BLOCKTEXTURE.value
                        else if (version == '1.14')
                            texturePrefix = AssetConstants.TEXTURES.value + '/' + AssetConstants.BLOCK_LITERAL.value

                        String model = Utilities.formatJson([parent  : 'block/orientable',
                                                             textures: [top  : "$modid:$texturePrefix/" + blockidentifier + '_top',
                                                                        front: "$modid:$texturePrefix/" + blockidentifier + '_front',
                                                                        side : "$modid:$texturePrefix/" + blockidentifier]])
                        modelWorld.setText(model)
                    }
                    break
                case BlockState.PANE.type:
                    if (generateState) {
                        def conditionarray = []
                        conditionarray.add([apply: [model: "$modid:$blockidentifier" + '_post']])
                        conditionarray.add([when: [north: true], apply: [model: "$modid:$blockidentifier" + '_side']])
                        conditionarray.add([when: [south: true], apply: [model: "$modid:$blockidentifier" + '_side_alt']])
                        conditionarray.add([when: [west: true], apply: [model: "$modid:$blockidentifier" + '_side_alt', y: 90]])
                        conditionarray.add([when: [east: true], apply: [model: "$modid:$blockidentifier" + '_side', y: 90]])
                        conditionarray.add([when: [north: false], apply: [model: "$modid:$blockidentifier" + '_noside']])
                        conditionarray.add([when: [east: false], apply: [model: "$modid:$blockidentifier" + '_noside_alt']])
                        conditionarray.add([when: [south: false], apply: [model: "$modid:$blockidentifier" + '_noside_alt', y: 90]])
                        conditionarray.add([when: [west: false], apply: [model: "$modid:$blockidentifier" + '_noside', y: 270]])
                        def map = [multipart: conditionarray]
                        blockstatefile.setText(Utilities.formatJson(map))
                    }
                    if (generateModels) {
                        String prefix = null;
                        if (version == '1.12')
                            prefix = AssetConstants.BLOCKMODEL.value
                        else if (version == '1.14')
                            prefix = AssetConstants.BLOCK_LITERAL.value
                        Path noSidePath = Paths.get(resourceDirectory.absolutePath, 'assets', modid, AssetConstants.MODELS_LITERAL.value, AssetConstants.BLOCKMODEL.value, blockidentifier + '_noside.json')
                        Files.createFile(noSidePath)
                        def noSideModel = [parent: "block/pane_noside", textures: [pane: "$modid:$prefix/$blockidentifier"]]
                        noSidePath.setText(Utilities.formatJson(noSideModel))

                        Path noSideAlt = Paths.get(resourceDirectory.absolutePath, 'assets', modid, AssetConstants.MODELS_LITERAL.value, AssetConstants.BLOCKMODEL.value, blockidentifier + '_noside_alt.json')
                        Files.createFile(noSideAlt)
                        def noSideAltModel = [parent: "block/pane_noside_alt", textures: [pane: "$modid:$prefix/$blockidentifier"]]
                        noSideAlt.setText(Utilities.formatJson(noSideAltModel))

                        Path postPath = Paths.get(resourceDirectory.absolutePath, 'assets', modid, AssetConstants.MODELS_LITERAL.value, AssetConstants.BLOCKMODEL.value, blockidentifier + '_post.json')
                        Files.createFile(postPath)
                        def postModel = [parent: "block/pane_post", textures: [pane: "$modid:$prefix/$blockidentifier", edge: "$modid:$prefix/$blockidentifier"]]
                        postPath.setText(Utilities.formatJson(postModel))

                        Path sidePath = Paths.get(resourceDirectory.absolutePath, 'assets', modid, AssetConstants.MODELS_LITERAL.value, AssetConstants.BLOCKMODEL.value, blockidentifier + '_side.json')
                        Files.createFile(sidePath)
                        def sideModel = [parent: "block/pane_side", textures: [pane: "$modid:$prefix/$blockidentifier", edge: "$modid:$prefix/$blockidentifier"]]
                        sidePath.setText(Utilities.formatJson(sideModel))

                        Path sideAltPath = Paths.get(resourceDirectory.absolutePath, 'assets', modid, AssetConstants.MODELS_LITERAL.value, AssetConstants.BLOCKMODEL.value, blockidentifier + '_side_alt.json')
                        Files.createFile(sideAltPath)
                        def sideAltModel = [parent: 'block/pane_side_alt', textures: [pane: "$modid:$prefix/$blockidentifier", edge: "$modid:$prefix/$blockidentifier"]]
                        sideAltPath.setText(Utilities.formatJson(sideAltModel))
                    }

                    break
                case BlockState.STAIRS.type:
                    if (generateState) {
                        def states = [:]
                        def product = Sets.cartesianProduct(Sets.newHashSet('facing=north', 'facing=south', 'facing=east', 'facing=west'),
                                Sets.newHashSet('half=top', 'half=bottom'),
                                Sets.newHashSet('shape=straight', 'shape=inner_right', 'shape=outer_right', 'shape=inner_left', 'shape=outer_left'))

                        String modelprefix = "$modid:$blockidentifier"
                        states.put('normal', [model: modelprefix])
                        states.put('inventory', [model: modelprefix])
                        String modelOuter = modelprefix + '_outer', modelInner = modelprefix + '_inner'

                        product.each {
                            List<Map> mapList = []
                            Map<String, Object> map = [:]
                            if (it.contains(BlockStateConditions.EAST_FACING.condition)) {
                                if (it.contains(BlockStateConditions.STAIRS_BOTTOM.condition)) {
                                    if (it.contains(BlockStateConditions.STRAIGHT_SHAPE.condition)) {
                                        map.put('model', modelprefix)
                                    } else if (it.contains(BlockStateConditions.OUTER_RIGHT_SHAPE.condition)) {
                                        map.put('model', modelOuter)
                                    } else if (it.contains(BlockStateConditions.OUTER_LEFT_SHAPE.condition)) {
                                        map.put('model', modelOuter)
                                        map.put('y', 270)
                                        map.put('uvlock', true)
                                    } else if (it.contains(BlockStateConditions.INNER_RIGHT_SHAPE.condition)) {
                                        map.put('model', modelInner)
                                    } else if (it.contains(BlockStateConditions.INNER_LEFT_SHAPE.condition)) {
                                        map.put('model', modelInner)
                                        map.put('y', 270)
                                        map.put('uvlock', true)
                                    }
                                } else if (it.contains(BlockStateConditions.STAIRS_TOP.condition)) {
                                    if (it.contains(BlockStateConditions.STRAIGHT_SHAPE.condition)) {
                                        map.put('model', modelprefix)
                                        map.put('x', 180)
                                        map.put('uvlock', true)
                                    } else if (it.contains(BlockStateConditions.OUTER_RIGHT_SHAPE.condition)) {
                                        map.put('model', modelOuter)
                                        map.put('x', 180)
                                        map.put('y', 90)
                                        map.put('uvlock', true)
                                    } else if (it.contains(BlockStateConditions.OUTER_LEFT_SHAPE.condition)) {
                                        map.put('model', modelOuter)
                                        map.put('x', 180)
                                        map.put('uvlock', true)
                                    } else if (it.contains(BlockStateConditions.INNER_RIGHT_SHAPE.condition)) {
                                        map.put('model', modelInner)
                                        map.put('x', 180)
                                        map.put('y', 90)
                                        map.put('uvlock', true)
                                    } else if (it.contains(BlockStateConditions.INNER_LEFT_SHAPE.condition)) {
                                        map.put('model', modelInner)
                                        map.put('x', 180)
                                        map.put('uvlock', true)
                                    }
                                }
                            } else if (it.contains(BlockStateConditions.WEST_FACING.condition)) {
                                if (it.contains(BlockStateConditions.STAIRS_BOTTOM.condition)) {
                                    if (it.contains(BlockStateConditions.STRAIGHT_SHAPE.condition)) {
                                        map.put('model', modelprefix)
                                        map.put('y', 180)
                                        map.put('uvlock', true)
                                    } else if (it.contains(BlockStateConditions.OUTER_RIGHT_SHAPE.condition)) {
                                        map.put('model', modelOuter)
                                        map.put('y', 180)
                                        map.put('uvlock', true)
                                    } else if (it.contains(BlockStateConditions.OUTER_LEFT_SHAPE.condition)) {
                                        map.put('model', modelOuter)
                                        map.put('y', 90)
                                        map.put('uvlock', true)
                                    } else if (it.contains(BlockStateConditions.INNER_RIGHT_SHAPE.condition)) {
                                        map.put('model', modelInner)
                                        map.put('y', 180)
                                        map.put('uvlock', true)
                                    } else if (it.contains(BlockStateConditions.INNER_LEFT_SHAPE.condition)) {
                                        map.put('model', modelInner)
                                        map.put('y', 90)
                                        map.put('uvlock', true)
                                    }
                                } else if (it.contains(BlockStateConditions.STAIRS_TOP.condition)) {
                                    if (it.contains(BlockStateConditions.STRAIGHT_SHAPE.condition)) {
                                        map.put('model', modelprefix)
                                        map.put('x', 180)
                                        map.put('y', 180)
                                        map.put('uvlock', true)
                                    } else if (it.contains(BlockStateConditions.OUTER_RIGHT_SHAPE.condition)) {
                                        map.put('model', modelOuter)
                                        map.put('x', 180)
                                        map.put('y', 270)
                                        map.put('uvlock', true)
                                    } else if (it.contains(BlockStateConditions.OUTER_LEFT_SHAPE.condition)) {
                                        map.put('model', modelOuter)
                                        map.put('x', 180)
                                        map.put('y', 180)
                                        map.put('uvlock', true)
                                    } else if (it.contains(BlockStateConditions.INNER_RIGHT_SHAPE.condition)) {
                                        map.put('model', modelInner)
                                        map.put('x', 180)
                                        map.put('y', 270)
                                        map.put('uvlock', true)
                                    } else if (it.contains(BlockStateConditions.INNER_LEFT_SHAPE.condition)) {
                                        map.put('model', modelInner)
                                        map.put('x', 180)
                                        map.put('y', 180)
                                        map.put('uvlock', true)
                                    }
                                }
                            } else if (it.contains(BlockStateConditions.SOUTH_FACING.condition)) {
                                if (it.contains(BlockStateConditions.STAIRS_BOTTOM.condition)) {
                                    if (it.contains(BlockStateConditions.STRAIGHT_SHAPE.condition)) {
                                        map.put('model', modelprefix)
                                        map.put('y', 90)
                                        map.put('uvlock', true)
                                    } else if (it.contains(BlockStateConditions.OUTER_RIGHT_SHAPE.condition)) {
                                        map.put('model', modelOuter)
                                        map.put('y', 90)
                                        map.put('uvlock', true)
                                    } else if (it.contains(BlockStateConditions.OUTER_LEFT_SHAPE.condition)) {
                                        map.put('model', modelOuter)
                                    } else if (it.contains(BlockStateConditions.INNER_RIGHT_SHAPE.condition)) {
                                        map.put('model', modelInner)
                                        map.put('y', 90)
                                        map.put('uvlock', true)
                                    } else if (it.contains(BlockStateConditions.INNER_LEFT_SHAPE.condition)) {
                                        map.put('model', modelInner)
                                    }
                                } else if (it.contains(BlockStateConditions.STAIRS_TOP.condition)) {
                                    if (it.contains(BlockStateConditions.STRAIGHT_SHAPE.condition)) {
                                        map.put('model', modelprefix)
                                        map.put('x', 180)
                                        map.put('y', 90)
                                        map.put('uvlock', true)
                                    } else if (it.contains(BlockStateConditions.OUTER_RIGHT_SHAPE.condition)) {
                                        map.put('model', modelOuter)
                                        map.put('x', 180)
                                        map.put('y', 180)
                                        map.put('uvlock', true)
                                    } else if (it.contains(BlockStateConditions.OUTER_LEFT_SHAPE.condition)) {
                                        map.put('model', modelOuter)
                                        map.put('x', 180)
                                        map.put('y', 90)
                                        map.put('uvlock', true)
                                    } else if (it.contains(BlockStateConditions.INNER_RIGHT_SHAPE.condition)) {
                                        map.put('model', modelInner)
                                        map.put('x', 180)
                                        map.put('y', 180)
                                        map.put('uvlock', true)
                                    } else if (it.contains(BlockStateConditions.INNER_LEFT_SHAPE.condition)) {
                                        map.put('model', modelInner)
                                        map.put('x', 180)
                                        map.put('y', 180)
                                        map.put('uvlock', true)
                                    }
                                }
                            } else if (it.contains(BlockStateConditions.NORTH_FACING.condition)) {
                                if (it.contains(BlockStateConditions.STAIRS_BOTTOM.condition)) {
                                    if (it.contains(BlockStateConditions.STRAIGHT_SHAPE.condition)) {
                                        map.put('model', modelprefix)
                                        map.put('y', 270)
                                        map.put('uvlock', true)
                                    } else if (it.contains(BlockStateConditions.OUTER_RIGHT_SHAPE.condition)) {
                                        map.put('model', modelOuter)
                                        map.put('y', 270)
                                        map.put('uvlock', true)
                                    } else if (it.contains(BlockStateConditions.OUTER_LEFT_SHAPE.condition)) {
                                        map.put('model', modelOuter)
                                        map.put('y', 180)
                                        map.put('uvlock', true)
                                    } else if (it.contains(BlockStateConditions.INNER_RIGHT_SHAPE.condition)) {
                                        map.put('model', modelInner)
                                        map.put('y', 270)
                                        map.put('uvlock', true)
                                    } else if (it.contains(BlockStateConditions.INNER_LEFT_SHAPE.condition)) {
                                        map.put('model', modelInner)
                                        map.put('y', 180)
                                        map.put('uvlock', true)
                                    }
                                } else if (it.contains(BlockStateConditions.STAIRS_TOP.condition)) {
                                    if (it.contains(BlockStateConditions.STRAIGHT_SHAPE.condition)) {
                                        map.put('model', modelprefix)
                                        map.put('x', 180)
                                        map.put('y', 270)
                                        map.put('uvlock', true)
                                    } else if (it.contains(BlockStateConditions.OUTER_RIGHT_SHAPE.condition)) {
                                        map.put('model', modelOuter)
                                        map.put('x', 180)
                                        map.put('uvlock', true)
                                    } else if (it.contains(BlockStateConditions.OUTER_LEFT_SHAPE.condition)) {
                                        map.put('model', modelOuter)
                                        map.put('x', 180)
                                        map.put('y', 270)
                                        map.put('uvlock', true)
                                    } else if (it.contains(BlockStateConditions.INNER_RIGHT_SHAPE.condition)) {
                                        map.put('model', modelInner)
                                        map.put('x', 180)
                                        map.put('uvlock', true)
                                    } else if (it.contains(BlockStateConditions.INNER_LEFT_SHAPE.condition)) {
                                        map.put('model', modelInner)
                                        map.put('x', 180)
                                        map.put('y', 270)
                                        map.put('uvlock', true)
                                    }
                                }
                            }
                            mapList.add(map)
                            states.put(it.toString().replace(" ", "").replace("[", "").replace("]", ""), map)
                        }

                        String str = Utilities.formatJson([variants: states])
                        blockstatefile.setText(str)
                    }

                    if (generateModels) {
                        String modelPrefix = modid + ':' + AssetConstants.BLOCKTEXTURE.value + '/' + blockidentifier
                        Path blockModelPath = Paths.get(resourceDirectory.absolutePath, AssetConstants.ASSETS.value, modid, AssetConstants.MODELS_LITERAL.value, AssetConstants.BLOCKMODEL.value)

                        File regularModel = Paths.get(blockModelPath.toString(), blockidentifier + '.json').toFile()
                        regularModel.createNewFile()
                        regularModel.setText(Utilities.formatJson([parent: "block/stairs", textures: [bottom: modelPrefix, top: modelPrefix, side: modelPrefix]]))

                        File inwardModel = Paths.get(blockModelPath.toString(), blockidentifier + '_inner.json').toFile()
                        inwardModel.createNewFile()
                        inwardModel.setText(Utilities.formatJson([parent: "block/inner_stairs", textures: [bottom: modelPrefix, top: modelPrefix, side: modelPrefix]]))

                        File outwardModel = Paths.get(blockModelPath.toString(), blockidentifier + '_outer.json').toFile()
                        outwardModel.createNewFile()
                        outwardModel.setText(Utilities.formatJson([parent: "block/outer_stairs", textures: [bottom: modelPrefix, top: modelPrefix, side: modelPrefix]]))
                    }
                    break
                case BlockState.DOOR.type:
                    String modelprefix = "$modid:$blockidentifier"
                    if (generateState) {
                        def statemap = [:]
                        def product = Sets.cartesianProduct(Sets.newHashSet('facing=east', 'facing=west', 'facing=north', 'facing=south'),
                                Sets.newHashSet('half=lower', 'half=upper'),
                                Sets.newHashSet('hinge=left', 'hinge=right'), Sets.newHashSet('open=true', 'open=false'), Sets.newHashSet('powered=true', 'powered=false'))

                        String topModel = modelprefix + '_top', topModelRightHinge = modelprefix + '_top_rh', bottomModel = modelprefix + '_bottom',
                               bottomModelRightHinge = modelprefix + '_bottom_rh'
                        product.each {
                            List<Map> list = []
                            Map<String, Object> map = [:]

                            if (it.contains(BlockStateConditions.EAST_FACING.condition)) {
                                if (it.contains(BlockStateConditions.DOOR_BOTTOM.condition)) {
                                    if (it.contains(BlockStateConditions.DOOR_HINGE_LEFT.condition)) {
                                        if (it.contains(BlockStateConditions.DOOR_OPEN.condition)) {

                                            map.put('model', bottomModelRightHinge)
                                            map.put('y', 90)
                                        } else if (it.contains(BlockStateConditions.DOOR_CLOSED.condition)) {
                                            map.put('model', bottomModel)
                                        }
                                    } else if (it.contains(BlockStateConditions.DOOR_HINGE_RIGHT.condition)) {
                                        if (it.contains(BlockStateConditions.DOOR_CLOSED.condition)) {
                                            map.put('model', bottomModelRightHinge)
                                        } else if (it.contains(BlockStateConditions.DOOR_OPEN.condition)) {
                                            map.put('model', bottomModel)
                                            map.put('y', 270)
                                        }
                                    }
                                } else if (it.contains(BlockStateConditions.DOOR_UPPER.condition)) {
                                    if (it.contains(BlockStateConditions.DOOR_HINGE_LEFT.condition)) {
                                        if (it.contains(BlockStateConditions.DOOR_OPEN.condition)) {
                                            map.put('model', topModelRightHinge)
                                            map.put('y', 90)
                                        } else if (it.contains(BlockStateConditions.DOOR_CLOSED.condition)) {
                                            map.put('model', topModel)
                                        }
                                    } else if (it.contains(BlockStateConditions.DOOR_HINGE_RIGHT.condition)) {
                                        if (it.contains(BlockStateConditions.DOOR_OPEN.condition)) {
                                            map.put('model', topModel)
                                            map.put('y', 270)
                                        } else if (it.contains(BlockStateConditions.DOOR_CLOSED.condition)) {
                                            map.put('model', topModelRightHinge)
                                        }
                                    }
                                }

                            } else if (it.contains(BlockStateConditions.SOUTH_FACING.condition)) {
                                if (it.contains(BlockStateConditions.DOOR_BOTTOM.condition)) {
                                    if (it.contains(BlockStateConditions.DOOR_HINGE_LEFT.condition)) {
                                        if (it.contains(BlockStateConditions.DOOR_OPEN.condition)) {
                                            map.put('model', bottomModelRightHinge)
                                            map.put('y', 180)
                                        } else if (it.contains(BlockStateConditions.DOOR_CLOSED.condition)) {
                                            map.put('model', bottomModel)
                                            map.put('y', 90)
                                        }
                                    } else if (it.contains(BlockStateConditions.DOOR_HINGE_RIGHT.condition)) {
                                        if (it.contains(BlockStateConditions.DOOR_OPEN.condition)) {
                                            map.put('model', bottomModel)
                                        } else if (it.contains(BlockStateConditions.DOOR_CLOSED.condition)) {
                                            map.put('model', bottomModelRightHinge)
                                            map.put('y', 90)
                                        }
                                    }
                                } else if (it.contains(BlockStateConditions.DOOR_UPPER.condition)) {
                                    if (it.contains(BlockStateConditions.DOOR_HINGE_LEFT.condition)) {
                                        if (it.contains(BlockStateConditions.DOOR_OPEN.condition)) {
                                            map.put('model', topModelRightHinge)
                                            map.put('y', 180)
                                        } else if (it.contains(BlockStateConditions.DOOR_CLOSED.condition)) {
                                            map.put('model', topModel)
                                            map.put('y', 90)
                                        }
                                    } else if (it.contains(BlockStateConditions.DOOR_HINGE_RIGHT.condition)) {
                                        if (it.contains(BlockStateConditions.DOOR_OPEN.condition))
                                            map.put('model', topModel)
                                        else if (it.contains(BlockStateConditions.DOOR_CLOSED.condition)) {
                                            map.put('model', topModelRightHinge)
                                            map.put('y', 90)
                                        }
                                    }
                                }
                            } else if (it.contains(BlockStateConditions.WEST_FACING.condition)) {
                                if (it.contains(BlockStateConditions.DOOR_BOTTOM.condition)) {
                                    if (it.contains(BlockStateConditions.DOOR_HINGE_LEFT.condition)) {
                                        if (it.contains(BlockStateConditions.DOOR_OPEN.condition)) {
                                            map.put('model', bottomModelRightHinge)
                                            map.put('y', 270)
                                        } else if (it.contains(BlockStateConditions.DOOR_CLOSED.condition)) {
                                            map.put('model', bottomModel)
                                            map.put('y', 180)
                                        }
                                    } else if (it.contains(BlockStateConditions.DOOR_HINGE_RIGHT.condition)) {
                                        if (it.contains(BlockStateConditions.DOOR_OPEN.condition)) {
                                            map.put('model', bottomModel)
                                            map.put('y', 90)
                                        } else if (it.contains(BlockStateConditions.DOOR_CLOSED.condition)) {
                                            map.put('model', bottomModelRightHinge)
                                            map.put('y', 180)
                                        }
                                    }
                                } else if (it.contains(BlockStateConditions.DOOR_UPPER.condition)) {
                                    if (it.contains(BlockStateConditions.DOOR_HINGE_LEFT.condition)) {
                                        if (it.contains(BlockStateConditions.DOOR_OPEN.condition)) {
                                            map.put('model', topModelRightHinge)
                                            map.put('y', 270)
                                        } else if (it.contains(BlockStateConditions.DOOR_CLOSED.condition)) {
                                            map.put('model', topModel)
                                            map.put('y', 180)
                                        }
                                    } else if (it.contains(BlockStateConditions.DOOR_HINGE_RIGHT.condition)) {
                                        if (it.contains(BlockStateConditions.DOOR_OPEN.condition)) {
                                            map.put('model', topModel)
                                            map.put('y', 90)
                                        } else if (it.contains(BlockStateConditions.DOOR_CLOSED.condition)) {
                                            map.put('model', topModelRightHinge)
                                            map.put('y', 180)
                                        }
                                    }
                                }
                            } else if (it.contains(BlockStateConditions.NORTH_FACING.condition)) {
                                if (it.contains(BlockStateConditions.DOOR_BOTTOM.condition)) {
                                    if (it.contains(BlockStateConditions.DOOR_HINGE_LEFT.condition)) {
                                        if (it.contains(BlockStateConditions.DOOR_OPEN.condition)) {
                                            map.put('model', bottomModel)
                                        } else if (it.contains(BlockStateConditions.DOOR_CLOSED.condition)) {
                                            map.put('model', bottomModel)
                                            map.put('y', 270)
                                        }
                                    } else if (it.contains(BlockStateConditions.DOOR_HINGE_RIGHT.condition)) {
                                        if (it.contains(BlockStateConditions.DOOR_OPEN.condition)) {
                                            map.put('model', bottomModel)
                                            map.put('y', 180)
                                        } else if (it.contains(BlockStateConditions.DOOR_CLOSED.condition)) {
                                            map.put('model', bottomModelRightHinge)
                                            map.put('y', 270)
                                        }
                                    }
                                } else if (it.contains(BlockStateConditions.DOOR_UPPER.condition)) {
                                    if (it.contains(BlockStateConditions.DOOR_HINGE_LEFT.condition)) {
                                        if (it.contains(BlockStateConditions.DOOR_OPEN.condition)) {
                                            map.put('model', topModelRightHinge)
                                        } else if (it.contains(BlockStateConditions.DOOR_CLOSED.condition)) {
                                            map.put('model', topModel)
                                            map.put('y', 270)
                                        }
                                    } else if (it.contains(BlockStateConditions.DOOR_HINGE_RIGHT.condition)) {
                                        if (it.contains(BlockStateConditions.DOOR_OPEN.condition)) {
                                            map.put('model', topModel)
                                            map.put('y', 180)
                                        } else if (it.contains(BlockStateConditions.DOOR_CLOSED.condition)) {
                                            map.put('model', topModelRightHinge)
                                            map.put('y', 270)
                                        }
                                    }
                                }

                            }
                            list.add(map)
                            if (map) {
                                statemap.put(it.toString().replace(" ", "").replace("[", "").replace("]", ""), list)
                            }
                        }

                        if (statemap) {
                            String json = Utilities.formatJson([variants: statemap])
                            blockstatefile.setText(json)
                        }
                    }
                    modelprefix = modid + ':' + AssetConstants.BLOCKTEXTURE.value + '/' + blockidentifier
                    Path doorModels = Paths.get(resourceDirectory.absolutePath, AssetConstants.ASSETS.value, modid, AssetConstants.MODELS_LITERAL.value, AssetConstants.BLOCKMODEL.value)

                    File doorTopModel = Paths.get(doorModels.toString(), blockidentifier + '_top.json').toFile()
                    doorTopModel.createNewFile()
                    doorTopModel.setText(Utilities.formatJson([parent: 'block/door_top', textures: [bottom: modelprefix + '_bottom', top: modelprefix + '_top']]))

                    File doorTopModelRightHinge = Paths.get(doorModels.toString(), blockidentifier + '_top_rh.json').toFile()
                    doorTopModelRightHinge.createNewFile()
                    doorTopModelRightHinge.setText(Utilities.formatJson([parent: 'block/door_top_rh', textures: [bottom: modelprefix + '_bottom', top: modelprefix + '_top']]))

                    File doorBottom = Paths.get(doorModels.toString(), blockidentifier + '_bottom.json').toFile()
                    doorBottom.createNewFile()
                    doorBottom.setText(Utilities.formatJson([parent: 'block/door_bottom', textures: [bottom: modelprefix + '_bottom', top: modelprefix + '_top']]))

                    File doorBottomRightHinge = Paths.get(doorModels.toString(), blockidentifier + '_bottom_rh.json').toFile()
                    doorBottomRightHinge.createNewFile()
                    doorBottomRightHinge.setText(Utilities.formatJson([parent: 'block/door_bottom_rh', textures: [bottom: modelprefix + '_bottom', top: modelprefix + '_top']]))

                    break
            }


            if (generateModels) {
                if (blockstate == BlockState.DIRECTIONAL.type)
                    itemModel.setText(Utilities.createBlockInventoryModel(modid, blockidentifier))
                else if (blockstate == BlockState.PANE.type)
                    itemModel.setText(Utilities.formatJson([parent: "item/generated", textures: [layer0: "$modid:blocks/$blockidentifier"]]))
                else
                    itemModel.setText(Utilities.createSimpleBlockItemModel(modid, blockidentifier))
            }


            if (version == '1.14') {
                int lootTableType = generateLootTable.getSelectionModel().getSelectedIndex();

                Path lootTable = Paths.get(resourceDirectory.absolutePath, AssetConstants.DATA.value, modid, AssetConstants.LOOT_TABLES.value, AssetConstants.BLOCKS_LITERAL.value, blockidentifier + '.json')
                def map = [:]
                if (lootTableType == 0) {
                    map = [type: 'minecraft:block', pools: [
                            [rolls     : 1, entries: [
                                    [type: 'minecraft:item', name: "$modid:$blockidentifier"]
                            ],
                             conditions: [
                                     [condition: 'minecraft:survives_explosion']
                             ]
                            ]
                    ]]
                } else if (lootTableType == 1) {
                    map = [type: 'minecraft:block', pools: [
                            [rolls     : 1, entries: [
                                    [type: 'minecraft:item', functions: [
                                            [function: 'minecraft:copy_nbt', source: 'block_entity', ops: [
                                                    [source: '', target: 'BlockEntityTag', op: 'replace']
                                            ]]
                                    ],
                                     name: "$modid:$blockidentifier"
                                    ]
                            ],
                             conditions: [
                                     [condition: 'minecraft:survives_explosion']
                             ]
                            ]
                    ]]
                }
                File f = Utilities.createJsonFile(lootTable, Utilities.formatJson(map))
                if (f != null && f.exists())
                    new Alert2(Alert.AlertType.INFORMATION, null, "Created $lootTable").show()
            }
            blockstatetab.setText(blockidentifier)
            if (blockstatefile.exists())
                blockstatetab.setTooltip(new Tooltip(blockstatefile.absolutePath))
            else if (modelWorld.exists())
                blockstatetab.setTooltip(new Tooltip(modelWorld.absolutePath))

        }

    }
}
