<template>
  <SideNav
    :items="items"
    :ignore-screen-width="ignoreScreenWidth"
    :window-width="$store.state.windowWidth"
    @close="$emit('close')"
  />
</template>

<script lang="ts">
import Vue from 'vue'
import SideNav from 'veera-vue/vue/SideNav.vue'

export default Vue.extend({
  components: {
    SideNav,
  },
  props: {
    ignoreScreenWidth: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    items() {
      return [
        {
          name: this.$t('Mailbox'),
          url: '/mailbox/in',
          icon: 'inbox',
        },
      ]
    },
  },
})
</script>

<style scoped lang="scss">
@import "@/styles/includes";
</style>
