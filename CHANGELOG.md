### Xplat
EMI now releases on Forge as well as Fabric and Quilt

### JEI Compat
* Installing JEI alongside EMI will allow them to exchange data
* JEI will be hidden, and EMI will have all of JEI's recipes
* Certain integrations like chance and remainders will be missing and there may be quirks in the recipe tree
* Thanks for the collaboration, mezz!

### Additions
* Fuel and Composting recipes added
* Ability to pick batch size in recipe tree that minimizes remainders
* Ability to translate and model arbitrary types of tags, currently added fluid tags (see Resource Formats)
* Added recipe handler for stonecutting
* Added `CoercedRecipeHandler` to handle crafting recipes in "crafting-like" inventories
* Several tags are now modelled

### Tweaks
* EMI should now calculate correct bulk batches internally for "cost per batch" measurement
* EMI now wraps its tooltips
* The way EMI tries to contextually assume recipes should be more accurate
* Recipe displays are now slightly more conscious of the screen space they occupy and will use slightly less space when possible
* Tweaked several recipe displays

### Fixes
* EMI's index should now contain stacks for mods that have improper creative tabs, and fallback to registry order if creative tabs are broken
* Various rendering fixes for EMI's overlay
* EMI's widgets are now isolated from the underlying screens fixing niche issues
* EMI now properly limits craft count when contextually assuming a recipe for a base cost synthetic favorite
* Fixed extremely niche visual issue with backwards jumps in system time
* Fixes to item cheating and deleting in cheat mode
* Fixed behavior with adding and removing favorites particularlly with attached recipe context
* Revamped tags to better consolodate matches
* Fixed "Composter not shown" (safe, thaumiel) #20
* Fixed "Long recipe IDs push tooltip offscreen" (safe) #64
* Fixed "Option to Expand Potion Effects" (safe, thaumiel) #130
* Fixed "Option to move potion effects to left" (safe, thaumiel) #135
* Fixed "Some items not rendering on first open when running with sodium" (euclid) #143
* Fixed "Workstation Does Not Support Recipe" (euclid) #146
* Fixed "Toggle Visibility Button" (safe, thaumiel) #150
* Fixed "Add recipe handler for stonecutter" (safe, thaumiel) #156
* Fixed "Virtual Fluids are Displayed in World Interaction" (safe) #159
* Fixed "Some Recipes have no ID" (safe) #163
* Fixed "Add category for furnace fuels" (safe, thaumiel) #164
* Fixed "Favorite/Default Tag Items" (euclid) #165
* Fixed "Hidden items still show automatically generated recipes" (euclid) #166
* Fixed "Spawning in items fails when in non-player inventory" (safe) #167
* Fixed "Tags that only have entries with #c:hidden_from_recipe_viewers should not be shown" (safe) #169

### Config
* New "Help Level" setting for controlling help tooltips and messages
* "Recipe Book Action" setting what actions in EMI the recipe book should perform, like toggling craftables or toggling EMI
* "Move Effects" -> "Effect Location" allowing control of location (and compressed status) of effects
* "Default Stack" bind for defaulting stacks discretely rather than for all outputs of a recipe
* "Delete Cursor Stack" for controlling what action (left click by default) in cheat mode should delete held stack
* "Edit Mode" for editing the index including binds for hiding stacks in edit mode

### Resource Formats
Several changes are too verbose to include here, see the wiki for updated examples and documentation
* Location of some internal EMI textures has changed
* Tag exclusions now support exclusions per tag type
* Index stack removal now matches NBT exactly
* Recipe defaults support more discrete forms of resolution including per output defaulting and tag resolution

### API
Various restructures of certain parts of the EMI API have occured.
While these are breaking in source, all mods compiled on old versions of EMI should continue to work.

#### API Additions
* Platform specific `EmiStack` construction methods for `FluidVariant`s and `FluidStack`s
* `EmiEntrypoint` annotation for Forge
* `EmiIngredientSerializer` (and `EmiStackSerializer` convenience class) added for serializing ingredients and stacks
* `BasicEmiRecipe` convenience class to reduce necessary code for simple recipes
* `EmiRecipeManager` available from `EmiApi` contains lookup methods for all recipes EMI knows about
* More methods on `EmiTooltipComponents`
* `SlotWidget` now exposes several methods for more discrete rendering and tooltip changes
* `TankWidget` now exists for rendering fluids in a vertical tank of arbitrary size with proper handling for floating fluids
* `TooltipWidget` for simply adding a tooltip to some location
* Several widgets now implement `WidgetTooltipHolder` allowing more discrete application of tooltips

### API Refactors
* `SlotWidget.output` -> `SlotWidget.large`
* `Comparison` has been nearly completely rewritten to support more detailed comparison methods as well as remove dependency on Fabric API
* Deprecated `EmiStack.of` methods for Fabric types have been moved to `FabricEmiStack.of`

### API Removals
* `EmiApi.prefermFill`
* Legacy `EmiRecipeHandler` (and accompanying registry method)
* `EmiIngredient.getAmountText`
* `EmiStack.Entry` and all related methods are now removed, the system was overcomplicated and not needed
* Concrete `EmiStack` and `EmiIngredient` types (`EmptyEmiStack`, `ItemEmiStack`, `FluidEmiStack`, `TagEmiIngredient`, `ListEmiIngredient`) have all been hidden from API and made internal
* Legacy version of `SlotWidget.custom`
