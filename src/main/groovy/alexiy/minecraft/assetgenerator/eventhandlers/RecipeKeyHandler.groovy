package alexiy.minecraft.assetgenerator.eventhandlers

import alexiy.minecraft.asset.generator.Recipes
import com.google.common.collect.TreeBasedTable
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.*

/**
 * Created on 12/27/17.
 */
class RecipeKeyHandler implements EventHandler<ActionEvent> {
    ChoiceBox<String> craftingtype
    List<String> jsonpattern, keys
    TreeBasedTable<Integer, Integer, Object> parameters
    TreeBasedTable<Integer, Integer, TextField> recipeLayout
    Button generateFile

    RecipeKeyHandler(ChoiceBox<String> types, List<String> jsonPattern, List<String> keys_,
                     TreeBasedTable<Integer, Integer, Object> parameterTable, TreeBasedTable<Integer, Integer, TextField> layout,
                     Button generate) {
        craftingtype = types
        jsonpattern = jsonPattern
        keys = keys_
        parameters = parameterTable
        recipeLayout = layout
        generateFile = generate
    }

    @Override
    void handle(ActionEvent event) {
        String type = craftingtype.getSelectionModel().getSelectedItem()
        jsonpattern.clear()
        keys.clear()
        for (i in 0..8) {
            Label label = parameters.get(i, 0) as Label
            label.setText('')
        }
        if (type == Recipes.SHAPED.name) {
            String element = ''
            int count = 0
            for (i in 0..2) {
                for (j in 0..2) {
                    TextField key = recipeLayout.get(i, j)
                    if (key.text) {
                        String t = key.text
                        if (t.startsWith('/')) {
                            t = t.substring(1)
                        }
                        if (!keys.contains(t)) keys.add(t)
                        element += t
                    } else element += ' '
                    count++
                    if (count == 3 && element != '   ') {
                        jsonpattern.add(element)
                        element = ''
                        count = 0
                    }
                }
            }

            for (int i = 0; i < keys.size(); i++) {
                Label keylabel = parameters.get(i, 0) as Label
                String key = keys.get(i)
                if (key.startsWith('/')) {
                    key = key.substring(1)
                }
                keylabel.setText(key)
//                println(parameters.get(i,2))
            }
//                                    println(keys)
//            println(jsonpattern)
        } else if (type == Recipes.SHAPELESS.name) {

            for (i in 0..2) {
                for (j in 0..2) {
                    TextField textField = recipeLayout.get(i, j)
                    if (textField.text) {
                        String identifier = textField.text
                        if (!identifier.startsWith('/')) {
                            if (!identifier.contains(':'))
                                identifier = 'minecraft:' + identifier
                        }
                        keys.add(identifier)

                    }
                }
            }
            for (i in 0..8) {
                TextField textField = parameters.get(i, 1) as TextField
                textField.setText('')
            }

//                                    println(keys)
            for (int i = 0; i < keys.size(); i++) {
                TextField idtextfield = parameters.get(i, 1) as TextField
                String identifier = keys.get(i)

                if (identifier.startsWith('/')) {

                } else if (identifier.contains('/')) {
                    String[] parts = identifier.split('/')
                    if (parts.length == 2) {
                        Spinner spinner = parameters.get(i, 2) as Spinner
                        spinner.getValueFactory().setValue(parts[1].toInteger())
                    }
                    identifier = identifier.substring(0, identifier.indexOf('/'))
                }
                idtextfield.setText(identifier)
            }

        }
        generateFile.setDisable(false)

    }
}
