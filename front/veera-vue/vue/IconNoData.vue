<template>
  <div class="icon-no-data-root">
    <svg
      fill="none"
      viewBox="0 0 47 33.391304"
      height="36"
      width="47"
    >
      <path
        stroke="#D9D9D9"
        d="M 46.5,11.956252 36.3543,0.453052 c -0.4869,-0.78357 -1.1984,-1.2574 -1.9475,-1.2574 H 12.5932 c -0.7491,0 -1.4606,0.47395 -1.9475,1.2574 L 0.5,11.956252 v 9.2394 h 46 z"
      />
      <path
        stroke="#D9D9D9"
        fill="#FAFAFA"
        d="m 33.1133,15.127152 c 0,-1.6056 0.9937,-2.9313 2.2264,-2.9315 H 46.5 v 18.1371 c 0,2.1219 -1.3206,3.8629 -2.9496,3.8629 H 3.4496 c -1.6291,0 -2.9496,-1.7412 -2.9496,-3.8629 v -18.1371 h 11.1603 c 1.2327,0 2.2264,1.3228 2.2264,2.9285 v 0.0211 c 0,1.6057 1.005,2.9016 2.2378,2.9016 h 14.751 c 1.2328,0 2.2378,-1.3078 2.2378,-2.9135 z"
      />
    </svg>
    <span>{{ text }}</span>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  props: {
    text: {
      type: String,
      default: 'No data',
    },
  },
})
</script>

<style scoped lang="scss">
.icon-no-data-root {
  display: flex;
  flex-direction: column;
  align-items: center;

  span {
    color: #a6a8b1;
    font-size: 0.9em;
    margin-top: 0.6em;
  }
}
</style>
