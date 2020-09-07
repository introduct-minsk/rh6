<template>
  <span
    class="veera-label-root"
    :class="[variant, { 'small-padding': small }]"
  >
    <span>
      <slot />
    </span>
    <i class="material-icons-outlined icon">{{ iconName }}</i>
  </span>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  props: {
    variant: {
      required: true,
      validator(value: string) {
        return ['info', 'success', 'warning', 'error'].includes(value)
      },
    },
    small: {  // todo
      type: Boolean,
      default: false,
    },
  },
  computed: {
    iconName() {
      return {
        info: 'info',
        success: 'check_circle_outline',
        error: 'error_outline',
        warning: 'info',
      }[this.variant]
    },
  },
})
</script>

<style scoped lang="scss">
@import "../styles/variables.scss";

.veera-label-root {
  display: inline-block;
  box-sizing: border-box;
  // padding: 0.4em 1.4em 0.4em 1.2em;
  padding: 0.3em 1.2em 0.25em 1em;
  border: 2px solid;
  border-radius: 4px;
  font-weight: bold;
  height: auto;
  position: relative;
  &.small-padding {
    // padding: 0.1em 1.4em 0.1em 0.4em;
  }
}
.icon {
  font-size: 1.6em;
  position: absolute;
  top: 0.1em;
  right: -0.55em;
  background-color: getColor(white);
}
.info {
  color: $color-info;
  border-color: $color-info;
}
.success {
  color: $color-success;
  border-color: $color-success;
}
.warning {
  color: $color-warning;
  border-color: $color-warning;
}
.error {
  color: $color-error;
  border-color: $color-error;
}
</style>
