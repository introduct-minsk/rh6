<template>
  <div
    class="message-list-root"
    :class="{empty: valid && !messages.length }"
  >
    <BOverlay
      :show="!valid"
      :variant="messages.length ? '' : 'transparent'"
      class="overlay-default"
    >
      <ErrorLabel
        v-if="messages.error"
        :error="messages.error"
      />
      <div
        v-else-if="messages.length"
        class="message-list"
      >
        <div
          v-for="message of messages"
          :key="message.id"
          class="message"
          @click="$router.push(getMessageLink(message.id))"
        >
          <div
            class="unread-indicator"
            :class="{ active: direction === 'in' && message.unread }"
          />
          <div class="details">
            <p
              v-if="direction === 'search'"
              class="box-name"
            >{{ isIncoming(message) ? $t('Inbox') : $t('Outbox') }}</p>
            <h4
              class="title text-break"
              :class="{ unread: direction === 'in' && message.unread }"
            >
              <span
                v-if="message.type==='NOTIFICATION'"
              >{{ $t('readReceiptShort', { id: message.sender.id }) }}</span>
              <span
                v-else
                :class="{ 'no-subject': !message.subject }"
              >{{ message.subject || `(${$t('No Subject')})` }}</span>
            </h4>
            <p class="properties text-break">
              <i class="material-icons-outlined icon">inbox</i>
              {{ message.createdOn | formatEstonian }}
              <span class="to-or-from">
                <template
                  v-if="direction === 'out' || !isIncoming(message)"
                >{{ $t('To') }}: {{ formatReceiver(message.receiver) }}</template>
                <template v-else>{{ $t('From') }}: {{ formatSender(message.sender) }}</template>
                <SignatureIcon
                  v-if="message.signature"
                  class="signature-icon"
                />
              </span>
            </p>
          </div>
          <div class="actions">
            <router-link
              :to="getMessageLink(message.id)"
              @click.native.stop
            >{{ $t('See message') }}</router-link>
          </div>
        </div>
      </div>
      <IconNoData
        v-else-if="valid"
        :text="$t('No data')"
      />
    </BOverlay>
    <div
      v-if="messages.length"
      class="paginator"
    >
      <div>
        <!-- todo: implement without padders? -->
      </div>
      <BPagination
        v-model="currentPage"
        :total-rows="totalRows"
        :per-page="pagination.perPage"
        :limit="6"
        pills
        size="sm"
        align="center"
        hide-goto-end-buttons
        class="veera"
      >
        <template v-slot:prev-text>
          <span class="material-icons-outlined">keyboard_backspace</span>
        </template>
        <template v-slot:next-text>
          <span class="material-icons-outlined flipped">keyboard_backspace</span>
        </template>
      </BPagination>

      <BFormGroup class="per-page-select">
        {{ $t('Show by') }}
        <BFormSelect
          v-model="perPage"
          class="c-dropdown"
          :options="[5, 10, 20, 50]"
        />
      </BFormGroup>
    </div>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'
import { BOverlay, BFormGroup, BPagination, BFormSelect } from 'bootstrap-vue'
import IconNoData from 'veera-vue/vue/IconNoData.vue'
import SignatureIcon from '@/lib/vue/icons/SignatureIcon.vue'
import ErrorLabel from './ErrorLabel.vue'
import { formatReceiver, formatSender } from '@/lib/ria-mailbox/component-helpers'

export default Vue.extend({
  components: {
    BOverlay,
    BFormGroup,
    BPagination,
    IconNoData,
    BFormSelect,
    SignatureIcon,
    ErrorLabel,
  },
  props: {
    messages: {
      type: [Array, Object],  // Object is for { error }
      required: true,
    },
    direction: {
      type: String,
      required: true,
      validator: value => ['in', 'out', 'search'].includes(value),
    },
    valid: {
      type: Boolean,
      default: true,
    },
  },
  computed: {
    pagination() {
      return this.$store.state.pagination[this.direction]
    },
    // todo: generalize, try mapFields()
    currentPage: {
      get() {
        return this.pagination.currentPage
      },
      set(value) {
        this.$store.commit('set', ['pagination', this.direction, 'currentPage', value])
      },
    },
    perPage: {
      get() {
        return this.pagination.perPage
      },
      set(value) {
        this.$store.commit('set', ['pagination', this.direction, 'perPage', value])
      },
    },
    totalRows() {
      // todo: remove after https://github.com/vuejs/vue/issues/11088 implemented
      // todo: don’t use $store, get it through props
      return this.$store.state.messages[this.direction].value?.totalElements
    },
  },
  methods: {
    getMessageLink(id) {
      return `/mailbox/${this.direction}/${id}`
    },
    formatReceiver,
    formatSender(sender) {
      return formatSender(sender, this.$t.bind(this))  // todo
    },
    isIncoming(message) {
      return message.receiver.id === this.$store.state.me.value.role.id
    },
  },
})
</script>

<style scoped lang="scss">
@import "@/styles/includes";

.message-list-root {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  &.empty {
    padding: $tab-padding;
  }

  .message-list {
    padding: $tab-padding;

    .message {
      padding: 1em 0;
      font-size: $o-font-size-sm;

      display: flex;
      cursor: pointer;

      &:hover {
        background-color: getColor(sapphire-blue-0);
      }

      & + .message {
        // todo
        border-top: 1px solid #dbdfe2;
      }

      .unread-indicator {
        flex: 0 0 auto;
        &.active {
          background-color: #007baf; // todo: make it sapphire?
        }
        width: 0.75em;
        height: 0.75em;
        border-radius: 0.5em;
        margin-top: 0.35em;
      }

      .details {
        flex: 1 1 auto;
        margin-left: 0.7em;

        .box-name {
          margin-bottom: 0.2em;
          color: getColor(black-coral-10);
        }

        .title {
          font-weight: bold;
          &.unread {
            color: getColor(sapphire-blue-10);
          }
        }

        .properties {
          color: getColor(black-coral-10);
          margin-top: 0.4em;
          .icon {
            font-size: 1rem;
            vertical-align: middle;
          }
          .to-or-from {
            margin-left: 1em;
          }
        }
      }

      .actions {
        margin-left: 1em;
        margin-right: 0.8em;
        white-space: nowrap;
      }
    }
  }
}
.signature-icon {
  height: 1.2em;
  margin-left: 0.3em;
  margin-top: -0.2em;
  fill: getColor(black-coral-10);
}
.paginator {
  padding: 8px 0 4px;
  box-shadow: 0px -4px 10px rgba(0, 0, 0, 0.08);
  display: flex;
  justify-content: space-between;

  > * {
    flex: 1;
  }
}
.per-page-select {
  text-align: right;
  font-size: 0.875em; // todo
  margin-bottom: 0; // override bs
  margin-right: 2em;
  select {
    font-size: $o-font-size-sm; // todo
    width: auto;
    height: 30px; // todo
    border: 1px solid #8f91a8;
    box-sizing: border-box;
    border-radius: 2px;
    margin-left: 8px;
  }
}
.no-subject {
  // todo: reuse?
  color: getColor(black-coral-4);
}
</style>
