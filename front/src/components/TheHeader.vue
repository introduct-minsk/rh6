<template>
  <div class="the-header-root">
    <a
      href="https://www.ria.ee"
      class="logo-href"
    >
      <StateServiceLogo
        v-if="visibilities.logo"
        :lines="[$t('Information System Authority'), $t('Information system')]"
      />
    </a>

    <div class="controls">
      <MenuButton
        v-if="visibilities.menuButton"
        @click.native="sideNavSidebarShown = true"
      />
      <SearchBar class="search-bar" />
      <LocaleDropdown v-if="visibilities.userControl" />
      <UserDropdown v-if="visibilities.userControl" />
      <LogoutLink v-if="visibilities.userControl" />

      <div
        v-if="visibilities.userButton"
        @click="userSidebarShown = true"
      >
        <i class="material-icons-outlined user-button">person_outline</i>
      </div>
    </div>

    <SideNavSidebar />
    <UserControlSidebar />
  </div>
</template>

<script lang="ts">
import Vue from 'vue'
import { mapFields } from 'vuex-map-fields'

import StateServiceLogo from 'veera-vue/vue/StateServiceLogo.vue'
import MenuButton from 'veera-vue/vue/MenuButton.vue'
import SearchBar from './SearchBar.vue'
import LocaleDropdown from './LocaleDropdown.vue'
import UserDropdown from './UserDropdown.vue'
import LogoutLink from './LogoutLink.vue'
import UserControlSidebar from './UserControlSidebar.vue'
import SideNavSidebar from './SideNavSidebar.vue'


const breakpoints = {
  userControls: 990,
}

export default Vue.extend({
  components: {
    StateServiceLogo,
    MenuButton,
    SearchBar,
    LocaleDropdown,
    UserDropdown,
    LogoutLink,
    UserControlSidebar,
    SideNavSidebar,
  },
  computed: {
    ...mapFields([
      'ui.userSidebarShown',
      'ui.sideNavSidebarShown',
    ]),
    visibilities() {
      return {
        // always `<` and always `>=`
        menuButton: !this.$store.getters.sidenavShown,
        logo: this.$store.getters.sidenavShown,
        userButton: this.$store.state.windowWidth < breakpoints.userControls,
        userControl: this.$store.state.windowWidth >= breakpoints.userControls,
      }
    },
  },
})
</script>

<style scoped lang="scss">
@import "@/styles/includes";

$bp-squeeze-last-pixels: 356px;

.the-header-root {
  background-color: getColor(white);
  box-shadow: 0px 1px 3px rgba(0, 0, 0, 0.2), 0px 2px 2px rgba(0, 0, 0, 0.12),
    0px 0px 2px rgba(0, 0, 0, 0.14);
  padding: 0 2.5rem;
  height: 99px;

  display: flex;
  justify-content: space-between;
  align-items: center;

  @media screen and (max-width: $bp-hide-sidenav) {
    height: 4.375em;
    padding: 0 1.8em;
  }

  @media screen and (max-width: $bp-squeeze-last-pixels) {
    padding: 0 1.5rem;
  }

  .controls {
    display: flex;
    align-items: flex-end;

    @media screen and (max-width: $bp-hide-sidenav) {
      width: 100%;
      justify-content: center;
      align-items: center;
    }

    > div {
      & + div {
        margin-left: 2em;
        border-left: 1px solid #dbdfe2; // todo
        padding-left: 2em;

        @media screen and (max-width: $bp-squeeze-last-pixels) {
          margin-left: 1em;
          padding-left: 1em;
        }
      }
    }
  }
}

.logo-href {
  flex-shrink: 0;
  text-decoration: none;
}

.user-button {
  padding: 0.3em;
  color: getColor(sapphire-blue-10);
  background-color: getColor(black-coral-0);
  border-radius: 1em;
  cursor: pointer;
}

.search-bar {
  @media screen and (min-width: $bp-hide-user-controls+1) {
    margin-bottom: 0.2em;
  }
  @media screen and (max-width: $bp-hide-sidenav) {
    flex: 1;
  }
}

.logo-href + .controls {
  margin-left: 2em;
  @media screen and (max-width: $bp-hide-sidenav) {
    margin-left: 0;
  }
}
</style>
