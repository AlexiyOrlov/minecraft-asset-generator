package alexiy.minecraft.assetgenerator

enum ItemModels {
    MIRROREDITEM('item/handheld', 'For handheld items'),
    BASICITEM('item/generated', 'For most items')

    public String value, description

    ItemModels(String value_, String description_) {
        value = value_
        description = description_
    }
}