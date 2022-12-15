### Tweaks
* Updated Chinese translation
* Adjusted reload messages to be more clear if the server is not providing data
* Craftables now take into account the cursor stack, so moving items around shouldn't cause constant changing valid recipes
* Folded nodes in the recipe tree no longer ignore their base costs as it is generally more useful
* The recipe screen will now truncate recipe name and show the full name on hover instead of spilling outside of bounds
* Some tweaks were made to how EMI coerces ingredients to tags
* The recipe tree now has less superfluous padding, it should be easier to fit more on the screen at once
* Zooming in the recipe tree should be cleaner

### Fixes
* Fixed performance issues baking large sets of recipes, particularly in packs with large amounts of enchantments and gear
* Fixed fluid count display in synfavs
* The root node of the recipe tree can be properly reassigned
* Fixed some default config settings and config presets
* Fixed screenshot scale issues on auto GUI scale
* Auto resolution of recipes from ingredients should now always display the correct tooltip
* 
* Minor stability and crash fixes