<template>
  <GlobalCard>
    <p>{{ $t('roleSelectPrompt') }}</p>
    <BListGroup class="mt-3">
      <BListGroupItem
        v-for="role of roles"
        :key="role.id"
        :disabled="busy"
        button
        @click="selectRole(role)"
      >{{ role.id }}</BListGroupItem>
    </BListGroup>
  </GlobalCard>
</template>

<script lang="ts">
import Vue from 'vue'
import { BListGroup, BListGroupItem } from 'bootstrap-vue'
import GlobalCard from './GlobalCard.vue'
import { mapState } from 'vuex'

export default Vue.extend({
  components: {
    BListGroup,
    BListGroupItem,
    GlobalCard,
  },
  data() {
    return {
      busy: false,
    }
  },
  computed: {
    ...mapState(['roles']),
  },
  methods: {
    async selectRole(role) {
      this.busy = true
      try {
        await this.$store.dispatch('selectRole', role)
      } finally {
        this.busy = false
      }
    },
  },
})
</script>

<style scoped lang="scss">
@import "@/styles/includes";
.list-group-item,
.list-group-item:hover {
  color: $color-link;
}
</style>
