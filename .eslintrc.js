module.exports = {
  root: true,
  env: {
    browser: false,
    node: true,
    "es6": true
  },
  parserOptions: {
     "ecmaVersion": 2018
  },
  extends: [
    'prettier',
    'plugin:prettier/recommended',
  ],
  plugins: [
    'prettier'
  ],
  // add your custom rules here
  rules: {
  }
}
