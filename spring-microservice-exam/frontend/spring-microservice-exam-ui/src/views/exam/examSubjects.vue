<template>
  <div class="app-container">
    <!--<div class="filter-container">-->
      <!--<el-input :placeholder="$t('table.subjectName')" v-model="subject.listQuery.subjectName" style="width: 200px;" class="filter-item" @keyup.enter.native="handleFilterSubject"/>-->
      <!--<el-button v-waves class="filter-item" type="primary" icon="el-icon-search" @click="handleFilterSubject">{{ $t('table.search') }}</el-button>-->
      <el-button v-if="exam_btn_subject_add" style="margin-bottom: 20px; float: right" class="filter-item" type="primary" icon="el-icon-check" @click="save()">保存</el-button>
      <!--<el-button v-if="exam_btn_subject_import" class="filter-item" type="success" icon="el-icon-upload2" @click="handleImportSubject">{{ $t('table.import') }}</el-button>-->
      <!--<el-button v-if="exam_btn_subject_export" class="filter-item" type="success" icon="el-icon-download" @click="handleExportSubject">{{ $t('table.export') }}</el-button>-->
    <!--</div>-->
    <!--需要先从数据库查出本考试的题型, 再让客户根据题型选择每种题型的个数及分数-->
    <spinner-loading v-if="subject.listLoading"/>

    <el-form ref="dataForm" :rules="rules" :model="form" :label-position="labelPosition" label-width="100px">
      <el-link type="primary" >请指定题型数量及分数</el-link>
      <template>
        <el-table
          :data="tableData"
          border
          style="width: 50%">
          <el-table-column prop="type" label="题型"></el-table-column>

          <el-table-column label="个数">
            <template slot-scope="scope">
              <span v-if="scope.row.type !== '累计'">{{scope.row.count}}</span>
              <span v-if="scope.row.type === '累计'">{{sumCount()}}</span>
            </template>
          </el-table-column>

          <el-table-column label="分值">
            <template slot-scope="scope">
              <el-input v-if="scope.row.type !== '累计'" class="update" clearable v-model="scope.row.score"></el-input>
              <span v-if="scope.row.type === '累计'" >-</span>
            </template>
          </el-table-column>

          <el-table-column
            fixed="right"
            label="操作"
            width="100">
            <template slot-scope="scope">
              <el-button v-if="scope.row.type !== '累计'" type="text" size="small" @click="dialog(scope.row)">选择题库</el-button>
              <span v-if="scope.row.type === '累计'" >-</span>
            </template>
          </el-table-column>

          <el-table-column label="总分">
            <template slot-scope="scope">
              <span v-if="scope.row.type !== '累计'" >{{scope.row.score * scope.row.count}}</span>
              <span v-if="scope.row.type === '累计'" >{{sumScore()}}</span>
            </template>
          </el-table-column>
        </el-table>
      </template>
      <div class="dashboard-editor-container">
        <el-row :gutter="32">
          <el-col :xs="24" :sm="24" :lg="8">
            <div class="chart-wrapper">
              <div class="chart-wrapper-header">
                <div>各题型总分占比</div>
              </div>
              <div class="chart-wrapper-body">
                <another-pie-chart/>
              </div>
            </div>
          </el-col>
          <el-col :xs="24" :sm="24" :lg="8">
            <div class="chart-wrapper">
              <div class="chart-wrapper-header">
                <div>难度占比</div>
              </div>
              <div class="chart-wrapper-body">
                <pie-chart/>
              </div>
            </div>
          </el-col>
          <el-col :xs="24" :sm="24" :lg="8">
            <div class="chart-wrapper">
              <div class="chart-wrapper-header">
                <div>各题库选题占比</div>
              </div>
              <div class="chart-wrapper-body">
                <question-bank-chart v-bind:sunny="subject.listQuery.examinationId"/>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
      <el-dialog
        title="请选择题库"
        :visible.sync="dialogVisible"
        width="50%"
        :before-close="handleClose">
        <template>
          <el-table
            :data="categoryList"
            style="width: 100%;margin-bottom: 20px;"
            row-key="id"
            border
            default-expand-all
            :tree-props="{children: 'children', hasChildren: 'hasChildren'}">
            <el-table-column
              prop="categoryName"
              label="题库"
              width="180">
            </el-table-column>
            <el-table-column
              label="简单难度"
              width="180">
              <template slot-scope="scope">
                <el-input-number v-model="scope.row.simpleNum" style="width: 150px" controls-position="right" @change="handleChange" :min="0" :max="10"></el-input-number>
              </template>
            </el-table-column>
            <el-table-column
              label="一般难度"
              width="180">
              <template slot-scope="scope">
                <el-input-number v-model="scope.row.commonlyNum" style="width: 150px" controls-position="right" @change="handleChange" :min="0" :max="10"></el-input-number>
              </template>
            </el-table-column>
            <el-table-column
              label="困难难度"
              width="180">
              <template slot-scope="scope">
                <el-input-number v-model="scope.row.difficultyNum " style="width: 150px" controls-position="right" @change="handleChange" :min="0" :max="10"></el-input-number>
              </template>
            </el-table-column>
            <el-table-column
              label="总数"
              width="180">
              <template slot-scope="scope">
                <span>{{ sumUp(scope.row) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </template>
        <span slot="footer" class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="determine()">确 定</el-button>
        </span>
      </el-dialog>
    </el-form>
    <!--<el-table-->
      <!--:data="subject.list"-->
      <!--highlight-current-row-->
      <!--style="width: 100%;"-->
      <!--@selection-change="handleSubjectSelectionChange"-->
      <!--@sort-change="sortSubjectChange">-->
      <!--<el-table-column type="selection" width="55"/>-->
      <!--<el-table-column :label="$t('table.subjectName')" min-width="120">-->
        <!--<template slot-scope="scope">-->
          <!--<span v-html="scope.row.subjectName"></span>-->
        <!--</template>-->
      <!--</el-table-column>-->
      <!--<el-table-column :label="$t('table.subject.type')" width="120">-->
        <!--<template slot-scope="scope">-->
          <!--<el-tag :type="scope.row.type | subjectTypeTagFilter" effect="dark" size="small">{{ scope.row.type | subjectTypeFilter }}</el-tag>-->
        <!--</template>-->
      <!--</el-table-column>-->
      <!--<el-table-column :label="$t('table.subject.score')" property="score" width="120">-->
        <!--<template slot-scope="scope">-->
          <!--<span>{{ scope.row.score }}</span>-->
        <!--</template>-->
      <!--</el-table-column>-->
      <!--<el-table-column :label="$t('table.modifyDate')" property="updateTime" width="150">-->
        <!--<template slot-scope="scope">-->
          <!--<span>{{ scope.row.modifyDate | fmtDate('yyyy-MM-dd hh:mm') }}</span>-->
        <!--</template>-->
      <!--</el-table-column>-->
      <!--<el-table-column :label="$t('table.modifier')" property="modifier" width="120">-->
        <!--<template slot-scope="scope">-->
          <!--<span>{{ scope.row.modifier }}</span>-->
        <!--</template>-->
      <!--</el-table-column>-->
      <!--<el-table-column :label="$t('table.actions')" class-name="status-col" width="100px">-->
        <!--<template slot-scope="scope">-->
          <!--<el-dropdown>-->
            <!--<span class="el-dropdown-link">-->
              <!--操作<i class="el-icon-caret-bottom el-icon&#45;&#45;right"></i>-->
            <!--</span>-->
            <!--<el-dropdown-menu slot="dropdown">-->
              <!--<el-dropdown-item v-if="exam_btn_subject">-->
                <!--<a @click="handleUpdateSubject(scope.row)">-->
                  <!--<span><i class="el-icon-edit"></i>{{ $t('table.edit') }}</span>-->
                <!--</a>-->
              <!--</el-dropdown-item>-->
              <!--<el-dropdown-item>-->
                <!--<a @click="handleViewSubject(scope.row)">-->
                  <!--<span><i class="el-icon-view"></i>{{ $t('table.preview') }}</span>-->
                <!--</a>-->
              <!--</el-dropdown-item>-->
              <!--<el-dropdown-item v-if="exam_btn_del">-->
                <!--<a @click="handleDeleteSubject(scope.row)">-->
                  <!--<span><i class="el-icon-delete"></i>{{ $t('table.delete') }}</span>-->
                <!--</a>-->
              <!--</el-dropdown-item>-->
            <!--</el-dropdown-menu>-->
          <!--</el-dropdown>-->
        <!--</template>-->
      <!--</el-table-column>-->
    <!--</el-table>-->

    <div class="pagination-container">
      <el-pagination v-show="subject.total>0" :current-page="subject.listQuery.pageNum" :page-sizes="[10,20,30,50]" :page-size="subject.listQuery.pageSize" :total="subject.total" background layout="total, sizes, prev, pager, next, jumper" @size-change="handleSubjectSizeChange" @current-change="handleSubjectCurrentChange"/>
    </div>

    <!-- 导入题目 -->
    <el-dialog :visible.sync="dialogImportVisible" :title="$t('table.import')">
      <el-row>
        <el-col :span="24">
          <el-upload
            drag
            :multiple="false"
            :auto-upload="true"
            :show-file-list="true"
            :before-upload="beforeUploadSubjectUpload"
            :on-progress="handleUploadSubjectProgress"
            :on-success="handleUploadSubjectSuccess"
            :action="importUrl"
            :headers="headers"
            :data="params"
            style="text-align: center;">
            <i class="el-icon-upload"></i>
            <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
            <div slot="tip" class="el-upload__tip">只能上传xlsx文件</div>
          </el-upload>
        </el-col>
      </el-row>
    </el-dialog>

    <!-- 预览题目 -->
    <el-dialog title="预览题目" :visible.sync="dialogViewVisible" width="60%" top="10vh">
      <div class="subject-title">
        <span class="subject-title-content" v-html="tempSubject.subjectName"/>
        <span class="subject-title-content">&nbsp;({{tempSubject.score}})分</span>
      </div>
      <ul v-if="tempSubject.type === 0 || tempSubject.type === 3" class="subject-options">
        <li class="subject-option" v-for="(option) in tempSubject.options" :key="option.id">
          <input class="toggle" type="checkbox">
          <label><span class="subject-option-prefix">{{option.optionName}}&nbsp;</span><span v-html="option.optionContent" class="subject-option-prefix"></span></label>
        </li>
      </ul>
      <ul v-if="tempSubject.type === 2" class="subject-options">
        <li class="subject-option">
          <input class="toggle" type="checkbox">
          <label><span class="subject-option-prefix">正确</span></label>
        </li>
        <li class="subject-option">
          <input class="toggle" type="checkbox">
          <label><span class="subject-option-prefix">错误</span></label>
        </li>
      </ul>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="dialogViewVisible = false">{{ $t('table.confirm') }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>

import { fetchSubjectListById, addExamRule } from '@/api/exam/exam'
import { fetchCategoryTree, fetchCategoryList } from '@/api/exam/subjectCategory'
import { getSubject, delSubject, exportSubject } from '@/api/exam/subject'
import { getToken } from '@/utils/auth'
import waves from '@/directive/waves'
import { mapGetters } from 'vuex'
import { notifyFail, notifySuccess, exportExcel, isNotEmpty } from '@/utils/util'
import SpinnerLoading from '@/components/SpinnerLoading'
import PieChart from '../dashboard/admin/components/PieChart'
import AnotherPieChart from '../dashboard/admin/components/AnotherPieChart'
import BarChart from '../dashboard/admin/components/BarChart'
import QuestionBankChart from "../dashboard/admin/components/QuestionBankChart";

export default {
  name: 'ExamSubjectsManagement',
  components: {
    QuestionBankChart,
    SpinnerLoading,
    PieChart,
    AnotherPieChart,
    BarChart
  },
  directives: {
    waves
  },
  data () {
    return {
      headers: {
        Authorization: 'Bearer ' + getToken()
      },
      // 表单包括题型及题库
      labelPosition: 'right',
      form: {
      },
      // 校验规则
      rules: {

      },
      categoryList: [],
      resultData:[],
      tableData: [],
      listQuery: {
        subjectName: undefined,
        categoryId: undefined,
        examinationId: undefined,
        sort: 'id',
        order: 'descending'
      },
      dialogVisible: false,
      multipleSelection: [],
      params: {
        busiType: '1'
      },
      tableKey: 0,
      list: null,
      total: null,
      listLoading: true,
      listQuery: {
        pageNum: 1,
        pageSize: 10,
        sort: 'id',
        order: 'descending'
      },
      // 导入题目的url
      importUrl: '/api/exam/v1/subject/import',
      // 题目
      subject: {
        listQuery: {
          pageNum: 1,
          pageSize: 10,
          examinationId: undefined,
          categoryId: '',
          sort: 'id',
          order: 'ascending'
        },
        list: null,
        total: null,
        listLoading: true,
        categoryId: undefined
      },

      exam_btn_add: false,
      exam_btn_edit: false,
      exam_btn_del: false,
      exam_btn_subject: false,
      exam_btn_subject_add: false,
      exam_btn_subject_del: false,
      exam_btn_subject_import: false,
      exam_btn_subject_export: false,
      // 导入弹窗状态
      dialogImportVisible: false,
      // 预览弹窗状态
      dialogViewVisible: false,
      uploadingSubject: false,
      dialogStatus: '',
      textMap: {
        update: '编辑',
        create: '新建'
      },
      // 多选题目
      multipleSubjectSelection: [],
      // 单选题目
      singleSubjectSelection: [],
      percentageSubject: '',
      tempSubject: {
        id: null,
        examinationId: undefined,
        categoryId: undefined,
        subjectName: '',
        type: 0,
        choicesType: 0,
        options: [
          { subjectChoicesId: '', optionName: 'A', optionContent: '' },
          { subjectChoicesId: '', optionName: 'B', optionContent: '' },
          { subjectChoicesId: '', optionName: 'C', optionContent: '' },
          { subjectChoicesId: '', optionName: 'D', optionContent: '' }
        ],
        answer: {
          subjectId: null,
          answer: '',
          answerType: '',
          score: '',
          type: 0
        },
        score: 5,
        analysis: '',
        level: 2
      },
      result: []
    }
  },
  created () {
    this.getList()
    this.exam_btn_add = this.permissions['exam:exam:add']
    this.exam_btn_edit = this.permissions['exam:exam:edit']
    this.exam_btn_del = this.permissions['exam:exam:del']
    this.exam_btn_subject = this.permissions['exam:exam:subject']
    this.exam_btn_subject_add = this.permissions['exam:exam:subject:add']
    this.exam_btn_subject_del = this.permissions['exam:exam:subject:del']
    this.exam_btn_subject_import = this.permissions['exam:exam:subject:import']
    this.exam_btn_subject_export = this.permissions['exam:exam:subject:export']
  },
  computed: {
    ...mapGetters([
      'elements',
      'permissions'
    ])
  },
  methods: {
    sumCount () {
      let numCount = 0
      for (let i = 0; i < this.tableData.length; i++) {
        if (this.tableData[i].type !== '累计') {
          numCount = Number(this.tableData[i].count) + Number(numCount)
        }
      }
      return numCount
    },
    sumScore () {
      let sumScore = 0
      for (let i = 0; i < this.tableData.length; i++) {
        if (this.tableData[i].type !== '累计') {
          sumScore = (Number(this.tableData[i].count) * Number(this.tableData[i].score)) + Number(sumScore)
        }
      }
      return sumScore
    },
    dialog(row) {
      this.dialogVisible = true
      // 调用后台查询题库相关信息(入参需要考试id, 题目类型Id, )
        fetchCategoryList({'examinationId' : this.subject.listQuery.examinationId, 'questionTypeId' : row.questionTypeId}).then(response => {
        console.log(response)
        let categoryList = response.data;
        for (let i = 0; i < categoryList.length; i++) {
          categoryList[i].questionTypeId = row.questionTypeId;
          categoryList[i].examId = this.subject.listQuery.examinationId
        }
        this.categoryList = categoryList;
      })
    },
    sumUp(row) {
      // 获取三种难度
      let simpleNum = row.simpleNum;
      let commonlyNum = row.commonlyNum;
      let difficultyNum = row.difficultyNum;
      return Number(simpleNum) + Number(commonlyNum) + Number(difficultyNum);
    },
    determine() {
      // 将每类题型的个数及总分带出
      let categoryList = this.categoryList;
      // 获取题型
      let questionTypeId = categoryList[0].questionTypeId;
      let tempList = {'questionTypeId' : questionTypeId, 'categoryList' : categoryList}
      if (this.resultData.length > 0) {
        for (let t = 0; t < this.resultData.length; t ++) {
          if ( this.resultData[t].questionTypeId === questionTypeId) {
            this.resultData.splice(t, 1);
            return;
          }
        }
      }
      this.resultData.push(tempList)
      let totalCount = 0;
      for (let i = 0; i < categoryList.length; i++) {
        // 计算该题型选择的总数
        totalCount = totalCount + categoryList[i].simpleNum + categoryList[i].commonlyNum + categoryList[i].difficultyNum
      }
      // 将总数放到tableData中
      for (let l = 0 ; l < this.tableData.length; l++) {
        if (questionTypeId === this.tableData[l].questionTypeId) {
          let temp = {'count': totalCount}
          this.tableData[l].count = totalCount;
        }
      }
      this.dialogVisible = false;
    },
    save() {
      // 将categoryList入库(包括考题类型、考题分数、题库类型、简单题数量、一般难度数量、困难难度数量)
      for (let i = 0; i < this.tableData.length; i++) {
        // 判断用户是否选择了题库
        if (this.tableData[i].count === 0 || this.tableData[i].score === 0) {
          notifyFail(this, '请指定题库及分数')
          return;
        }
        let score = this.tableData[i].score;
        let questionTypeId = this.tableData[i].questionTypeId;

        for (let l = 0; l < this.resultData.length; l++) {
          if (questionTypeId === this.resultData[l].questionTypeId) {
            this.resultData[l].score = score;
          }
        }
      }
      addExamRule(this.resultData).then(() => {
        notifySuccess(this, '保存成功')
        this.$router.go(0);
      })
    },
    handleChange(value) {
    },
    handleClose (done) {
      this.$confirm('确认关闭？')
        .then(_ => {
          done()
        })
        .catch(_ => {})
    },
    toggleSelection (rows) {
      if (rows) {
        rows.forEach(row => {
          this.$refs.multipleTable.toggleRowSelection(row)
        })
      } else {
        this.$refs.multipleTable.clearSelection()
      }
    },
    handleChange (value) {
    },
    handleSelectionChange (val) {
      this.multipleSelection = val
    },
    handleSizeChange (val) {
      this.listQuery.limit = val
      this.getList()
    },
    handleCurrentChange (val) {
      this.listQuery.pageNum = val
      this.getList()
    },
    handleSubjectSizeChange (val) {
      this.subject.listQuery.limit = val
      this.getList()
    },
    handleSubjectCurrentChange (val) {
      this.subject.listQuery.pageNum = val
      this.getList()
    },
    sortSubjectChange (column, prop, order) {
      this.subject.listQuery.sort = column.prop
      this.subject.listQuery.order = column.order
      this.getList()
    },
    handleFilterSubject () {
      this.subject.listQuery.pageNum = 1
      this.getList()
    },
    getList () {
      let subjectInfo = this.$route.params.id
      if (!isNotEmpty(subjectInfo)) {
        return
      }
      this.subject.listLoading = true
      let subjectInfoArr = subjectInfo.split('-')
      this.subject.listQuery.examinationId = subjectInfoArr[0]
      fetchSubjectListById(this.subject.listQuery).then(response => {
        let tableData = response.data.list;
        for (let i = 0; i < tableData.length; i++) {
          let questionTypeId = tableData[i].questionTypeId;
          tableData[i].examId = this.subject.listQuery.examinationId;
          if (questionTypeId === 1) {
            tableData[i].type = '选择题'
          } else if (questionTypeId === 2) {
            tableData[i].type = '判断题'
          } else if (questionTypeId === 3) {
            tableData[i].type = '简答题'
          }
        }
        this.tableData = tableData;
        setTimeout(() => {
          this.subject.listLoading = false
        }, 500)
      })
    },
    // 新建题目
    handleCreateSubject () {
      this.$router.push({
        path: `/exam/subjects/detail/${this.subject.listQuery.examinationId}-undefined-0-0`
      })
    },
    // 导入
    handleImportSubject () {
      this.dialogImportVisible = true
    },
    // 导出
    handleExportSubject () {
      // 没选择题目，导出所有
      if (this.multipleSubjectSelection.length === 0) {
        this.$confirm('是否导出所有题目?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'success'
        }).then(() => {
          exportSubject([], this.subject.listQuery.examinationId).then(response => {
            // 导出Excel
            exportExcel(response)
          })
        }).catch(() => {})
      } else {
        // 导出选中
        this.$confirm('是否导出选中的题目?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'success'
        }).then(() => {
          let ids = []
          for (let i = 0; i < this.multipleSubjectSelection.length; i++) {
            ids.push(this.multipleSubjectSelection[i].id)
          }
          exportSubject(ids, '').then(response => {
            // 导出Excel
            exportExcel(response)
          })
        }).catch(() => {})
      }
    },
    handleSubjectSelectionChange (val) {
      this.multipleSubjectSelection = val
    },
    // 修改题目
    handleUpdateSubject (row) {
      let examinationId = this.subject.listQuery.examinationId
      this.$router.push({
        path: `/exam/subjects/detail/${examinationId}-${row.id}-${row.type}-0`
      })
    },
    // 查看题目
    handleViewSubject (row) {
      // 加载题目信息
      getSubject(row.id, { type: row.type }).then(response => {
        this.tempSubject = response.data.data
        this.dialogViewVisible = true
      })
    },
    // 删除题目
    handleDeleteSubject (row) {
      this.$confirm('确定要删除吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        delSubject(row.id, { type: row.type }).then(() => {
          this.getList()
          notifySuccess(this, '删除成功')
        })
      }).catch(() => {})
    },
    handleUploadSubjectProgress (event, file, fileList) {
      this.uploadingSubject = true
      this.percentageSubject = parseInt(file.percentage.toFixed(0))
    },
    // 上传成功
    handleUploadSubjectSuccess () {
      this.dialogImportVisible = false
      this.getList()
      notifySuccess(this, '导入成功')
      this.uploadingSubject = false
    },
    // 上传前
    beforeUploadSubjectUpload (file) {
      const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      const isLt10M = file.size / 1024 / 1024 < 10
      if (!isExcel) {
        this.$message.error('上传附件只能是 excel 格式!')
      }
      if (!isLt10M) {
        this.$message.error('上传附件大小不能超过 10MB!')
      }
      return isExcel && isLt10M
    }
  }
}
</script>

