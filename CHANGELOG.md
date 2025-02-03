### Additions
* Search sidebar target can be configured

### Tweaks
* Notable performance improvements to ingredient construction
* Wrap packet sends with a check in later versions
* Improved accuracy of EMI reloading errors

### Fixes
* Serialization of stacks with sizes other than 1 now serialize properly #803
* Fix tag ingredient rendering in Fabulous #811
* Fix stack batcher causing certain recipe tree categories to render at the wrong position #790
* Populate item groupps in correct order (fixing duplicate items) #774 #603 #621
* Pick which sidebar search targets #377
* Fix search baking hang #800
* Fix certain JEI integration breaking inconsistently on NeoForge

### API
* Added experimental API to get query if stack is disabled from EmiRegistry
* Added experimental API to get EmiTooltipMetadata from composed tooltips being rendered for mod compatibility
