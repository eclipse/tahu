const path = require('path')
const { defaults } = require('jest-config')

module.exports = {
  roots: [path.join(__dirname, './src')],
  rootDir: path.join(__dirname, '.'),
  testEnvironment: 'node',
  testMatch: ['**/__tests__/**'],
  moduleDirectories: ['node_modules', __dirname, path.join(__dirname, './src')],
  coverageDirectory: path.join(__dirname, './coverage/'),
  collectCoverageFrom: ['**/src/**/*.js'],
  coveragePathIgnorePatterns: ['.*/__tests__/.*'],
  watchPlugins: [
    require.resolve('jest-watch-select-projects'),
    require.resolve('jest-watch-typeahead/filename'),
    require.resolve('jest-watch-typeahead/testname')
  ]
}
