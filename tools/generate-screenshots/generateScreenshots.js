const { t, addLocale, useLocales } = require('ttag');
const https = require('https');
const { uniq } = require('lodash');
const fs = require('fs');

// Returns the locale as language code without country code etc. removed
// (for example "en" if given "en-GB").
function localeWithoutCountry(locale) {
  return locale.substring(0, 2);
}

// fetches the application translations from github
async function loadTranslations() {
  const localizationPath = 'https://raw.githubusercontent.com/sozialhelden/wheelmap-react-frontend/master/src/lib/translations.json';
  const loadLocalization = new Promise((resolve, reject) => {
    https.get(localizationPath, resp => {
      let data = '';
      
      resp.on('data', (chunk) => {
        data += chunk;
      });

      resp.on('end', () => {
        resolve(JSON.parse(data));
      });
    }) .on('error', (e) => { reject(e); console.log(e) });
  });
  return loadLocalization;
}

const selectors = {
  startButton: () => browser.$(`~${t`Okay, let’s go!`}`),
  placeMarker: () => browser.$(`~${'Bunte SchokoWelt' + ' ' + t`Fully wheelchair accessible`}`),
  expandButton: () => browser.$(`~${t`Expand details`}`),
};

function s(name) {
  return selectors[name] ? selectors[name]() : null;
}

function waitAndTapElement(element, timeout = 5000) {
  element.waitForExist(timeout);
  browser.touchAction([{
    element,
    action: 'tap',
  }]);
}

function saveScreenshot(name) {
  const locale = browser.config.capabilities.locale;
  const device = browser.config.capabilities.device;
  const screenshotDir = browser.config.screenshotDir;

  const targetDir = `${screenshotDir}/${locale.replace(/_/,'-')}`;
  const screenshotPath = `${targetDir}/${device}-${name}.png`;
  fs.mkdirSync(targetDir, { recursive: true });
  console.info(`Saving screenshot to ${screenshotPath}`)
  browser.saveScreenshot(screenshotPath);
}

describe('Screenshot flow', () => {
  before(async () => {
    const translations = await loadTranslations();
    const locale = browser.config.capabilities.locale;
    const preferredLocales = uniq([
      translations[locale] ? locale : localeWithoutCountry(locale),
      'en_US'
    ]);
    preferredLocales.forEach(key => addLocale(key, translations[key]));
    useLocales(preferredLocales);
  });


  it('sets the location', () => {
    // Set device location to a place with nice photos
    browser.setGeoLocation({ latitude: 52.5147041, longitude: 13.3904551, altitude: 70 });
  });

  it('shows places', () => {
    browser.pause(5000); // wait for translations to be loaded
    saveScreenshot("0-StartScreen");
    waitAndTapElement(s('startButton'));
    browser.pause(3000); // wait for dialog to be gone
    saveScreenshot("1-Places");
  });

  it('opens a single place\'s details (with nice photos!)', () => {
    browser.execute('mobile: pinch', { scale: 1.6, velocity: 0.5 });
    browser.pause(5000); // wait for places to be loaded
    waitAndTapElement(s('placeMarker'), 15, 15);
    waitAndTapElement(s('expandButton'));
    browser.pause(20000); // wait for panel to be animated and photos to be loaded
    saveScreenshot("2-PlaceDetails");
  });

  // it('switches to edit mode', () => {
  //   waitAndTapElement(s('editButton'));
  //   browser.pause(1000); // wait for panel to be animated
  //   waitAndTapElement(s('partiallyOption'));
  //   browser.pause(1000); // wait for element to be clicked
  //   saveScreenshot("EditingStatus");
  //   waitAndTapElement(s('cancelButton'))
  // });

  // it('switches to image adding', () => {
  //   waitAndTapElement(s('addImagesButton'))
  //   browser.pause(1000); // wait for panel to be animated
  //   saveScreenshot("AddImages");
  //   waitAndTapElement(s('cancelButton'))
  // });

  // it('filters visible places', () => {
  //   waitAndTapElement(s('searchButton'))
  //   waitAndTapElement(s('shoppingButton'))
  //   waitAndTapElement(s('atLeastPartiallyWheelchairAccessibleButton'))
  //   browser.execute('mobile: pinch', { scale: 0.25, velocity: 0.5 });
  //   browser.pause(10000); // wait for places to be loaded
  //   saveScreenshot("3-Filter");
  //   waitAndTapElement(s('goButton'))
  // });
  
  // it('shows a big train station', () => {
  //   // Set device location to Hauptbahnhof, Berlin
  //   browser.setGeoLocation({ latitude: 52.5251, longitude: 13.3694, altitude: 70 });
  //   waitAndTapElement(s('showMeWhereIAmButton'))
  //   browser.execute('mobile: pinch', { scale: 1.3, velocity: 1 });
  //   browser.pause(3000); // wait for places to be loaded
  //   saveScreenshot("MainStation");
  // });
});