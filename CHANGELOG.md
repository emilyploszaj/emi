This update significantly improves reload performance

### Tweaks
* Built in recipe sorting algorithms are now significantly faster, speeding up reloads
* Reduced tooltip checking on stacks to speed up reloads
* Tweaked how the recipe tree works with duplicate outputs and differing chance

### Fixes
* EMI can pass non-fully initialized screens to exclusion area providers #240
* Certain tooltips are rendering incorrectly in 1.20.1 #241
* Ingredients created from tags may be empty if they're equivalent to better tags #242
* Incompatible with ModernUI #243
* Fixed duplicate recipes showing up in certain lookups