<template>
  <BSidebar
    v-model="userSidebarShown"
    class="user-contol-sidebar-root"
    right
    shadow
  >
    <div class="controls">
      <LocaleDropdown side="left" />
      <UserDropdown />
      <LogoutLink />
    </div>
  </BSidebar>
</template>

<script lang="ts">
import Vue from 'vue'
import { BSidebar } from 'bootstrap-vue'
import { mapFields } from 'vuex-map-fields'

import LocaleDropdown from './LocaleDropdown.vue'
import UserDropdown from './UserDropdown.vue'
import LogoutLink from './LogoutLink.vue'

export default Vue.extend({
  components: {
    BSidebar,
    LocaleDropdown,
    UserDropdown,
    LogoutLink,
  },
  computed: {
    ...mapFields(['ui.userSidebarShown']),
  },
})
</script>

<style scoped lang="scss">
@import "@/styles/includes";

.controls {
  padding: 3em;
  display: flex;
  flex-direction: column;

  > div + div {
    border-top: $header-control-border;
    margin-top: 1em;
    padding-top: 1em;
  }
}
</style>
