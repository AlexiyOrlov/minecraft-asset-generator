package alexiy.minecraft.asset.generator;

/**
 * Created on 8/7/17.
 */
public enum AssetConstants {
    ASSETS("assets"), MODELS_LITERAL("models"), BLOCKMODEL("block"), ITEMMODEL("item"), BLOCKSTATES("blockstates"), TEXTURES("textures"), BLOCKTEXTURE("blocks"), ITEMTEXTURE("items"), BLOCK_LITERAL("block"), ITEM_LITERAL("item"), DATA("data"), LOOT_TABLES("loot_tables"), BLOCKS_LITERAL("blocks");

    AssetConstants(String v) {
        value = v;
    }

    public String value;
}
