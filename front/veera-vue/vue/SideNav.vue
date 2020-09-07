<template>
  <div
    class="side-nav-root"
    :class="{hidden}"
  >
    <div
      href="#"
      class="item"
      :class=" { clickable: !isNarrow }"
      @click.prevent="onBurgerOrHideClicked"
    >
      <i
        v-if="hidden"
        class="material-icons-outlined icon"
      >menu</i>
      <i
        v-else
        class="material-icons-outlined icon"
      >close</i>
      <span class="title">{{ $t('Hide menu') }}</span>
    </div>
    <!-- todo: support multiple items,
    currently it’s for the purposes of the assignment only-->
    <div
      v-for="item of items"
      :key="item.name"
      class="item active"
      :class="{ clickable: $route.path !== item.url }"
      @click="$router.push(item.url).catch(e => e)"
    >
      <i class="material-icons-outlined icon inactive-icon">{{ item.icon }}</i>
      <span class="title">{{ item.name }}</span>
    </div>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'

const breakpoints = {
  collapse: 1200,
}

export default Vue.extend({
  props: {
    items: {
      type: Array,
      // todo: remove the “as” when https://github.com/microsoft/TypeScript/issues/38279 gets fixed
      default: () => [] as Array<any>,
    },
    windowWidth: {
      type: Number,
      required: true,
    },
    ignoreScreenWidth: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      userHidden: false,
    }
  },
  computed: {
    hidden() {
      return !this.ignoreScreenWidth && (this.userHidden || this.isNarrow)  // todo
    },
    isNarrow() {
      return !this.ignoreScreenWidth && this.windowWidth < breakpoints.collapse
    },
  },
  methods: {
    onBurgerOrHideClicked() {
      if (!this.isNarrow) {
        this.userHidden = !this.userHidden
      }
      if (this.ignoreScreenWidth) {
        this.$emit('close')
      }
    },
  },
})
</script>

<style scoped lang="scss">
@import "../styles/variables.scss";

.side-nav-root {
  display: flex;
  flex-direction: column;

  width: $side-nav-width;
  color: getColor(white);
  background-color: getColor(sapphire-blue-10);
  user-select: none;
  transition: width 0.1s;

  .item {
    min-height: 100px;
    border-bottom: 1px solid #003a68;
    font-size: $o-font-size-lg;
    display: flex;
    align-items: center;
    white-space: nowrap;

    &.clickable {
      cursor: pointer;
    }
    &.active {
      background-color: getColor(sapphire-blue-12);
    }

    > .icon {
      margin: 0 1em;
    }
  }

  .title {
    width: 208px;
  }

  &.hidden {
    width: 4.6875rem;
    transition: width 0.1s;

    .title {
      display: none;
    }

    .icon {
      margin: 0 1.1em 0 1em;
    }
  }
}
.inactive-icon {
  color: #dadada; // todo
}
</style>
