
require('dotenv').config();

['BROWSERSTACK_USERNAME', 'BROWSERSTACK_ACCESS_KEY', 'BROWSERSTACK_APP_ID'].forEach(variableName => {
    if (!process.env[variableName]) {
      throw new Error(`Please define ${variableName} as environment variable.`);
    }
  });

const { flatten } = require('lodash');

const devicesByPlatform = {
  ios: [
    // { device: 'iPhone 6S Plus', os_version: '11.0' },
    // { device: 'iPhone 8', os_version: '11.0' },
    { device: 'iPhone X', os_version: '11.0' },
    // { device: 'iPad Pro', os_version: '11.0', deviceOrientation: 'landscape' },
  ],
  android: [
    //{ device: 'Samsung Galaxy S9 Plus', os_version: '8.0' },
    { device: 'Google Pixel 2', os_version: '8.0' },
    //{ device: 'Samsung Galaxy Tab S3', os_version: '8.0' }
  ],
};

const devices = devicesByPlatform[process.env.FASTLANE_PLATFORM_NAME];

const locales = [
  { locale: 'en_US', language: 'en' },
  // { locale: 'de_DE', language: 'en' },
  // { locale: 'da', language: 'da' },
  // { locale: 'el', language: 'el' },
  // { locale: 'es_ES', language: 'es' },
  // { locale: 'fr_FR', language: 'fr' },
  // { locale: 'ja', language: 'ja' },
  // { locale: 'ru', language: 'ru' },
  // { locale: 'sv', language: 'sv' },
];

const commonCapabilities = {
  app: process.env.BROWSERSTACK_APP_ID,
  name: 'parallel_appium_test',
  build: 'webdriver-browserstack',
  'browserstack.debug': true,
  'browserstack.video': false,
  'browserstack.local': false,
  real_mobile: true,
  autoGrantPermissions: true,
  autoWebview: false, // setting this to true will stop accessibility id queries
  // automationName: 'appium',
  autoGrantPermissions: true,
  autoAcceptAlerts: true,
};


exports.config = {
  runner: 'local',

  user: process.env.BROWSERSTACK_USERNAME,
  key: process.env.BROWSERSTACK_ACCESS_KEY,

  updateJob: false,

  specs: [
    './generateScreenshots.js'
  ],

  exclude: [
  ],

  maxInstances: 2,

  reporters: ['spec'],

  screenshotDir: '../../env/wheelmap.org/screenshots',

  capabilities: flatten(devices.map(device => locales.map(locale => ({ ...commonCapabilities, ...device, ...locale })))),

  logLevel: 'verbose',
  coloredLogs: true,
  waitforTimeout: 15000,
  connectionRetryTimeout: 90000,
  connectionRetryCount: 3,

  framework: 'mocha',
  mochaOpts: {
    ui: 'bdd',
    timeout: 40000
  }
};

// console.log(JSON.stringify(exports.config, null, 2));