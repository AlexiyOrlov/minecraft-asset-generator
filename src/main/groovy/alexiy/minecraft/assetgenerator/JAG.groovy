package alexiy.minecraft.assetgenerator


import alexiy.minecraft.assetgenerator.eventhandlers.CreateFile
import com.google.common.collect.HashMultimap
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
import javafx.scene.layout.VBox
import javafx.stage.DirectoryChooser
import javafx.stage.Modality
import javafx.stage.Screen
import javafx.stage.Stage

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

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
        if (!settings.exists())
            settings.createNewFile()
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
        newfile.setOnAction(new CreateFile(tabPane, primaryStage))

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
