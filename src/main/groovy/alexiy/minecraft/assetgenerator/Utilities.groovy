package alexiy.minecraft.assetgenerator

import alexiy.minecraft.asset.generator.eventhandlers.MinecraftVersion
import groovy.json.JsonOutput
import javafx.scene.control.Label

import java.nio.file.Path

/**
 * Created on 12/24/17.
 */
class Utilities {

    static final String BLOCK = 'block', ITEM = 'item', LOOT_TABLES = 'loot_tables', RECIPES = 'recipes', ITEMS = 'items'

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

    static void createAssetFoldersIfNeeded(final String minecraftVersion, final File resourceFolder, final String modIdentifier) {
        final File dataRoot = new File(resourceFolder, 'data')
        final File dataModID = new File(dataRoot, modIdentifier)
        if (minecraftVersion == MinecraftVersion.V1_15.version) {
            dataModID.mkdirs()
        }

        final File assetRoot = new File(resourceFolder, 'assets')
        assetRoot.mkdir()

        final File assetmodId = new File(assetRoot, modIdentifier)
        assetmodId.mkdir()

        File advancements = new File(assetmodId, 'advancements')
        advancements.mkdir()

        File blockstates = new File(assetmodId, 'blockstates')
        blockstates.mkdir()

        File translation = new File(assetmodId, 'lang')
        translation.mkdir()

        File lootTables = null
        if (minecraftVersion == MinecraftVersion.V1_12.version) {
            lootTables = new File(assetmodId, LOOT_TABLES)
        } else if (minecraftVersion == MinecraftVersion.V1_15.version) {
            lootTables = new File(dataModID, LOOT_TABLES)
        }
        lootTables.mkdir()
        File entities = new File(lootTables, 'entities')
        entities.mkdir()
        File blocks = new File(lootTables, 'blocks')
        blocks.mkdir()

        File models = new File(assetmodId, 'models')
        models.mkdir()
        File blockModels = new File(models, BLOCK)
        blockModels.mkdir()
        File itemModels = new File(models, ITEM)
        itemModels.mkdir()

        File recipes = null
        if (minecraftVersion == MinecraftVersion.V1_12.version) {
            recipes = new File(assetmodId, RECIPES)
        } else if (minecraftVersion == MinecraftVersion.V1_15.version) {
            recipes = new File(dataModID, RECIPES)
        }
        recipes.mkdir()



    }
}
