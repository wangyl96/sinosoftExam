<template>
  <div class="app-container">
    <el-card>
      <el-row :gutter="20" style="padding: 10px;">
        <el-col :span="4" style="text-align: center;">
          <span>选择题目</span>
          <div class="answer-number">
            <!--judgeAnswer(value)-->
            <el-button class="number-btn" :class="{ 'button-hcolor':judgeAnswer(value) }" circle v-for="(value, index) in subjectIds" :key="index" @click="toSubject(index, value.subjectId, value.type)" >&nbsp;{{index + 1}}&nbsp;</el-button>
          </div>
        </el-col>
        <el-col :span="16"  class="subject-box-card">
          <el-form ref="dataAnswerForm" :model="tempAnswer" :label-position="labelPosition" label-width="100px">
            <div class="user-info">
              <el-row>
                <el-col :span="6">
                  <el-form-item label="考生姓名：">
                    <span>{{ tempAnswer.userName }}</span>
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="得分：">
                    <span>{{ tempAnswer.subject.temporaryScore }}</span>
                  </el-form-item>
                </el-col>
                <!--<el-col :span="6">
                  <el-form-item label="耗时：">
                    <span>{{ tempAnswer.duration }}</span>
                  </el-form-item>
                </el-col>-->
                <!--<el-col :span="6">
                  <el-form-item label="状态：">
                    <el-tag :type="tempAnswer.markStatus | simpleTagStatusFilter(1) ">{{ tempAnswer.markStatus | submitStatusFilter }}</el-tag>
                  </el-form-item>
                </el-col>-->
                <el-col :span="6">
                  <el-button type="success" icon="el-icon-check"  @click="completeMarking">批改完成</el-button>
                </el-col>
              </el-row>
            </div>
            <el-divider></el-divider>
            <div class="subject-content">
              <!-- 选择题 -->
              <div class="subject-content-option">
                <div class="subject-title">
                  <span class="subject-title-number">{{ subjectIndex }} .</span>
                  {{ tempAnswer.subject.subjectName }}({{tempAnswer.subject.answerScore}})分
                </div>
                <div v-if="tempAnswer.subject.type === 0 || tempAnswer.subject.type === 2 || tempAnswer.subject.type === 3">
                  <ul class="subject-options" v-for="option in tempAnswer.subject.options" :key="option.id">
                    <li class="subject-option" :class="getClass(option.right)">
                      <label><span class="subject-option-prefix">{{ option.optionName }}&nbsp;</span><span v-html="option.optionContent" class="subject-option-prefix"></span></label>
                    </li>
                  </ul>
                </div>
                <!-- 简答题 -->
                <!--<div v-if="tempAnswer.type === 1">-->
                  <!--<p>-->
                    <!--<span v-html="tempAnswer.answer"></span>-->
                  <!--</p>-->
                <!--</div>-->
              </div>
            </div>
            <el-row>
              <el-col :span="24">
                <el-form-item label="考生答案：">
                  <span v-html="tempAnswer.answer"></span>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="24">
                <el-form-item label="参考答案：">
                  <span v-html="tempAnswer.subject.answer.answer"></span>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="24">
                <el-form-item label="解析：">
                  <span v-html="tempAnswer.subject.analysis"></span>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="24">
                <el-form-item label="答题结果：">
                  <el-switch
                    style="display: block"
                    v-model="correct"
                    active-color="#13ce66"
                    inactive-color="#ff4949"
                    active-text="正确"
                    inactive-text="错误"
                    @change="changeState">
                  </el-switch>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="24">
                <el-form-item label="得分：">
                  <el-input v-model="tempAnswer.subject.temporaryScore"/>
                </el-form-item>
              </el-col>
            </el-row>
            <div class="subject-buttons" v-if="tempAnswer.subject.id !== ''">
              <el-button plain @click="last" :loading="loadingLast">上一题</el-button>
              <el-button plain @click="next" v-show="!lastOne()" :loading="loadingNext">下一题</el-button>
            </div>
          </el-form>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script>
