<template>
  <button
    variant="primary"
    class="c-btn veera-button-root text-nowrap"
    :class="[`c-btn--${variant}`, { busy }]"
    :disabled="disabled"
    @click="onClick"
  >
    <!-- todo -->
    <!-- v-on="$listeners" -->
    <!-- :disabled="busy" -->
    <span
      v-if="matIcon || $slots.icon || busy"
      class="icon-container"
    >
      <BSpinner
        v-if="busy"
        small
      />
      <template v-else>
        <i
          v-if="matIcon"
          class="material-icons-outlined material-icon"
        >{{ matIcon }}</i>
        <slot name="icon" />
      </template>
    </span>
    <slot />
  </button>
</template>

<script lang="ts">
import Vue from 'vue'
import { BSpinner } from 'bootstrap-vue'

export default Vue.extend({
  components: {
    BSpinner,
  },
  props: {
    variant: {
      type: String,
      default: 'primary',
      validator: v => ['primary', 'secondary'].includes(v),  // todo
    },
    busy: {
      type: Boolean,
      default: false,
    },
    disabled: {
      type: Boolean,
      default: false,
    },
    matIcon: {
      type: String,
      default: undefined,
    },
  },
  methods: {
    onClick() {
      if (!this.busy) {
        this.$emit('click')
      }
    },
  },
})
</script>

<style scoped lang="scss">
@import "../styles/variables.scss";

.veera-button-root {
  .spinner-border {
    width: 1em;
    height: 1em;
    margin-right: 0.4em;
    vertical-align: top;
  }
  &.busy {
    cursor: unset;
  }
}
.icon-container {
  display: inline-block;
  width: 2em;
  margin-left: -0.5em;
}
.material-icon {
  font-size: 1em;
  vertical-align: middle;
  line-height: inherit;
}
</style>
