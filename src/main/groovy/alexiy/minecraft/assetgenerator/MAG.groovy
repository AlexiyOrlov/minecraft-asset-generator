package alexiy.minecraft.assetgenerator

import alexiy.minecraft.asset.generator.MinecraftVersion
import alexiy.minecraft.asset.generator.eventhandlers.*
import javafx.application.Application
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Rectangle2D
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.stage.Modality
import javafx.stage.Screen
import javafx.stage.Stage
import org.knowbase.Dialog2
import org.knowbase.Vbox2
import org.knowbase.tools.Settings

import java.nio.file.Paths

/**
 * Created on 2/10/20.
 */
class MAG extends Application {

    static Rectangle2D visualScreenBounds

    final String LAST_RESOURCE_PATH = 'Path to resources', LASTSAVEDIRECTORY = 'Last save directory', LAST_MOD_ID = 'Last mod id',
                 ITEM_ID_FILE = 'Item id file name', LAST_MINECRAFT_VERSION = 'Last minecraft version'
    static String itemIdFile = "ID export.txt"
    static MAG instance
    static Stage mainStage
    Settings settings
    TabPane tabPane
    Vbox2 rootBox
    MenuBar menuBar
    static String lastModId
    static String lastMinecraftVersion
    static String lastResourceFolder
    static String lastAdvancementTrigger

    @Override
    void start(Stage primaryStage) throws Exception {
        instance = this
        mainStage = primaryStage
        settings = new Settings(Paths.get('settings.txt'))
        lastMinecraftVersion = settings.getOrDefault(LAST_MINECRAFT_VERSION, MinecraftVersion.V1_12.version)
        lastModId = settings.getOrDefault(LAST_MOD_ID, "minecraft:")
        lastAdvancementTrigger = settings.getOrDefault("Last trigger", "minecraft:inventory_changed")
        lastResourceFolder = settings.getOrDefault(LAST_RESOURCE_PATH, System.getProperty('user.home'))
        visualScreenBounds = Screen.getPrimary().getVisualBounds()
        MenuItem createItemModel = new MenuItem("Item model")
        createItemModel.setOnAction(new CreateItemModel(this))
        MenuItem createRecipe = new MenuItem('Recipe')
        createRecipe.setOnAction(new CreateRecipe(this))
        MenuItem createCustomBlockstate = new MenuItem('Custom blockstate')
        createCustomBlockstate.setOnAction(new CreateCustomBlockstate(this))
        MenuItem createStandardBlockstate = new MenuItem('Standard blockstate')
        createStandardBlockstate.setOnAction(new CreateStandardBlockState(this))
        MenuItem createAdvancement = new MenuItem('Advancement')
        createAdvancement.setOnAction(new CreateAdvancement(this))

        MenuItem createAssetFolders = new MenuItem('Make asset folders')
        createAssetFolders.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            void handle(ActionEvent event) {
                Dialog2<Boolean, Vbox2> dialog2 = new Dialog2('Confirm options', new Vbox2(), Modality.WINDOW_MODAL, ButtonType.APPLY, ButtonType.CANCEL)
                Vbox2 vbox2 = dialog2.getContainer() as Vbox2
                TextField version, resourceFolder, modid
                vbox2.children.add(version = new TextField(lastMinecraftVersion))
                vbox2.children.add(resourceFolder = new TextField(lastResourceFolder))
                vbox2.children.add(modid = new TextField(lastModId.contains(':') ? lastModId.substring(0, lastModId.indexOf(':')) : lastModId))
                dialog2.setResultConverter(o -> {
                    if (o == ButtonType.APPLY) {
                        return true
                    }
                    return false
                })
                def result = dialog2.showAndWait()
                if (result.isPresent()) {
                    if (result.get() == true) {
                        if (version.text && resourceFolder.text && modid.text) {
                            String domain = modid.text
                            if (domain.contains(':'))
                                domain = domain.substring(0, domain.indexOf(':'))
                            Utilities.createAssetFoldersIfNeeded(version.text, new File(resourceFolder.text), domain)
                            lastModId = domain
                        } else {
                            new org.knowbase.Alert2(Alert.AlertType.ERROR, 'Fill all fields', ButtonType.OK).show()
                        }
                    }
                }
            }
        })

        Menu files = new Menu("Files", null, createAssetFolders, createItemModel, createRecipe, createCustomBlockstate,
                createStandardBlockstate, createAdvancement)
        menuBar = new MenuBar(files)
        tabPane = new TabPane()
        rootBox = new Vbox2(menuBar, tabPane)
        Scene scene = new Scene(rootBox, visualScreenBounds.getWidth() / 2, visualScreenBounds.getHeight() - 30)
        primaryStage.setScene(scene)
        primaryStage.setTitle("Minecraft asset generator")
        primaryStage.show()
    }

    @Override
    void stop() throws Exception {
        settings.put(LAST_MINECRAFT_VERSION, lastMinecraftVersion)
        settings.put(LAST_MOD_ID, lastModId)
        settings.put(LAST_RESOURCE_PATH, lastResourceFolder)
        settings.put("Last trigger", lastAdvancementTrigger)
        settings.save()
    }
}
