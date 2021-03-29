# Minecraft JSON asset generator
This is an application I've been developing
for automating Json file creation for Minecraft
which are used as assets in the game.
# Structure
Originally it was a Groovy application, but
later I decided to start migrating it to Java
because of increasing difficulty of maintaining
Groovy code. Currently, it has 2 packages:
`alexiy.minecraft.assetgenerator` which
contains old code and should not be used, and
`alexiy.minecraft.asset.generator` which contains 
new code, with `MAG` as the main class.

# Goals
Groovy code must be migrated to Java or rewritten from scratch.

As JavaFX is no longer included in JDK, an 
alternative should be found and used as UI
implementation.

Rewrite of the project must be done on `rewrite` branch.