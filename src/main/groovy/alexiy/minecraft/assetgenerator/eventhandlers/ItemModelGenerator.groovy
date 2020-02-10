package alexiy.minecraft.assetgenerator.eventhandlers

import alexiy.minecraft.assetgenerator.AssetConstants
import alexiy.minecraft.assetgenerator.ItemModels
import alexiy.minecraft.assetgenerator.JAG
import alexiy.minecraft.assetgenerator.Utilities
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.*

/**
 * Created on 12/27/17.
 */
class ItemModelGenerator implements EventHandler<ActionEvent> {
    ChoiceBox<String> parentmodels, mcver
    TextField modidentifier, objectidentifier
    Tab itemtab

    ItemModelGenerator(ChoiceBox<String> modelChoice, TextField modid, TextField itemIdentifier, ChoiceBox<String> version, Tab currentTab) {
        parentmodels = modelChoice
        modidentifier = modid
        objectidentifier = itemIdentifier
        itemtab = currentTab
        mcver = version;
        mcver.selectionModel.select(1)
    }

    @Override
    void handle(ActionEvent event) {
        String parent
        parent = parentmodels.getSelectionModel().getSelectedItem()
        String modid = modidentifier.text
        String itemid = objectidentifier.text
        if (modid && itemid && JAG.resourceDirectory) {
            File model = new File(JAG.resourceDirectory.absolutePath + "/$AssetConstants.ASSETS.value/$modid/$AssetConstants.MODELS_LITERAL.value/$AssetConstants.ITEMMODEL.value/", itemid + '.json')
            if (model.parent) model.parentFile.mkdirs()
            if (model.createNewFile()) {
                String version = mcver.selectionModel.getSelectedItem();
                if (parent == ItemModels.MIRROREDITEM.value) {
                    String text
                    if (version == '1.12')
                        text = Utilities.formatJson([parent: ItemModels.MIRROREDITEM.value, textures: [layer0: "$modid:$AssetConstants.ITEMTEXTURE.value/$itemid"]])
                    else if (version == '1.14')
                        text = Utilities.formatJson([parent: ItemModels.MIRROREDITEM.value, textures: [layer0: "$modid:$AssetConstants.ITEMMODEL.value/$itemid"]])

                    model.setText(text)
                } else if (parent == ItemModels.BASICITEM.value) {
                    String text3
                    if (version == '1.12')
                        text3 = Utilities.formatJson([parent: ItemModels.BASICITEM.value, textures: [layer0: "$modid:$AssetConstants.ITEMTEXTURE.value/$itemid"]])
                    else if (version == '1.14')
                        text3 = Utilities.formatJson([parent: ItemModels.BASICITEM.value, textures: [layer0: "$modid:$AssetConstants.ITEMMODEL.value/$itemid"]])
                    model.setText(text3)
                }
                Alert alert = new Alert(Alert.AlertType.INFORMATION, '')
                alert.setHeaderText("Generated " + "$model.absolutePath")
                alert.show()
                itemtab.setText(itemid)
                itemtab.setTooltip(new Tooltip(model.absolutePath))
            }
        }
    }
}
