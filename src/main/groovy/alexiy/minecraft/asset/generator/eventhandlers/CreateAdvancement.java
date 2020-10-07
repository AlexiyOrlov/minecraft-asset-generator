package alexiy.minecraft.asset.generator.eventhandlers;

import alexiy.minecraft.assetgenerator.MAG;
import alexiy.minecraft.assetgenerator.Utilities;
import com.google.common.collect.ArrayListMultimap;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.stage.DirectoryChooser;
import org.knowbase.Alert2;
import org.knowbase.Hbox2;
import org.knowbase.Vbox2;

import java.io.File;
import java.util.*;

public class CreateAdvancement implements EventHandler<ActionEvent> {
    public CreateAdvancement(MAG mag_) {
        mag = mag_;
    }

    @Override
    public void handle(ActionEvent event) {
        TextField identifier = new TextField();
        identifier.setTooltip(new Tooltip("Identifier"));
        final List<List<?>> criterions = new ArrayList<>();
        final Vbox2 vbox2 = new Vbox2();
        Button addCriterion = new Button("Add criterion");
        addCriterion.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent et) {
                TextField criterion = new TextField("");
                criterion.setTooltip(new Tooltip("Criterion name"));
                TextField trigger = new TextField(getPreviousTrigger());
                trigger.setTooltip(new Tooltip("Trigger id"));
                Label label = new Label("Conditions:");
                Label items = new Label("Items:");
                Vbox2 itemArray = new Vbox2();
                TextField itemid = new TextField();
                itemid.setTooltip(new Tooltip("Item identifier"));
                TextField count = new TextField("1");
                itemArray.getChildren().addAll(itemid, count);
                final FlowPane flowPane = new FlowPane(itemArray);
                vbox2.getChildren().addAll(criterion, trigger, label, new Hbox2(items, flowPane));
                Button addItem = new Button("Add item");
                addItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent evnt) {
                        TextField textField = new TextField();
                        textField.setTooltip(new Tooltip("Item identifier or tag (#x)"));
                        TextField itemAmount = new TextField("1");
                        itemAmount.setTooltip(new Tooltip("Exact number or range (x-y)"));
                        flowPane.getChildren().add(new Vbox2(textField, itemAmount));
                        mag.getTabPane().requestLayout();
                    }

                });
                vbox2.getChildren().addAll(addItem);
                List<Region> criterionInfo = new ArrayList<>(Arrays.asList(criterion, trigger, flowPane));
                criterions.add(criterionInfo);
                mag.getTabPane().requestLayout();
            }

        });

        Label rewards = new Label("Rewards:");
        final Vbox2 rewardsContainer = new Vbox2();
        Label experience = new Label("Experience");
        TextField exp = new TextField();
        exp.setTooltip(new Tooltip("Integer"));
        Button addReward = new Button("Add reward");
        addReward.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent evt) {
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
                    TextField trigger = (TextField) list.get(1);
                    FlowPane conditions = (FlowPane) list.get(2);
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
                                linkedHashMap.put("max", 64);
                            }
                        }
                        if (!linkedHashMap.isEmpty())
                            map.put("count", linkedHashMap);
                        conds.add(map);
                    });
                    Map<String, Object> map1 = Utilities.singleEntryMap("items", conds);
                    Map<String, Object> objectMap = new LinkedHashMap<>(2);
                    objectMap.put("trigger", trigger.getText());
                    objectMap.put("conditions", map1);
                    root.put(name.getText(), objectMap);
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
                root.put("rewards", hashMap);
                if (!title.getText().isEmpty()) {
                    LinkedHashMap<String, Object> display = new LinkedHashMap<>(3);
                    display.put("title", title.getText());
                    display.put("description", description.getText());
                    display.put("icon", Utilities.singleEntryMap("item", displayItem.getText()));
                    root.put("display", display);
                }
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setInitialDirectory(new File(MAG.getLastResourceFolder()));
                File file = directoryChooser.showDialog(MAG.getMainStage());
                if (file != null) {
                    File jsonFile = new File(file, identifier.getText() + ".json");
                    Utilities.createJsonFile(jsonFile.toPath(), Utilities.formatJson(root));
                    MAG.setLastResourceFolder(file.getAbsolutePath());
                    MAG.setLastModId(file.getParentFile().getName());
                    new Alert2(Alert.AlertType.INFORMATION, "Generated file " + jsonFile).show();
                }
            }

        });
        vbox2.getChildren().addAll(identifier, new Vbox2(new Hbox2(label, title), new Hbox2(desclable, description), new Hbox2(itemLabel, displayItem)), new Hbox2(addCriterion), new Hbox2(rewards, experience, exp, addReward), rewardsContainer, generate);
        Tab tab = new Tab(MAG.getLastModId() + identifier.getText(), vbox2);
        mag.getTabPane().getTabs().add(tab);

    }

    public String getPreviousTrigger() {
        return previousTrigger;
    }

    public void setPreviousTrigger(String previousTrigger) {
        this.previousTrigger = previousTrigger;
    }

    private String previousTrigger;
    private MAG mag;
}

