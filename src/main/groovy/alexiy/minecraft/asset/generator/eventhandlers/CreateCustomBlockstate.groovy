package alexiy.minecraft.asset.generator.eventhandlers

import alexiy.minecraft.assetgenerator.MAG
import alexiy.minecraft.assetgenerator.Utilities
import com.google.common.collect.Sets
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.stage.DirectoryChooser
import org.knowbase.Hbox2
import org.knowbase.Vbox2

import java.nio.file.Path
import java.nio.file.Paths

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
                TextField fullId = new TextField(id)
                fullId.setTooltip(new Tooltip('Full block identifier'))
                TextField listOfProperties = new TextField()
                listOfProperties.setPromptText('Properties')
                listOfProperties.setTooltip(new Tooltip('Write comma-separated blockstate properties in corresponding order'))
                GridPane grid = new GridPane()
                grid.setVgap(6)
                grid.setHgap(6)
                Button setOutput = new Button('Set destination')
                setOutput.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    void handle(ActionEvent evt) {
                        DirectoryChooser directoryChooser = new DirectoryChooser()
                        directoryChooser.setInitialDirectory(new File(MAG.lastResourceFolder))
                        def selection = directoryChooser.showDialog(MAG.mainStage)
                        if (selection) {
                            outputPath = selection
                            resourcePath.setText(selection.canonicalPath)
                            MAG.lastResourceFolder = selection.canonicalPath
                        }
                    }
                })
                Button next = new Button('Next')
                Vbox2 content = new Vbox2(fullId, listOfProperties, new Hbox2(setOutput, resourcePath), next, grid)
                next.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    void handle(ActionEvent evt) {
                        if (fullId.text && listOfProperties.text) {
                            def properties = listOfProperties.text.split(',')
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
                                                sets.add(set2)
                                            }
                                        }
                                        Set<List<String>> product = Sets.cartesianProduct(sets) as Set<List<String>>
                                        def map = [:]
                                        product.each { it ->
                                            String variant = ''
                                            it.each { s ->
                                                variant += "$s,"
                                            }
                                            map.put(variant.substring(0, variant.length() - 1), ['model': fullId.text])
                                        }
                                        map = ['variants': map]
                                        String output = Utilities.formatJson(map)
                                        //array is easier than sub-strings
                                        String[] fullid = fullId.text.split(':')
                                        Path path = Paths.get(resourcePath.text, 'assets', fullid[0], 'blockstates', fullid[1] + '.json')
                                        Utilities.createJsonFile(path, output)
                                        MAG.lastResourceFolder = resourcePath.text
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
