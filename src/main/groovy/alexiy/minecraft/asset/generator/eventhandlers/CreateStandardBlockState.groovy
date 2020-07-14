package alexiy.minecraft.asset.generator.eventhandlers

import alexiy.minecraft.asset.generator.MinecraftVersion
import alexiy.minecraft.assetgenerator.*
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.*
import javafx.stage.DirectoryChooser
import org.knowbase.Alert2
import org.knowbase.Hbox2
import org.knowbase.Vbox2

import java.nio.file.Paths

/**
 * Created on 3/4/20.
 */
class CreateStandardBlockState implements EventHandler<ActionEvent> {
    private MAG mag;

    CreateStandardBlockState(MAG mag) {
        this.mag = mag
    }

    @Override
    void handle(ActionEvent event) {
        TextField modidr = new TextField(MAG.lastModId)
        TextField blockidr = new TextField()
        TextField directionName = new TextField('facing')
        directionName.setVisible(false)
        blockidr.setTooltip(new Tooltip("Block identifier"))
        Label path = new Label(MAG.lastResourceFolder)
        Button setPath = new Button("Set output path")
        setPath.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            void handle(ActionEvent evt) {
                DirectoryChooser directoryChooser = new DirectoryChooser()
                directoryChooser.setInitialDirectory(new File(MAG.lastResourceFolder))
                def res = directoryChooser.showDialog(MAG.mainStage)
                if (res) {
                    path.setText(res.absolutePath)
                    MAG.lastResourceFolder = res.absolutePath
                }
            }
        })
        Button generate = new Button("Generate")
        ChoiceBox<MinecraftVersion> version = new ChoiceBox<>(FXCollections.observableArrayList(MinecraftVersion.values()))
        version.selectionModel.select(MinecraftVersion.values().find { it.version == MAG.lastMinecraftVersion })
        ChoiceBox<BlockState> choices = new ChoiceBox<>(FXCollections.observableArrayList(BlockState.values()))
        choices.selectionModel.select(BlockState.SIMPLE_BLOCK)
        Label description = new Label(BlockState.SIMPLE_BLOCK.description)
        Label parentModel = new Label(BlockModel.SINGLETEXTURE.value)
        CheckBox generateItemModel = new CheckBox("Generate item model")
        generateItemModel.setSelected(true)
        ChoiceBox<String> lootTable = new ChoiceBox<>(FXCollections.observableArrayList('None', 'Self', 'Save'))
        lootTable.getSelectionModel().select(1)
        choices.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            void handle(ActionEvent even) {
                directionName.setVisible(false)
                BlockState blockState = choices.getSelectionModel().getSelectedItem()
                BlockModel blockModel = blockState.defaultModel
                if (blockModel != null) {
                    description.setText(blockModel.description)
                    parentModel.setText(blockModel.value)
                } else {
                    description.setText(blockState.description)
                    parentModel.setText('')
                }
                switch (blockState) {
                    case BlockState.DIRECTIONAL:
                        directionName.setVisible(true)
                        break
                    default:
                        directionName.setVisible(false)
                        break
                }
            }
        })
        generate.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            void handle(ActionEvent evnt) {
                String modidentr = modidr.text
                String blockidenr = blockidr.text
                String directionname = directionName.text
                if (modidentr && blockidenr) {
                    BlockState parent = choices.selectionModel.getSelectedItem()
                    MinecraftVersion minecraftVersion = version.selectionModel.getSelectedItem()
                    File modelFile = Paths.get(path.text, AssetConstants.ASSETS.value, modidentr, AssetConstants.MODELS_LITERAL.value, AssetConstants.BLOCK_LITERAL.value, blockidenr + '.json').toFile()
                    String texturePath
                    switch (minecraftVersion) {
                        case MinecraftVersion.V1_12:
                            texturePath = "$modidentr:$AssetConstants.BLOCKTEXTURE.value/$blockidenr"
                            break
                        case MinecraftVersion.V1_15:
                            texturePath = "$modidentr:$AssetConstants.BLOCK_LITERAL.value/$blockidenr"
                            break
                    }
                    String blockstateContent = '{}'
                    switch (parent) {
                        case BlockState.SIMPLE_BLOCK:
                        case BlockState.CROSS:
                        case BlockState.DIFFERENT_SIDES:
                        case BlockState.TOP_BOTTOM_SIDE:
                        case BlockState.COLUMN:
                            blockstateContent = createSimpleBlockstate(minecraftVersion, modidentr, blockidenr)
                            break
                        case BlockState.DIRECTIONAL:
                            def map = [:]
                            if (directionname) {
                                Direction.values().each {
                                    String direction = directionname + '=' + it.name
                                    String axis = it.around.toString()
                                    if (minecraftVersion == MinecraftVersion.V1_12)
                                        map.put(direction, [model: "$modidentr:$blockidenr", (axis): it.rotation])
                                    else if (minecraftVersion == MinecraftVersion.V1_15)
                                        map.put(direction, [model: "$modidentr:$AssetConstants.BLOCK_LITERAL.value/$blockidenr", (axis): it.rotation])
                                }
                            }
                            map = [variants: map]
                            blockstateContent = Utilities.formatJson(map)
                            break
                        case BlockState.HORIZONTAL:
                            def map = [:]
                            def horizontals = Direction.getHorizontals()
                            horizontals.each {
                                String key = 'facing=' + it.name
                                String axis = it.around.toString()
                                if (minecraftVersion == MinecraftVersion.V1_15) {
                                    map.put(key, [model: "$modidentr:$AssetConstants.BLOCK_LITERAL.value/$blockidenr", (axis): it.rotation])
                                }
                            }
                            map = [variants: map]
                            blockstateContent = Utilities.formatJson(map)
                            break
                    }
                    if (blockstateContent) {
                        File file = new File("$path.text/$AssetConstants.ASSETS.value/$modidentr/$AssetConstants.BLOCKSTATES.value/$blockidenr" + '.json')
                        if (file.exists()) {
                            new Alert2(Alert.AlertType.INFORMATION, "$file exists", ButtonType.OK).show()
                        } else {
                            if (file.createNewFile()) {
                                file.setText(blockstateContent)
                                new Alert2(Alert.AlertType.INFORMATION, "Created blockstate $file", ButtonType.OK).show()
                            }
                        }
                    }
                    if (texturePath) {
                        if (modelFile.createNewFile()) {
                            String modelContent = '{}'
                            switch (parent) {
                                case BlockState.SIMPLE_BLOCK:
                                    modelContent = Utilities.formatJson([parent: BlockModel.SINGLETEXTURE.value, textures: [all: texturePath]])
                                    break
                                case BlockState.CROSS:
                                    modelContent = Utilities.formatJson([parent: BlockModel.CROSS.value, textures: [cross: texturePath]])
                                    break
                                case BlockState.DIFFERENT_SIDES:
                                    modelContent = Utilities.formatJson([parent  : BlockModel.DIFFERENTSIDES.value,
                                                                         textures: [particle: texturePath + '_up', east: texturePath + '_east',
                                                                                    west    : texturePath + '_west', north: texturePath + '_north',
                                                                                    south   : texturePath + '_south', up: texturePath + '_up',
                                                                                    down    : texturePath + '_down']])
                                    break
                                case BlockState.COLUMN:
                                    modelContent = Utilities.formatJson([parent: BlockModel.COLUMN.value, textures: [side: texturePath,
                                                                                                                     end : texturePath + '_end']])
                                    break
                                case BlockState.TOP_BOTTOM_SIDE:
                                    modelContent = Utilities.formatJson([parent  : BlockModel.DIFFERENT_TOPANDBOTTOM.value,
                                                                         textures: [top : texturePath + '_top', bottom: texturePath + '_bottom',
                                                                                    side: texturePath]])
                                    break
                                case BlockState.DIRECTIONAL:
                                    modelContent = Utilities.formatJson([parent  : BlockModel.DIRECTIONAL.value,
                                                                         textures: [bottom: "$texturePath" + '_bottom',
                                                                                    side  : "$texturePath" + '_side', platform: "$texturePath"]])
                                    break
                            }
                            modelFile.setText(modelContent)
                            new Alert2(Alert.AlertType.INFORMATION, "Created model $modelFile", ButtonType.OK).show()
                        }

                        if (generateItemModel.isSelected()) {
                            File itemModel = Paths.get(path.text, AssetConstants.ASSETS.value, modidentr, AssetConstants.MODELS_LITERAL.value, AssetConstants.ITEM_LITERAL.value, blockidenr + '.json').toFile()
                            if (itemModel.createNewFile()) {
                                itemModel.setText(Utilities.formatJson([parent: "$modidentr:block/$blockidenr"]))
                                new Alert2(Alert.AlertType.INFORMATION, "Created item model $itemModel", ButtonType.OK).show()
                            }
                        }
                        String loottable = lootTable.getSelectionModel().getSelectedItem();
                        if (loottable != 'None') {
                            File loot = Paths.get(path.text, AssetConstants.DATA.value, modidentr, AssetConstants.LOOT_TABLES.value, AssetConstants.BLOCKS_LITERAL.value, blockidenr + '.json').toFile()
                            if (loot.createNewFile()) {
                                String table = '{}'
                                if (loottable == 'Self') {
                                    table = Utilities.formatJson([type: "minecraft:block", pools: [
                                            [
                                                    rolls     : 1,
                                                    entries   : [
                                                            [
                                                                    type: "minecraft:item",
                                                                    name: "$modidentr:$blockidenr"
                                                            ]
                                                    ],
                                                    conditions: [
                                                            [condition: "minecraft:survives_explosion"]
                                                    ]
                                            ]
                                    ]])
                                }
                                loot.setText(table)
                                new Alert2(Alert.AlertType.INFORMATION, "Created default loot table $loot", ButtonType.OK).show()
                            }
                        }
                    }
                    MAG.lastMinecraftVersion = minecraftVersion.version
                    println(path.text)
                    println(minecraftVersion)
                    println(parent.name())
                }
            }
        })
        Vbox2 vbox2 = new Vbox2(modidr, blockidr, directionName, new Hbox2(setPath, path), version, new Hbox2(choices, parentModel), description, generateItemModel, lootTable, generate)
        Tab tab = new Tab("$modidr.text:$blockidr.text", vbox2)
        mag.tabPane.getTabs().add(tab)
    };

    private static String createSimpleBlockstate(MinecraftVersion version, String mod, String blockId) {
        String content
        switch (version) {
            case MinecraftVersion.V1_12:
                content = Utilities.formatJson([variants: [normal: [model: "$mod:$blockId"]]])
                break
            case MinecraftVersion.V1_15:
                content = Utilities.formatJson([variants: [(""): [model: "$mod:$AssetConstants.BLOCK_LITERAL.value/$blockId"]]])
                break
        }
        return content
    }
}