//class CreateAdvancement implements EventHandler<ActionEvent> {
//    String previousTrigger;
//    private MAG mag;
//
//    CreateAdvancement(MAG mag_) {
//        mag=mag_;
//    }
//
//    @Override
//    void handle(ActionEvent event) {
//        TextField identifier = new TextField("$MAG.lastModId:")
//        List<List> criterions=[]
//        def vbox2 = new Vbox2()
//        Button addCriterion = new Button('Add criterion')
//        addCriterion.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            void handle(ActionEvent et) {
//                TextField criterion = new TextField('')
//                criterion.setTooltip(new Tooltip('Criterion name'))
//                TextField trigger = new TextField(previousTrigger)
//                trigger.setTooltip(new Tooltip('Trigger id'))
//                Label label = new Label('Conditions:')
//                Label items = new Label('Items:')
//                Vbox2 itemArray = new Vbox2()
//                TextField itemid = new TextField()
//                itemid.setTooltip(new Tooltip('Item identifier'))
//                TextField count = new TextField('1')
//                itemArray.getChildren().addAll(itemid, count)
//                def flowPane = new FlowPane(itemArray)
//                vbox2.getChildren().addAll(criterion,trigger,label, new Hbox2(items, flowPane))
//                Button addItem = new Button('Add item')
//                addItem.setOnAction(new EventHandler<ActionEvent>() {
//                    @Override
//                    void handle(ActionEvent evnt) {
//                        def textField = new TextField()
//                        textField.setTooltip(new Tooltip('Item identifier or tag (#x)'))
//                        def itemAmount = new TextField('1')
//                        itemAmount.setTooltip(new Tooltip("Exact number or range (x-y)"))
//                        flowPane.getChildren().add(new Vbox2(textField, itemAmount))
//                        mag.tabPane.requestLayout()
//                    }
//                })
//                vbox2.getChildren().addAll(addItem)
//                def criterionInfo=[criterion,trigger,flowPane]
//                criterions.add(criterionInfo)
//                mag.tabPane.requestLayout()
//            }
//        })
//
//        Label rewards = new Label('Rewards')
//        Vbox2 rewardsContainer = new Vbox2()
//        Button addReward = new Button('Add reward')
//        addReward.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            void handle(ActionEvent evt) {
//                TextInputDialog rewardType = new TextInputDialog('loot')
//                rewardType.setTitle("Reward type")
//                rewardType.setHeaderText("'loot','recipes','function',recipes")
//                def click = rewardType.showAndWait();
//                if (click.isPresent()) {
//                    String type = click.get();
//                    Label typeLabel = new Label(type)
//                    switch (type) {
//                        case 'loot':
//                            TextField lootTable = new TextField(MAG.lastModId)
//                            lootTable.setTooltip(new Tooltip('Loot table id'))
//                            Button addLootTable = new Button('Add loot table')
//                            rewardsContainer.getChildren().addAll(typeLabel, lootTable)
//                            addLootTable.setOnAction(new EventHandler<ActionEvent>() {
//                                @Override
//                                void handle(ActionEvent ent) {
//                                    rewardsContainer.getChildren().add(new TextField(lootTable.getText()))
//                                    mag.tabPane.requestLayout()
//                                }
//
//                            })
//                            vbox2.getChildren().add(addLootTable)
//                            break
//                        default:
//                            new Alert2(Alert.AlertType.WARNING, "Invalid reward: $type").show()
//                    }
//                    mag.tabPane.requestLayout()
//                }
//            }
//        })
//        Button generate=new Button('Generate file')
//        generate.setOnAction(new EventHandler<ActionEvent>() {
//            def map=[:];
//            @Override
//            void handle(ActionEvent evnt) {
//                criterions.forEach(new Consumer<List>() {
//                    @Override
//                    void accept(List list) {
//                        TextField name=list.get(0);
//                        TextField trigger=list.get(1)
//                        FlowPane conditions=list.get(2);
//                        def conds=[]
//                        def map=[]
//                        conditions.getChildren().forEach(new Consumer<Node>() {
//                            @Override
//                            void accept(Node node) {
//                                Vbox2 condition=node as Vbox2
//                                TextField itemId=condition.getChildren().get(0)as TextField
//                                TextField count=condition.getChildren().get(1)as TextField
//                                String item=itemId.getText()
//                                if(!item.contains(':'))
//                                    item='minecraft:'+item
//                                String cnt=count.getText()
//                                def itm=[item:item]
//                                if(item.startsWith('#'))
//                                    itm=[tag:item.substring(1)]
//                                if(cnt.contains('-'))
//                                {
//                                    String[] strings=cnt.split('-')
//                                    itm+=[count:[min:strings[0],max:strings[1]]]
//                                }
//                                else if(cnt.toInteger()>1)
//                                {
//                                    itm+=[count: [min: cnt.toInteger(),max: 64]]
//                                }
//                                map.add(itm)
//                            }
//                        })
//                        map=[items:map]
//                        map=[conditions:map]
//                        map+=[trigger:trigger.getText()]
//                        map=[name.getText():map]
//                        println(Utilities.formatJson(map));
//                    };
//                });
//            };
//        })
//        vbox2.getChildren().addAll(new Hbox2(identifier, addCriterion), new Hbox2(rewards, addReward), rewardsContainer,generate)
//        Tab tab = new Tab(identifier.getText(), vbox2)
//        mag.tabPane.getTabs().add(tab)
//
//    }
//}
