package alexiy.minecraft.asset.generator.eventhandlers

import alexiy.minecraft.asset.generator.MAG
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.ChoiceBox

/**
 * Updates {@link MAG#lastMinecraftVersion}
 */
class VersionSelector implements EventHandler<ActionEvent> {

    private ChoiceBox<String> versionChoice

    VersionSelector(ChoiceBox<String> versionChoice) {
        this.versionChoice = versionChoice
    }

    @Override
    void handle(ActionEvent event) {
        MAG.lastMinecraftVersion = versionChoice.getSelectionModel().getSelectedItem()
    }
}
