package alexiy.minecraft.asset.generator.eventhandlers;

import alexiy.minecraft.assetgenerator.MAG;
import alexiy.minecraft.assetgenerator.Utilities;
import com.google.common.collect.ArrayListMultimap;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.DirectoryChooser;
import org.knowbase.Alert2;
import org.knowbase.Hbox2;
import org.knowbase.Vbox2;

import java.io.File;
import java.util.*;

public class CreateAdvancement implements EventHandler<ActionEvent> {

    private final MAG mag;

    public CreateAdvancement(MAG mag_) {
        mag = mag_;
    }

    @Override
    public void handle(ActionEvent event) {
        TextField identifier = new TextField();
        identifier.setTooltip(new Tooltip("Identifier"));
        TextField parent = new TextField(MAG.getLastParentAdvancement());
        parent.setTooltip(new Tooltip("Parent advancement"));
        final List<List<?>> criterions = new ArrayList<>();
        final Vbox2 vbox2 = new Vbox2();
        Button addCriterion = new Button("Add criterion");
        addCriterion.setOnAction(et -> {
            TextField criterion = new TextField("");
            criterion.setTooltip(new Tooltip("Criterion name"));
            ChoiceBox<AdvancementTrigger> advancementTriggerChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(AdvancementTrigger.values()));
            advancementTriggerChoiceBox.setTooltip(new Tooltip("Trigger id"));
            advancementTriggerChoiceBox.setOnAction(event1 -> {
                AdvancementTrigger trigger = advancementTriggerChoiceBox.getSelectionModel().getSelectedItem();
                switch (trigger) {
                    case INVENTORY_CHANGED:
                        Label items = new Label("Items:");
                        Vbox2 itemArray = new Vbox2();
                        TextField itemid = new TextField();
                        itemid.setTooltip(new Tooltip("Item identifier"));
                        TextField count = new TextField("1");
                        itemArray.getChildren().addAll(itemid, count);
                        final FlowPane flowPane = new FlowPane(itemArray);
                        Button addItem = new Button("Add item");
                        addItem.setOnAction(evnt -> {
                            TextField textField = new TextField();
                            textField.setTooltip(new Tooltip("Item identifier or tag (#x)"));
                            TextField itemAmount = new TextField("1");
                            itemAmount.setTooltip(new Tooltip("Exact number or range (x-y)"));
                            flowPane.getChildren().add(new Vbox2(textField, itemAmount));
                            mag.getTabPane().requestLayout();
                        });
                        vbox2.getChildren().addAll(new Hbox2(items, flowPane, addItem));
                        List<Region> criterionInfo = new ArrayList<>(Arrays.asList(criterion, advancementTriggerChoiceBox, flowPane));
                        criterions.add(criterionInfo);
                        mag.getTabPane().requestLayout();
                        break;
                    case KILLED_ENTITY:
                        Label entityLabel = new Label("Entity:");
                        TextField identifier1 = new TextField();
                        identifier1.setTooltip(new Tooltip("Entity identifier"));
                        Hbox2 container = new Hbox2(entityLabel, identifier1);
                        vbox2.getChildren().addAll(container);
                        criterions.add(new ArrayList<>(Arrays.asList(criterion, advancementTriggerChoiceBox, container)));
                        mag.getTabPane().requestLayout();
                        break;
                }
                MAG.setLastAdvancementTrigger(trigger.name());
            });
            Label label = new Label("Conditions:");
            vbox2.getChildren().addAll(criterion, advancementTriggerChoiceBox, label);
            mag.getTabPane().requestLayout();
        });

        Label rewards = new Label("Rewards:");
        final Vbox2 rewardsContainer = new Vbox2();
        Label experience = new Label("Experience");
        TextField exp = new TextField();
        exp.setTooltip(new Tooltip("Integer"));
        Button addReward = new Button("Add reward");
        addReward.setOnAction(evt -> {
            TextInputDialog rewardType = new TextInputDialog("loot");
            rewardType.setTitle("Reward type");
            rewardType.setHeaderText("'loot' or 'recipes'");
            Optional<String> click = rewardType.showAndWait();
            if (click.isPresent()) {
                String type = click.get();
                Label typeLabel = new Label(type);
                switch (type) {
                    case "loot":
                        final TextField lootTable = new TextField(MAG.getLastModId());
                        lootTable.setTooltip(new Tooltip("Loot table id"));
                        rewardsContainer.getChildren().addAll(typeLabel, lootTable);
                        break;
                    case "recipes":
                        TextField recipe = new TextField(MAG.getLastModId());
                        recipe.setTooltip(new Tooltip("Recipe id"));
                        rewardsContainer.getChildren().addAll(typeLabel, recipe);
                        break;
                    default:
                        new Alert2(Alert.AlertType.WARNING, "Invalid reward: " + type).show();
                }
                mag.getTabPane().requestLayout();
            }
        });

