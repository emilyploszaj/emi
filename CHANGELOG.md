### Tweaks
* EMI now does not display in screens without slots

### Fixes
* EMI rendering in incorrect screens without slots #205, #334, #412
* Wrong recipe handler is used for inventory crafting when JEI is present #408
* EMI doesn't back out of the recipe screen to the original source #366

### API
* `EmiRecipe.getBackingRecipe` has been added for returning the vanilla recipe the `EmiRecipe` represents.
* `EmiPlugin.initialize` has been added for registering content required to load EMI and plugins, like serializers.
* Mods can now completely disable stacks in EMI, similar to the resource pack format.
