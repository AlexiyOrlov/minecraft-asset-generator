package alexiy.minecraft.asset.generator.eventhandlers

import alexiy.minecraft.asset.generator.MinecraftVersion
import alexiy.minecraft.assetgenerator.AssetConstants
import alexiy.minecraft.assetgenerator.MAG
import alexiy.minecraft.assetgenerator.Recipes
import alexiy.minecraft.assetgenerator.Utilities
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
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

    public static final String RESULT = 'result', TYPE = 'type'
    private MAG mag
    static final String RECIPE = 'recipe'

    CreateRecipe(MAG mag) {
        this.mag = mag
    }

    @Override
    void handle(ActionEvent event) {
        Path outputPath = Paths.get(MAG.lastResourceFolder)
        Label label = new Label(MAG.lastResourceFolder)
        GridPane metadata = new GridPane()
        for (row in 0..2) {
            for (column in 0..2) {
                TextField mt = new TextField('0')
                mt.setMaxWidth(40)
                mt.setTooltip(new Tooltip('Metadata'))
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
                TextField textField = new TextField()
                textField.setTooltip(new Tooltip('Ingredient name'))
                gridPaneShapeless.add(textField, column, row)
            }
        }
        GridPane gridPaneShaped = new GridPane()
        gridPaneShaped.setHgap(6)
        gridPaneShaped.setVgap(6)
        for (row in 0..2) {
            for (column in 0..2) {
                TextField textField = new TextField()
                textField.setMaxWidth(30)
                textField.setTooltip(new Tooltip('Key'))
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
                if (!string.contains(':')) {
                    string = "minecraft:$string"
                    MAG.lastModId = 'minecraft'
                } else
                    MAG.lastModId = string.substring(0, string.indexOf(':'))
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
                Button extractKeys = new Button('Extract recipe keys')
                Vbox2 keycontainer = new Vbox2()
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
                                extractKeys.setVisible(false)
                                keycontainer.setVisible(false)
                                break
                            case Recipes.SHAPED:
                                gridPaneShapeless.setVisible(false)
                                gridPaneShaped.setVisible(true)
                                extractKeys.setVisible(true)
                                keycontainer.setVisible(true)
                                break
                        }
                    }
                })
                ChoiceBox<String> versionChoice = new ChoiceBox<>(FXCollections.observableArrayList(MinecraftVersion.values()*.version)) as ChoiceBox<String>
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
                resultCount.setTooltip(new Tooltip('Result stack size'))
                resultCount.setMaxWidth(50)
                Button create = new Button('Generate')
                create.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    void handle(ActionEvent evt) {
                        Map<String, Object> fileContent = [:]
                        String resultid = resultItem.getText()
                        def parts = resultid.split(':')
                        Recipes recipeType = recipesChoiceBox.getSelectionModel() getSelectedItem()
                        Path assetRoot = null
                        String version = versionChoice.getSelectionModel().getSelectedItem()

                        switch (version) {
                            case MinecraftVersion.V1_12.version: assetRoot = Paths.get('assets')
                                break
                            case MinecraftVersion.V1_15.version: assetRoot = Paths.get('data')
                                break
                        }
                        if (recipeType == Recipes.SHAPELESS) {
                            fileContent.put(TYPE, 'minecraft:crafting_shapeless')
                            ObservableList<TextField> names = gridPaneShapeless.getChildren() as ObservableList<TextField>
                            def ingredients = []
                            for (name in names) {
                                String str = name.getText()
                                if (str) {
                                    if (!str.contains(':'))
                                        str = "minecraft:$str"
                                    if (version == MinecraftVersion.V1_12.version) {
                                        TextField meta = metadata.childrenUnmodifiable.get(names.indexOf(name)) as TextField
                                        if (meta.text && meta.text.isInteger())
                                            ingredients.add([item: str, data: Integer.parseInt(meta.text)])
                                    } else {
                                        ingredients.add([item: str])
                                    }
                                }
                            }
                            if (!ingredients.isEmpty()) {
                                fileContent.put('ingredients', ingredients)
                            } else {
                                new Alert2(Alert.AlertType.ERROR, 'No ingredients', ButtonType.OK).show()
                            }
                            Utilities.createJsonFile(Paths.get(label.getText(), assetRoot.toString(), parts[0], parts[1] + '.json'), Utilities.formatJson(fileContent))
                        } else if (recipeType == Recipes.SHAPED) {
                            fileContent.put(TYPE, 'minecraft:crafting_shaped')
                            ObservableList<TextField> keys = gridPaneShaped.getChildrenUnmodifiable() as ObservableList<TextField>
                            ArrayList<String> keylist = new ArrayList(9)
                            for (int i = 0; i < keys.size(); i++) {
                                def field = keys.get(i)
                                String value = field.text
                                if (value.isEmpty())
                                    field.setText(' ')
                            }
                            keylist.addAll(keys*.text)
                            def line1 = keylist.subList(0, 3)
                            def line2 = keylist.subList(3, 6)
                            def line3 = keylist.subList(6, 9)
                            String joined1 = String.join('', line1)
                            String joined2 = String.join('', line2)
                            String joined3 = String.join('', line3)
                            fileContent.put('pattern', [joined1, joined2, joined3])
                            Set<String> uniques = new HashSet<>(keylist)
                            ObservableList<Node> nodes = keycontainer.childrenUnmodifiable
                            if (uniques && nodes) {
//                               List<TextField> textfield= nodes.findAll({it instanceof TextField}) as List<TextField>
                                def keymap = [:]
                                boolean hasEmptyValues = false
                                l2:
                                for (u in uniques) {
                                    l:
                                    for (n in nodes) {
                                        if (n instanceof HBox) {
                                            HBox hBox = n
                                            for (o in hBox.childrenUnmodifiable) {
                                                if (o instanceof Label) {
                                                    Label labl = o
                                                    if (labl.text == u) {
                                                        int index = hBox.childrenUnmodifiable.indexOf(labl)
                                                        TextField textField = hBox.childrenUnmodifiable.get(index + 1) as TextField
                                                        String ingred = textField.text
                                                        final String path = ingred
                                                        if (ingred) {
                                                            if (ingred && !ingred.contains(':'))
                                                                ingred = "minecraft:$ingred"
                                                            def input
                                                            if (version == MinecraftVersion.V1_12.version) {
                                                                TextField textf = metadata.getChildrenUnmodifiable().get(index) as TextField
                                                                input = [item: ingred, data: textf.text.toInteger()]
                                                            } else if (path.startsWith('#')) {  //place tag
                                                                if (!path.contains(':')) {
                                                                    input = [tag: 'minecraft:' + path.substring(1)]
                                                                } else
                                                                    input = [tag: path.substring(1)]
                                                            } else
                                                                input = [item: ingred]
                                                            keymap.put(u, input)
                                                            break l
                                                        } else {
                                                            hasEmptyValues = true
                                                            break l2
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if (hasEmptyValues)
                                    new Alert2(Alert.AlertType.ERROR, 'Detected empty value for recipe key', ButtonType.OK).show()
                                else {
                                    fileContent.put('key', keymap)
                                }
                            } else {
                                new Alert2(Alert.AlertType.ERROR, 'No key mapping defined', ButtonType.OK).show()
                                return
                            }
                        }

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
                            fileContent.put(RESULT, [item: resultid, count: amountt, data: 0])
                        else
                            fileContent.put(RESULT, [item: resultid, count: amountt])

                        String outputRoot = null
                        if (version == MinecraftVersion.V1_12.version)
                            outputRoot = AssetConstants.ASSETS.value
                        else if (version == MinecraftVersion.V1_15.version)
                            outputRoot = AssetConstants.DATA.value
                        Path path = Paths.get(label.getText(), outputRoot, parts[0], 'recipes', parts[1] + '.json')
                        Utilities.createJsonFile(path, Utilities.formatJson(fileContent))

                    }
                })
                extractKeys.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    void handle(ActionEvent e) {
                        ObservableList<TextField> fields = gridPaneShaped.childrenUnmodifiable as ObservableList<TextField>
                        Set<String> uniqueKyes = []

                        uniqueKyes.addAll(fields*.text as Set)
                        uniqueKyes.removeAll('')
                        if (uniqueKyes) {
                            keycontainer.children.clear()
                            for (k in uniqueKyes) {
                                TextField ingredient = new TextField()
                                ingredient.setTooltip(new Tooltip('Ingredient item name'))
                                keycontainer.children.add(new Hbox2(new Label(k), ingredient))
                            }
                        }
                    }
                })
                Vbox2 container = new Vbox2(resultItem, new Hbox2(selectResourceFolder, label), versionChoice, recipesChoiceBox, new Hbox2(gridPaneShaped, metadata, extractKeys, keycontainer), gridPaneShapeless, resultCount, create)
                Tab tab = new Tab("Recipe ($string)", container)
                mag.tabPane.getTabs().add(tab)

            }
        }
    }
}
