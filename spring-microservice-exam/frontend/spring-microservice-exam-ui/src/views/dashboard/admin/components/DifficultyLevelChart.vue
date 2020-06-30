<template>
  <div :class="className" :style="{height:height,width:width}"/>
</template>

<script>
import echarts from 'echarts' // echarts theme
import { difficultyLevelChart } from '@/api/exam/subjectCategory'
import { debounce } from '@/utils'
require('echarts/theme/macarons')

export default {
  props: {
    className: {
      type: String,
      default: 'chart'
    },
    sunny: {
      type: String,
      required: true
    },
    width: {
      type: String,
      default: '100%'
    },
    height: {
      type: String,
      default: '350px'
    }
  },
  data () {
    return {
      chart: null,
      chartData: [],
      nameData: []
    }
  },
  mounted () {
    this.questionBankChart()
    this.__resizeHandler = debounce(() => {
      if (this.chart) {
        this.chart.resize()
      }
    }, 100)
    window.addEventListener('resize', this.__resizeHandler)
  },
  beforeDestroy () {
    if (!this.chart) {
      return
    }
    window.removeEventListener('resize', this.__resizeHandler)
    this.chart.dispose()
    this.chart = null
  },
  methods: {
    questionBankChart () {
      difficultyLevelChart({ 'examinationId': this.sunny }).then(response => {
        let data = response.data
        if (data.length == 0) {
          this.initChart([], [])
        } else {
          let chartData = []
          let nameData = []
          if (data.simpleNum > 0) {
            chartData.push({ 'name': '简单', 'value': data.simpleNum })
            nameData.push('简单')
          }
          if (data.commonlyNum > 0) {
            chartData.push({ 'name': '一般', 'value': data.commonlyNum })
            nameData.push('一般')
          }
          if (data.difficultyNum > 0) {
            chartData.push({ 'name': '困难', 'value': data.difficultyNum })
            nameData.push('困难')
          }
          this.initChart(chartData, nameData)
        }

      })
    },
    initChart (chartData, nameData) {
      console.log(chartData)
      this.chart = echarts.init(this.$el, 'macarons')
      this.chart.setOption({
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          bottom: '10',
          left: 'center',
          data: nameData
        },
        series: [
          {
            name: '难度等级分布',
            type: 'pie',
            selectedMode: 'single',
            radius: [0, '30%'],
            label: {
              position: 'inner'
            },
            labelLine: {
              show: false
            },
            data: [
            ]
          },
          {
            name: '难度等级分布',
            type: 'pie',
            radius: ['40%', '55%'],
            label: {
              normal: {
                formatter: function (param) {
                  return param.name + '\n' + Math.round(param.percent) + '%'
                }
              }
            },
            data: chartData,
            animationEasing: 'cubicInOut',
            animationDuration: 2600
          }
        ]
      })
    }

  }
}
</script>
