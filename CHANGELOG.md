### Additions
* Add creative-only cheat mode #1043
* Added support for specifying components in JSON rather than stringified NBT in newer versions #1132

### Tweaks
* Added recipe filling sanity checks #1125
* Tweaked overflowing workstations in the recipe screen #1161
* Various language updates (`ru_ru`, `es_ar`, `pl_pl`, `de_de`, `zh_tw`, `ja_jp`)
* Added logging for Mixins that target EMI.

### Fixes
* Fix invalid nonexistent ID errors #935
* Fix JEMI layout builder retention #951
* Fix unenchantable items having enchantment recipes #938
* Fix give component syntax for newer versions #1045
* Fix duplicate cost rendering #1078

### API
* Added `EmiScreenBoundsProvider` and registration for custom non-handled screens
