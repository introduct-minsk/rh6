module.exports = {
  preset: '@vue/cli-plugin-unit-jest/presets/typescript-and-babel',
  collectCoverage: true,
  collectCoverageFrom: [
    '**/*.{ts,vue}',
    '!**/node_modules/**',
    '!**/dist/**',
    '!**/coverage/**',
    '!*.config.js',
    '!src/lib/hwcrypto.js',  // 3rdparty
    '!src/lib/CachedValue.ts',  // 3rdparty
    '!src/{main,i18n}.ts',
    '!src/router/**',
    '!src/store/mock.ts',  // used for dev only
    '!src/store/index.ts',
    '!src/lib/ria-mailbox/api.ts',
  ],
}
