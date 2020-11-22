package alexiy.minecraft.asset.generator.eventhandlers;

import alexiy.minecraft.asset.generator.*;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;

import java.io.File;

public class GenerateStandardBlockstate implements EventHandler<ActionEvent> {
    private MAG mag;

    public GenerateStandardBlockstate(MAG mag) {
        this.mag = mag;
    }

    @Override
    public void handle(ActionEvent event) {
        TextField modidr = new TextField(MAG.getLastModId());
        modidr.setTooltip(new Tooltip("Mod identifier"));
        TextField blockidr = new TextField();
        blockidr.setTooltip(new Tooltip("Block identifier"));
        TextField directionName = new TextField("facing");
        directionName.setVisible(false);
        blockidr.setTooltip(new Tooltip("Block identifier"));
        Label path = new Label(MAG.getLastResourceFolder());
        Button setPath = new Button("Set output path");
        setPath.setOnAction(event1 -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File(MAG.getLastResourceFolder()));
            File dire = directoryChooser.showDialog(MAG.getMainStage());
            if (dire != null) {
                path.setText(dire.getAbsolutePath());
                MAG.setLastResourceFolder(dire.getAbsolutePath());
            }
        });

        ChoiceBox<MinecraftVersion> version = new ChoiceBox<>(FXCollections.observableArrayList(MinecraftVersion.values()));
        version.getSelectionModel().select(MinecraftVersion.V1_15);
        ChoiceBox<BlockState> choices = new ChoiceBox<>(FXCollections.observableArrayList(BlockState.values()));
        choices.getSelectionModel().select(BlockState.SIMPLE_BLOCK);
        Label description = new Label(BlockState.SIMPLE_BLOCK.getDescription());
        Label parentModel = new Label(BlockModel.SINGLETEXTURE.value);
        CheckBox generateItemModel = new CheckBox("Generate item model");
        generateItemModel.setSelected(true);

        ChoiceBox<String> lootTable = new ChoiceBox<>(FXCollections.observableArrayList("None", "Self", "Save"));
        lootTable.getSelectionModel().select(1);
        choices.setOnAction(event1 -> {
            BlockState blockState = choices.getValue();
            directionName.setVisible(blockState == BlockState.DIRECTIONAL);
            BlockModel model = blockState.getDefaultModel();
            if (model != null) {
                parentModel.setText(model.value);
                description.setText(model.description);
            } else {
                parentModel.setText("");
                description.setText(blockState.getDescription());
            }
        });
        Button generate = new Button("Generate");
        Button generateColoredBlocks = new Button("Generate colored blocks");
        generate.setOnAction(event1 -> {
            String mod = modidr.getText();
            String identifier = blockidr.getText();
            String directionValue = directionName.getText();
            if (mod != null && identifier != null) {
                BlockState parent = choices.getValue();
                MinecraftVersion minecraftVersion = version.getValue();
                String texturePath;
                switch (minecraftVersion) {
                    case V1_12:
                        texturePath = mod + AssetConstants.BLOCKS_LITERAL.value + "/" + identifier;
                        break;
                    case V1_15:
                        texturePath = mod + AssetConstants.BLOCK_LITERAL.value + "/" + identifier;
                        break;
                }
                String blockstateContent = "{}";
                switch (parent) {
                    case SIMPLE_BLOCK:
                    case CROSS:
                    case DIFFERENT_SIDES:
                    case TOP_BOTTOM_SIDE:
                    case COLUMN:
                        blockstateContent = CreateStandardBlockState.createSimpleBlockstate(minecraftVersion, mod, identifier);
                        break;

                }
            }
        });
    }
}
