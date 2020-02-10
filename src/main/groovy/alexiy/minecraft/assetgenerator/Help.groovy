package alexiy.minecraft.assetgenerator

/**
 * Created on 12/25/17.
 */
class Help {
    final static def recipe = "Window layout:\n" +
            " - recipe type\n" +
            " - recipe pattern\n" +
            " - mod identifier field\n" +
            " - recipe key map, which consists of 9 rows \n" +
            " - result text field\n" +
            " - custom file name field\n" +
            " - creation button"
    final static def FOR_ALL_RECIPES = "Inputs go to recipe pattern fields. After filling the inputs, press 'Store keys " +
            "and values' button. Then set the parameters as needed. After that, 'Select resources folder'. Current output" +
            "folder path is shown in the button's tooltip. Then fill the result text field - write the object's " +
            "registry name and, if necessary, result's count and metadata prepended by '/'. Check the 'Custom file name'" +
            " if you want, otherwise, the file's name will be result's resource path. Finally, press 'Create'."
    final static def SHAPELESS_RECIPE = "In 'Shapeless' recipe mode you have to write input items into recipe pattern. " +
            "Typically these are items' registry names. You can omit 'minecraft' domain for vanilla items - it will be " +
            "added automatically if not present. To use ores, type '/' and the ore name."
    final static def SHAPED_RECIPE = "In 'Shaped' mode write the keys into recipe pattern. Then press 'Store keys and " +
            "values' - keys will appear to the left. Write corresponding resource locations. If you want to use an ore," +
            " write '/' and the ore name."
}
