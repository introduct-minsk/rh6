<template>
  <BDropdown
    ref="dropdown"
    class="file-selector-root"
    :class="{ disabled }"
    :disabled="disabled"
    no-caret
    dropup
    no-flip
    offset="0,10px"
  >
    <template v-slot:button-content>
      <i class="material-icons-outlined icon material-icon">link</i>
      {{ $t('Attach files') }}
    </template>

    <div
      class="dropzone center-content-using-flex"
      :class="{ 'drop-hovered': dropHovered }"
      @dragenter="onDragEnter"
    >
      <div
        v-show="dropHovered"
        class="drop-mask"
        @dragleave="onDragLeave"
        @drop.prevent="onDrop"
        @dragover.prevent
      />
      <div>
        <p class="dragndrop1">{{ $t('fileSelector.line1') }}</p>
        <p class="dragndrop2">
          {{ $t('fileSelector.line2') }}
          <FileSelectorButton
            class="file-selector-button"
            button-class="c-btn c-btn--primary"
            :multiple="multiple"
            @input="onInput"
          >{{ $t('fileSelector.choose') }}</FileSelectorButton>
        </p>
      </div>
    </div>
    <p class="tip-text">{{ $t('fileSelector.multipleTip') }}</p>
  </BDropdown>
</template>

<script lang="ts">
import Vue from 'vue'
import FileSelectorButton from './FileSelectorButton.vue'
import {
  BDropdown,
} from 'bootstrap-vue'

export default Vue.extend({
  components: {
    BDropdown,
    FileSelectorButton,
  },
  props: {
    disabled: {
      type: Boolean,
      default: false,
    },
    multiple: {
      type: Boolean,
      default: true,
    },
  },
  data() {
    return {
      dropHovered: false,
    }
  },
  computed: {},
  methods: {
    onInput(e) {
      this.$refs.dropdown.hide(true)
      this.$emit('input', e)
    },
    onDrop(e: DragEvent) {
      let files = [...e.dataTransfer.items]  // todo
        .filter(({ kind }) => kind === 'file')
        .map(x => x.getAsFile())

      this.$refs.dropdown.hide(true)
      this.dropHovered = false
      this.$emit('input', files)
    },
    onDragEnter() {
      this.dropHovered = true
    },
    onDragLeave() {
      this.dropHovered = false
    },
  },
})
</script>

<style scoped lang="scss">
@import "../styles/variables.scss";

// https://www.figma.com/file/oXTMAykdzvK8jQqeenrdli/Veera-Design-System?node-id=1497%3A4915

::v-deep {
  .btn.disabled {
    cursor: default; // bs bug: https://github.com/bootstrap-vue/bootstrap-vue/issues/4687
  }
  .dropdown-menu {
    width: 318px;
    height: 157px;
    padding: 1em 1em 0.5em;

    border: 1px solid getColor(black-coral-3);
    box-shadow: 0px 1px 5px rgba(0, 0, 0, 0.4); // todo

    &:before {
      $size: 10px;
      $background-color: getColor(white);

      content: "";
      width: 0;
      height: 0;
      border-left: $size solid transparent;
      border-right: $size solid transparent;
      border-top: $size solid $background-color;
      position: absolute;
      bottom: 0;
      margin-bottom: -$size;
      // box-shadow: 0px 1px 5px rgba(0, 0, 0, 0.4);  // todo
    }
  }
}

.file-selector-root {
  ::v-deep {
    button.dropdown-toggle {
      padding: 0 0.7em 0;
      background-color: getColor(sapphire-blue-10);
      color: getColor(white);
      font-weight: bold;
      font-size: 0.9em;
      border-radius: 5px;
      border: none;
      // line-height: 2em;

      &:active,
      &[aria-expanded="true"] {
        background-color: getColor(sapphire-blue-13);
      }
    }
  }
}

.icon {
  // todo: reuse
  vertical-align: middle;
  line-height: inherit;
}
.dropzone {
  // position: relative;
  height: 110px;
  border: 1px dashed getColor(black-coral-8);
  border-radius: 2px;

  &.drop-hovered {
    border-width: 3px;
  }
}
.drop-mask {
  height: 100%;
  width: 100%;
  top: 0;
  position: absolute;
  background-color: getColor(white);
  opacity: 0.8;
}
.dragndrop1 {
  font-size: 1.5rem;
}
.dragndrop2 {
  font-size: 0.875em;
}
.file-selector-button {
  display: inline;
  ::v-deep {
    button {
      margin-left: 0.3em;
      padding: 5px 1.7em;
      font-size: 0.875em;
    }
  }
}
.tip-text {
  font-size: $o-font-size-sm;
  margin-top: 0.34em;
}
</style>
