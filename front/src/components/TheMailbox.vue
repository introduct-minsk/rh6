<template>
  <div class="the-mailbox-root">
    <h2 class="heading">{{ $t('Mailbox') }}</h2>
    <MailboxMessage
      v-if="$route.params.messageId"
      class="message"
    />
    <BTabs
      v-else
      v-model="currentTab"
      class="veera"
      :content-class="['tab', { empty:isCurrentTabEmpty }]"
      active-nav-item-class="active"
    >
      <BTab :class="{ empty: !messagesIn.length }">
        <template v-slot:title>{{ $t('Inbox') }}</template>
        <MessageList
          :messages="messagesIn"
          :valid="$store.state.messages.in.valid"
          direction="in"
        />
      </BTab>

      <BTab
        :title="$t('Outbox')"
        :class="{empty:!messagesOut.length}"
      >
        <MessageList
          :messages="messagesOut"
          :valid="$store.state.messages.out.valid"
          direction="out"
        />
      </BTab>

      <BTab
        :title="$t('New message')"
        class="new-message-tab"
      >
        <NewMessage />
      </BTab>

      <BTab
        v-if="$store.state.searchTerm.trim()"
        :class="{ empty: !$store.state.messages.search.value.content.length }"
        :title="$t('Search results')"
      >
        <MessageList
          :messages="$store.state.messages.search.value.content"
          :valid="$store.state.messages.search.valid"
          direction="search"
        />
      </BTab>
    </BTabs>
    <!-- todo: why doesn’t it get rendered as a separate component? -->
    <BToast
      id="my-toast"
      variant="info"
      no-auto-hide
      no-close-button
    >
      <template v-slot:toast-title="{ hide }">
        {{ $t('You have new messages') }}
        <i
          class="material-icons-outlined ml-auto"
          style="cursor:pointer;"
          @click="hide"
        >close</i>
      </template>

      <template v-slot="{ hide }">
        <router-link
          to="/mailbox/unread"
          @click.native="hide"
        >{{ $t('See new messages') }}</router-link>
      </template>
    </BToast>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'
import { BTabs, BTab, BToast } from 'bootstrap-vue'
import MailboxMessage from './MailboxMessage.vue'
import NewMessage from './NewMessage.vue'
import MessageList from './MessageList.vue'
import { mapGetters } from 'vuex'
import { mapFields } from 'vuex-map-fields'
import { api } from '@/lib/ria-mailbox/api'

enum Tabs {
  in,
  out,
  new,
  search,
}

export default Vue.extend({
  components: {
    BTabs,
    BTab,
    BToast,
    NewMessage,
    MessageList,
    MailboxMessage,
  },
  computed: {
    ...mapGetters(['messagesOut', 'messagesIn']),
    ...mapFields(['currentTab']),
    isCurrentTabEmpty() {
      return [
        !this.messagesIn.length,
        !this.messagesOut.length,
        false,
        !this.$store.state.messages.search.value.content.length,
      ][this.currentTab]
    },
  },
  watch: {
    async currentTab(value) {
      this.$router.push(`/mailbox/${Tabs[value]}`).catch(e => e)
    },
    $route: {
      immediate: true,
      async handler(route) {
        if (route.params.tab === 'unread') {
          this.$store.commit('set', ['pagination', 'in', 'currentPage', 1])
          this.$store.dispatch('fetchMessages', 'in')  // todo: handle error
          this.$router.replace(`/mailbox/in`)
          return
        }

        this.currentTab = Tabs[route.params.tab]
        if (route.params.tab === Tabs[Tabs.search] && !this.$store.state.searchTerm.trim()) {
          this.$router.replace('/mailbox/in')
          this.currentTab = Tabs.in
        }
      },
    },
  },
  mounted() {
    api.onWebsocketMessage(this.onWebsocketMessage)
  },
  methods: {
    onWebsocketMessage(message) {
      if (message.type === 'MESSAGE_RECEIVED') {
        if (this.$route.path === '/mailbox/in'
          && this.$store.state.pagination.in.currentPage === 1
        ) {
          this.$store.dispatch('fetchMessages', 'in')  // todo: handle error
        } else {
          this.$bvToast.show('my-toast')
        }
      }
    },
  },
})
</script>

<style scoped lang="scss">
@import "@/styles/includes";

.the-mailbox-root {
  padding: 2.5em 1em;
  display: flex;
  flex-direction: column;

  .heading {
    flex-shrink: 0;
    margin-bottom: 1rem;
  }

  .message {
    padding: $tab-padding; // todo
    background-color: getColor(white);
    border: 1px solid #dbdfe2; //todo
    border-radius: 0 0 5px 5px;
  }

  .tabs {
    ::v-deep .tab {
      &.empty {
        padding: 1.375rem 1.5rem;
        display: flex;
        justify-content: center;
        align-items: center;
      }
    }
  }
}
.tab-body {
  padding: $tab-padding; // todo
  padding-bottom: 0;
  h3 {
    font-weight: bold;
  }
}
</style>
