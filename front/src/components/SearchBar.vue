<template>
  <div class="search-bar-root">
    <i class="material-icons-outlined icon">search</i>
    <BFormInput
      v-model="searchTerm"
      type="search"
      :placeholder="`${$t('searchPlaceholder')}`"
      class="input"
    />
  </div>
</template>

<script lang="ts">
import Vue from 'vue'
import { BFormInput } from 'bootstrap-vue'
import { mapFields } from 'vuex-map-fields'
import { reportError } from '@/lib/ria-mailbox/component-helpers'
import debounce from 'lodash/debounce'

export default Vue.extend({
  components: {
    BFormInput,
  },
  data() {
    return {
      debounced: debounce(async function(term) {
        try {
          await this.$store.dispatch('search', term)
        } catch (e) {
          reportError(e.response?.data?.error, 'errors.api', this.$bvToast, this.$i18n, { type: 'server' })
        }
      } as any, 500),
    }
  },
  computed: {
    ...mapFields(['searchTerm']),
  },
  watch: {
    searchTerm(term) {
      if (term.trim()) {
        setTimeout(() => {  // fragile, todo, why not nextTick()?
          this.$router.push('/mailbox/search').catch(e => e)
        }, 1)
        this.debounced(term)
      }
    },
  },
})
</script>

<style scoped lang="scss">
@import "@/styles/includes";

.search-bar-root {
  display: flex;
  align-items: center;

  .icon {
    flex-shrink: 0;
    color: getColor(sapphire-blue-10);
  }

  input {
    border: none;
    padding-left: 0.35em;
  }

  ::placeholder {
    color: getColor(black-coral-9);
  }
}
</style>
