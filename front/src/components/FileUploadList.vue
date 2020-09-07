<template>
  <div>
    <input
      ref="fileInput"
      class="d-none"
      type="file"
      @input="onReplacementSelected($event.target.files)"
    />
    <BTable
      class="file-upload-list-table"
      :items="files"
      :fields="fields"
      thead-class="d-none"
      td-class="td"
    >
      <template v-slot:cell(status)="{item}">
        <VeeraLabel
          v-if="item.numFilesLimitStatus"
          variant="warning"
          small
        >{{ $t('Over the limit') }}</VeeraLabel>
        <VeeraLabel
          v-else
          :variant="statusToVariant(item.status)"
          small
        >
          <span class="text-capitalize-first">{{ statusText(item) }}</span>
        </VeeraLabel>
      </template>
      <template v-slot:cell(tools)="{item}">
        <BButton
          class="action-button"
          variant="link"
          :disabled="isDeleteButtonDisabled(item)"
          @click="$store.dispatch('deleteAttachment', item.object)"
        >
          <i class="material-icons-outlined icon">delete_outline</i>
          <span>{{ $t('Delete') }}</span>
        </BButton>
        <BButton
          class="action-button text-capitalize-first"
          variant="link"
          :disabled="isDeleteButtonDisabled(item)"
          @click="replaceAttachment(item.object)"
        >
          <i class="material-icons-outlined icon">edit</i>
          <span>{{ $t('Change') }}</span>
        </BButton>
      </template>
    </BTable>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'
import { BTable, BButton } from 'bootstrap-vue'
import VeeraLabel from 'veera-vue/vue/VeeraLabel.vue'

export default Vue.extend({
  components: {
    BTable,
    BButton,
    VeeraLabel,
  },
  props: {
    files: {
      type: Array,
      required: true,
    },
    disabled: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      replacee: undefined,
      fields: [
        {
          key: 'name',
          tdClass: 'td file-name text-break',
        },
        {
          key: 'status',
          tdClass: 'td',
        },
        {
          key: 'tools',
          tdClass: 'td tools text-right text-nowrap',
        },
      ],
    }
  },
  methods: {
    isDeleteButtonDisabled(item) {
      return this.disabled
        || ['deleting', 'uploading'].includes(item.status)
    },
    replaceAttachment(replacee) {
      this.replacee = replacee
      this.$refs.fileInput.click()
    },
    onReplacementSelected(files: FileList) {
      if (!files.length) {
        return
      }
      this.$store.dispatch('replaceAttachment', [this.replacee, files[0]])
      this.replacee = undefined
    },
    statusToVariant(status) {
      return {
        uploaded: 'success',
        error: 'error',
        tooLarge: 'error',
      }[status] || 'info'
    },
    statusText(item) {
      // debugger
      if (item.status === 'error' && item.error?.response.data.error) {
        return `${this.$t('Error')}: ${item.error.response.data.error}`
      }
      return this.$t(`uploadStatuses.${item.status}`)
    },
  },
})
</script>

<style scoped lang="scss">
@import "@/styles/includes";

::v-deep {
  .td {
    // padding: 0;
    vertical-align: middle;
    // &.name {
    //   padding-left: 0.5em;
    //   &.status {
    //   }
    // }
    padding-left: 0.5em;

    &.file-name {
      min-width: 7em;
      padding-left: 0;
    }
    &.tools {
      padding-right: 0;
      padding-left: 0;
    }
  }
}

.action-button {
  font-size: inherit;
  border-width: 1px;
  padding: 0.2em 0.5em;
  & + .action-button {
    // margin-left: 0.4em;
  }
  &:last-of-type {
    padding-right: 0;
  }
  .icon {
    font-size: 1.2em;
    vertical-align: middle;
  }
  span {
    text-decoration: underline;
    margin-left: 0.2em;
  }
}

.error {
  color: $color-error;
}
</style>
