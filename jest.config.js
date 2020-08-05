// jest.config.js

// eslint max-len: ["error", { "ignoreStrings": true }]
const modName = '\\.(jpg|jpeg|png|gif|eot|otf|webp|svg|';
modName += 'ttf|woff|woff2|mp4|webm|wav|mp3|m4a|aac|oga)$'
module.exports = {
  'moduleNameMapper': {
    modName: '<rootDir>/__mocks__/fileMock.js',
    '\\.(scss|sass|css)$': 'identity-obj-proxy',
  },
  'verbose': true,
  'setupFiles': ['jest-canvas-mock'],
};
