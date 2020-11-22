package alexiy.minecraft.asset.generator;

public enum AdvancementTrigger {
    INVENTORY_CHANGED("minecraft:inventory_changed"),
    KILLED_ENTITY("minecraft:player_killed_entity");

    public String identifier;

    AdvancementTrigger(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String toString() {
        return identifier;
    }
}
