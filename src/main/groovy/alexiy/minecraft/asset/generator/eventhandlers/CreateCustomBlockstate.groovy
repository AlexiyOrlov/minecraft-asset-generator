package alexiy.minecraft.asset.generator.eventhandlers

import alexiy.minecraft.assetgenerator.MAG
import com.google.common.collect.Sets
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import org.knowbase.Vbox2

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
                TextField tf = new TextField()
                tf.setPromptText('Properties')
                tf.setTooltip(new Tooltip('Write comma-separated blockstate properties in corresponding order'))
                GridPane grid = new GridPane()
                grid.setVgap(6)
                grid.setHgap(6)
                Button next = new Button('Next')
                Vbox2 content = new Vbox2(textField, tf, next, grid)
                next.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    void handle(ActionEvent evt) {
                        if (textField.text && tf.text) {
                            def properties = tf.text.split(',')
                            if (properties) {
                                properties.eachWithIndex { it, index ->
                                    TextField values = new TextField()
                                    values.setPromptText('Values')
                                    values.setTooltip(new Tooltip('Write possible comma-separated values of properties'))
                                    grid.add(new Label(it), 0, index); grid.add(values, 1, index)
                                }
                                Button generate = new Button('Generate')
                                generate.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    void handle(ActionEvent ev) {
                                        def sets = []
                                        grid.childrenUnmodifiable.eachWithIndex { it, index ->
                                            if (it instanceof Label) {
                                                Label label = it
                                                String property = label.text
                                                TextField values = grid.childrenUnmodifiable.get(index + 1) as TextField
                                                String[] strings = values.text.split(',')
                                                Set<String> set = strings.toList().toSet()
                                                HashSet set2 = []
                                                set.each { s ->
                                                    set2.add("$property=$s")
                                                }
                                                println(set2)
                                                sets.add(set2)
                                            }
                                        }
                                        Set<List<String>> product = Sets.cartesianProduct(sets) as Set<List<String>>
                                        def map = [:]
                                        product.each { it ->
                                            String variant
                                            it.each { s ->
                                            }
                                        }
                                        println(product)
                                    }
                                })
                                content.children.add(generate)
                                mag.tabPane.requestLayout() //this makes added elements visible
                            }
                        }
                    }
                })
                Tab tab = new Tab("Custom blockstate ($id)", content)
                mag.tabPane.getTabs().add(tab)
            }
        }
    }
}
