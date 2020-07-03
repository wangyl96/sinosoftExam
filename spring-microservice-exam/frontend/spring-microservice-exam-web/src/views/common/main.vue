<template>
  <section class="main" style="height: 90%;">
    <div class="main-wrapper" style="height: 100%;">
      <div class="main-content" style="height: 100%;">
        <transition name="el-fade-in">
          <router-view :key="key"/>
        </transition>
      </div>
    </div>
  </section>
</template>

<script>

export default {
  name: 'Main',
  computed: {
    key () {
      return this.$route.fullPath
    }
  },
  methods: {
    // TODO 提交考试
    handleSubmitExam () {
      console.log('handleSubmitExam')
    }
  }
}
</script>
