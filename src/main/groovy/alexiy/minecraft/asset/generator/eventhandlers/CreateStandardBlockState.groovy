package alexiy.minecraft.asset.generator.eventhandlers

import alexiy.minecraft.asset.generator.MinecraftVersion
import alexiy.minecraft.assetgenerator.BlockModels
import alexiy.minecraft.assetgenerator.BlockStates
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
        ChoiceBox<String> choices = new ChoiceBox<>(FXCollections.observableArrayList(BlockStates.values()*.type)) as ChoiceBox<String>
        choices.selectionModel.select(BlockStates.SIMPLE_BLOCK.type)
        Label description = new Label(BlockStates.SIMPLE_BLOCK.description)
        Label parentModel = new Label(BlockModels.BLOCKSINGLETEXTURE.value)
        choices.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            void handle(ActionEvent even) {
                BlockStates blockState = BlockStates.values().find({ it -> it.type == choices.selectionModel.getSelectedItem() })
                description.setText(blockState.description)
                switch (blockState) {
                    case BlockStates.COLUMN:
                        parentModel.setText(BlockModels.BLOCKCOLUMN.value)
                        break
                    case BlockStates.SIMPLE_BLOCK:
                        parentModel.setText(BlockModels.BLOCKSINGLETEXTURE.value)
                        break
                    case BlockStates.CROSS:
                        parentModel.setText(BlockModels.CROSS.value)
                        break
                    case BlockStates.DIFFERENT_SIDES:
                        parentModel.setText(BlockModels.BLOCKDIFFERENTSIDES.value)
                        break
                    case BlockStates.TOP_BOTTOM_SIDE:
                        parentModel.setText(BlockModels.BLOCKDIFFERENTTOPANDBOTTOM.value)
                        break
                    case BlockStates.DIRECTIONAL:
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
                    String parent = choices.selectionModel.getSelectedItem()
                    String v = version.selectionModel.getSelectedItem()
                    println(path.text)
                    println(parentModel.text)
                    println(v)
                    MAG.lastMinecraftVersion = v
                    println(parent)
                }
            }
        })
        Vbox2 vbox2 = new Vbox2(modidr, blockidr, new Hbox2(setPath, path), version, new Hbox2(choices, parentModel), description, generate)
        Tab tab = new Tab("$modidr.text:$blockidr.text", vbox2)
        mag.tabPane.getTabs().add(tab)
    }
}
