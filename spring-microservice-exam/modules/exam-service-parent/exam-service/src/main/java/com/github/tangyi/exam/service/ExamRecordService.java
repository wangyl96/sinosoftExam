package com.github.tangyi.exam.service;

import com.github.pagehelper.PageInfo;
import com.github.tangyi.common.basic.utils.excel.ExcelToolUtil;
import com.github.tangyi.common.basic.vo.DeptVo;
import com.github.tangyi.common.basic.vo.UserRecordVo;
import com.github.tangyi.common.basic.vo.UserVo;
import com.github.tangyi.common.core.constant.CommonConstant;
import com.github.tangyi.common.core.exceptions.CommonException;
import com.github.tangyi.common.core.model.ResponseBean;
import com.github.tangyi.common.core.service.CrudService;
import com.github.tangyi.common.core.utils.DateUtils;
import com.github.tangyi.common.core.utils.PageUtil;
import com.github.tangyi.common.core.utils.ResponseUtil;
import com.github.tangyi.common.security.utils.SysUtil;
import com.github.tangyi.exam.api.dto.*;
import com.github.tangyi.exam.api.enums.SubmitStatusEnum;
import com.github.tangyi.exam.api.module.*;
import com.github.tangyi.exam.api.vo.QuestionCategoryVO;
import com.github.tangyi.exam.excel.model.ExamRecordExcelModel;
import com.github.tangyi.exam.mapper.*;
import com.github.tangyi.exam.utils.ExamRecordUtil;
import com.github.tangyi.user.api.feign.UserServiceClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 考试记录service
 *
 * @author tangyi
 * @date 2018/11/8 21:20
 */
@Slf4j
@AllArgsConstructor
@Service
public class ExamRecordService extends CrudService<ExamRecordMapper, ExaminationRecord> {

	private final UserServiceClient userServiceClient;

	private final ExaminationService examinationService;

	private final AnswerMapper answerMapper;

	private final SubjectService subjectService;


	/**
	 * 选择题
	 */
	private final static Integer CHOICE = 1;

	/**
	 * 判断题
	 */
	private final static Integer JUDGEMENT = 2;

	/**
	 * 简答题
	 */
	private final static Integer SHORTANSWER  = 3;

	@Resource
	private ExamQuestionExamMapper examQuestionExamMapper;

	@Resource
	private ExamQuestionCategoryMapper examQuestionCategoryMapper;

	@Resource
	private SubjectChoicesMapper subjectChoicesMapper;

	@Resource
	private SubjectJudgementMapper subjectJudgementMapper;

	@Resource
	private SubjectShortAnswerMapper subjectShortAnswerMapper;

	@Resource
	private ExaminationSubjectMapper examinationSubjectMapper;

	@Resource
	private ExamRecordMapper examRecordMapper;

	/**
     * 查询考试记录
     *
     * @param examRecord examRecord
     * @return ExamRecord
     * @author tangyi
     * @date 2019/1/3 14:10
     */
    @Override
    @Cacheable(value = "record#" + CommonConstant.CACHE_EXPIRE, key = "#examRecord.id")
    public ExaminationRecord get(ExaminationRecord examRecord) {
        return super.get(examRecord);
    }

