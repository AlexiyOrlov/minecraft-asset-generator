package alexiy.minecraft.assetgenerator
/**
 * Created on 8/7/17.
 */
enum AssetConstants {
    ASSETS('assets'),
    MODELS_LITERAL('models'),
    @Deprecated
    BLOCKMODEL('block'),
    @Deprecated
    ITEMMODEL('item'),
    BLOCKSTATES('blockstates'),
    TEXTURES('textures'),
    BLOCKTEXTURE('blocks'),
    ITEMTEXTURE('items'),
    BLOCK_LITERAL('block'),
    ITEM_LITERAL('item'),
    DATA('data'),
    LOOT_TABLES('loot_tables'),
    BLOCKS_LITERAL('blocks')

    public String value

    AssetConstants(String v) {
        value = v
    }
}




