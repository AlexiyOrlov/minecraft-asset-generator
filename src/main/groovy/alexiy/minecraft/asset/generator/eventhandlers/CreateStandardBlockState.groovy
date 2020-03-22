package alexiy.minecraft.asset.generator.eventhandlers

import alexiy.minecraft.asset.generator.MinecraftVersion
import alexiy.minecraft.assetgenerator.AssetConstants
import alexiy.minecraft.assetgenerator.BlockModels
import alexiy.minecraft.assetgenerator.BlockState
import alexiy.minecraft.assetgenerator.MAG
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.*
import javafx.stage.DirectoryChooser
import org.knowbase.Hbox2
import org.knowbase.Vbox2

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
        ChoiceBox<String> version = new ChoiceBox<>(FXCollections.observableArrayList(MinecraftVersion.values()*.version)) as ChoiceBox<String>
        version.selectionModel.select(MAG.lastMinecraftVersion)
        ChoiceBox<BlockState> choices = new ChoiceBox<>(FXCollections.observableArrayList(BlockState.values()))
        choices.selectionModel.select(BlockState.SIMPLE_BLOCK)
        Label description = new Label(BlockState.SIMPLE_BLOCK.description)
        Label parentModel = new Label(BlockModels.BLOCKSINGLETEXTURE.value)
        choices.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            void handle(ActionEvent even) {
                BlockState blockState = choices.getSelectionModel().getSelectedItem()
                //BlockState.values().find({ it -> it.type == choices.selectionModel.getSelectedItem() })
                description.setText(blockState.description)
                switch (blockState) {
                    case BlockState.COLUMN:
                        parentModel.setText(BlockModels.BLOCKCOLUMN.value)
                        break
                    case BlockState.SIMPLE_BLOCK:
                        parentModel.setText(BlockModels.BLOCKSINGLETEXTURE.value)
                        break
                    case BlockState.CROSS:
                        parentModel.setText(BlockModels.CROSS.value)
                        break
                    case BlockState.DIFFERENT_SIDES:
                        parentModel.setText(BlockModels.BLOCKDIFFERENTSIDES.value)
                        break
                    case BlockState.TOP_BOTTOM_SIDE:
                        parentModel.setText(BlockModels.BLOCKDIFFERENTTOPANDBOTTOM.value)
                        break
                    case BlockState.DIRECTIONAL:
                        parentModel.setText(BlockModels.BLOCKDIRECTIONAL.value)
                        break
                    default: parentModel.setText('')
                }
            }
        })
        generate.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            void handle(ActionEvent evnt) {
                String fullidentifier = "$modidr.text:$blockidr.text"
                if (fullidentifier) {
                    BlockState parent = choices.selectionModel.getSelectedItem()
                    String v = version.selectionModel.getSelectedItem()
                    String texturePath = ''
                    switch (v) {
                        case MinecraftVersion.V1_12.version:
                            texturePath = "$modidr.text:$AssetConstants.BLOCKTEXTURE.value/$blockidr.text"
                            break
                        case MinecraftVersion.V1_15.version:
                            texturePath = "$modidr.text:$AssetConstants.BLOCK_LITERAL.value/$blockidr.text"
                            break
                    }
                    switch (parent) {
                        case BlockState.SIMPLE_BLOCK:
                            break
                    }
                    println(path.text)
                    println(parentModel.text)
                    println(v)
                    MAG.lastMinecraftVersion = v
                    println(parent.name())
                }
            }
        })
        Vbox2 vbox2 = new Vbox2(modidr, blockidr, new Hbox2(setPath, path), version, new Hbox2(choices, parentModel), description, generate)
        Tab tab = new Tab("$modidr.text:$blockidr.text", vbox2)
        mag.tabPane.getTabs().add(tab)
    }
}
