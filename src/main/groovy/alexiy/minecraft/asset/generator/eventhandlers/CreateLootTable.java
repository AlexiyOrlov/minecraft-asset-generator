package alexiy.minecraft.asset.generator.eventhandlers;

import alexiy.minecraft.assetgenerator.MAG;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import org.knowbase.Vbox2;

import java.util.ArrayList;
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
        Vbox2 lootPool = new Vbox2();
        lootPool.getChildren().addAll(pools, choiceBox, addLootPool);
        List<Vbox2> lootEntries = new ArrayList<>();
        addLootPool.setOnAction(event1 -> {
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
                            lootEntry.getChildren().addAll(minCount, maxCount);
                    }
                    functionChoiceBox.getSelectionModel().clearSelection();
                    mag.getTabPane().requestLayout();
                });
                lootEntry.getChildren().addAll(functions, functionChoiceBox);
                lootEntries.add(lootEntry);
                mag.getTabPane().requestLayout();
            });
            mag.getTabPane().requestLayout();
        });
        Button generate = new Button("Generate");
        generate.setOnAction(event1 -> {
            lootEntries.forEach(vbox2 -> {
                ObservableList<Node> children = vbox2.getChildren();
                System.out.println(children);
            });
        });
        lootPool.getChildren().add(generate);
        Tab tab = new Tab(MAG.getLastModId(), lootPool);
        mag.getTabPane().getTabs().add(tab);
    }
}