        Label label = new Label("Name/title");
        TextField title = new TextField();
        Label desclable = new Label("Short description");
        TextField description = new TextField();
        description.setTooltip(new Tooltip("How to obtain this advancement"));
        Label itemLabel = new Label("Display item");
        TextField displayItem = new TextField();
        displayItem.setTooltip(new Tooltip("Item id"));
        Button generate = new Button("Generate file");
        generate.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent evnt) {
                LinkedHashMap<String, Object> root = new LinkedHashMap<>();
                for (List<?> list : criterions) {
                    TextField name = (TextField) list.get(0);
                    ChoiceBox<AdvancementTrigger> trigger = (ChoiceBox<AdvancementTrigger>) list.get(1);
                    Pane conditions = (Pane) list.get(2);
                    AdvancementTrigger advancementTrigger = trigger.getSelectionModel().getSelectedItem();
                    Map<String, Object> objectMap = new LinkedHashMap<>(2);
                    String criterionName = name.getText();
                    switch (advancementTrigger) {
                        case INVENTORY_CHANGED:
                            List<Object> conds = new ArrayList<>();
                            conditions.getChildren().forEach(node -> {
                                LinkedHashMap<String, Object> map = new LinkedHashMap<>(1);
                                Vbox2 condition = (Vbox2) node;
                                TextField itemId = (TextField) condition.getChildren().get(0);
                                TextField count = (TextField) condition.getChildren().get(1);
                                String item = itemId.getText();
                                if (item.startsWith("#")) {
                                    String sub = item.substring(1);
                                    if (!sub.contains(":"))
                                        sub = "minecraft:" + sub;
                                    map.put("tag", sub);
                                } else {
                                    if (!item.contains(":")) {
                                        String s = "minecraft:" + item;
                                        map.put("item", s);
                                    } else
                                        map.put("item", item);
                                }
                                String cnt = count.getText();
                                LinkedHashMap<String, Object> linkedHashMap = new LinkedHashMap<>(1);
                                if (cnt.contains("-")) {
                                    String[] strings = cnt.split("-");
                                    linkedHashMap.put("min", strings[0]);
                                    linkedHashMap.put("max", strings[1]);
                                } else {
                                    if (Integer.parseInt(cnt) > 1) {
                                        linkedHashMap.put("min", Integer.parseInt(cnt));
                                    }
                                }
                                if (!linkedHashMap.isEmpty())
                                    map.put("count", linkedHashMap);
                                conds.add(map);
                            });
                            Map<String, Object> map1 = Utilities.singleEntryMap("items", conds);
                            objectMap.put("trigger", advancementTrigger.identifier);
                            objectMap.put("conditions", map1);
                            root.put(criterionName, objectMap);
                            break;
                        case KILLED_ENTITY:
                            TextField entityId = (TextField) conditions.getChildren().get(1);
                            String idString = entityId.getText();
                            Map<String, Object> mapp = new LinkedHashMap<>(2);
                            mapp.put("trigger", advancementTrigger.identifier);
                            mapp.put("conditions", Utilities.singleEntryMap("entity", Utilities.singleEntryMap("type", idString)));
                            root.put(criterionName, mapp);
                            break;
                    }
                }
                ArrayListMultimap<String, Object> listMultimap = ArrayListMultimap.create();
                ObservableList<Node> childrenUnmodifiable = rewardsContainer.getChildrenUnmodifiable();
                for (int i = 0; i < childrenUnmodifiable.size(); i += 2) {
                    Label node = (Label) childrenUnmodifiable.get(i);
                    TextField textField = (TextField) childrenUnmodifiable.get(i + 1);
                    listMultimap.put(node.getText(), textField.getText());
                }
                root = Utilities.singleEntryMap("criteria", root);
                LinkedHashMap<String, Object> hashMap = new LinkedHashMap<>(listMultimap.size());
                for (String key : listMultimap.keySet()) {
                    hashMap.put(key, listMultimap.get(key));
                }
                if (!exp.getText().isEmpty()) {
                    hashMap.put("experience", Integer.parseInt(exp.getText()));
                }
                if (!hashMap.isEmpty())
                    root.put("rewards", hashMap);
                if (!title.getText().isEmpty()) {
                    LinkedHashMap<String, Object> display = new LinkedHashMap<>(3);
                    display.put("title", title.getText());
                    display.put("description", description.getText());
                    display.put("icon", Utilities.singleEntryMap("item", displayItem.getText()));
                    root.put("display", display);
                }
                if (!parent.getText().isEmpty()) {
                    root.put("parent", parent.getText());
                    MAG.setLastParentAdvancement(parent.getText());
                }
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setInitialDirectory(new File(MAG.getLastResourceFolder()));
                File file = directoryChooser.showDialog(MAG.getMainStage());
                if (file != null) {
                    File jsonFile = new File(file, identifier.getText() + ".json");
                    Utilities.createJsonFile(jsonFile.toPath(), Utilities.formatJson(root));
                    MAG.setLastResourceFolder(file.getAbsolutePath());
                    File p = file.getParentFile();
                    while (p.getParentFile() != null) {
                        if (p.getName().equals("data")) {
                            break;
                        }
                        p = p.getParentFile();
                    }
                    MAG.setLastModId(p.listFiles()[0].getName());
                    new Alert2(Alert.AlertType.INFORMATION, "Generated file " + jsonFile).show();
                }
            }

        });
        vbox2.getChildren().addAll(identifier, parent, new Vbox2(new Hbox2(label, title), new Hbox2(desclable, description), new Hbox2(itemLabel, displayItem)), new Hbox2(addCriterion), new Hbox2(rewards, experience, exp, addReward), rewardsContainer, generate);
        Tab tab = new Tab(MAG.getLastModId() + identifier.getText(), new ScrollPane(vbox2));
        tab.setTooltip(new Tooltip(MAG.getLastResourceFolder()));
        mag.getTabPane().getTabs().add(tab);
    }
}