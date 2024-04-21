### Tweaks
* Significantly reduce EMI's RAM consumption at the minor cost of niche ItemStack identity dependency scenarios (halved total RAM consumption in smaller pack testing with other optimization mods).
* Fluid hovering highlight no longer covers the fluid in the sidebar.

### Fixes
* Tooltips rendering in peculiar locations in Mekanism GUIs #480
* Support for custom tag types #483

### API (Experimental)
* Expose registry adapter registration to allow tag collection of arbitrary registry types.
* SlotWidget.getRecipe is no longer marked as internal.
