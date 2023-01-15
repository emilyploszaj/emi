### Additions
* Added an option for using a global config
* Added logical OR searching #122
* Added index stack hiding and organization data formats
* Added basic custom recipe data formats for info and world interaction
* Added a category property data format
* Added a search alias data format
* Added a recipe filter data format
* Added a favorited recipe icon
* Added config to entirely remove mod name tooltips
* Added config for recipe screen size and workstation location

### Tweaks
* Adjusted how recipes are sorted on usage lookup to show more specific results first
* Added Inventory Tabs compat with moved potion effects #94
* Adjusted textures for resource pack creators #96
* Changed how EMI handles input interaction to generally be more consistent
* EMI now reloads on resource reload
* Adjusted cost per batch to better take into account one time costs
* Properly display the sponge smelting recipe remainder
* The recipe tree screen is now batched, improving performance for significantly large recipe trees

### Fixes
* Reduced frequency of MC-258939 #126
* Fixed recipe filling strictness #100
* Added category prioritization #81
* Added configurable recipe viewer size #63
* Fixed keybinds failing in certain screens #32

### API
* Refactored recipe handlers
* Added search control endpoints
