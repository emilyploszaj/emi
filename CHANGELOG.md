### Updated to 1.21
* Due to internal rendering changes, stack batching is temporarily disabled

### Tweaks
* Mouse buttons above 3 can now interact with stacks in the inventory #538
* Automatically support c:hidden_from_recipe_viewers for stacks with keys that have a registry adapter #545
* Tag translations have been made more consistent with loader standards adapted from EMI #529

### Fixes
* Fixed `EmiIngredient.of` for `TagKey`s creating empty ingredients from hidden tags #554
* Fixed EMI crashing when hovering hide_tooltip items #530
* Fixed registry adapters not working with dynamic registries #527
* Fixed legacy Forge packet handling #516
