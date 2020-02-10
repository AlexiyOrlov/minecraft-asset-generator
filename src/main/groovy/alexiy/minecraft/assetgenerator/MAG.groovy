package alexiy.minecraft.assetgenerator

import javafx.application.Application
import javafx.geometry.Rectangle2D
import javafx.scene.layout.VBox
import javafx.stage.Stage
import org.knowbase.tools.Settings

import java.nio.file.Paths

/**
 * Created on 2/10/20.
 */
class MAG extends Application {

    static Rectangle2D visualScreenBounds
    VBox rootBox

    final String RESOURCESPATH = 'Path to resources=', LASTSAVEDIRECTORY = 'Last save directory=', LAST_MOD_ID = 'Last mod id=',
                 ITEM_ID_FILE = 'Item id file name='
    static String itemIdFile = "ID export.txt"
    static MAG instance
    Settings settings

    @Override
    void start(Stage primaryStage) throws Exception {
        instance = this
        settings = new Settings(Paths.get('settings.txt'))

        primaryStage.setTitle("Minecraft asset generator")
        primaryStage.show()
    }

    @Override
    void stop() throws Exception {
        settings.save()
    }
}
