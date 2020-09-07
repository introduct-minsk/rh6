<template>
  <GlobalCard>
    <ErrorLabel :error="error" />
  </GlobalCard>
</template>

<script lang="ts">
import Vue from 'vue'
import GlobalCard from './GlobalCard.vue'
import ErrorLabel from './ErrorLabel.vue'

export default Vue.extend({
  components: {
    GlobalCard,
    ErrorLabel,
  },
  props: {
    error: {
      type: undefined,
      required: true,
    },
  },
})
</script>

<style scoped lang="scss">
@import "@/styles/includes";
</style>
