## EMI Contribution Guidelines
This is a rough document on things to be conscious of when submitting to EMI's codebase.
I will apologize in advance for the lack of documentation and tooling available to make this process simple.
EMI's codebase is currently optimized for core developer usage and little care has been made for onboarding, which is unfortunate.
Additionally, this document is informal, and not a strict set of rules.

### Code Style
As a baseline for code contribution to EMI, please follow existing code style.
The repo does not currently have an autoformatter.
Some notable examples of patterns in the codebase:

* Absolutely no usage of Java's `var` keyword, all types should be explicitly specified
* No usage of star imports, for example, `import net.minecraft.*;`, all imports should be explicitly specified
* No single line blocks, for example `if (foo) bar();`, all blocks should use curly braces

### Target Latest
EMI backports all features to all supported branches, only target the branch for the latest version in PRs and do not open additional PRs for previous versions.

### Stable Diffs
It is useful to have the last time a line was touched be the last time its functionality was modified, not its formatting or structure. Limit refactors and keep modifications relevant.

### Minecraft is not an API
Avoid Minecraft functions where possible and use EMI's abstractions. Such abstractions exist for GUIs and drawing, input, config, loader function, and more.
The existing codebase should provide a decent example.
EMI supports many versions of minecraft such as `1.21.1`, `1.19.4`, `1.4.7`, `1.6.4`, and more.
Changes are merged down and abstraction is key.

### EMI *is* an API
Do not break EMI's API or ABI. Breakages are taken very seriously and almost entirely avoided.

In this area, do not add to API unless necessary, and, if so, be very confident in the provided solution being the best solution to expose and to support indefinitely.

### Leave as Found
Do not modify code that is unneccessary for your contribution.
This includes reformatting, adjusting functionality of helpers, and simple changes to unrelated behavior.

### Bite Sized
Do not bundle features together to review, keep PRs as small as feasible.
Larger features are particularly prone to scrutiny, it is valuable to screen ideas and implementation solutions before submitting them, to reduce wasted work.

### Standard for Quality
EMI's codebase has a high standard for quality, particularly for submissions.
Serious attention is given to API surface, user interaction/experience, and UI.
Modifications to any of these should come with justifications for additions and alternative options considered.
A resistance to bloat in all forms is notable, changes should solve as many problems at once and integrate well into existing systems.

### A Note on AI
If you want a quick answer, do not provide LLM generated code to my repository.

If you want a slower answer, submitting a PR includes your explicit vouching for the items on this document, particularly its quality and additionally your full understanding of its functionality. You (not a prompted model) should be able to explain what choices you made and justify them.
If you think you might be able to do this with an LLM, don't. If you're sure you can and an LLM will just speed you up, be careful.
The onus is on you to provide code that is both high quality and yours to submit.
