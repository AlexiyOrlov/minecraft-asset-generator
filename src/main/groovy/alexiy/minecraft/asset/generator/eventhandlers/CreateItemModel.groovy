package alexiy.minecraft.asset.generator.eventhandlers

import alexiy.minecraft.asset.generator.MAG
import alexiy.minecraft.asset.generator.MinecraftVersion
import alexiy.minecraft.assetgenerator.AssetConstants
import alexiy.minecraft.assetgenerator.ItemModel
import alexiy.minecraft.assetgenerator.Utilities
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.*
import javafx.stage.DirectoryChooser
import org.knowbase.Alert2
import org.knowbase.Hbox2
import org.knowbase.Vbox2

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Created on 2/13/20.
 */
class CreateItemModel implements EventHandler<ActionEvent> {
    private MAG mag

    CreateItemModel(MAG mag) {
        this.mag = mag
    }

    @Override
    void handle(ActionEvent event) {
        File outputPath = new File(MAG.lastResourceFolder)
        Label resourcePath = new Label(MAG.lastResourceFolder)

        TextField textField = new TextField(MAG.lastModId)
        textField.setPromptText('Full item identifier')

        ChoiceBox<String> choiceBox = new ChoiceBox<>(FXCollections.observableArrayList(MinecraftVersion.values()*.version))
        choiceBox.selectionModel.select(MAG.lastMinecraftVersion)
        choiceBox.setOnAction(new VersionSelector(choiceBox))
        ChoiceBox<ItemModel> variants = new ChoiceBox<>(FXCollections.observableArrayList(ItemModel.values()))
        variants.selectionModel.select(ItemModel.SIMPLE_ITEM)

        Button selectResourceFolder = new Button('Set output folder:')
        selectResourceFolder.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            void handle(ActionEvent ev) {
                DirectoryChooser directoryChooser = new DirectoryChooser()
                directoryChooser.setInitialDirectory(new File(MAG.lastResourceFolder))
                def selection = directoryChooser.showDialog(MAG.mainStage)
                if (selection) {
                    outputPath = selection
                    resourcePath.setText(selection.canonicalPath)
                    MAG.lastResourceFolder = selection.canonicalPath
                }
            }
        })
        Button generate = new Button('Generate')
        generate.setOnAction(evt -> {
            if (outputPath && textField.text) {
                String string = textField.getText()
                if (!string.contains(":"))
                    string = "minecraft:$string"
                String[] path = string.split(':')
                String modidentifier = path[0]
                String itemid = path[1]
                String minecraftVersion = choiceBox.selectionModel.getSelectedItem()
                ItemModel vart = variants.getSelectionModel().getSelectedItem()
                Path modelFile = Paths.get(outputPath.canonicalPath, AssetConstants.ASSETS.value, modidentifier, AssetConstants.MODELS_LITERAL.value, Utilities.ITEM, itemid + '.json')
                if (Files.exists(modelFile)) {
                    new Alert2(Alert.AlertType.ERROR, "File $string exists already", ButtonType.OK).show()
                } else {
                    Files.createFile(modelFile)
                    String content
                    String textureFolder = null
                    if (minecraftVersion == MinecraftVersion.V1_12.version) {
                        textureFolder = Utilities.ITEMS
                    } else if (minecraftVersion == MinecraftVersion.V1_15.version) {
                        textureFolder = Utilities.ITEM
                    }
                    content = Utilities.formatJson([parent: vart.value, textures: [layer0: "$modidentifier:$textureFolder/$itemid"]])
                    Files.write(modelFile, Collections.singleton(content))
                    MAG.lastModId = modidentifier
                    new Alert2(Alert.AlertType.INFORMATION, "Created ${modelFile.toString()}", ButtonType.OK).show()
                }
            }
        })
        Hbox2 resourceContainer = new Hbox2(selectResourceFolder, resourcePath)
        Vbox2 content = new Vbox2(textField, choiceBox, variants, resourceContainer, generate)
        Tab tab = new Tab("Item model ($MAG.lastModId)", content)
        mag.tabPane.getTabs().add(tab)

    }
}

