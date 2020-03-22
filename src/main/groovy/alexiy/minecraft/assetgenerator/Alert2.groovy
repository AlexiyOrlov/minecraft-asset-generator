package alexiy.minecraft.assetgenerator

import com.sun.istack.internal.Nullable
import javafx.scene.control.Alert
import javafx.scene.layout.Region

/**
 * Created on 8/7/17.
 */
@Deprecated
class Alert2 extends Alert {
    {
        setResizable(true)
        getDialogPane().setMinHeight(Region.USE_PREF_SIZE)
    }

    Alert2(AlertType alertType, String header) {
        super(alertType)
        setHeaderText(header)
    }

    Alert2(AlertType alertType, @Nullable String header, String content) {
        super(alertType, content)
        setHeaderText(header)
    }
}
