<template>
  <BTable
    class="file-download-list-table"
    :items="files"
    :fields="fields"
    thead-class="d-none"
    td-class="td"
  >
    <template v-slot:cell(link)="{item}">
      <a :href="item.url">
        <i class="material-icons-outlined icon">save_alt</i>
        {{ $t('Download') }}
      </a>
    </template>
  </BTable>
</template>

<script lang="ts">
import Vue from 'vue'
import { BTable } from 'bootstrap-vue'

export default Vue.extend({
  components: {
    BTable,
  },
  props: {
    files: {
      type: Array,
      required: true,
    },
  },
  data() {
    return {
      fields: [
        {
          key: 'name',
          tdClass: 'td name text-break',
        },
        {
          key: 'link',
          tdClass: 'td download text-right text-nowrap',
        },
      ],
    }
  },
})
</script>

<style scoped lang="scss">
@import "@/styles/includes";

::v-deep {
  .td {
    vertical-align: middle;
    &.name {
      padding-left: 0.5em;
    }
    &.download {
      text-align: right;
      padding-right: 0.5em;
    }
  }
  .icon {
    // todo: move to lib
    font-size: 1.2em;
    vertical-align: middle;
  }
}
</style>
