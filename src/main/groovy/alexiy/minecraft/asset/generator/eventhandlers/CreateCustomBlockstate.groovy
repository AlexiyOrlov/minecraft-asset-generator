package alexiy.minecraft.asset.generator.eventhandlers

import alexiy.minecraft.assetgenerator.MAG
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.control.TextInputDialog
import javafx.scene.control.Tooltip

/**
 * Created on 2/15/20.
 */
class CreateCustomBlockstate implements EventHandler<ActionEvent> {
    private MAG mag

    CreateCustomBlockstate(MAG mag) {
        this.mag = mag
    }

    @Override
    void handle(ActionEvent event) {
        File outputPath = new File(MAG.lastResourceFolder)
        Label resourcePath = new Label(MAG.lastResourceFolder)
        TextInputDialog identifier = new TextInputDialog(MAG.lastModId)
        identifier.setHeaderText('Write a full identifier')
        def result = identifier.showAndWait()
        if (result.isPresent()) {
            String id = result.get()
            if (id) {
                TextField textField = new TextField(id)
                textField.setTooltip(new Tooltip('Full block identifier'))
            }
        }
    }
}
