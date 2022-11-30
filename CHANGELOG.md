### Sidebars
* Sidebars have been completely redone
* Every sidebar can have its size, positioning, and theming options individually changed
* There are now 4 configurable sidebars, including two new top and bottom sidebars
* Top and bottom sidebars can be aligned to be above or below existing sidebars

### Config
* EMI's config system got a facelift, the UI should look nicer, and be much nicer to use
* Changes are displayed and can be easily reverted
* Config presets now exist to set a variety of settings at once to a preset option rather than navigating a lot of individual options

### Searching
* Increased search performance
* Added tag searching
* Added config options for searching tooltip/mod/tags without prefix

### Screenshots
* A button can be enabled to save clean screenshots of recipes at a specified scale

### Tweaks
* Moved to a proper EMI logger
* Unmoved status effects are now squished to not overlap a ton of EMI's sidebar
* Arrows in the recipe screen no longer shift around when changing tabs

### Fixes
* Batched rendering no longer breaks certain rendering conditions (#82)
* #28

### API
* `EmiDragDropHandler`s can now render feedback
* Added `EmiDragDropHandler.BoundsBased` and `EmiDragDropHandler.SlotBased`, simple drag drop handlers with simple rendering
* Deprecated the `EmiStack.Entry` system. It was mostly duplication of existing systems, not often useful, and cleaner to eventually remove
* Deprecated usage of `FluidVariant` in API methods
* Added more convenience methods to `Bounds`, likely only useful internally
* Added alignment options to `TextWidget`
