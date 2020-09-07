<template>
  <BOverlay
    :show="!message && !error"
    spinner-small
    class="overlay-default"
  >
    <ErrorLabel
      v-if="error"
      :error="error.error"
    />
    <div v-else-if="message">
      <h3
        class="subject text-break"
        :class="{'no-subject': !message.subject }"
      >{{ subject }}</h3>
      <p class="details text-break">
        <span v-if="isIncoming">{{ $t('From') }}: {{ sender }}</span>
        <span v-else>{{ $t('To') }}: {{ receiver }}</span>
        <span class="timestamp">{{ message.createdOn | formatEstonian }}</span>
      </p>
      <SignedBy
        v-if="message.signature"
        :signature="message.signature"
        :message-id="message.id"
        class="signed-by"
      />
      <p class="body text-break">
        <template v-for="(line, i) of bodyLines">
          {{ line }}
          <br
            v-if="i < bodyLines.length-1"
            :key="i"
          />
        </template>
      </p>
      <FileDownloadList
        v-if="message.attachments"
        class="attachment-list"
        :files="attachments"
      />
    </div>
    <div
      v-if="message || error"
      class="button-container text-center"
    >
      <button
        variant="primary"
        class="c-btn c-btn--primary"
        @click="onBack"
      >{{ $t('Back') }}</button>
    </div>
  </BOverlay>
</template>

<script lang="ts">
import Vue from 'vue'
import SignedBy from './SignedBy.vue'
import FileDownloadList from './FileDownloadList.vue'
import ErrorLabel from './ErrorLabel.vue'
import { BOverlay } from 'bootstrap-vue'
import { api } from '@/lib/ria-mailbox/api'
import { formatEstonian } from '../lib/datetime'
import { formatReceiver, formatSender } from '@/lib/ria-mailbox/component-helpers'


export default Vue.extend({
  components: {
    BOverlay,
    SignedBy,
    FileDownloadList,
    ErrorLabel,
  },
  data() {
    return {
      message: undefined,
      error: undefined,
    }
  },
  computed: {
    isNotification() {
      return this.message.type === 'NOTIFICATION'
    },
    isIncoming() {
      if (this.message.receiver.id === this.message.sender.id) {
        return this.$route.params.tab === 'in'
      }
      return this.message.receiver.id === this.$store.state.me.value.role.id
    },
    subject() {
      if (this.isNotification) {
        return this.$t('readReceiptShort', {
          id: this.message.sender.id,
        })
      }
      return this.message.subject || `(${this.$t('No Subject')})`
    },
    bodyLines() {
      if (this.isNotification) {
        return [
          this.$t('readReceiptFull', {
            nameFirst: this.message.sender.firstName,
            nameLast: this.message.sender.lastName,
            subject: this.message.related.subject,
            timestamp: formatEstonian(this.message.createdOn),
          }),
        ]
      }

      return this.message?.text?.replace(/\n{3,}/, '\n\n')
        .split('\n')
    },
    attachments() {
      return this.message.attachments.map(({ name, id }) => ({
        name,
        url: `/api/v1/messages/${this.message.id}/attachments/${id}`,
      }))
    },
    receiver() {
      return formatReceiver(this.message.receiver)
    },
    sender() {
      return formatSender(this.message.sender, this.$t.bind(this))  // todo
    },
  },
  async created() {
    try {
      this.message = await api.get('messages', this.$route.params.messageId)
    } catch (error) {
      this.error = { error }
    }
    this.$store.dispatch('fetchMessagesAll')  // to remove _unread_
  },
  methods: {
    onBack() {
      // todo: a link?
      this.$router.push(`/mailbox/${this.$route.params.tab}`)
    },
  },
})
</script>

<style scoped lang="scss">
@import "@/styles/includes";
$max-width: 50rem;

.subject {
  font-weight: bold;
  // margin-top: 1.375rem;
  padding-bottom: 0.625rem;
  border-bottom: 1px solid #dbdfe2;
  max-width: $max-width;
}
.no-subject {
  color: getColor(black-coral-4);
}
.details {
  color: getColor(black-coral-10);
  font-size: $o-font-size-sm;
  border-bottom: 1px dashed #dbdfe2;
  margin-top: 20px;
  padding-bottom: 15px;

  .timestamp {
    margin-left: 1rem;
  }
}
.body {
  margin-top: 1.5em;
  max-width: $max-width;
  color: #34394c; // todo: why not in veera?
}
.button-container {
  margin-top: 3em;
}
.signed-by {
  margin-top: 1em;
  font-size: $o-font-size-sm;
}
.attachment-list {
  margin-top: 2em;
  max-width: 30em;
  font-size: 0.85em;
}
</style>
