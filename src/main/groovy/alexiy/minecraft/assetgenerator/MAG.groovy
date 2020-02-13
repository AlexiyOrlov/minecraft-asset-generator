package alexiy.minecraft.assetgenerator

import alexiy.minecraft.asset.generator.eventhandlers.CreateItemModel
import alexiy.minecraft.asset.generator.eventhandlers.MinecraftVersion
import javafx.application.Application
import javafx.geometry.Rectangle2D
import javafx.scene.Scene
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.control.TabPane
import javafx.stage.Screen
import javafx.stage.Stage
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
    static MinecraftVersion lastMinecraftVersion
    static String lastResourceFolder

    @Override
    void start(Stage primaryStage) throws Exception {
        instance = this
        mainStage = primaryStage
        settings = new Settings(Paths.get('settings.txt'))
        lastMinecraftVersion = MinecraftVersion.valueOf(settings.getOrDefault(LAST_MINECRAFT_VERSION, MinecraftVersion.V1_12.toString()))
        lastModId = settings.getOrDefault(LAST_MOD_ID, "minecraft:")
        lastResourceFolder = settings.getOrDefault(LAST_RESOURCE_PATH, System.getProperty('user.home'))
        visualScreenBounds = Screen.getPrimary().getVisualBounds()
        MenuItem createItemModel = new MenuItem("Item model")
        createItemModel.setOnAction(new CreateItemModel(this))
        Menu files = new Menu("Files", null, createItemModel)
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
        settings.put(LAST_MINECRAFT_VERSION, lastMinecraftVersion.toString())
        settings.put(LAST_MOD_ID, lastModId)
        settings.put(LAST_RESOURCE_PATH, lastResourceFolder)
        settings.save()
    }
}
