<template>
  <MenuDropdown
    :label="$t('User')"
    :value="me ? me.id : 'EE00000000000'"
  >
    <BDropdownItem
      v-if="me"
      disabled
    >{{ me.firstName }} {{ me.lastName }}</BDropdownItem>
    <BDropdownItem
      v-if="me.dateOfBirth"
      disabled
    >{{ me.dateOfBirth }}</BDropdownItem>
    <BDropdownItem
      v-if="me.address"
      disabled
    >{{ me.address }}</BDropdownItem>

    <template v-if="!$store.getters.userRepresentsThemselves">
      <BDropdownDivider />
      <BDropdownItem disabled>{{ $t('onBehalfOf', {who: me.role.id}) }}</BDropdownItem>
    </template>
  </MenuDropdown>
</template>

<script lang="ts">
import Vue from 'vue'
import { BDropdownItem, BDropdownDivider } from 'bootstrap-vue'
import { mapState } from 'vuex'
import MenuDropdown from 'veera-vue/vue/MenuDropdown.vue'

export default Vue.extend({
  components: {
    BDropdownItem,
    BDropdownDivider,
    MenuDropdown,
  },
  computed: {
    ...mapState({
      me: (state: any) => state.me.value,
    }),
  },
})
</script>

<style scoped lang="scss">
@import "@/styles/includes";

.b-dropdown {
  ::v-deep {
    // @media all and (-ms-high-contrast: none), (-ms-high-contrast: active) {
    //   // prevent long names from triggering
    //   //   https://github.com/bootstrap-vue/bootstrap-vue/issues/1300
    //   .dropdown-item {
    //     white-space: normal;
    //   }
    // }
    .dropdown-item {
      white-space: normal;
      // font-size: $o-font-size-sm;
      font-size: 0.875em;
    }
  }
}
</style>
