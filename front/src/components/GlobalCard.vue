<template>
  <div class="midcenter">
    <BCard class="card">
      <div class="user-controls">
        <LocaleDropdown />
        <LogoutLink />
      </div>
      <slot />
    </BCard>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'
import { BCard } from 'bootstrap-vue'
import LogoutLink from './LogoutLink.vue'
import LocaleDropdown from './LocaleDropdown.vue'

export default Vue.extend({
  components: {
    BCard,
    LogoutLink,
    LocaleDropdown,
  },
})
</script>

<style scoped lang="scss">
@import "@/styles/includes";

.card {
  box-shadow: $shadow-1;
}
.user-controls {
  margin-bottom: 1.2em;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  * + * {
    margin-left: 1.4em;
  }
}
</style>