	/**
	 * 获取分页数据
	 *
	 * @param pageNum    pageNum
	 * @param pageSize   pageSize
	 * @param sort       sort
	 * @param order      order
	 * @param examRecord examRecord
	 * @return PageInfo
	 * @author tangyi
	 * @date 2018/11/10 21:33
	 */
    public PageInfo<ExaminationRecordDto> examRecordList(ExaminationRecord examRecord, String pageNum, String pageSize, String sort, String order) {
		examRecord.setTenantCode(SysUtil.getTenantCode());
		PageInfo<ExaminationRecordDto> examRecordDtoPageInfo = new PageInfo<>();
		List<ExaminationRecordDto> examRecordDtoList = new ArrayList<>();
		// 查询考试记录
		PageInfo<ExaminationRecord> examRecordPageInfo = this.findPage(
				PageUtil.pageInfo(pageNum, pageSize, sort, order), examRecord);
		if (CollectionUtils.isNotEmpty(examRecordPageInfo.getList())) {
			// 查询考试信息
			List<Examination> examinations = examinationService.findListById(examRecordPageInfo.getList().stream().map(ExaminationRecord::getExaminationId).distinct().toArray(Long[]::new));
			examRecordPageInfo.getList().forEach(tempExamRecord -> {
				// 找到考试记录所属的考试信息
				Examination examinationRecordExamination = examinations.stream()
						.filter(tempExamination -> tempExamRecord.getExaminationId().equals(tempExamination.getId()))
						.findFirst().orElse(null);
				// 转换成ExamRecordDto
				if (examinationRecordExamination != null) {
					ExaminationRecordDto examRecordDto = new ExaminationRecordDto();
					BeanUtils.copyProperties(examinationRecordExamination, examRecordDto);
					examRecordDto.setId(tempExamRecord.getId());
					examRecordDto.setStartTime(tempExamRecord.getStartTime());
					examRecordDto.setEndTime(tempExamRecord.getEndTime());
					examRecordDto.setScore(tempExamRecord.getScore());
					examRecordDto.setUserId(tempExamRecord.getUserId());
					examRecordDto.setExaminationId(tempExamRecord.getExaminationId());
					// 正确题目数
					examRecordDto.setCorrectNumber(tempExamRecord.getCorrectNumber());
					examRecordDto.setInCorrectNumber(tempExamRecord.getInCorrectNumber());
					// 提交状态
					examRecordDto.setSubmitStatus(tempExamRecord.getSubmitStatus());
					examRecordDtoList.add(examRecordDto);
				}
			});
			this.fillExamUserInfo(examRecordDtoList, examRecordPageInfo.getList().stream().map(ExaminationRecord::getUserId).distinct().toArray(Long[]::new));
		}
		examRecordDtoPageInfo.setTotal(examRecordPageInfo.getTotal());
		examRecordDtoPageInfo.setPages(examRecordPageInfo.getPages());
		examRecordDtoPageInfo.setPageSize(examRecordPageInfo.getPageSize());
		examRecordDtoPageInfo.setPageNum(examRecordPageInfo.getPageNum());
		examRecordDtoPageInfo.setList(examRecordDtoList);
		return examRecordDtoPageInfo;
	}

    /**
     * 更新考试记录
     *
     * @param examRecord examRecord
     * @return ExamRecord
     * @author tangyi
     * @date 2019/1/3 14:10
     */
    @Override
    @Transactional
    @CacheEvict(value = "record", key = "#examRecord.id")
    public int update(ExaminationRecord examRecord) {
        return super.update(examRecord);
    }

    /**
     * 删除考试记录
     *
     * @param examRecord examRecord
     * @return ExamRecord
     * @author tangyi
     * @date 2019/1/3 14:10
     */
    @Override
    @Transactional
    @CacheEvict(value = "record", key = "#examRecord.id")
    public int insert(ExaminationRecord examRecord) {
        return super.insert(examRecord);
    }

    /**
     * 根据用户id、考试id查找
     *
     * @param examRecord examRecord
     * @return ExamRecord
     * @author tangyi
     * @date 2018/12/26 13:58
     */
    public ExaminationRecord getByUserIdAndExaminationId(ExaminationRecord examRecord) {
        return this.dao.getByUserIdAndExaminationId(examRecord);
    }

    /**
     * 批量删除
     *
     * @param ids ids
     * @return int
     * @author tangyi
     * @date 2019/1/3 14:11
     */
    @Override
    @Transactional
    @CacheEvict(value = "record", allEntries = true)
    public int deleteAll(Long[] ids) {
        return super.deleteAll(ids);
    }