import { completeMarking } from '@/api/exam/examRecord'
import { getAnswerByRecordId, markTemporaryAnswer } from '@/api/exam/answer'
import { getSubjectIds } from '@/api/exam/exam'
import waves from '@/directive/waves'
import { mapGetters } from 'vuex'
import { messageFail, messageWarn, messageSuccess } from '@/utils/util'
import Tinymce from '@/components/Tinymce'
import { nextSubjectType, answerType } from '@/const/constant'
let sign=[];
export default {
  name: 'MarkExam',
  components: {
    Tinymce
  },
  directives: {
    waves
  },
  data () {
    return {
      examRecordId: undefined,
      examinationId: undefined,
      subjectId: undefined,
      loadingLast: false,
      loadingNext: false,
      subjectIndex: 1,
      subjectIds: [],
      answer: '',
      duration:'',
      tempAnswer: {
        temporaryScore:'',
        subject: {
          markMap:[],
          score: '',
          answerScore:'',
          temporaryScore:'',
          answer: {
            answer: ''
          }
        }
      },
      labelPosition: 'right',
      currentRecord: {
        id: '',
        userName: '',
        deptName: ''
      },
      correct: true,
      score: 0
    }
  },
  created () {
    const ids = this.$route.params.id.split('-')
    this.examinationId = ids[0]
    this.examRecordId = ids[1]
    this.handleMarking()
  },
  computed: {
    ...mapGetters([
      'elements',
      'permissions'
    ])
  },
  methods: {
    resetTemp () {
      this.temp = {
        id: '',
        examinationName: ''
      }
    },

    handleMarking () {
      getAnswerByRecordId(this.examRecordId, undefined, undefined).then(response => {
        if (response.data.data === null) {
          messageFail(this, '加载答题失败')
          return
        }
        this.tempAnswer = response.data.data
        const { id, score } = this.tempAnswer.subject
        this.subjectId = id
        // this.tempAnswer.temporaryScore = score
        this.correct = this.tempAnswer.answerType === 0
        getSubjectIds(this.examinationId, this.tempAnswer.userId).then(response => {
          const subjectData = response.data.data
          if (subjectData.length > 0) {
            for (let i = 0; i < subjectData.length; i++) {
              const { subjectId, type } = subjectData[i]
              this.subjectIds.push({ subjectId, type, index: i + 1 })
            }
          }
        })
      }).catch(() => {
        messageFail(this, '加载答题失败')
      })
    },

    changeState(){
      if(this.correct === true){
        this.tempAnswer.subject.temporaryScore = this.tempAnswer.subject.answerScore
      }else{
        this.tempAnswer.subject.temporaryScore = 0
      }
    },

    judgeAnswer(value){
      let signs;
      sign=this.tempAnswer.subject.markMap
      Object.keys(sign).forEach(function(key){
        if ( value.subjectId === key ) {
          signs=sign[key]
        }
      });
      return signs
    },
    // 完成批改
    completeMarking () {
      if (this.tempAnswer.subject.temporaryScore === '') {
        messageWarn(this, '得分不能为空')
        return
      }else if (this.tempAnswer.subject.temporaryScore < 0) {
        messageWarn(this, '分数不能小于0分, 请重新打分')
        return
      }else if (this.tempAnswer.subject.temporaryScore > this.tempAnswer.subject.answerScore) {
        messageWarn(this, '分数设置超总分, 请重新打分')
        return
      }
      this.saveCurrentAnswerAndNext(null,null,null,1)
      completeMarking({ id: this.examRecordId }).then(response => {
        if (response.data.data) {
          messageSuccess(this, '操作成功')
        }
      })
      let view ={fullPath: '/exam/mark/'+this.examinationId+'-'+this.examRecordId+''};
      this.$store.dispatch('delView', view).then(({ visitedViews }) => {
          if (view.fullPath === this.$route.fullPath) {
            const latestView = visitedViews.slice(-1)[0];
            if (latestView) {
              this.$router.push(latestView)
            } else {
              this.$router.push('/')
            }
          }
        });
    },
    // 上一题
    last () {
      if (this.tempAnswer.subject.temporaryScore === '') {
        messageWarn(this, '得分不能为空')
        return
      }else if (this.tempAnswer.subject.temporaryScore < 0) {
        messageWarn(this, '分数不能小于0分, 请重新打分')
        return
      }else if (this.tempAnswer.subject.temporaryScore > this.tempAnswer.subject.answerScore) {
        messageWarn(this, '分数设置超总分, 请重新打分')
        return
      }
      for (let i = 0; i < this.subjectIds.length; i++) {
        if (this.subjectIds[i].subjectId === this.subjectId) {
          if (i === 0) {
            messageSuccess(this, '已经是第一题了')
            break
          }
          let { subjectId, type, index } = this.subjectIds[--i]
          this.subjectIndex = index
          this.saveCurrentAnswerAndNext(subjectId, type, nextSubjectType.last,0)
          break
        }
      }
      console.log(this);
    },
    // 下一题
    next () {
      if (this.tempAnswer.subject.temporaryScore === '') {
        messageWarn(this, '得分不能为空')
        return
      }else if (this.tempAnswer.subject.temporaryScore < 0) {
        messageWarn(this, '分数不能小于0分, 请重新打分')
        return
      }else if (this.tempAnswer.subject.temporaryScore > this.tempAnswer.subject.answerScore) {
        messageWarn(this, '分数设置超总分, 请重新打分')
        return
      }
      for (let i = 0; i < this.subjectIds.length; i++) {
        if (this.subjectIds[i].subjectId === this.subjectId) {
          if (i === this.subjectIds.length - 1) {
            messageSuccess(this, '已经是最后一题了')
            break
          }
          let { subjectId, type, index } = this.subjectIds[++i]
          this.subjectIndex = index
          this.saveCurrentAnswerAndNext(subjectId, type, nextSubjectType.next, 0)
          break
        }
      }
    },
    lastOne () {
      for (let i = 0; i < this.subjectIds.length; i++) {
        if (this.subjectIds[i].subjectId === this.subjectId) {
          if (i === this.subjectIds.length - 1) {
            return true
          }
        }
      }
    },
    // 跳到指定的题目
    toSubject (index, subjectId, subjectType) {
      if (this.tempAnswer.subject.temporaryScore === '') {
        messageWarn(this, '得分不能为空')
        return
      }else if (this.tempAnswer.subject.temporaryScore < 0) {
        messageWarn(this, '分数不能小于0分, 请重新打分')
        return
      }else if (this.tempAnswer.subject.temporaryScore > this.tempAnswer.subject.answerScore) {
        messageWarn(this, '分数设置超总分, 请重新打分')
        return
      }
      this.subjectIndex = index + 1
      this.saveCurrentAnswerAndNext(subjectId, subjectType, nextSubjectType.next,0)
    },
    saveCurrentAnswerAndNext (subjectId, subjectType, nextType,updateType) {
      // 更新分数、批改状态
      const answer = {
        id: this.tempAnswer.id,
        temporaryScore: this.tempAnswer.subject.temporaryScore,
        markStatus: 1,
        answerType: this.correct ? 0 : 1,
        updateType: updateType
      }
      markTemporaryAnswer(answer).then(() => {
        if (subjectId !== undefined && subjectId !== '' && subjectId !== null) {
          this.getAnswer(subjectId, subjectType, nextType)
        }
      })
    },
    getAnswer (nextSubjectId, subjectType, nextType) {
      this.startLoading(nextType)
      getAnswerByRecordId(this.examRecordId, nextSubjectId, subjectType, nextSubjectType.current).then(response => {
        if (response.data.data === null) {
          messageWarn(this, '加载题目失败')
        } else {
          this.tempAnswer = response.data.data
          const { id, score } = this.tempAnswer.subject
          this.subjectId = id
          // this.tempAnswer.temporaryScore = score
          this.correct = parseInt(this.tempAnswer.answerType) === 0
          this.updateSubjectIndex()
        }
        this.endLoading(nextType)
      }).catch(error => {
        console.log(error)
        messageFail(this, '加载答题失败')
        this.endLoading(nextType)
      })
    },
    startLoading (nextType) {
      if (nextType === nextSubjectType.next) {
        this.loadingNext = true
      } else if (nextType === nextSubjectType.last) {
        this.loadingLast = true
      } else {
        this.loadingNext = true
      }
    },
    endLoading (nextType) {
      if (nextType === nextSubjectType.next) {
        this.loadingNext = false
      } else if (nextType === nextSubjectType.last) {
        this.loadingLast = false
      } else {
        this.loadingNext = false
      }
    },
    // 更新题目索引
    updateSubjectIndex () {
      this.subjectIndex = this.getSubjectIndex(this.subjectId)
    },
    // 获取题目索引
    getSubjectIndex (targetId) {
      for (let subject of this.subjectIds) {
        let { subjectId, index } = subject
        if (subjectId === targetId) {
          return index
        }
      }
      return 1
    },
    getClass (right) {
      return answerType[right]
    }
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style lang="scss" rel="stylesheet/scss" scoped>
  .button-hcolor {
    background-color: #0099FF;
    color:#fff;
  }
  .subject-title {
    color: #333333;
    font-size: 18px;
    line-height: 22px;
    margin-bottom: 10px;
    padding-left: 20px;
    position: relative;
    .subject-title-number {
      position: absolute;
      left: -25px;
      top: 0;
      display: inline-block;
      width: 40px;
      line-height: 22px;
      text-align: right;
    }
  }
  .subject-option {
    padding-bottom: 10px;
    padding-left: 10px;
  }
  .score {
    margin: 20px;
  }
  .subject-content {
    padding: 12px 0 12px 22px;
    margin-bottom: 12px;
    position: relative;
    color: #666666;
    text-align: left;
  }
  .correct {
    color: #F56C6C;
  }
  .right {
    color: #67C23A;
  }
  .score-gray-box-title {
    text-align: center;
  }
  .subject-buttons {
    text-align: center;
  }
  .answer-number {
    padding: 12px;
    .number-btn {
      margin: 6px;
    }
  }
  .subject-options {
    margin: 0;
    padding: 0;
    list-style: none;
    > li {
      position: relative;
      font-size: 24px;
      label {
        word-break: break-all;
        display: block;
        line-height: 1.0;
        transition: color 0.4s;
        font-weight: normal;
      }
      /* 选项名称 */
      .subject-option-prefix {
        font-size: 16px;
        display: inline-block
      }
    }
  }
  .answer {
    padding: 6px;
  }
</style>
