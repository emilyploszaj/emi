### Tweaks
* Re-enabled stack batching for 1.21
* Query creative group contents on thread. This may cause slight notable performance dips but will significantly improve compatibility
* Redundant JEI initialization will not be performed on mods with dedicated EMI integration, increasing performance in those situations

### Fixes
* Various fixes to modern JEI compatibility
* Fix recipe filling behavior having incorrect behavior with edge case slot interactions
* Fixed invisible slots having rendered overlays when they should not #654
* Fixed handling for extra mouse button binds #645
