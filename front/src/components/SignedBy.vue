<template>
  <div>
    <VeeraLabel :variant="status">
      <SignatureIcon
        class="signature-icon"
        :class="status"
      />
      {{ signedByText }}
      <template v-if="signature.signedBy !== null">
        {{ $t('signedByAt') }}
        <span class="text-nowrap">{{ timestamp }}</span>
      </template>
      |
      <a :href="digidocDownloadHref">{{ $t('digidocLink') }}</a>
      <br />
      <span
        v-if="status === 'error'"
        class="caution"
      >{{ $t(error) }}</span>
    </VeeraLabel>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'
import capitalize from 'lodash/capitalize'
import moment from 'moment'

import VeeraLabel from 'veera-vue/vue/VeeraLabel.vue'
import SignatureIcon from '@/lib/vue/icons/SignatureIcon.vue'

export default Vue.extend({
  components: {
    VeeraLabel,
    SignatureIcon,
  },
  props: {
    signature: {
      type: Object,
      required: true,
    },
    messageId: {
      type: String,
      required: true,
    },
  },
  computed: {
    status() {
      return this.signature.valid ? 'success' : 'error'
    },
    error() {
      if (this.signature.valid === null) {
        return 'validationServiceDown'
      }
      return 'signatureInvalid'
    },
    signedByText() {
      if (this.signature.valid !== null) {
        let [nameLast, mameFirst, code] = this.signature.signedBy
          .split(',')
          .map(capitalize)
        return `${this.$t('Signed with digital ID')}: ${mameFirst} ${nameLast} (${code})`
      }
      return `${this.$t('Signed with digital ID')}`
    },
    timestamp() {
      let timestamp = moment(this.signature.signingTime)
      return `${timestamp.format('DD.MM.YYYY')} ${timestamp.format('HH:mm:ss')}`
    },
    digidocDownloadHref() {
      return `/api/v1/messages/${this.messageId}/sign`
    },
  },
})
</script>

<style scoped lang="scss">
@import "@/styles/includes";
.signature-icon {
  height: 1.3em;
  margin-right: 0.3em;
}
.success {
  fill: $color-success;
}
.error {
  fill: $color-error;
}
.caution {
  color: getColor(white);
  background-color: $color-error;
  padding: 0 0.4em;
}
</style>
