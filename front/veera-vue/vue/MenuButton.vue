<template>
  <div
    class="menu-button-root"
    role="button"
  >
    <i class="material-icons-outlined">menu</i>
    <span class="legend">{{ $t('menu') }}</span>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
})
</script>

<style scoped lang="scss">
@import "../styles/variables.scss";

.menu-button-root {
  display: flex;
  flex-direction: column;
  align-items: center;

  color: getColor(sapphire-blue-10);
  cursor: pointer;

  .legend {
    text-transform: capitalize;
    font-size: 0.625em;
  }
}
</style>
