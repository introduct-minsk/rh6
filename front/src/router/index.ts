import Vue from 'vue'
import VueRouter from 'vue-router'
import HomePage from '@/components/HomePage.vue'
import TheMailbox from '@/components/TheMailbox.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    redirect: '/mailbox/in',
  },
  {
    path: '/mailbox',
    redirect: '/mailbox/in',
  },
  {
    path: '/mailbox/:tab',
    component: HomePage,
    children: [
      {
        path: '',
        component: TheMailbox,
      },
      {
        path: ':messageId',
        component: TheMailbox,
      },
    ],
  },
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes,
})

export default router
