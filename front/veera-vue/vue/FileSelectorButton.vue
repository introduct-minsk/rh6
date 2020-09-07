<template>
  <div>
    <input
      ref="input"
      class="input"
      type="file"
      :multiple="multiple"
      @input="onSelected($event.target.files)"
    />
    <button
      :class="buttonClass"
      @click="clicked"
    >
      <slot />
    </button>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  props: {
    buttonClass: {
      type: [Object, String],
      default: () => '' as string,
    },
    multiple: {
      type: Boolean,
      default: false,
    },
  },
  methods: {
    clicked() {
      this.$refs.input.click()
    },
    onSelected(files: FileList) {
      if (!files.length) {
        return
      }
      this.$emit('input', files)
    },
  },
})
</script>

<style scoped lang="scss">
.input {
  display: none;
}
</style>
