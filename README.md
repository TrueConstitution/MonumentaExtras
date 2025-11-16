Monumenta Extras
==============

A client-sided, general quality of life mod designed for Monumenta (server.playmonumenta.com).
This is the Java rewrite of the original project, which was written in javascript.

Disclaimer
----------------
This mod is _**use at your own risk**_. While its features are designed to align with Monumenta rules as much as possible, some of its features step into gray areas. I am not responsible for you being banned for using this mod outside of its intentional usage. For concerns regarding this matter, see the **For Monumenta moderators** section.

Support
----------------
For any suggestions, bugs, issues, or any other concerns regarding this mod, join my discord server and post your concerns in their designated channels.

Reporting Issues
----------------
This is for tech-savvy developer folks to report their concerns.

Requests for support should go in the [discord](https://discord.gg/DvxMnqsY3n) instead.
Search the [issue list](https://github.com/TrueConstitution/MonumentaExtras/issues) to see if your suggestion/bug was already reported. Include closed issues in your search.

If it has not been reported, create a new issue with at least the following information:

- a short, descriptive title
- version information, such as the version of Minecraft and the version of the mod
- steps for reproducing the issue
- a detailed description of the issue, including applicable logs/crash reports

Please place logs either in a [code block](https://guides.github.com/features/mastering-markdown/) or upload them as a plain text file.

Please note that some files could contain "sensitive" information. You are responsible for censoring information such as your computer name from your log file.
Do note that overcensoring information such as JVM details and System details can, at times, lead to your issue being dismissed due to a lack of information.

Contributing
----------------
Since 2.0 release, the source code for this mod is published [here](https://github.com/TrueConstitution/MonumentaExtras/).
Additionally, some aspects of this mod, such as some helpful configs to import, are crowdsourced. You can find some examples of that [here](https://github.com/TrueConstitution/MMEAPI/).

Features
----------------
This mod aims to provide quality of life features such as solvers, splits, misc fixes, and more.
Currently still a work in progress, not all features of the old mod are ported yet.

Misc
----------------
- Scoreboard: Display a scoreboard with helpful information.
- Tooltip Screenshotter: Press Ctrl-C to screenshot a tooltip you are hovering over.

Solvers
----------------
- Spell Estimator: Estimate the time remaining on spell casts based on bossbar progress.
- SKR Scroll Solver: Solves based on message or predicts based on particles the approximate location of every SKR Scroll location.
- (WIP) SKR Solvers: Solvers for SKR puzzle rooms
- ~~Tesseract Solvers~~ This feature has been removed since 2.0 refactor, as it is too specific and non-repeatable for this general QoL mod.
- ~~Quest Solvers~~ Removed for the same reason as tesseract solvers

Celestial Zenith
----------------
- Charm Analysis: Shows helpful information regarding the type and roll of a zenith charm.
- Charm Database: Stores data of charms inside containers. Includes location information, container titles, etc. This is particularly helpful for optimizing charm loadouts when combined with the [charm evaluator program](https://github.com/TrueConstitution/monumenta-charm-eval), whose input can be obtained by /mme czcharms exportdb.

Splits
----------------
Time your strike, content, and dungeon runs with this feature!

Some predefined splits with additional helpful information display like reward info, miniboss status, and more have been provided.

![An example look of a predefined split for Black Mist](https://cdn.modrinth.com/data/cached_images/2b960514626ffa4ebd59cb67096087dd18008e6f.jpeg)

You can also define your own split in customsplits.json inside the mod's config folder. There is a tutorial config provided for reference by default.

For Monumenta moderators
----------------
If any feature is too borderline, contact me for its removal or reimplementation.