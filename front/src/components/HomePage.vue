<template>
  <div class="home-page-root">
    <div class="above-footer">
      <AppSideNav
        v-if="$store.getters.sidenavShown"
        class="menu"
      />
      <div class="header-and-content">
        <TheHeader class="header" />
        <router-view class="content" />
      </div>
    </div>
    <TheFooter class="footer" />
  </div>
</template>

<script lang="ts">
import Vue from 'vue'
import TheFooter from 'veera-vue/vue/TheFooter.vue'
import TheHeader from './TheHeader.vue'
import AppSideNav from './AppSideNav.vue'

export default Vue.extend({
  components: {
    TheFooter,
    AppSideNav,
    TheHeader,
  },
})
</script>

<style scoped lang="scss">
@import "@/styles/includes";

.home-page-root {
  height: 100%;
  display: flex;
  flex-direction: column;

  .above-footer {
    flex: 1 0 auto;
    display: flex;

    .menu {
      flex-shrink: 0;
    }

    .header-and-content {
      flex: 1 0;
      display: flex;
      flex-direction: column;
      background-color: getColor(black-coral-0);

      .header {
        flex-shrink: 0;
      }
      .content {
        flex: 1 0 auto;
      }
    }
  }

  .footer {
    flex-shrink: 0;
  }
}
</style>
