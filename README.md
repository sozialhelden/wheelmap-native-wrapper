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

Install webdriver.io for the automatic generation of screenshots

    cd tools/generate-screenshots
    npm install

## Building

### Prepare environment variables

- `cp env/wheelmap.org/Secret.example.env env/wheelmap.org/Secret.env`
- Adapt the values in `env/wheelmap.org/Secret.env`

### Getting certificates for signing the app

Sync your local iOS signing certificates from the GitHub store:

- `CERTIFICATES_REPOSITORY="git@github.com:sozialhelden/certificates.git" FASTLANE_USER=[APPLE_ID] bundle exec fastlane match`

This might fail after a while because the certificates expire. In this case, you can 'nuke' them with:

- `CERTIFICATES_REPOSITORY="git@github.com:sozialhelden/certificates.git" FASTLANE_USER=[APPLE_ID] bundle exec fastlane match nuke development`
- `CERTIFICATES_REPOSITORY="git@github.com:sozialhelden/certificates.git" FASTLANE_USER=[APPLE_ID] bundle exec fastlane match nuke distribution`

### Creating builds with fastlane

Run these commands for building development/beta/app store builds on iOS:

- `PROJECT_ENV=wheelmap.org FASTLANE_USER=[APPLE_ID] bundle exec fastlane ios development`
- `PROJECT_ENV=wheelmap.org FASTLANE_USER=[APPLE_ID] bundle exec fastlane ios beta`

Run these commands for building development/beta/app store builds on Android:

- `PROJECT_ENV=wheelmap.org FASTLANE_USER=[GOOGLE_DEVELOPER_EMAIL] bundle exec fastlane android development`
- `PROJECT_ENV=wheelmap.org FASTLANE_USER=[GOOGLE_DEVELOPER_EMAIL] bundle exec fastlane android beta`

Hint: If you want to build the project in Android Studio, open the 'android' folder as project in Android Studio, **not** the root folder of this repo.

Usually, the first thing that you have to do is to update/re-sync gradle. Android studio will give you a hint on loading how to do this.

