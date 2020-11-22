package alexiy.minecraft.asset.generator;

public enum LootEntryType {
    ITEM("minecraft:item"),
    TAG("minecraft:tag");
    public String type;

    LootEntryType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}