<template>
  <footer class="the-footer-root">
    <FooterItem
      :heading="$t('help')"
      icon="phone"
    >
      <p>{{ $t('Write to us') }}</p>
      <p>
        <a href="mailto:help@ria.ee">help@ria.ee</a>
      </p>
      <p>
        <a href="https://www.eesti.ee/est/kontaktid">{{ $t('Contacts of the authorities') }}</a>
      </p>
    </FooterItem>
    <FooterItem
      heading="eesti.ee"
      icon="notifications"
    >
      <p>
        <a
          href="https://www.facebook.com/riigiinfosysteemiamet"
          target="_blank"
        >{{ $t('Follow us on Facebook') }}</a>
        &nbsp;
        <i
          class="material-icons-outlined"
          style="font-size: 1em"
        >launch</i>
      </p>
      <p>
        <a href="https://www.eesti.ee/et/riigiportaali-abi">{{ $t('State portal help') }}</a>
      </p>
    </FooterItem>
    <FooterItem
      :heading="$t('communication')"
      icon="chat_bubble_outline"
    >
      <p>
        <a href="https://www.eesti.ee/et/tagasiside">{{ $t('Write a feedback') }}</a>
      </p>
    </FooterItem>
  </footer>
</template>

<script lang="ts">
import Vue from 'vue'
import FooterItem from './FooterItem.vue'

export default Vue.extend({
  components: {
    FooterItem,
  },
})
</script>

<style scoped lang="scss">
@import "@/styles/includes";

$bg-color: #34394c; // no var in veera-styles

.the-footer-root {
  background-color: $bg-color;
  padding: 4em 0;

  display: flex;

  justify-content: space-around;
  @supports not (-ms-ime-align: auto) {
    justify-content: space-evenly;
  }

  // todo: use a var
  @media (max-width: 576px) {
    flex-direction: column;
    align-items: center;

    .footer-item {
      text-align: center;
      display: block;
      width: 18em;

      & + .footer-item {
        border-left: none;
        border-top: 1px solid rgba(#8f91a8, 0.1); // todo
        margin-top: 1.5em;
        padding-top: 1.5em;
      }
      ::v-deep .icon {
        display: none;
      }
    }
  }
}
</style>
