<template>
  <BSidebar
    v-model="sideNavSidebarShown"
    class="side-nav-sidebar-root"
    no-header
    shadow
  >
    <template v-slot:default="{ hide }">
      <AppSideNav
        ignore-screen-width
        class="body"
        @close="hide"
      />
    </template>
  </BSidebar>
</template>

<script lang="ts">
import Vue from 'vue'
import { BSidebar } from 'bootstrap-vue'
import { mapFields } from 'vuex-map-fields'

import AppSideNav from './AppSideNav.vue'

export default Vue.extend({
  components: {
    BSidebar,
    AppSideNav,
  },
  computed: {
    ...mapFields(['ui.sideNavSidebarShown']),
  },
})
</script>

<style scoped lang="scss">
@import "@/styles/includes";

.side-nav-sidebar-root {
  width: $side-nav-width;

  .body {
    height: 100%;
  }
}
</style>
