import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import i18n from './i18n'
import { ToastPlugin } from 'bootstrap-vue'
import './styles/main.scss'
import { formatEstonian } from './lib/datetime'

Vue.config.productionTip = false

Vue.use(ToastPlugin)
Vue.filter('formatEstonian', formatEstonian)

new Vue({
  router,
  store,
  i18n,
  render: h => h(App),
}).$mount('#app')
