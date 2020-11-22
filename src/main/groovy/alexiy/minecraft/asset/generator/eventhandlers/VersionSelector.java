package alexiy.minecraft.asset.generator.eventhandlers;

import alexiy.minecraft.asset.generator.MAG;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ChoiceBox;

/**
 * Updates {@link MAG#getLastMinecraftVersion()}
 */
public class VersionSelector implements EventHandler<ActionEvent> {
    public VersionSelector(ChoiceBox<String> versionChoice) {
        this.versionChoice = versionChoice;
    }

    @Override
    public void handle(ActionEvent event) {
        MAG.setLastMinecraftVersion(versionChoice.getSelectionModel().getSelectedItem());
    }

    private final ChoiceBox<String> versionChoice;
}
