<template>
  <div class="logout">
    <BLink
      href="/logout"
      @click.prevent="logout"
    >{{ $t('Log out') }}</BLink>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'
import { BLink } from 'bootstrap-vue'

export default Vue.extend({
  components: {
    BLink,
  },
  methods: {
    async logout() {
      await this.$store.dispatch('logout')
      window.location.assign('/oauth2/authorization/tara')
    },
  },
})
</script>

<style scoped lang="scss">
.logout {
  a {
    text-decoration: underline;
    white-space: nowrap;
  }
}
</style>
