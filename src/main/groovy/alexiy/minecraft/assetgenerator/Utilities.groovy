package alexiy.minecraft.assetgenerator


import groovy.json.JsonOutput
import javafx.scene.control.Label

import java.nio.file.Path

/**
 * Created on 12/24/17.
 */
class Utilities {
    static String splitParameter(String result, HashMap<String, Object> json) {
        if (result.contains('/')) {
            def rj
            def array = result.split('/')
            if (array.length == 2) {
                rj = [item: array[0], count: array[1].toShort()]
            } else if (array.length == 3) {
                rj = [item: array[0], count: array[1].toShort(), data: array[2].toShort()]
            }
            json.put('result', rj)
            result = result.substring(0, result.indexOf('/'))
            return result
        } else {
            json.put('result', [item: result])
        }
        return result
    }

    static void putOreKey(Map recipeMap, String itemName, Label keyLabel) {
        if (itemName.startsWith('/')) {
            recipeMap.putAll([(keyLabel.text): [type: 'forge:ore_dict', ore: itemName.substring(1)]])

        }
    }

    static String formatJson(Object ob) {
        JsonOutput.prettyPrint(JsonOutput.toJson(ob))
    }

    static String createDirectionalBlockModel(String modid, String identifier, String version) {
        if (version == '1.12')
            return formatJson([parent: "block/cube_bottom_top", textures: [top   : modid + ":blocks/$identifier",
                                                                           side  : modid + ":blocks/$identifier" + "_side",
                                                                           bottom: modid + ":blocks/$identifier"]])
        else if (version == '1.14') {
            return formatJson([parent: "block/cube_bottom_top", textures:
                    [top   : modid + ":block/$identifier",
                     side  : modid + ":block/$identifier" + "_side",
                     bottom: modid + ":block/$identifier"]
            ])
        }
        return null
    }

    static String createSimpleBlockState(String modid, String blockidentifier, String mversion) {
        if (mversion == "1.12")
            return formatJson([variants: [normal: [model: "$modid:$blockidentifier"]]])
        else if (mversion == "1.14")
            return formatJson([variants: [(""): [model: "$modid:$AssetConstants.BLOCKMODEL.value/$blockidentifier"]]])
    }

    static String createSimpleBlockItemModel(String modid, String identifier) {
        formatJson([parent: "$modid:$AssetConstants.BLOCKMODEL.value/$identifier"])
    }

    static String createBlockInventoryModel(String modid, String identifier) {
        formatJson([parent: "$modid:$AssetConstants.BLOCKMODEL.value/$identifier" + '_inventory'])
    }

    /**Creates a Json file (dirs included) and sets its content*/
    static File createJsonFile(Path name, String content) {
        File file = name.toFile()
        if (file.parentFile) file.parentFile.mkdirs()
        if (file.createNewFile()) {
            file.setText(content)
            return file
        }
        return null
    }
}
