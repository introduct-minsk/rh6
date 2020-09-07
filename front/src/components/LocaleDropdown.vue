<template>
  <MenuDropdown
    :label="$t('Lang')"
    :value="locale === 'ee' ? 'Est': 'Eng'"
    :side="side"
  >
    <!-- todo: generalize, make v-model -->
    <BDropdownItem
      :disabled="locale === 'ee'"
      @click="setLocale('ee')"
    >EST</BDropdownItem>
    <BDropdownItem
      :disabled="locale === 'en'"
      @click="setLocale('en')"
    >ENG</BDropdownItem>
  </MenuDropdown>
</template>

<script lang="ts">
import Vue from 'vue'
import { BDropdownItem } from 'bootstrap-vue'
import MenuDropdown from 'veera-vue/vue/MenuDropdown.vue'
import { mapActions, mapState } from 'vuex'

export default Vue.extend({
  components: {
    BDropdownItem,
    MenuDropdown,
  },
  props: {
    side: {
      type: String,
      default: 'right',
    },
  },
  computed: {
    ...mapState({
      locale: (state: any) => state.settings.value.locale,
    }),
  },
  methods: {
    ...mapActions(['setLocale']),
  },
})
</script>

<style scoped lang="scss">
</style>
