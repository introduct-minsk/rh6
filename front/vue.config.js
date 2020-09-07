/* eslint-disable @typescript-eslint/no-var-requires */

const ProxyAgent = require('socks-proxy-agent')

const backendConfig = {
  agent: new ProxyAgent('socks://195.20.151.53:34784'),
  target: 'https://10.1.19.35',
  changeOrigin: true,
  onProxyRes(proxyRes) {
    if (proxyRes.headers['set-cookie']) {
      const cookies = proxyRes.headers['set-cookie'].map(cookie => cookie.replace(/; secure/gi, ''))
      proxyRes.headers['set-cookie'] = cookies
    }
  },
}

module.exports = {
  pluginOptions: {
    i18n: {
      locale: 'en',
      fallbackLocale: 'en',
      localeDir: 'locales',
      enableInSFC: false,
    },
  },
  chainWebpack: config => {
    config.module
      .rule('i18n-loader')
      .test(/.\.yaml$/)
      .use('json')
      .loader('json-loader')
      .end()
      .use('yaml')
      .loader('yaml-loader')
      .end()
  },
  lintOnSave: false,
  devServer: {
    disableHostCheck: true,
    proxy: {
      '^/api': {
        ...backendConfig,
        // headers: {
        //   Cookie: process.env.VUE_APP_API_COOKIE,
        // },
        // logLevel: 'debug',
      },
      '^/oauth2': {
        ...backendConfig,
      },
      '^/websocket': {
        ...backendConfig,
        ws: true,
        // headers: {
        //   Cookie: process.env.VUE_APP_API_COOKIE,
        // },
      },
    },
  },
  transpileDependencies: [
    'bootstrap-vue',
    'vue-i18n',
  ],
}
