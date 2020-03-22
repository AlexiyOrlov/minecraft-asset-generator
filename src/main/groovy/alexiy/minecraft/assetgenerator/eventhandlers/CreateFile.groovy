package alexiy.minecraft.assetgenerator.eventhandlers

import alexiy.minecraft.assetgenerator.*
import com.google.common.collect.Sets
import com.google.common.collect.TreeBasedTable
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import javafx.util.Callback

import java.nio.file.Paths

import static alexiy.minecraft.assetgenerator.AssetConstants.BLOCKSTATES

/**
 * Created on 2/10/20.
 */
class CreateFile implements EventHandler<ActionEvent> {
    TabPane tabPane
    Stage primaryStage

    CreateFile(TabPane tabPane, Stage stage) {
        this.tabPane = tabPane
        primaryStage = stage
    }


    @Override
    void handle(ActionEvent event) {
        Dialog<RadioButton> dialog = new Dialog()
        dialog.setTitle('Select a variant')

        RadioButton itemmodel = new RadioButton('Item model')
        RadioButton recipe = new RadioButton('Recipe')
        RadioButton blockState = new RadioButton('Blockstate')
        RadioButton customBlockState = new RadioButton('Custom blockstate')
        VBox vBox = new VBox(6, itemmodel, recipe, blockState, customBlockState)
        dialog.getDialogPane().setContent(vBox)
        ToggleGroup group = new ToggleGroup()
        group.toggles.addAll(itemmodel, recipe, blockState, customBlockState)

        group.selectToggle(itemmodel)
        dialog.dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)
        dialog.setResultConverter(new Callback<ButtonType, RadioButton>() {
            @Override
            RadioButton call(ButtonType param) {
                if (param == ButtonType.OK) {
                    return group.selectedToggle as RadioButton
                }
                return null
            }
        })
        ChoiceBox<String> minecraftversion = new ChoiceBox<>(FXCollections.observableArrayList("1.12", "1.14"));
        def opt = dialog.showAndWait()
        if (opt.isPresent()) {
            RadioButton radioButton = opt.get() as RadioButton
            TextField modidentifier = new TextField(JAG.lastModid)
            modidentifier.setPromptText('Mod identifier')
            if (radioButton == itemmodel) {
                ChoiceBox<String> parentmodels = new ChoiceBox<>(FXCollections.observableArrayList(ItemModel.values().value))
                parentmodels.selectionModel.select(1)
                TextField objectidentifier
                objectidentifier = new TextField()
                objectidentifier.setPromptText('Item identifier')
                Button generate = new Button('Generate model file')
                VBox content = new VBox(6, minecraftversion, parentmodels, modidentifier, objectidentifier, generate)
                Tab itemtab = new Tab('Item model', content)
                generate.setOnAction(new ItemModelGenerator(parentmodels, modidentifier, objectidentifier, minecraftversion, itemtab))
                tabPane.tabs.add(itemtab)
            } else if (radioButton == customBlockState) {

                DirectoryChooser directoryChooser = new DirectoryChooser()
                if (JAG.resourceDirectory.parentFile)
                    directoryChooser.setInitialDirectory(JAG.resourceDirectory.parentFile)
                else directoryChooser.setInitialDirectory(JAG.resourceDirectory)
                directoryChooser.setTitle("Choose the resource directory")
                def optional = directoryChooser.showDialog(primaryStage)
                if (optional) {
                    JAG.resourceDirectory = optional
                    TextInputDialog registryName = new TextInputDialog(JAG.lastModid + ':')
                    registryName.setHeaderText('Write a registry name')
                    def o = registryName.showAndWait()
                    if (o.isPresent()) {
                        String rname = o.get()
                        if (rname.matches('[a-z0-9_]+:[a-z0-9_]+')) {
                            String modid = rname.substring(0, rname.indexOf(':'))
                            String blockid = rname.substring(rname.indexOf(':') + 1)
                            TextInputDialog textInputDialog = new TextInputDialog()
                            textInputDialog.setHeaderText("Write comma-separated names of block's properties in the same order")
                            def result = textInputDialog.showAndWait()
                            if (result.isPresent()) {
                                String string = result.get()
                                String[] propertyNames = string.split(',')
                                if (propertyNames) {
                                    Dialog<Object> propValues = new Dialog<>()
                                    DialogPane dialogPane = propValues.getDialogPane()
                                    GridPane gridPane = new GridPane()
                                    gridPane.setHgap(6)
                                    gridPane.setVgap(6)
//                                    println(propertyNames)
                                    propertyNames.eachWithIndex { String entry, int i ->
                                        gridPane.add(new Label(entry), 0, i)
                                        gridPane.add(new TextField(), 1, i)
                                    }
                                    dialogPane.setContent(gridPane)
                                    dialogPane.buttonTypes.addAll(ButtonType.APPLY, ButtonType.CANCEL)
                                    propValues.setHeaderText('Write possible values per property, comma-separated')
                                    propValues.setResultConverter(new Callback<ButtonType, Object>() {
                                        @Override
                                        Object call(ButtonType param) {
                                            if (param == ButtonType.APPLY) {
                                                List<TextField> textfields = gridPane.children.findAll {
                                                    it instanceof TextField
                                                } as List<TextField>
                                                List<HashSet<String>> combinations = []
                                                for (int i = 0; i < textfields.size(); i++) {
                                                    String values = textfields.get(i).text
                                                    HashSet<String> pairs = []
                                                    if (values) {
                                                        String[] splitvalues = values.split(',')
                                                        if (splitvalues) {
                                                            splitvalues.each {
                                                                pairs.add(propertyNames[i] + '=' + it)
                                                            }
                                                        } else return null
                                                    } else return null
                                                    if (pairs)
                                                        combinations.add(pairs)
//                                                    println(pairs)
                                                }
//                                                println(combinations)

                                                def cartesianproduct = Sets.cartesianProduct(combinations)
//                                                println(cartesianproduct)


                                                def stubMap = [:]
                                                for (l in cartesianproduct) {
                                                    stubMap.put(l.toString().replace(' ', '').replace('[', '').replace(']', ''), [model: modid + ':' + blockid])
                                                }
                                                def variants = [variants: stubMap]
                                                def json = Utilities.formatJson(variants)
                                                File blockstate = Utilities.createJsonFile(Paths.get(JAG.resourceDirectory.absolutePath, AssetConstants.ASSETS.value, modid, BLOCKSTATES.value, blockid + '.json'), json)
                                                JAG.lastModid = modid
                                                new Alert2(Alert.AlertType.INFORMATION, "Stub created - $blockstate").show()

                                            }
                                            return null
                                        }
                                    })
                                    propValues.showAndWait()
                                }
                            }
                        } else {
//                                println(o.get())
                            Alert2 alert2 = new Alert2(Alert.AlertType.INFORMATION, 'Invalid value. Lowercase chars, numbers and _ are allowed')
                            alert2.show()
                        }
                    }
                }
            } else if (radioButton == recipe) {
                Tab recipetab = new Tab('Recipe')
                tabPane.tabs.add(recipetab)
                VBox content = new VBox(6)
                ChoiceBox<String> craftingtype = new ChoiceBox<>(FXCollections.observableList(Recipes.values().name))
                craftingtype.selectionModel.select(0)
                GridPane mapgrid = new GridPane()
                GridPane recipematrix = new GridPane()
                TreeBasedTable<Integer, Integer, TextField> recipeLayout = TreeBasedTable.create()
                TreeBasedTable<Integer, Integer, Object> parameters = TreeBasedTable.create()
                TextField resultitem = new TextField()
                CheckBox customname = new CheckBox('Custom file name')
                TextField customFilename = new TextField()
                customFilename.setDisable(true)
                resultitem.setPromptText('Result item identifier/count/meta')
                for (i in 0..2) {
                    for (j in 0..2) {
                        TextField textField = new TextField()
                        recipeLayout.put(i, j, textField)
                        recipematrix.add(textField, j, i)
                    }
                }

                for (i in 0..8) {

                    Label keyLabel = new Label()
                    TextField registryname = new TextField()
                    Spinner<Short> metadata = new Spinner<>(0, Short.MAX_VALUE, 0)
                    metadata.setEditable(true)
                    parameters.put(i, 0, keyLabel)
                    parameters.put(i, 1, registryname)
                    parameters.put(i, 2, metadata)
                    mapgrid.add(keyLabel, 0, i)
                    mapgrid.add(registryname, 1, i)
                    mapgrid.add(metadata, 2, i)
                }

                ArrayList<String> keys = []
                Button generateFile = new Button('Create')
                Button extractKeys = new Button('Store keys and values')

                List<String> jsonpattern = []
                extractKeys.setOnAction(new RecipeKeyHandler(craftingtype, jsonpattern, keys, parameters, recipeLayout, generateFile))

                generateFile.setDisable(true)
                generateFile.setOnAction(new RecipeGenerator(craftingtype, modidentifier, resultitem, customFilename, jsonpattern, keys, parameters, customname, minecraftversion))
//
                content.getChildren().addAll(minecraftversion, craftingtype, recipematrix, modidentifier, mapgrid, extractKeys, JAG.instance.setOutputFolder, resultitem, customname, customFilename, generateFile)
                recipetab.setContent(content)
                content.setPrefHeight(tabPane.getHeight())
                customname.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    void handle(ActionEvent e) {
                        customFilename.setDisable(!customname.isSelected())
                    }
                })
            } else if (radioButton == blockState)      //TODO
            {
                ChoiceBox<String> blocksstates = new ChoiceBox(FXCollections.observableArrayList(BlockState.values()*.type))
                blocksstates.setTooltip(new Tooltip('List of preset blockstates'))
                Button generate = new Button('Generate')
                TextField objectidentifier
                objectidentifier = new TextField()
                objectidentifier.setPromptText('Block identifier')
                Label description = new Label()
                TextField directionname = new TextField('facing')
                directionname.setVisible(false)
                directionname.setTooltip(new Tooltip('Name of the direction state'))
                TextField optionalSingleTexture = new TextField()
                optionalSingleTexture.setPromptText('Single texture name (optional)')
                blocksstates.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    void handle(ActionEvent et) {
                        def sel = blocksstates.getSelectionModel().getSelectedItem()
                        BlockState parentModels = BlockState.values().find { it.type == sel }
                        description.setText(parentModels.description)
                        if (parentModels != BlockState.DIRECTIONAL) {
                            directionname.setVisible(false)
                        } else directionname.setVisible(true)
                    }
                })
                minecraftversion.selectionModel.select(1);
                HBox options = new HBox(6, minecraftversion, blocksstates, description, directionname)
                VBox contentpane = new VBox(6, options, modidentifier, objectidentifier, optionalSingleTexture, generate)
                Tab blockstatetab = new Tab('Block state', contentpane)
                generate.setOnAction(new BlockStateGenerator(blocksstates, modidentifier, objectidentifier, directionname, optionalSingleTexture, blockstatetab, minecraftversion, contentpane))
                tabPane.tabs.add(blockstatetab)

            }

        }

    }

}
