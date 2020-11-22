package alexiy.minecraft.asset.generator;

public enum LootTableType {
    CHEST("chest"),
    ADVANCEMENT_REWARD("advancement_reward");
    public String type;

    LootTableType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
