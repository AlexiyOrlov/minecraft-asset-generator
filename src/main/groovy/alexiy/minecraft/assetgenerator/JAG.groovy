package alexiy.minecraft.assetgenerator

import alexiy.minecraft.assetgenerator.eventhandlers.BlockStateGenerator
import alexiy.minecraft.assetgenerator.eventhandlers.ItemModelGenerator
import com.google.common.collect.HashMultimap
import com.google.common.collect.Sets
import com.google.common.collect.TreeBasedTable
import javafx.application.Application
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Rectangle2D
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType
import javafx.scene.input.Clipboard
import javafx.scene.input.DataFormat
import javafx.scene.input.MouseEvent
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.DirectoryChooser
import javafx.stage.Modality
import javafx.stage.Screen
import javafx.stage.Stage
import javafx.util.Callback

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static alexiy.minecraft.assetgenerator.AssetConstants.BLOCKSTATES

/**
 * Created on 8/6/17.
 */
class JAG extends Application {
    static JAG instance
    static Rectangle2D visualScreenBounds
    VBox rootBox
//    static final String ASSETS='assets',MODELS='models',BLOCKMODEL='block',
//    ITEMMODEL='item',BLOCKSTATES='blockstates',TEXTURES='textures',BLOCKTEXTURE='blocks',ITEMTEXTURE='items'

    final String RESOURCESPATH = 'Path to resources=', LASTSAVEDIRECTORY = 'Last save directory=', LAST_MOD_ID = 'Last mod id=',
                 ITEM_ID_FILE = 'Item id file name='
    static String itemIdFile = "ID export.txt"
    File lastsaveDirectory
    static File resourceDirectory
    final File settings = new File('settings.txt')
    static String lastModid
    Button setOutputFolder