<style lang="scss" rel="stylesheet/scss" scoped>
  .subject-title {
    font-size: 18px;
    line-height: 22px;
    .subject-title-number {
      display: inline-block;
      line-height: 22px;
    }
    .subject-title-content {
      display: inline-block;
    }
  }
  .dashboard-editor-container {
    margin-top: 50px;
    padding: 12px;
    .chart-wrapper {
      background: #fff;
      padding: 16px 16px 0;
      margin-bottom: 32px;
      box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
    }
    .chart-wrapper-header {
      padding: 8px 12px;
      border-bottom: 1px solid #EBEEF5;
      -webkit-box-sizing: border-box;
      box-sizing: border-box;
      .chart-wrapper-header-select {
        position: absolute;
        right: 0;
        top: 6px;
        padding: 8px 12px;
      }
    }
    .chart-wrapper-body {
      padding-top: 20px;
    }
  }
  .subject-options {
    margin: 0;
    padding: 0;
    list-style: none;
    > li {
      position: relative;
      font-size: 24px;
      .toggle {
        opacity: 0;
        text-align: center;
        width: 35px;
        /* auto, since non-WebKit browsers doesn't support input styling */
        height: auto;
        position: absolute;
        top: 0;
        bottom: 0;
        margin: auto 0;
        border: none;
        /* Mobile Safari */
        -webkit-appearance: none;
        appearance: none;
      }
      .toggle+label {
        background-image: url('data:image/svg+xml;utf8,%3Csvg%20xmlns%3D%22http%3A//www.w3.org/2000/svg%22%20width%3D%2240%22%20height%3D%2240%22%20viewBox%3D%22-10%20-18%20100%20135%22%3E%3Ccircle%20cx%3D%2250%22%20cy%3D%2250%22%20r%3D%2250%22%20fill%3D%22none%22%20stroke%3D%22%23ededed%22%20stroke-width%3D%223%22/%3E%3C/svg%3E');
        background-repeat: no-repeat;
        background-position: center left;
        background-size: 30px;
      }
      .toggle:checked+label {
        background-size: 30px;
        background-image: url('data:image/svg+xml;utf8,%3Csvg%20xmlns%3D%22http%3A//www.w3.org/2000/svg%22%20width%3D%2240%22%20height%3D%2240%22%20viewBox%3D%22-10%20-18%20100%20135%22%3E%3Ccircle%20cx%3D%2250%22%20cy%3D%2250%22%20r%3D%2250%22%20fill%3D%22none%22%20stroke%3D%22%23bddad5%22%20stroke-width%3D%223%22/%3E%3Cpath%20fill%3D%22%235dc2af%22%20d%3D%22M72%2025L42%2071%2027%2056l-4%204%2020%2020%2034-52z%22/%3E%3C/svg%3E');
      }
      label {
        word-break: break-all;
        padding: 10px 10px 10px 45px;
        display: block;
        line-height: 1.0;
        transition: color 0.4s;
      }
      /* 选项名称 */
      .subject-option-prefix {
        font-size: 16px;
        display: inline-block
      }
    }
  }
</style>
