# Wheelmap Native Wrapper

This repository contains tools & app templates to wrap the wheelmap-react-frontend application into a native executable.

It's a replacement for the old cordova native builds and meant to be more up-to date, easier to extend and less error prone.

## Dependencies

Install the latest Xcode command line tools:

    xcode-select --install


Install bundle using

    sudo gem install bundler -NV

Install the bundles

    bundle install

Install graphicsmagick for icon generation

    brew install graphicsmagick

## Building

Run these commands for building development/beta/app store builds on iOS:

- `fastlane PROJECT_ENV=wheelmap.org FASTLANE_USER=[APPLE_ID] bundle exec fastlane development`
- `fastlane PROJECT_ENV=wheelmap.org FASTLANE_USER=[APPLE_ID] bundle exec fastlane beta`
- `fastlane PROJECT_ENV=wheelmap.org FASTLANE_USER=[APPLE_ID] bundle exec fastlane appstore`
