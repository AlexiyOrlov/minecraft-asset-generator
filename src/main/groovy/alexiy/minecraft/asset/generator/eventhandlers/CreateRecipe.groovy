package alexiy.minecraft.asset.generator.eventhandlers

import alexiy.minecraft.asset.generator.MinecraftVersion
import alexiy.minecraft.assetgenerator.MAG
import alexiy.minecraft.assetgenerator.Recipes
import alexiy.minecraft.assetgenerator.Utilities
import com.google.common.collect.TreeBasedTable
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.stage.DirectoryChooser
import org.knowbase.Alert2
import org.knowbase.Hbox2
import org.knowbase.Vbox2

import java.nio.file.Path
import java.nio.file.Paths

/**
 * Created on 2/13/20.
 */
class CreateRecipe implements EventHandler<ActionEvent> {

    private MAG mag

    CreateRecipe(MAG mag) {
        this.mag = mag
    }

    @Override
    void handle(ActionEvent event) {
        Path outputPath = Paths.get(MAG.lastResourceFolder)
        Label label = new Label(MAG.lastResourceFolder)
        TreeBasedTable<Integer, Integer, String> shapedInputs = TreeBasedTable.create()
        GridPane metadata = new GridPane()
        for (row in 0..2) {
            for (column in 0..2) {
                TextField mt = new TextField('0')
                mt.setMaxWidth(40)
                metadata.add(mt, column, row)
            }
        }
        if (MAG.lastMinecraftVersion != MinecraftVersion.V1_12.version)
            metadata.setVisible(false)
        GridPane gridPaneShapeless = new GridPane()
        gridPaneShapeless.setVgap(6)
        gridPaneShapeless.setHgap(6)
        for (row in 0..2) {
            for (column in 0..2) {
                gridPaneShapeless.add(new TextField(), column, row)
            }
        }
        GridPane gridPaneShaped = new GridPane()
        gridPaneShaped.setHgap(6)
        gridPaneShaped.setVgap(6)
        for (row in 0..2) {
            for (column in 0..2) {
                TextField textField = new TextField()
                textField.setMaxWidth(30)
                gridPaneShaped.add(textField, column, row)
            }
        }
        gridPaneShapeless.setVisible(false)
        TextInputDialog identifier = new TextInputDialog(MAG.lastModId)
        identifier.setHeaderText('Write full item identifier')
        def result = identifier.showAndWait()
        if (result.isPresent()) {
            String string = result.get()
            if (string) {
                if (!string.contains(':'))
                    string = "minecraft:$string"
                TextField resultItem = new TextField(string)
                resultItem.setPromptText('Full recipe identifier')
                Button selectResourceFolder = new Button('Set output folder:')
                selectResourceFolder.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    void handle(ActionEvent ev) {
                        DirectoryChooser directoryChooser = new DirectoryChooser()
                        directoryChooser.setInitialDirectory(new File(MAG.lastResourceFolder))
                        def selection = directoryChooser.showDialog(MAG.mainStage)
                        if (selection) {
                            outputPath = selection.toPath()
                            label.setText(selection.canonicalPath)
                            MAG.lastResourceFolder = selection.canonicalPath
                        }
                    }
                })
                ChoiceBox<Recipes> recipesChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(Recipes.values()))
                recipesChoiceBox.selectionModel.select(Recipes.SHAPED)
                recipesChoiceBox.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    void handle(ActionEvent e) {
                        Recipes recipes = recipesChoiceBox.selectionModel.getSelectedItem()
                        switch (recipes) {
                            case Recipes.SHAPELESS:
                                gridPaneShaped.setVisible(false)
                                gridPaneShapeless.setVisible(true)
                                break
                            case Recipes.SHAPED:
                                gridPaneShapeless.setVisible(false)
                                gridPaneShaped.setVisible(true)
                                break
                        }
                    }
                })
                ChoiceBox<String> versionChoice = new ChoiceBox<>(FXCollections.observableArrayList(MinecraftVersion.values()*.version))
                versionChoice.selectionModel.select(MAG.lastMinecraftVersion)
                versionChoice.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    void handle(ActionEvent evnt) {
                        if (versionChoice.getSelectionModel().getSelectedItem() == MinecraftVersion.V1_12.version)
                            metadata.setVisible(true)
                        else
                            metadata.setVisible(false)
                        MAG.lastMinecraftVersion = versionChoice.getSelectionModel().getSelectedItem()
                    }
                })
                TextField resultCount = new TextField('1')
                resultCount.setPromptText('Result stack size')
                resultCount.setMaxWidth(50)
                Button create = new Button('Generate')
                create.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    void handle(ActionEvent evt) {
                        Map<String, Object> fileContent = [:]
                        Recipes recipeType = recipesChoiceBox.getSelectionModel() getSelectedItem()
                        if (recipeType == Recipes.SHAPELESS) {
                            fileContent.put('type', 'minecraft:crafting_shapeless')
                            ObservableList<TextField> keys = gridPaneShapeless.getChildren() as ObservableList<TextField>
                            def ingredients = []
                            String version = versionChoice.getSelectionModel().getSelectedItem()
                            for (field in keys) {
                                String str = field.getText()
                                if (str) {
                                    if (!str.contains(':'))
                                        str = "minecraft:$str"
                                    if (version == MinecraftVersion.V1_12.version) {
                                        TextField meta = metadata.childrenUnmodifiable.get(keys.indexOf(field)) as TextField
                                        if (meta.text && meta.text.isInteger())
                                            ingredients.add([item: str, data: Integer.parseInt(meta.text)])
                                    } else {
                                        ingredients.add([item: str])
                                    }
                                }
                            }
                            if (!ingredients.isEmpty()) {
                                fileContent.put('ingredients', ingredients)
                                String amount = resultCount.getText()
                                int amountt = 1
                                if (amount.isInteger()) {
                                    amountt = Integer.parseInt(amount)
                                    if (amountt > 64)
                                        amountt = 64
                                    else if (amountt < 1)
                                        amountt = 1
                                }
                                if (version == MinecraftVersion.V1_12.version)
                                    fileContent.put('result', [item: resultItem.getText(), count: amountt, data: 0])
                                else
                                    fileContent.put('result', [item: resultItem.getText(), count: amountt])
                            } else {
                                new Alert2(Alert.AlertType.ERROR, 'No ingredients', ButtonType.OK).show()
                            }
                        }
                        println(Utilities.formatJson(fileContent))
                    }
                })
                Vbox2 container = new Vbox2(resultItem, new Hbox2(selectResourceFolder, label), versionChoice, recipesChoiceBox, new Hbox2(gridPaneShaped, metadata), gridPaneShapeless, resultCount, create)
                Tab tab = new Tab("Recipe ($string)", container)
                mag.tabPane.getTabs().add(tab)

            }
        }
    }
}