	/**
	 * 获取用户、部门相关信息
	 * @param examRecordDtoList examRecordDtoList
	 * @param userIds userIds
	 */
    public void fillExamUserInfo(List<ExaminationRecordDto> examRecordDtoList, Long[] userIds) {
		// 查询用户信息
		ResponseBean<List<UserRecordVo>> returnT = userServiceClient.findUserById(userIds);
		if (ResponseUtil.isSuccess(returnT)) {
			// 查询部门信息
			ResponseBean<List<DeptVo>> deptResponseBean = userServiceClient.findDeptById(returnT.getData().stream().map(UserRecordVo::getDeptId).distinct().toArray(Long[]::new));
			if (ResponseUtil.isSuccess(deptResponseBean)) {
				examRecordDtoList.forEach(tempExamRecordDto -> {
					// 查询、设置用户信息
					UserRecordVo examRecordDtoUserVo = returnT.getData().stream()
							.filter(tempUserVo -> tempExamRecordDto.getUserId().equals(tempUserVo.getId()))
							.findFirst().orElse(null);
					if (examRecordDtoUserVo != null) {
						//设置用户名
						tempExamRecordDto.setName(examRecordDtoUserVo.getUsername());
						//设置公司
						tempExamRecordDto.setCompany(examRecordDtoUserVo.getCompany());
						//设置岗位
						tempExamRecordDto.setStation(examRecordDtoUserVo.getStation());
						// 设置姓名
						tempExamRecordDto.setUserName(examRecordDtoUserVo.getName());
						// 查询、设置部门信息
						if (CollectionUtils.isNotEmpty(deptResponseBean.getData())) {
							DeptVo examRecordDtoDeptVo = deptResponseBean.getData().stream()
									// 根据部门ID过滤
									.filter(tempDept -> tempDept.getId().equals(examRecordDtoUserVo.getDeptId()))
									.findFirst().orElse(null);
							// 设置部门名称
							if (examRecordDtoDeptVo != null)
								tempExamRecordDto.setDeptName(examRecordDtoDeptVo.getDeptName());
						}
					}
				});
			}
		}
	}

	/**
	 * 查询考试记录数
	 * @param examinationRecord examinationRecord
	 * @return int
	 * @author tangyi
	 * @date 2020/1/31 5:17 下午
	 */
	public int findExaminationRecordCount(ExaminationRecord examinationRecord) {
		return this.dao.findExaminationRecordCount(examinationRecord);
	}

	/**
	 *
	 * 根据时间范围查询考试记录数
	 * @param start start
	 * @return List
	 * @author tangyi
	 * @date 2020/1/31 10:17 下午
	 */
	public List<ExaminationRecord> findExaminationRecordCountByDate(Date start) {
		return this.dao.findExaminationRecordCountByDate(start);
	}

	/**
	 * 导出
	 *
	 * @param ids ids
	 * @param request request
	 * @param response response
	 * @author tangyi
	 * @date 2018/12/31 22:28
	 */
	public void exportExamRecord(Long[] ids, HttpServletRequest request, HttpServletResponse response) {
		try {
			List<ExaminationRecord> examRecordList;
			if (ArrayUtils.isNotEmpty(ids)) {
				examRecordList = this.findListById(ids);
			} else {
				// 导出全部
				ExaminationRecord examRecord = new ExaminationRecord();
				examRecord.setTenantCode(SysUtil.getTenantCode());
				examRecordList = this.findList(examRecord);
			}
			// 查询考试、用户、部门数据
			if (CollectionUtils.isNotEmpty(examRecordList)) {
				List<ExaminationRecordDto> examRecordDtoList = new ArrayList<>();
				// 查询考试信息
				List<Examination> examinations = examinationService.findListById(examRecordList.stream().map(ExaminationRecord::getExaminationId).distinct().toArray(Long[]::new));
				// 用户id
				Set<Long> userIdSet = new HashSet<>();
				examRecordList.forEach(tempExamRecord -> {
					// 查找考试记录所属的考试信息
					Examination examRecordExamination = examinations.stream()
							.filter(tempExamination -> tempExamRecord.getExaminationId().equals(tempExamination.getId()))
							.findFirst().orElse(null);
					if (examRecordExamination != null) {
						ExaminationRecordDto recordDto = new ExaminationRecordDto();
						recordDto.setId(tempExamRecord.getId());
						recordDto.setExaminationName(examRecordExamination.getExaminationName());
						recordDto.setStartTime(tempExamRecord.getStartTime());
						recordDto.setEndTime(tempExamRecord.getEndTime());
						recordDto.setDuration(
								ExamRecordUtil.getExamDuration(tempExamRecord.getStartTime(), tempExamRecord.getEndTime()));
						recordDto.setScore(tempExamRecord.getScore());
						recordDto.setUserId(tempExamRecord.getUserId());
						recordDto.setCorrectNumber(tempExamRecord.getCorrectNumber());
						recordDto.setInCorrectNumber(tempExamRecord.getInCorrectNumber());
						recordDto.setSubmitStatusName(
								SubmitStatusEnum.match(tempExamRecord.getSubmitStatus(), SubmitStatusEnum.NOT_SUBMITTED).getName());
						userIdSet.add(tempExamRecord.getUserId());
						examRecordDtoList.add(recordDto);
					}
				});
				this.fillExamUserInfo(examRecordDtoList, userIdSet.toArray(new Long[0]));
				ExcelToolUtil.writeExcel(request, response, ExamRecordUtil.convertToExcelModel(examRecordDtoList), ExamRecordExcelModel.class);
			}
		} catch (Exception e) {
			log.error("Export examRecord failed", e);
		}
	}

