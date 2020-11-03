package alexiy.minecraft.asset.generator.eventhandlers;

import alexiy.minecraft.assetgenerator.MAG;
import alexiy.minecraft.assetgenerator.Utilities;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import org.knowbase.Alert2;
import org.knowbase.Hbox2;
import org.knowbase.Vbox2;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class CreateLootTable implements EventHandler<ActionEvent> {
    private final MAG mag;

    public CreateLootTable(MAG mag) {
        this.mag = mag;
    }

    @Override
    public void handle(ActionEvent event) {
        TextField identifier = new TextField();
        identifier.setTooltip(new Tooltip("Identifier"));
        ChoiceBox<LootTableType> choiceBox = new ChoiceBox<>(FXCollections.observableArrayList(LootTableType.values()));
        choiceBox.getSelectionModel().select(0);
        Label pools = new Label("Loot Pools:");
        Button addLootPool = new Button("Add loot pool");
        List<VBox> lootPools = new ArrayList<>();
        HashMap<VBox, List<Vbox2>> poolsToEntries = new HashMap<>();
        VBox lootPoolBox = new Vbox2();
        addLootPool.setOnAction(event1 -> {
            List<Vbox2> lootEntries = new ArrayList<>();
            Vbox2 lootPool = new Vbox2();
            TextField rolls = new TextField("1");
            rolls.setTooltip(new Tooltip("Roll count"));
            Label entries = new Label("Loot entries");
            Button addEntry = new Button("Add loot entry");
            lootPool.getChildren().addAll(rolls, entries, addEntry);
            addEntry.setOnAction(event2 -> {
                Vbox2 lootEntry = new Vbox2();
                lootPool.getChildren().add(lootEntry);
                ChoiceBox<LootEntryType> lootTableTypes = new ChoiceBox<>(FXCollections.observableArrayList(LootEntryType.values()));
                lootTableTypes.setTooltip(new Tooltip("Type"));
                TextField textField = new TextField();
                Label entryLabel = new Label("Loot entry:");
                lootEntry.getChildren().addAll(entryLabel, lootTableTypes, textField);
                lootTableTypes.setOnAction(event3 -> {
                    switch (lootTableTypes.getSelectionModel().getSelectedItem()) {
                        case ITEM:
                            textField.setTooltip(new Tooltip("Item identifier"));
                            break;
                        case TAG:
                            textField.setTooltip(new Tooltip("Item tag"));
                            break;
                    }
                    mag.getTabPane().requestLayout();
                });
                Label functions = new Label("Functions");
                ChoiceBox<Function> functionChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(Function.values()));
                functionChoiceBox.setOnAction(event3 -> {
                    switch (functionChoiceBox.getSelectionModel().getSelectedItem()) {
                        case SET_COUNT:
                            TextField minCount = new TextField();
                            TextField maxCount = new TextField();
                            minCount.setTooltip(new Tooltip("Min. amount"));
                            maxCount.setTooltip(new Tooltip("Max. amount"));
                            lootEntry.getChildren().addAll(new Hbox2(new Label(Function.SET_COUNT.toString()), minCount, maxCount));
                    }
                    functionChoiceBox.getSelectionModel().clearSelection();
                    mag.getTabPane().requestLayout();
                });
                lootEntry.getChildren().addAll(functions, functionChoiceBox);
                lootEntries.add(lootEntry);
                mag.getTabPane().requestLayout();
            });
            lootPools.add(lootPool);
            lootPoolBox.getChildren().add(lootPool);
            poolsToEntries.put(lootPool, lootEntries);
            mag.getTabPane().requestLayout();
        });
        Button generate = new Button("Generate");
        generate.setOnAction(event1 -> {
            LinkedHashMap<String, Object> poolMap = Utilities.singleEntryMap("type", choiceBox.getSelectionModel().getSelectedItem().type);
            List<Object> poollist = new ArrayList<>();
            lootPools.forEach(vBox -> {
                List<Object> entries = new ArrayList<>();
                TextField rolls = (TextField) vBox.getChildrenUnmodifiable().get(0);
                LinkedHashMap<String, Object> entrymap = Utilities.singleEntryMap("rolls", Integer.parseInt(rolls.getText()));
                poolsToEntries.get(vBox).forEach(vbox2 -> {

                    ObservableList<Node> children = vbox2.getChildren();
                    ChoiceBox<LootEntryType> lootEntryType = (ChoiceBox<LootEntryType>) children.get(1);
                    TextField value = (TextField) children.get(2);
                    List<Node> functions = children.subList(5, children.size());
                    ArrayList<LinkedHashMap<String, Object>> functionList = new ArrayList<>();
                    for (int i = 0; i < functions.size(); i += 3) {
                        HBox hBox = (HBox) functions.get(i);
                        ObservableList<Node> childrenUnmodifiable = hBox.getChildrenUnmodifiable();
                        Label label = (Label) childrenUnmodifiable.get(0);
                        switch (label.getText()) {
                            case "set_count":
                                TextField minimum = (TextField) childrenUnmodifiable.get(1);
                                TextField maximum = (TextField) childrenUnmodifiable.get(2);
                                LinkedHashMap<String, Integer> counts = new LinkedHashMap<>();
                                counts.put("min", Integer.parseInt(minimum.getText()));
                                counts.put("max", Integer.parseInt(maximum.getText()));
                                LinkedHashMap<String, Object> functs = Utilities.singleEntryMap("function", label.getText());
                                functs.put("count", counts);
                                functionList.add(functs);
                                break;
                        }
                    }
                    LinkedHashMap<String, Object> entry = new LinkedHashMap<>();
                    entry.put("type", lootEntryType.getSelectionModel().getSelectedItem().toString());
                    entry.put("name", value.getText());
                    entry.put("functions", functionList);
                    entries.add(entry);
                    entrymap.put("entries", entries);
                });
                poollist.add(entrymap);
            });
            poolMap.put("pools", poollist);
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File(MAG.getLastResourceFolder()));
            File dir = directoryChooser.showDialog(MAG.getMainStage());
            if (!identifier.getText().isEmpty() && dir != null) {
                String json = Utilities.formatJson(poolMap);
                File lootTable = new File(dir, identifier.getText() + ".json");
                Utilities.createJsonFile(lootTable.toPath(), json);
                MAG.setLastResourceFolder(dir.getAbsolutePath());
                File p = lootTable.getParentFile();
                while (p.getParentFile() != null) {
                    if (p.getName().equals("data")) {
                        break;
                    }
                    p = p.getParentFile();
                }
                MAG.setLastModId(p.listFiles()[0].getName());
                new Alert2(Alert.AlertType.INFORMATION, "Generated loot table " + lootTable).show();
            }
        });
        Vbox2 content = new Vbox2(identifier, choiceBox, addLootPool, pools, lootPoolBox, generate);
        Tab tab = new Tab(MAG.getLastModId(), content);
        mag.getTabPane().getTabs().add(tab);
    }
}