    @Override
    void start(Stage primaryStage) throws Exception {
        instance = this
        primaryStage.setTitle("Minecraft Json generator")
        setOutputFolder = new Button("Select 'resources' folder")
        if (!settings.exists()) settings.createNewFile()
        else {
            final String string = settings.text
            String resdir = string.find(RESOURCESPATH + '.+')
            if (resdir) {
                resdir = resdir.substring(resdir.indexOf('=') + 1)
                println(resdir)
                resourceDirectory = new File(resdir)
                setOutputFolder.setTooltip(new Tooltip(resdir))

            }
            String lastsaveddir = string.find(LASTSAVEDIRECTORY + '.+')
            if (lastsaveddir) {
                println(lastsaveddir)
            }
            String lastmodid = string.find(LAST_MOD_ID + '.+')
            if (lastmodid) lastModid = lastmodid.substring(lastmodid.indexOf('=') + 1)
            String idFile = string.find(ITEM_ID_FILE + '.+')
            if (idFile)
                itemIdFile = idFile.substring(idFile.indexOf('=') + 1)

        }
        setOutputFolder.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            void handle(ActionEvent event) {
                DirectoryChooser directoryChooser = new DirectoryChooser()
                directoryChooser.setTitle('Select resource directory')
                if (lastsaveDirectory) directoryChooser.setInitialDirectory(lastsaveDirectory)
                File dir = directoryChooser.showDialog(primaryStage)
                if (dir) {
                    resourceDirectory = dir
                    setOutputFolder.setTooltip(new Tooltip("File output to $resourceDirectory.absolutePath"))
                }

            }
        })
        visualScreenBounds = Screen.primary.visualBounds
        TabPane tabPane = new TabPane()
        MenuItem newfile = new MenuItem('Create file')
        newfile.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            void handle(ActionEvent event) {
                Dialog<RadioButton> dialog = new Dialog()
                dialog.setTitle('Select a variant')

                RadioButton itemmodel = new RadioButton('Item model')
                RadioButton recipe = new RadioButton('Recipe')
                RadioButton blockState = new RadioButton('Blockstate')
                RadioButton customBlockState = new RadioButton('Custom blockstate')
                RadioButton achievement = new RadioButton('Advancement')
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
                    TextField modidentifier = new TextField(lastModid)
                    modidentifier.setPromptText('Mod identifier')
                    if (radioButton == itemmodel) {
                        ChoiceBox<String> parentmodels = new ChoiceBox<>(FXCollections.observableArrayList(ItemModels.values().value))
                        parentmodels.selectionModel.select(1)
                        TextField objectidentifier
                        objectidentifier = new TextField()
                        objectidentifier.setPromptText('Item identifier')
                        Button generate = new Button('Generate model file')
                        VBox content = new VBox(6, minecraftversion, parentmodels, modidentifier, objectidentifier, setOutputFolder, generate)
                        Tab itemtab = new Tab('Item model', content)
                        generate.setOnAction(new ItemModelGenerator(parentmodels, modidentifier, objectidentifier, minecraftversion, itemtab))
                        tabPane.tabs.add(itemtab)
                    } else if (radioButton == customBlockState) {

                        DirectoryChooser directoryChooser = new DirectoryChooser()
                        if (resourceDirectory.parentFile)
                            directoryChooser.setInitialDirectory(resourceDirectory.parentFile)
                        else directoryChooser.setInitialDirectory(resourceDirectory)
                        directoryChooser.setTitle("Choose the resource directory")
                        def optional = directoryChooser.showDialog(primaryStage)
                        if (optional) {
                            resourceDirectory = optional
                            TextInputDialog registryName = new TextInputDialog(lastModid + ':')
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
                                                            if (pairs) combinations.add(pairs)
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
                                                        File blockstate = Utilities.createJsonFile(Paths.get(resourceDirectory.absolutePath, AssetConstants.ASSETS.value, modid, BLOCKSTATES.value, blockid + '.json'), json)
                                                        lastModid = modid
                                                        new Alert2(AlertType.INFORMATION, "Stub created - $blockstate").show()

                                                    }
                                                    return null
                                                }
                                            })
                                            propValues.showAndWait()
                                        }
                                    }
                                } else {
//                                println(o.get())
                                    Alert2 alert2 = new Alert2(AlertType.INFORMATION, 'Invalid value. Lowercase chars, numbers and _ are allowed')
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
                        content.getChildren().addAll(minecraftversion, craftingtype, recipematrix, modidentifier, mapgrid, extractKeys, setOutputFolder, resultitem, customname, customFilename, generateFile)
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
                        ChoiceBox<String> blocksstates = new ChoiceBox(FXCollections.observableArrayList(BlockStates.values().type))
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
                                BlockStates parentModels = BlockStates.values().find { it.type == sel }
                                description.setText(parentModels.description)
                                if (parentModels != BlockStates.DIRECTIONAL) {
                                    directionname.setVisible(false)
                                } else directionname.setVisible(true)
                            }
                        })
                        minecraftversion.selectionModel.select(1);
                        HBox options = new HBox(6, minecraftversion, blocksstates, description, directionname)
                        VBox contentpane = new VBox(6, options, modidentifier, objectidentifier, optionalSingleTexture, setOutputFolder, generate)
                        Tab blockstatetab = new Tab('Block state', contentpane)
                        generate.setOnAction(new BlockStateGenerator(blocksstates, modidentifier, objectidentifier, directionname, optionalSingleTexture, blockstatetab, minecraftversion, contentpane))
                        tabPane.tabs.add(blockstatetab)

                    } else if (radioButton == achievement) {
                        ChoiceBox<String> types = new ChoiceBox<>()
                    }

                }

            }
        })

        Menu files = new Menu('Files', null, newfile)
        MenuItem recipeHelp = new MenuItem('All recipes'), shapedRecipe = new MenuItem('Shaped recipes'),
                 shapelessRecipe = new MenuItem('Shapeless recipes')
        recipeHelp.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            void handle(ActionEvent event) {
                Alert2 alert2 = new Alert2(AlertType.INFORMATION, null, Help.FOR_ALL_RECIPES)
                alert2.show()
            }
        })
        shapelessRecipe.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            void handle(ActionEvent event) {
                Alert2 alert2 = new Alert2(AlertType.INFORMATION, '', Help.SHAPELESS_RECIPE)
                alert2.show()
            }
        })
        shapedRecipe.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            void handle(ActionEvent event) {
                Alert2 alert2 = new Alert2(AlertType.INFORMATION, '', Help.SHAPED_RECIPE)
                alert2.show()
            }
        })
        Menu help = new Menu('Usage', null, recipeHelp, shapedRecipe, shapelessRecipe)

        MenuItem itemidentifiers = new MenuItem("Item identifiers");
        itemidentifiers.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            void handle(ActionEvent event) {
                Path idFile = Paths.get(itemIdFile)
                if (Files.exists(idFile)) {
                    def strings = Files.readAllLines(idFile)
                    HashMultimap<String, String> domainMap = HashMultimap.create()
                    strings.each {
                        String[] identifier = it.split(':')
                        domainMap.put(identifier[0], identifier[1])
                    }
                    Dialog<String> dialog = new Dialog<>()
                    dialog.setTitle("Select item identifier to copy")
                    VBox vBox = new VBox(6)
                    dialog.getDialogPane().setContent(vBox)
                    List<String> doms = FXCollections.observableArrayList(domainMap.keySet())
                    doms.sort()
                    ChoiceBox<String> domains = new ChoiceBox<>(doms)
                    domains.selectionModel.select(0)
                    ListView<String> stringListView = new ListView<>(FXCollections.observableArrayList(domainMap.get(domains.selectionModel.getSelectedItem()).sort()))
                    domains.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        void handle(ActionEvent e) {
                            String domain = domains.getSelectionModel().getSelectedItem()
                            Set<String> paths = domainMap.get(domain)
                            def list = FXCollections.observableArrayList(paths)
                            list.sort()
                            stringListView.setItems(list)
                        }
                    })
                    stringListView.setPrefWidth(400)
                    stringListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                        @Override
                        void handle(MouseEvent even) {
                            String object = domains.selectionModel.getSelectedItem() + ":" + stringListView.selectionModel.getSelectedItem()
                            Clipboard clipboard = Clipboard.systemClipboard
                            clipboard.setContent([(DataFormat.PLAIN_TEXT): object])
                        }
                    })
                    vBox.getChildren().addAll(domains, stringListView)
