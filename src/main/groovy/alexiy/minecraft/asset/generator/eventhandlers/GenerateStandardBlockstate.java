package alexiy.minecraft.asset.generator.eventhandlers;

import alexiy.minecraft.asset.generator.*;
import alexiy.minecraft.assetgenerator.Utilities;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import org.knowbase.Alert2;
import org.knowbase.Hbox2;
import org.knowbase.Vbox2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;

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
            directoryChooser.setTitle("Select 'resources' directory");
            directoryChooser.setInitialDirectory(new File(MAG.getLastResourceFolder()));
            File dire = directoryChooser.showDialog(MAG.getMainStage());
            if (dire != null) {
                path.setText(dire.getAbsolutePath());
                MAG.setLastResourceFolder(dire.getAbsolutePath());
            }
        });

        ChoiceBox<MinecraftVersion> version = new ChoiceBox<>(FXCollections.observableArrayList(MinecraftVersion.values()));
        version.getSelectionModel().select(MinecraftVersion.V1_15);
        ChoiceBox<BlockState> choices = new ChoiceBox<>(FXCollections.observableArrayList(BlockState.SLAB));
        choices.getSelectionModel().select(BlockState.SIMPLE_BLOCK);
        Label description = new Label(BlockState.SIMPLE_BLOCK.getDescription());
        Label parentModel = new Label(BlockModel.SINGLETEXTURE.value);
        CheckBox generateItemModel = new CheckBox("Generate item model");
        generateItemModel.setSelected(true);

        ChoiceBox<BlockDrop> lootTable = new ChoiceBox<>(FXCollections.observableArrayList(BlockDrop.NONE, BlockDrop.SELF));
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
                String texturePath = null;
                switch (minecraftVersion) {
                    case V1_12:
                        texturePath = mod + ":" + AssetConstants.BLOCKS_LITERAL.value + "/" + identifier;
                        break;
                    case V1_15:
                        texturePath = mod + ":" + AssetConstants.BLOCK_LITERAL.value + "/" + identifier;
                        break;
                }
                String blockstateContent = null;
                switch (parent) {
                    case SIMPLE_BLOCK:
                    case CROSS:
                    case DIFFERENT_SIDES:
                    case TOP_BOTTOM_SIDE:
                    case COLUMN:
                        blockstateContent = CreateStandardBlockState.createSimpleBlockstate(minecraftVersion, mod, identifier);
                        break;
                    case SLAB:

                        LinkedHashMap<String, Object> slabTypes = new LinkedHashMap<>(3);
                        slabTypes.put("type=bottom", Utilities.singleEntryMap("model", mod + ":block/" + identifier + "_slab_bottom"));
                        slabTypes.put("type=top", Utilities.singleEntryMap("model", mod + ":block/" + identifier + "_slab_top"));
                        slabTypes.put("type=double", Utilities.singleEntryMap("model", mod + ":block/" + identifier));
                        LinkedHashMap<String, Object> linkedHashMap = Utilities.singleEntryMap("variants", slabTypes);
                        blockstateContent = Utilities.formatJson(linkedHashMap);
                        break;
                }
                if (blockstateContent != null) {
                    Path file = Paths.get(path.getText(), AssetConstants.ASSETS.value, mod, AssetConstants.BLOCKSTATES.value, identifier + "_slab.json");
                    if (Files.exists(file))
                        new Alert2(Alert.AlertType.WARNING, file + " exists", ButtonType.OK).show();
                    else {
                        try {
                            Files.createDirectories(file.getParent());
                            Files.createFile(file);
                            Files.write(file, Collections.singleton(blockstateContent));
                            new Alert2(Alert.AlertType.INFORMATION, "Created blockstate " + file, ButtonType.CLOSE).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                Path modelFile;
                switch (parent) {
                    default:
                        modelFile = Paths.get(path.getText(), AssetConstants.ASSETS.value, mod, AssetConstants.MODELS_LITERAL.value, AssetConstants.BLOCK_LITERAL.value, identifier + ".json");
                }
                if (Files.exists(modelFile) && parent != BlockState.SLAB) {
                    new Alert2(Alert.AlertType.WARNING, modelFile + " exists", ButtonType.OK).show();
                } else {
                    try {
                        Files.createDirectories(modelFile.getParent());
                        if (Files.notExists(modelFile))
                            Files.createFile(modelFile);
                        String fileContent = null;
                        switch (parent) {
                            case SLAB:
                                Path topModel = Paths.get(path.getText(), AssetConstants.ASSETS.value, mod, AssetConstants.MODELS_LITERAL.value, AssetConstants.BLOCK_LITERAL.value, identifier + "_slab_top.json");
                                LinkedHashMap<String, Object> texturesTop = new LinkedHashMap<>(3);
                                texturesTop.put("bottom", mod + ":block/" + identifier);
                                texturesTop.put("top", mod + ":block/" + identifier);
                                texturesTop.put("side", mod + ":block/" + identifier);
                                LinkedHashMap<String, Object> modelMap = new LinkedHashMap<>(2);
                                modelMap.put("parent", "minecraft:block/slab_top");
                                modelMap.put("textures", texturesTop);
                                Files.write(topModel, Collections.singleton(Utilities.formatJson(modelMap)));

                                Path bottomModel = Paths.get(path.getText(), AssetConstants.ASSETS.value, mod, AssetConstants.MODELS_LITERAL.value, AssetConstants.BLOCK_LITERAL.value, identifier + "_slab_bottom.json");
                                LinkedHashMap<String, Object> texturesBottom = new LinkedHashMap<>(3);
                                texturesBottom.put("bottom", mod + ":block/" + identifier);
                                texturesBottom.put("top", mod + ":block/" + identifier);
                                texturesBottom.put("side", mod + ":block/" + identifier);
                                LinkedHashMap<String, Object> bottomModelMap = new LinkedHashMap<>(2);
                                bottomModelMap.put("parent", "minecraft:block/slab");
                                bottomModelMap.put("textures", texturesBottom);
                                Files.write(bottomModel, Collections.singleton(Utilities.formatJson(bottomModelMap)));

                                LinkedHashMap<String, Object> texturesFull = Utilities.singleEntryMap("all", texturePath);
                                LinkedHashMap<String, Object> fullModel = new LinkedHashMap<>(2);
                                fullModel.put("parent", BlockModel.SINGLETEXTURE.value);
                                fullModel.put("textures", texturesFull);
                                fileContent = Utilities.formatJson(fullModel);
                                break;
                        }

                        Files.write(modelFile, Collections.singleton(fileContent));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


                if (generateItemModel.isSelected()) {
                    Path pathItemModel;
                    switch (parent) {
                        case SLAB:
                            identifier = identifier + "_slab";
                            break;
                    }
                    pathItemModel = Paths.get(path.getText(), AssetConstants.ASSETS.value, mod, AssetConstants.MODELS_LITERAL.value, AssetConstants.ITEM_LITERAL.value, identifier + ".json");
                    if (Files.exists(pathItemModel))
                        new Alert2(Alert.AlertType.WARNING, pathItemModel + " exists").show();
                    else {
                        switch (parent) {
                            case SLAB:
                                identifier = identifier + "_bottom";
                                break;
                        }
                        try {
                            Files.createDirectories(pathItemModel.getParent());
                            Files.createFile(pathItemModel);
                            Files.write(pathItemModel, Collections.singleton(Utilities.formatJson(Utilities.singleEntryMap("parent", mod + ":block/" + identifier))));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                BlockDrop blockDrop = lootTable.getValue();
                Path blockTable = Paths.get(path.getText(), AssetConstants.DATA.value, mod, AssetConstants.LOOT_TABLES.value, AssetConstants.BLOCKS_LITERAL.value, identifier + ".json");
                String content = null;
                switch (blockDrop) {
                    case SELF:
                        LinkedHashMap<String, Object> condition = Utilities.singleEntryMap("condition", "minecraft:survives_explosion");
                        LinkedHashMap<String, Object> entry = new LinkedHashMap<>(2);
                        entry.put("type", "minecraft:item");
                        entry.put("name", mod + ":" + identifier);
                        LinkedHashMap<String, Object> pool = new LinkedHashMap<>(3);
                        pool.put("rolls", 1);
                        pool.put("entries", Collections.singletonList(entry));
                        pool.put("conditions", Collections.singletonList(condition));
                        LinkedHashMap<String, Object> pools = new LinkedHashMap<>(2);
                        pools.put("type", "minecraft:block");
                        pools.put("pools", Collections.singletonList(pool));
                        content = Utilities.formatJson(pools);
                        break;
                    case SAVED:
                }
                if (Files.exists(blockTable))
                    new Alert2(Alert.AlertType.WARNING, "Block loot " + blockTable + " exists").show();
                else {
                    try {
                        Files.createDirectories(blockTable.getParent());
                        Files.createFile(blockTable);
                        Files.write(blockTable, Collections.singleton(content));
                        new Alert2(Alert.AlertType.INFORMATION, "Created block table " + blockTable).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        Vbox2 container = new Vbox2(modidr, blockidr, directionName, new Hbox2(setPath, path), version, new Hbox2(choices, parentModel), description, generateItemModel, lootTable, generateColoredBlocks, generate);
        mag.getTabPane().getTabs().add(new Tab(modidr.getText() + ":" + blockidr.getText(), container));
    }
}
