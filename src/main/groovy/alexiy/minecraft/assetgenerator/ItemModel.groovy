package alexiy.minecraft.assetgenerator

enum ItemModel {
    HELD_ITEM('item/handheld', 'For handheld items'),
    SIMPLE_ITEM('item/generated', 'For most items')

    public String value, description

    ItemModel(String value_, String description_) {
        value = value_
        description = description_
    }
}