	/**
	 * 查询参与考试人数
	 *
	 * @return ExaminationDashboardDto
	 * @author tangyi
	 * @date 2019/10/27 20:07:38
	 */
	public ExaminationDashboardDto findExamDashboardData(String tenantCode) {
		ExaminationDashboardDto dashboardDto = new ExaminationDashboardDto();
		Examination examination = new Examination();
		examination.setCommonValue(SysUtil.getUser(), SysUtil.getSysCode(), tenantCode);
		// 考试数量
		dashboardDto.setExaminationCount(examinationService.findExaminationCount(examination));
		// 考生数量
		dashboardDto.setExamUserCount(examinationService.findExamUserCount(examination));
		// 考试记录数量
		ExaminationRecord examinationRecord = new ExaminationRecord();
		examinationRecord.setCommonValue(examination.getCreator(), examination.getApplicationCode(), examination.getTenantCode());
		dashboardDto.setExaminationRecordCount(this.findExaminationRecordCount(examinationRecord));
		return dashboardDto;
	}

	/**
	 * 查询过去n天的考试记录数据
	 * @param tenantCode tenantCode
	 * @param pastDays pastDays
	 * @return ExaminationDashboardDto
	 * @author tangyi
	 * @date 2020/1/31 5:46 下午
	 */
	public ExaminationDashboardDto findExamRecordTendency(String tenantCode, int pastDays) {
		ExaminationDashboardDto dashboardDto = new ExaminationDashboardDto();
		Examination examination = new Examination();
		examination.setCommonValue(SysUtil.getUser(), SysUtil.getSysCode(), tenantCode);
		Map<String, String> tendencyMap = new LinkedHashMap<>();
		LocalDateTime start = null;
		pastDays = -pastDays;
		for (int i = pastDays; i <= 0; i++) {
			LocalDateTime localDateTime = DateUtils.plusDay(i);
			if (i == pastDays) {
				start = localDateTime;
			}
			tendencyMap.put(localDateTime.format(DateUtils.FORMATTER_DAY), "0");
		}
		List<ExaminationRecord> examinationRecords = this.findExaminationRecordCountByDate(DateUtils.asDate(start));
		if (CollectionUtils.isNotEmpty(examinationRecords)) {
			Map<String, List<ExaminationRecord>> examinationRecordsMap = examinationRecords.stream()
					.peek(examinationRecord -> examinationRecord
							.setExt(DateUtils.asLocalDateTime(examinationRecord.getCreateDate())
									.format(DateUtils.FORMATTER_DAY)))
					.collect(Collectors.groupingBy(ExaminationRecord::getExt));
			log.info("ExamRecordTendency map: {}", examinationRecordsMap);
			examinationRecordsMap.forEach((key, value) -> tendencyMap.replace(key, String.valueOf(value.size())));
		}
		dashboardDto.setExamRecordDate(new ArrayList<>(tendencyMap.keySet()));
		dashboardDto.setExamRecordData(new ArrayList<>(tendencyMap.values()));
		return dashboardDto;
	}

