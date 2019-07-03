fastlane documentation
================
# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```
xcode-select --install
```

Install _fastlane_ using
```
[sudo] gem install fastlane -NV
```
or alternatively using `brew cask install fastlane`

# Available Actions
## Android
### android development
```
fastlane android development
```
Build the app package locally
### android test_notify
```
fastlane android test_notify
```
Test notification!

----

## iOS
### ios development
```
fastlane ios development
```
Creates a local build of the app, configured from the env/project
### ios release_build
```
fastlane ios release_build
```

### ios app_store
```
fastlane ios app_store
```
Deploys a release build of the app, configured from the env/project
### ios beta
```
fastlane ios beta
```
Deploys a beta build of the app to testflight, configured from the env/project

----

This README.md is auto-generated and will be re-generated every time [fastlane](https://fastlane.tools) is run.
More information about fastlane can be found on [fastlane.tools](https://fastlane.tools).
The documentation of fastlane can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
