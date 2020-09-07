<template>
  <div class="footer-item">
    <div class="icon">
      <i class="material-icons-outlined">{{ icon }}</i>
    </div>
    <div>
      <h4 class="title">{{ heading }}</h4>
      <slot />
    </div>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  props: {
    heading: {
      type: String,
      required: true,
    },
    icon: {
      type: String,
      required: true,
    },
  },
})
</script>

<style scoped lang="scss">
@import "../styles/variables.scss";

.footer-item {
  line-height: 26px;
  color: getColor(white);
  display: flex;

  & + & {
    border-left: 1px solid rgba(#8f91a8, 0.1);
  }

  a {
    color: getColor(white);
    text-decoration: underline;
  }

  .title {
    text-transform: uppercase;
    margin: 0.5em 0 1em;
    font-weight: 300;
  }

  .icon {
    color: getColor(white);
    margin: 0 1em 0 2em;
    i {
      background: getColor(black-coral-10);
      border-radius: 1em;
      padding: 0.4em;
    }
  }

  &:first-of-type {
    .icon {
      margin-left: 0;
    }
  }

  // todo: create breakpoint mixins
  @media (max-width: 800px) {
    .icon {
      margin-left: 0.5em;
    }
  }
}
</style>
