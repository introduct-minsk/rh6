<template>
  <div>
    <VeeraLabel variant="error">{{ message }}</VeeraLabel>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'
import VeeraLabel from 'veera-vue/vue/VeeraLabel.vue'
import { bakeErrorMessage } from '../lib/ria-mailbox/component-helpers'

export default Vue.extend({
  components: {
    VeeraLabel,
  },
  props: {
    error: {
      type: undefined,
      required: true,
    },
  },
  computed: {
    message() {
      return bakeErrorMessage(
        this.error.response?.data?.error,
        'errors.api',
        this.$i18n,
        { type: 'server' },
      )
    },
  },
})
</script>

<style scoped lang="scss">
@import "@/styles/includes";
</style>
