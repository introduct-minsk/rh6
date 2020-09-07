<template>
  <GlobalError
    v-if="error"
    :error="error.error"
  />
  <div
    v-else-if="!me.valid || !settings.valid"
    class="midcenter"
  >
    <StateServiceLogo />
  </div>
  <RoleSelector v-else-if="me.valid && !me.value.role" />
  <router-view v-else />
</template>

<script lang="ts">
import Vue from 'vue'
import StateServiceLogo from 'veera-vue/vue/StateServiceLogo.vue'
import RoleSelector from './components/RoleSelector.vue'
import GlobalError from './components/GlobalError.vue'
import { mapState } from 'vuex'
import { reportError } from './lib/ria-mailbox/component-helpers'

export default Vue.extend({
  components: {
    StateServiceLogo,
    RoleSelector,
    GlobalError,
  },
  data() {
    return {
      error: undefined,
    }
  },
  computed: {
    ...mapState({
      locale: (state: any) => state.settings.value.locale,
    }),
    ...mapState([
      'me',
      'settings',
    ]),
  },
  watch: {
    // todo: find a better place
    locale: {
      immediate: true,
      handler(value) {
        this.$i18n.locale = value
        document.title = this.$t('Infosystem')
      },
    },
  },
  async created() {
    try {
      let errorCode = await this.$store.dispatch('init')
      if (errorCode === 'aar-problem') {
        reportError('aar_oigused_service_unavailable', 'errors.api', this.$bvToast, this.$i18n)
      }
    } catch (error) {
      if (
        // prevent error flash on login
        error?.response?.status !== 401
        && error?.code !== 'ECONNABORTED'  // todo: understand why
      ) {
        this.error = { error }
      }
    }
  },
})

</script>

<style scoped lang="scss">
</style>
