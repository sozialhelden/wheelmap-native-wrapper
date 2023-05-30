fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## Android

### android create_icons

```sh
[bundle exec] fastlane android create_icons
```

Create the app icons

### android development

```sh
[bundle exec] fastlane android development
```

Build the app package locally

### android release_build

```sh
[bundle exec] fastlane android release_build
```

Make a release build

### android screenshots

```sh
[bundle exec] fastlane android screenshots
```

Make screenshots for PlayStore

### android beta

```sh
[bundle exec] fastlane android beta
```

Build the app package and deploy to hockey app and play store beta

----


## iOS

### ios development

```sh
[bundle exec] fastlane ios development
```

Creates a local build of the app, configured from the env/project

### ios release_build

```sh
[bundle exec] fastlane ios release_build
```

Make a release build

### ios screenshots

```sh
[bundle exec] fastlane ios screenshots
```

Make screenshots for App Store

### ios beta

```sh
[bundle exec] fastlane ios beta
```

Deploys a beta build of the app to testflight, configured from the env/project

----


## Mac

### mac development

```sh
[bundle exec] fastlane mac development
```

Creates a local build of the app, configured from the env/project

### mac release_build

```sh
[bundle exec] fastlane mac release_build
```

Make a release build

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
