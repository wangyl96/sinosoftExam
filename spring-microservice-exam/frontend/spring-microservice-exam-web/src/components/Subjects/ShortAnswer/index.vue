<template>
  <div>
    <div class="subject-content">
      <div class="subject-title">
        {{ index }}
        <span class="subject-title-content" v-html="subjectInfo.subjectName"/>
        <span class="subject-title-content" v-if="subjectInfo.score !== undefined && subjectInfo.score !== 0">&nbsp;({{subjectInfo.score}})分</span>
        <div class="subject-tinymce">
          <el-input
            type="textarea"
            :autosize="{ minRows: 2, maxRows: 4}"
            style="height: 300px"
            placeholder="请输入内容"
            v-model="userAnswer">
          </el-input>
          <!--<tinymce :height="height" v-model="userAnswer"/>-->
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Tinymce from '@/components/Tinymce'
export default {
  name: 'ShortAnswer',
  components: {
    Tinymce
  },
  props: {
    height: {
      type: Number,
      required: false,
      default: 300
    }
  },
  data () {
    return {
      subjectCount: 0,
      subjectInfo: {
        subjectName: '',
        score: 0
      },
      userAnswer: '',
      index: ''
    }
  },
  methods: {
    getAnswer () {
      return this.userAnswer
    },
    setAnswer (answer) {
      this.userAnswer = answer
    },
    setSubjectInfo (subject, subjectCount, index) {
      this.subjectCount = subjectCount
      this.subjectInfo = subject
      if (subject.hasOwnProperty('answer')) {
        this.setAnswer(subject.answer.answer)
      }
      this.index = index + '.'
    },
    getSubjectInfo () {
      return this.subjectInfo
    }
  }
}
</script>

<style lang="scss" scoped>
  @import "../../../assets/css/subject.scss";
  .subject-tinymce {
    margin: 12px;
  }
</style>