	/**
	 * 成绩详情
	 * @param id id
	 * @return ExaminationRecordDto
	 * @author tangyi
	 * @date 2020/2/21 9:26 上午
	 */
	public ExaminationRecordDto details(Long id) {
		//获取该条考试记录
		ExaminationRecord examRecord = this.get(id);
		if (examRecord == null)
			throw new CommonException("ExamRecord is not exist");
		//获取考试表记录
		Examination examination = examinationService.get(examRecord.getExaminationId());
		if (examination == null)
			throw new CommonException("Examination is not exist");
		ExaminationRecordDto examRecordDto = new ExaminationRecordDto();
		BeanUtils.copyProperties(examination, examRecordDto);
		examRecordDto.setId(examRecord.getId());
		examRecordDto.setStartTime(examRecord.getStartTime());
		examRecordDto.setEndTime(examRecord.getEndTime());
		examRecordDto.setScore(examRecord.getScore());
		examRecordDto.setUserId(examRecord.getUserId());
		examRecordDto.setExaminationId(examRecord.getExaminationId());
		examRecordDto.setDuration(
				ExamRecordUtil.getExamDuration(examRecord.getStartTime(), examRecord.getEndTime()));
		// 正确题目数
		examRecordDto.setCorrectNumber(examRecord.getCorrectNumber());
		examRecordDto.setInCorrectNumber(examRecord.getInCorrectNumber());
		// 提交状态
		examRecordDto.setSubmitStatus(examRecord.getSubmitStatus());
		// 答题列表
		List<Answer> answers = answerMapper.findListByExamRecordId(examRecord.getId());
		// 获取该考试各题型考试分数
		List<ExamQuestionExam> examQuestionExamList = examQuestionExamMapper.getRuleById(examRecord.getExaminationId());
//		examQuestionExamList.stream().forEach(e -> {
//			answers.stream().forEach(m -> {
//				int type = m.getType();
//				if (type == 0 || type == 3) {
//					type = 1;
//				} else if (type == 1) {
//					type = 3;
//				}
//				if (type == e.getQuestionTypeId() && 0.0 != m.getScore()) {
//					m.setScore(Double.valueOf(e.getScore().toString()));
//				}
//			});
//		});
		if (CollectionUtils.isNotEmpty(answers)) {
			List<AnswerDto> answerDtos = answers.stream().map(answer -> {
				AnswerDto answerDto = new AnswerDto();
				BeanUtils.copyProperties(answer, answerDto);
				SubjectDto subjectDto = subjectService.get(answer.getSubjectId(), answer.getType());
				answerDto.setSubject(subjectDto);
				answerDto.setDuration(ExamRecordUtil.getExamDuration(answer.getStartTime(), answer.getEndTime()));
				return answerDto;
			}).collect(Collectors.toList());
			examRecordDto.setAnswers(answerDtos);
		}
		this.fillExamUserInfo(Collections.singletonList(examRecordDto), new Long[] {examRecord.getUserId()});
		return examRecordDto;
	}

