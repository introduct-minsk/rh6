<template>
  <BDropdown
    class="menu-dropdown-root"
    variant="outline"
    no-caret
    :right="side==='right'"
  >
    <template v-slot:button-content>
      <div class="button-container">
        <p class="label">{{ label }}</p>
        <p class="value">
          <span>{{ value }}</span>
          <svg
            width="14"
            height="9"
            viewBox="0 0 14 9"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              fill-rule="evenodd"
              clip-rule="evenodd"
              d="M13.6819 1.24917C13.7943 1.37091 13.7867 1.56071 13.6649 1.67309L6.89119 7.92579C6.77287 8.03501 6.58943 8.03134 6.47557 7.91748L0.212132 1.65404C0.0949748 1.53689 0.0949747 1.34694 0.212132 1.22978L1.20208 0.239829C1.31924 0.122671 1.50919 0.122672 1.62635 0.239829L6.73483 5.34831L12.3084 0.203484C12.4301 0.0911029 12.6199 0.0986948 12.7323 0.220441L13.6819 1.24917Z"
              fill="#005AA3"
            />
          </svg>
        </p>
      </div>
    </template>
    <slot />
  </BDropdown>
</template>

<script lang="ts">
import Vue from 'vue'
import { BDropdown } from 'bootstrap-vue'

export default Vue.extend({
  components: {
    BDropdown,
  },
  props: {
    label: {
      type: String,
      required: true,
    },
    side: {
      type: String,
      default: 'right',
      validator: value => ['right', 'left'].includes(value),
    },
    value: {
      type: String,
      required: true,
    },
  },
})
</script>

<style scoped lang="scss">
@import "../styles/variables.scss";

$label-color: getColor(black-coral-10);

.menu-dropdown-root {
  ::v-deep {
    button {
      background-color: inherit;
      border: none;
      padding: 0;
      color: $label-color;
    }

    .button-container {
      display: flex;
      flex-direction: column;
      align-items: flex-start;

      .label {
        font-size: 0.875em;
        font-weight: 300;
      }
      .value {
        color: getColor(sapphire-blue-10);
        font-size: 1em;

        display: flex;
        justify-content: space-between;
        align-items: baseline;

        > svg {
          margin-left: 0.5em;
        }
      }
    }
  }
}
</style>