//
                    dialog.initModality(Modality.NONE)
                    dialog.getDialogPane().buttonTypes.add(ButtonType.CLOSE)
                    dialog.show()
                } else {
                    new Alert2(AlertType.WARNING, "Supply an item id file").show()
                }
            }
        })
        MenuItem settings = new MenuItem('Settings')
        settings.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            void handle(ActionEvent event) {
                TextInputDialog textInputDialog = new TextInputDialog(itemIdFile)
                textInputDialog.setHeaderText('Name of the file to parse item ids from')
                def res = textInputDialog.showAndWait();
                if (res.isPresent())
                    itemIdFile = res.get()
            }
        })
        Menu prefrences = new Menu('Preferences', null, settings)

        Menu utilities = new Menu("Utilities", null, itemidentifiers)
        MenuBar menuBar = new MenuBar(files, help, prefrences, utilities)
        rootBox = new VBox(6, menuBar, tabPane)
        Scene scene = new Scene(rootBox, visualScreenBounds.width / 2.toDouble(), visualScreenBounds.height - 200)
        primaryStage.setScene(scene)
        primaryStage.show()

    }

    @Override
    void stop() throws Exception {
        settings.withWriter {

            if (resourceDirectory) {
                it.append("$RESOURCESPATH$resourceDirectory.absolutePath\n")
            }
            if (lastsaveDirectory) {
                it.append("$LASTSAVEDIRECTORY$lastsaveDirectory.absolutePath\n")
            }
            if (lastModid)
                it.append("$LAST_MOD_ID$lastModid\n")
            if (itemIdFile)
                it.append("$ITEM_ID_FILE$itemIdFile\n")
        }
    }
}
