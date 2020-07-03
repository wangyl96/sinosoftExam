<template>
  <div>
    <div class="subject-content">
      <div class="subject-title">
        {{ index }}
        <span class="subject-title-content" v-html="subjectInfo.subjectName"/>
        <span class="subject-title-content" v-if="subjectInfo.score !== undefined && subjectInfo.score !== 0">&nbsp;({{subjectInfo.score}})分</span>
      </div>
      <ul class="subject-options" v-for="option in options" :key="option.id">
        <li class="subject-option">
          <input class="toggle" type="checkbox" :checked="userAnswer === option.optionName" :id="'option' + option.id" @change="toggleOption(option)">
          <label :for="'option' + option.id">
            <span class="subject-option-prefix">{{ option.optionName }}&nbsp;</span>
            <span v-html="option.optionContent" class="subject-option-prefix" />
          </label>
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Choices',
  data () {
    return {
      subjectCount: 0,
      subjectInfo: {
        subjectName: '',
        score: 0
      },
      options: [],
      showOptions: [],
      userAnswer: '',
      index: '',
      optionsExhibition: []
    }
  },
  methods: {
    random (m, n) {
      var random = Math.floor(Math.random() * (m - n) + n)
      return random
    },
    getAnswer () {
      return this.userAnswer
    },
    setAnswer (answer) {
      this.userAnswer = answer
    },
    setSubjectInfo (subject, subjectCount, index) {
      this.subjectCount = subjectCount
      this.subjectInfo = subject
      if (subject.hasOwnProperty('options')) {
        this.options = subject.options
        this.showOptions = subject.options;
        console.log(this.showOptions)
      }
      for (let i = 0; i < this.options.length; i++) {
       this.optionsExhibition.push(this.options[i].optionName)
      }

      if (subject.hasOwnProperty('answer')) {
        this.setAnswer(subject.answer.answer)
      }
      this.index = index + '.'
    },
    getSubjectInfo () {
      this.subjectInfo.options = this.options
      return this.subjectInfo
    },
    // 选中选项
    toggleOption (option) {
      if (this.userAnswer === option.optionName) {
        this.userAnswer = ''
      } else {
        this.userAnswer = option.optionName
      }
    }
  }
}
</script>

<style lang="scss" scoped>
  @import "../../../assets/css/subject.scss";
</style>
