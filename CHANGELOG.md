## 2.0.3

### New Features
* Add splits import command (/mme import splits [SplitName])
* Add reload config command (/mme reload)

### Bug Fixes
* Fix nullpointer exceptions thrown in logs due to missing null check for SpellEstimator
* Fix CustomSplit serialization, now can actually use custom splits
* Fix spacing for splits timer messages
* Fix sharding & dimension detection for splits