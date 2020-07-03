<template>
  <div id="app">
    <router-view v-if="isRouterAlive" class="main" style="height: 100%;"> </router-view>
  </div>
</template>

<script>
export default {
  name: 'app',
  provide () {
    return {
      reload: this.reload
    }
  },
  data () {
    return {
      isRouterAlive: true
    }
  },
  methods: {
    reload () {
      this.isRouterAlive = false;
      this.$nextTick(function () {
        this.isRouterAlive = true
      })
    }
  }
}
</script>

<style lang="scss" rel="stylesheet/scss">
  @import "assets/css/style.scss";
  #app {
    height: 100%;
  }
</style>
