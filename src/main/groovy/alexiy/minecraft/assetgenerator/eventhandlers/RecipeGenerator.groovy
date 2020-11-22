package alexiy.minecraft.assetgenerator.eventhandlers

import alexiy.minecraft.asset.generator.AssetConstants
import alexiy.minecraft.asset.generator.Recipes
import alexiy.minecraft.assetgenerator.Alert2
import alexiy.minecraft.assetgenerator.JAG
import alexiy.minecraft.assetgenerator.Utilities
import com.google.common.collect.TreeBasedTable
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.*

import java.nio.file.Paths

/**
 * Created on 12/27/17.
 */
class RecipeGenerator implements EventHandler<ActionEvent> {
    ChoiceBox<String> craftingtype
    TextField modidentifier, resultitem, customFilename
    List<String> jsonpattern, keys
    TreeBasedTable<Integer, Integer, Object> parameters
    CheckBox customname
    ChoiceBox<String> version

    RecipeGenerator(ChoiceBox<String> types, TextField modid, TextField result, customFileName,
                    List<String> jsonPattern, List<String> keyList, TreeBasedTable<Integer, Integer, Object> parameterTable, CheckBox useCustomName, ChoiceBox<String> minecraftVersion) {
        craftingtype = types
        modidentifier = modid
        resultitem = result
        resultitem.setText(modidentifier.getText() + ':')
        customFilename = customFileName
        jsonpattern = jsonPattern
        keys = keyList
        parameters = parameterTable
        customname = useCustomName
        version = minecraftVersion
        version.selectionModel.select(1)
    }

    @Override
    void handle(ActionEvent event) {
        def type = craftingtype.getSelectionModel().getSelectedItem()
        String modid = modidentifier.text
        String mcvers = version.getSelectionModel().getSelectedItem()
        if (modid && resultitem.getText() && !resultitem.getText().endsWith(':')) {
            JAG.lastModid = modid
            //TODO tags; other recipes
            if (type == Recipes.SHAPED.name) {
                def jsonrecipe = [type: 'minecraft:crafting_shaped', pattern: jsonpattern]
                def jsonkeys = [:]
                for (i in 0..8) {
                    Label keylable = parameters.get(i, 0) as Label
                    TextField item = parameters.get(i, 1) as TextField
                    if (keylable.text && item.text) {
                        String itemname = item.text
                        if (itemname.startsWith('/')) {
                            Utilities.putOreKey(jsonkeys, itemname, keylable)
                        } else {
                            if (!itemname.contains(':'))
                                itemname = 'minecraft:' + itemname
                            Spinner<Short> metadata = parameters.get(i, 2) as Spinner
                            if (mcvers == '1.12') {
                                //FIXME meta doesn't change when writing the value directly in the spinner
                                jsonkeys.putAll([(keylable.text): [item: itemname, data: metadata.value]])
                            } else if (mcvers == '1.14')
                                jsonkeys.putAll([(keylable.text): [item: itemname]])
                        }
                    }
                }
                jsonrecipe.put('key', jsonkeys)

                String result = resultitem.text
                if (resultitem)
                    result = Utilities.splitParameter(result, jsonrecipe)
//                                            println(result)
                String r = Utilities.formatJson(jsonrecipe)
//                                            println(r)
                if (customname.selected && customFilename.text)
                    result = customFilename.text

                File recipeFile
                if (mcvers == '1.12')
                    recipeFile = Utilities.createJsonFile(Paths.get(JAG.resourceDirectory.absolutePath, AssetConstants.ASSETS.value, modid, 'recipes', result.substring(result.indexOf(':') + 1) + '.json'), r)
                else if (mcvers == '1.14')
                    recipeFile = Utilities.createJsonFile(Paths.get(JAG.resourceDirectory.absolutePath, AssetConstants.DATA.value, modid, 'recipes', result.substring(result.indexOf(':') + 1) + '.json'), r)
                Alert2 alert2 = new Alert2(Alert.AlertType.INFORMATION, null, "Created $recipeFile.absolutePath")
                alert2.show()

            } else if (type == Recipes.SHAPELESS.name) {
                HashMap<String, Object> json = [type: 'minecraft:crafting_shapeless']
                def ingredients = []
                keys.each {
                    Spinner<Short> metadata = parameters.get(keys.indexOf(it), 2) as Spinner
                    if (it.startsWith('/'))
                        ingredients.add([type: 'forge:ore_dict', ore: it.substring(1)])
                    else {
                        if (mcvers == '1.12')
                            ingredients.add([item: it, data: metadata.getValue()])
                        else if (mcvers == '1.14')
                            ingredients.add([item: it])
                    }
                }
                json.put('ingredients', ingredients)

                String result = resultitem.text
                def rj
                if (result.contains('/')) {
                    def array = result.split('/')
                    if (array.length == 2) {
                        rj = [item: array[0], count: array[1].toShort()]
                    } else if (array.length == 3) {
                        rj = [item: array[0], count: array[1].toShort(), data: array[2].toShort()]
                    }
                    json.put('result', rj)
                    result = result.substring(0, result.indexOf('/'))
                } else
                    json.put('result', [item: resultitem.text])
                String string = Utilities.formatJson(json)
                if (customname.selected && customFilename.text)
                    result = customFilename.text
                File recipefile
                if (mcvers == "1.12")
                    recipefile = Utilities.createJsonFile(Paths.get(JAG.resourceDirectory.absolutePath, AssetConstants.ASSETS.value, modid, 'recipes', result.substring(result.indexOf(':') + 1) + '.json'), string)
                else if (mcvers == '1.14')
                    recipefile = Utilities.createJsonFile(Paths.get(JAG.resourceDirectory.absolutePath, AssetConstants.DATA.value, modid, 'recipes', result.substring(result.indexOf(':') + 1) + '.json'), string)

                Alert2 alert2 = new Alert2(Alert.AlertType.INFORMATION, null, "Created $recipefile.absolutePath")
                alert2.show()
            }

        }

    }
}
