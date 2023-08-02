### Tweaks
* Changed how potions are compared slightly for better lookups
* Changed how recipe lookup baking works, it should be faster
* Removed JEMI debug print

### Fixes
* Fixed mundane potion and water bottle lookups being the same thing in the recipe tree
* Fixed WidgetHolder.addTooltip incorrect implementation #267
* Fixed lookup baking adjustments causing some recipes to not show up correctly
* Fixed EmiIngredients allowing multiple identical stacks to coexist and break tag coercion