module.exports = {
  root: true,

  env: {
    node: true,
  },

  extends: [
    'plugin:vue/recommended',
    '@vue/standard',
    '@vue/typescript/recommended',
  ],

  parserOptions: {
    ecmaVersion: 2020,
  },

  rules: {
    // overrides >
    '@typescript-eslint/no-explicit-any': 'off',
    '@typescript-eslint/no-use-before-define': ['error', { functions: false }],
    'comma-dangle': ['warn', 'always-multiline'],
    'no-console': process.env.NODE_ENV === 'production' ? 'error' : 'off',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'error' : 'off',
    'no-empty-function': relaseOnly(),
    'no-multi-spaces': ['warn', { ignoreEOLComments: true }],
    'no-multiple-empty-lines': ['error', { max: 3, maxEOF: 1 }],
    'no-useless-constructor': 'off',  // gives false positives
    'operator-linebreak': ['warn', 'before'],
    'prefer-const': 'off',
    'quote-props': ['warn', 'consistent-as-needed'],
    'quotes': ['warn', 'single', { allowTemplateLiterals: true }],
    'space-before-function-paren': ['warn', {
      anonymous: 'never',
      named: 'never',
      asyncArrow: 'always',
    }],
    'vue/component-name-in-template-casing': ['error'],
    'vue/html-self-closing': ['error', {
      html: {
        void: 'always',
      },
    }],
    // conflicts with the vetur formatter, todo
    'vue/multiline-html-element-content-newline': 'off',
    'vue/return-in-computed-property': 'off',
    // conflicts with the vetur formatter, todo
    'vue/singleline-html-element-content-newline': 'off',

    // extensions >
    'arrow-parens': ['error', 'as-needed'],

    // '': ['error', ''],
  },

  overrides: [
    {
      files: [
        '**/__tests__/*.{j,t}s?(x)',
        '**/tests/unit/**/*.spec.{j,t}s?(x)',
      ],
      env: {
        jest: true,
      },
    },
  ],
}

function relaseOnly(value = ['error']) {
  return process.env.RELEASE ? value : 'off'
}