	/**
	 * 给考试创建考题
	 * @param addSubjectExamDTO
	 * @return
	 */
	@Transactional(rollbackFor = Exception.class)
	public Integer addSubjectExamList(AddSubjectExamDTO addSubjectExamDTO) {
		// 先判断该用户是否已经参加过该考试, 若参加过则不允许再参加
		Integer count = examRecordMapper.findRecordIdByExaminationIdAndUserId(addSubjectExamDTO.getExaminationId(), addSubjectExamDTO.getUserId());
		if (count != null && count != 0) {
			return -1;
		}
        Long examinationId = addSubjectExamDTO.getExaminationId();
        Long userId = addSubjectExamDTO.getUserId();
		List<QuestionCategoryVO> resultList = new ArrayList<>();
		// 1.根据考试id查询本考试所涉及的考试内容,根据题目类型排序
		List<ExamQuestionCategory> examQuestionCategoryList = examQuestionCategoryMapper.findRuleByExaminationId(examinationId);
		// 2.创建三个空集合分别放置对应的题目类型
		List<ExamQuestionCategory> choiceList = new ArrayList<>();
		List<ExamQuestionCategory> judgementList = new ArrayList<>();
		List<ExamQuestionCategory> shortAnswerList = new ArrayList<>();
		// 3.将考题分类添加
		examQuestionCategoryList.stream().forEach(e -> {
			if (CHOICE.equals(e.getQuestionTypeId())) {
				// 选择题
				choiceList.add(e);
			} else if (JUDGEMENT.equals(e.getQuestionTypeId())) {
				// 判断题
				judgementList.add(e);
			} else if (SHORTANSWER.equals(e.getQuestionTypeId())) {
				// 简答题
				shortAnswerList.add(e);
			}
		});
		// 4.处理选择题
		resultList = getResult(examinationId, userId, resultList, choiceList, 1);
		resultList = getResult(examinationId, userId, resultList, judgementList, 2);
		resultList = getResult(examinationId, userId, resultList, shortAnswerList, 3);
		// 5.删除本考生以前的考题
		Integer deteleNum = examinationSubjectMapper.deleteOld(examinationId, userId);
		// 6.添加本次试题
		Integer addNum = examinationSubjectMapper.addNew(resultList);
		return addNum;
	}

	private List<QuestionCategoryVO> getResult(Long examinationId, Long userId, List<QuestionCategoryVO> resultList, List<ExamQuestionCategory> questionCategoryList, int type) {
		if (questionCategoryList.size() > 0) {
			// 根据考试难度及题库抽取试题
			questionCategoryList.stream().forEach(e -> {
				// 题库id
				Long categoryId = e.getCategoryId();
				// 获取简单难度数量并抽题
				Integer simpleNum = e.getQuestionSimpleNum();
				Integer commonlyNum = e.getQuestionCommonlyNum();
				Integer difficultyNum = e.getQuestionDifficultyNum();
				List<QuestionCategoryVO> simpleQuestionsList = new ArrayList<>();
				List<QuestionCategoryVO> commonlyQuestionsList = new ArrayList<>();
				List<QuestionCategoryVO> difficultyQuestionsList = new ArrayList<>();
				if (1 == type) {
					simpleQuestionsList = subjectChoicesMapper.findQuestions(categoryId, simpleNum, 1, examinationId, userId);
					commonlyQuestionsList = subjectChoicesMapper.findQuestions(categoryId, commonlyNum, 2, examinationId, userId);
					difficultyQuestionsList = subjectChoicesMapper.findQuestions(categoryId, difficultyNum, 3, examinationId, userId);
				} else if (2 == type) {
					simpleQuestionsList = subjectJudgementMapper.findQuestions(categoryId, simpleNum, 1, examinationId, userId);
					commonlyQuestionsList = subjectJudgementMapper.findQuestions(categoryId, commonlyNum, 2, examinationId, userId);
					difficultyQuestionsList = subjectJudgementMapper.findQuestions(categoryId, difficultyNum, 3, examinationId, userId);
				} else if (3 == type) {
					simpleQuestionsList = subjectShortAnswerMapper.findQuestions(categoryId, simpleNum, 1, examinationId, userId);
					commonlyQuestionsList = subjectShortAnswerMapper.findQuestions(categoryId, commonlyNum, 2, examinationId, userId);
					difficultyQuestionsList = subjectShortAnswerMapper.findQuestions(categoryId, difficultyNum, 3, examinationId, userId);
				}
				resultList.addAll(simpleQuestionsList);
				resultList.addAll(commonlyQuestionsList);
				resultList.addAll(difficultyQuestionsList);
			});
		}
		return resultList;
	}
}
