package com.github.tangyi.exam.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.github.pagehelper.PageInfo;
import com.github.tangyi.common.basic.properties.SysProperties;
import com.github.tangyi.common.core.constant.CommonConstant;
import com.github.tangyi.common.core.service.CrudService;
import com.github.tangyi.common.core.utils.PageUtil;
import com.github.tangyi.common.core.utils.zxing.QRCodeUtils;
import com.github.tangyi.common.security.utils.SysUtil;
import com.github.tangyi.exam.api.dto.AddExamRuleDTO;
import com.github.tangyi.exam.api.dto.ExaminationDto;
import com.github.tangyi.exam.api.dto.SubjectDto;
import com.github.tangyi.exam.api.module.*;
import com.github.tangyi.exam.api.vo.ExamRuleResultVO;
import com.github.tangyi.exam.api.vo.ExamRuleVO;
import com.github.tangyi.exam.mapper.*;
import com.github.tangyi.user.api.constant.AttachmentConstant;
import com.github.tangyi.user.api.module.Attachment;
import io.swagger.models.auth.In;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 考试service
 *
 * @author tangyi
 * @date 2018/11/8 21:19
 */
@Slf4j
@AllArgsConstructor
@Service
public class ExaminationService extends CrudService<ExaminationMapper, Examination> {

    private final SubjectService subjectService;

    private final ExaminationSubjectService examinationSubjectService;

	private final CourseService courseService;

	private final SysProperties sysProperties;

	@Resource
	private ExamQuestionExamMapper examQuestionExamMapper;

	@Resource
	private ExamQuestionCategoryMapper examQuestionCategoryMapper;

	@Resource
	private ExamExaminationTimeMapper examExaminationTimeMapper;

	@Resource
	private ExaminationMapper examinationMapper;

	@Resource
	private SubjectCategoryMapper subjectCategoryMapper;

	@Resource
	private ExamRecordMapper examRecordMapper;

	@Resource
	private SubjectChoicesMapper subjectChoicesMapper;

	@Resource
	private SubjectJudgementMapper subjectJudgementMapper;

	@Resource
	private SubjectShortAnswerMapper subjectShortAnswerMapper;

    /**
     * 查询考试
     *
     * @param examination examination
     * @return Examination
     * @author tangyi
     * @date 2019/1/3 14:06
     */
    @Override
    @Cacheable(value = "examination#" + CommonConstant.CACHE_EXPIRE, key = "#examination.id")
    public Examination get(Examination examination) {
        return super.get(examination);
    }

	/**
	 * 新增考试
	 *
	 * @param examinationDto examinationDto
	 * @return int
	 * @author tangyi
	 * @date 2019/1/3 14:06
	 */
	@Transactional(rollbackFor = Exception.class)
	public int insert(ExaminationDto examinationDto) {
		String questionType = examinationDto.getQuestionStyle();
		JSONArray jsonArray = JSONUtil.parseArray(questionType);
		StringBuffer str = new StringBuffer("");
		jsonArray.stream().forEach(e -> {
			str.append(JSONUtil.parseObj(e).getStr("questionTypeName"));
			str.append(",");
		});
		questionType = str.toString();
		questionType = questionType.substring(0, questionType.length() -1);
		examinationDto.setQuestionStyle(questionType);
		this.initExaminationLogo(examinationDto);
		Examination examination = new Examination();
		BeanUtils.copyProperties(examinationDto, examination);
		examination.setCourseId(examinationDto.getCourse().getId());
		examination.setCommonValue(SysUtil.getUser(), SysUtil.getSysCode(), SysUtil.getTenantCode());
		int num = super.insert(examination);
		Long id = examination.getId();
		// 将考试时长入库
		Integer insertTimeNum = examExaminationTimeMapper.insert(examinationDto.getTotalTime(), id);
		log.info("创建考试id:{}, 考试时长:{}分钟, 考试时长数量", id, examinationDto.getTotalTime(), insertTimeNum);
		// 将考试与考题关联
		List<ExamQuestionExam> examQuestionExamList = new ArrayList<>();
		jsonArray.stream().forEach(e -> {
			ExamQuestionExam examQuestionExam = new ExamQuestionExam()
								.setQuestionTypeId(JSONUtil.parseObj(e).getInt("id"))
								.setExamId(id)
								.setCount(0)
								.setScore(0);
			examQuestionExamList.add(examQuestionExam);
		});
		// 将examQuestionExamList入库
		int add = examQuestionExamMapper.insertForeach(examQuestionExamList);

		// 查出所有的题库
		List<SubjectCategory> subjectCategoryList = subjectCategoryMapper.getList();
		// exam_question_category入库
		List<Map<String, Object>> mapList = new ArrayList<>();
		examQuestionExamList.stream().forEach(e -> {
			subjectCategoryList.stream().forEach(m -> {
				Map<String, Object> categoryMap = new HashMap<>();
				categoryMap.put("questionTypeId", e.getQuestionTypeId());
				categoryMap.put("examId", id);
				categoryMap.put("categoryId", m.getId());

				mapList.add(categoryMap);
			});
		});
		// 入库
		Integer addNum = examQuestionCategoryMapper.insertForeachCategory(mapList);
		log.info("考试id:{},题库选题新增{}条", id, addNum);
		log.info("该考试有{}种题型", add);
		return num;
	}

	/**
	 * 新增考试选题规则
	 * @param addExamRuleDTO
	 * @return
	 */
	@Transactional(rollbackFor = Exception.class)
	public Integer addExamRule(AddExamRuleDTO addExamRuleDTO) {
		// 先判断总分与总分是否一致
		int totalSorce = 0;
		Long examId = 0L;
		Map<String, Object> ruleMap= addExamRuleDTO.getExamRule();
		List<Map<String, Object>> examQuestionExamsList = (List<Map<String, Object>>)ruleMap.get("examQuestionExamsList");
		List<Map<String, Object>> examRuleList = (List<Map<String, Object>>)ruleMap.get("examRuleVOList");
		for (Map<String, Object> e : examQuestionExamsList) {
			int sorce = Integer.valueOf(e.get("score").toString());
			int size = Integer.valueOf(e.get("count").toString());
			totalSorce = totalSorce + size * sorce;
		}
		// 根据考试ID查询设置的考试总分
		Integer baseSorce = examinationMapper.getScore(addExamRuleDTO.getExaminationId());
		if (totalSorce != baseSorce) {
			return -1;
		}
		// 选取的总个数及题型分数规则存库
		int deleteNum1 = examQuestionExamMapper.delete(addExamRuleDTO.getExaminationId());
		int creatNum1 = examQuestionExamMapper.insertForeachExamQuestionExam(examQuestionExamsList);
		// 将题库选题规则存库
		int deleteNum = examQuestionCategoryMapper.delete(addExamRuleDTO.getExaminationId());
		int creatNum = examQuestionCategoryMapper.insertForeach(examRuleList);
//		String questionTypeId = "";
//		Integer score = 0;
//		List<ExamQuestionCategory> examQuestionCategoryList = new ArrayList<>();
//		List<ExamQuestionExam> examQuestionExamList = new ArrayList<>();
//		List<Integer> types = new ArrayList<>();
//		for (Map<String, Object> map : examRuleList) {
//			ExamQuestionExam examQuestionExam = new ExamQuestionExam();
//			types.add(Integer.valueOf(map.get("questionTypeId").toString()));
//			questionTypeId = map.get("questionTypeId").toString();
//			score = Integer.valueOf(map.get("score").toString());
//			List<Map<String, Object>> ruleList = (List<Map<String, Object>>)map.get("categoryList");
//			// JSON转实体
//			examQuestionCategoryList  = jsonToModule(examQuestionCategoryList, ruleList, score);
//			// 将每种难度个数累加
//			int num = 0;
//			//e.getQuestionCommonlyNum() + e.getQuestionSimpleNum() + e.getQuestionDifficultyNum()
//			for (ExamQuestionCategory examQuestionCategory : examQuestionCategoryList) {
//				if (StringUtils.equals(questionTypeId, examQuestionCategory.getQuestionTypeId().toString())) {
//					 num += examQuestionCategory.getQuestionDifficultyNum() + examQuestionCategory.getQuestionCommonlyNum() + examQuestionCategory.getQuestionSimpleNum();
//				}
//			}
//			examQuestionExam.setCount(num);
//			examQuestionExam.setScore(score);
//			examQuestionExam.setExamId(examQuestionCategoryList.get(0).getExaminationId());
//			examQuestionExam.setQuestionTypeId(Integer.valueOf(questionTypeId));
//			examQuestionExamList.add(examQuestionExam);
//		}
//		// 将提醒分布存库(先删后增)
//		// 获取修改的考试类型
//		int creatNum = 0;
//		if (types.size() >= 1) {
//			int deleteNum1 = examQuestionExamMapper.delete(examQuestionExamList.get(0).getExamId(), types);
//			int creatNum1 = examQuestionExamMapper.insertForeachExamQuestionExam(examQuestionExamList);
//			// 将考试规则存库(先将该该考试的规则删除, 在新增)
//			int deleteNum = examQuestionCategoryMapper.delete(examQuestionCategoryList.get(0).getExaminationId(), types);
//			creatNum = examQuestionCategoryMapper.insertForeach(examQuestionCategoryList);
//		}
		return 0;
	}

	private List<ExamQuestionCategory> jsonToModule(List<ExamQuestionCategory> examQuestionCategoryList, List<Map<String, Object>> ruleList, Integer score) {
		for (Map<String, Object> e : ruleList) {
			JSONObject js = JSONUtil.parseObj(e);
			Integer simpleNum = js.getInt("simpleNum");
			Integer commonlyNum = js.getInt("commonlyNum");
			Integer difficultyNum = js.getInt("difficultyNum");
			simpleNum = null == simpleNum ? 0 : simpleNum;
			commonlyNum = null == commonlyNum ? 0 : commonlyNum;
			difficultyNum = null == difficultyNum ? 0 : difficultyNum;
			if (simpleNum == 0 && commonlyNum == 0 && difficultyNum == 0) {
				continue;
			}
			Integer questionTypeId = js.getInt("questionTypeId");
			Long categoryId = js.getLong("id");
			Long examId = js.getLong("examId");
			ExamQuestionCategory examQuestionCategory = new ExamQuestionCategory()
					.setCategoryId(categoryId)
					.setQuestionCommonlyNum(commonlyNum)
					.setQuestionDifficultyNum(difficultyNum)
					.setQuestionSimpleNum(simpleNum)
					.setQuestionTypeId(questionTypeId)
					.setQuestionTypeScore(score)
					.setExaminationId(examId);
			examQuestionCategoryList.add(examQuestionCategory);
		}

		return examQuestionCategoryList;
	}

	/**
	 * 获取分页数据
	 *
	 * @param pageNum     pageNum
	 * @param pageSize    pageSize
	 * @param sort        sort
	 * @param order       order
	 * @param examination examination
	 * @return PageInfo
	 * @author tangyi
	 * @date 2018/11/10 21:10
	 */
	public PageInfo<ExaminationDto> examinationList(String pageNum, String pageSize, String sort, String order, Examination examination) {
		examination.setTenantCode(SysUtil.getTenantCode());
		PageInfo<Examination> page = findPage(PageUtil.pageInfo(pageNum, pageSize, sort, order), examination);
		PageInfo<ExaminationDto> examinationDtoPageInfo = new PageInfo<>();
		BeanUtils.copyProperties(page, examinationDtoPageInfo);
		if (CollectionUtils.isNotEmpty(page.getList())) {
			List<Course> courses = courseService.findListById(page.getList().stream().map(Examination::getCourseId).distinct().toArray(Long[]::new));
			List<ExaminationDto> examinationDtos = page.getList().stream().map(exam -> {
				ExaminationDto examinationDto = new ExaminationDto();
				Integer time = examExaminationTimeMapper.getExamTime(exam.getId());
				exam.setTotalTime(time);
				BeanUtils.copyProperties(exam, examinationDto);
				// 设置考试时长
				// 设置考试所属课程
				courses.stream().filter(tempCourse -> tempCourse.getId().equals(exam.getCourseId())).findFirst().ifPresent(examinationDto::setCourse);
				// 初始化封面图片
				this.initExaminationLogo(examinationDto);
				return examinationDto;
			}).collect(Collectors.toList());
			examinationDtoPageInfo.setList(examinationDtos);
		}
		return examinationDtoPageInfo;
	}

    /**
     * 更新考试
     *
     * @param examinationDto examinationDto
     * @return int
     * @author tangyi
     * @date 2019/1/3 14:07
     */
    @Transactional
    @CacheEvict(value = "examinationDto", key = "#examinationDto.id")
    public int update(ExaminationDto examinationDto) {
    	if (examinationDto.getAvatarId() == null || examinationDto.getAvatarId() == 0L) {
    		this.initExaminationLogo(examinationDto);
		}
		Examination examination = new Examination();
		BeanUtils.copyProperties(examinationDto, examination);
		if (examinationDto.getCourse() != null)
			examination.setCourseId(examinationDto.getCourse().getId());
		examination.setCommonValue(SysUtil.getUser(), SysUtil.getSysCode(), SysUtil.getTenantCode());
        return super.update(examination);
    }

    /**
     * 删除考试
     *
     * @param examination examination
     * @return int
     * @author tangyi
     * @date 2019/1/3 14:07
     */
    @Override
    @Transactional
    @CacheEvict(value = "examination", key = "#examination.id")
    public int delete(Examination examination) {
		this.deleteExaminationSubject(new Long[]{examination.getId()});
        return super.delete(examination);
    }

    /**
     * 批量删除
     *
     * @param ids ids
     * @return int
     * @author tangyi
     * @date 2018/12/4 9:51
     */
    @Override
    @Transactional
    @CacheEvict(value = "examination", allEntries = true)
    public int deleteAll(Long[] ids) {
		this.deleteExaminationSubject(ids);
        return super.deleteAll(ids);
    }

	/**
	 * 删除题目、考试题目关联信息
	 *
	 * @param ids ids
	 * @return int
	 * @author tangyi
	 * @date 2018/12/4 9:51
	 */
	@Transactional
    public void deleteExaminationSubject(Long[] ids) {
		for (Long id : ids) {
			List<ExaminationSubject> examinationSubjects = examinationSubjectService
					.findListByExaminationId(id);
			if (CollectionUtils.isNotEmpty(examinationSubjects)) {
				// 删除考试题目
				SubjectDto subjectDto = new SubjectDto();
				ExaminationSubject es = new ExaminationSubject();
				examinationSubjects.forEach(examinationSubject -> {
					subjectDto.setId(examinationSubject.getSubjectId());
					subjectDto.setType(examinationSubject.getType());
					subjectService.physicalDelete(subjectDto);
					// 删除关联表
					es.setSubjectId(examinationSubject.getSubjectId());
					examinationSubjectService.deleteBySubjectId(es);
				});
			}
		}
	}

    /**
     * 查询考试数量
     *
     * @param examination examination
     * @return int
     * @author tangyi
     * @date 2019/3/1 15:32
     */
    public int findExaminationCount(Examination examination) {
        return this.dao.findExaminationCount(examination);
    }

    /**
     * 根据考试ID获取题目分页数据
     *
     * @param subjectDto subjectDto
     * @param pageNum    pageNum
     * @param pageSize   pageSize
     * @param sort       sort
     * @param order      order
     * @return PageInfo
     * @author tangyi
     * @date 2019/06/16 16:00
     */
    public ExamRuleResultVO findSubjectPageById(SubjectDto subjectDto, String pageNum, String pageSize, String sort, String order) {
		// 获取考试id
		Long examinationId = subjectDto.getExaminationId();
		// 根据考试id查询题目
		List<ExamRuleVO> temp = examQuestionExamMapper.getExamQuestionExamById(examinationId);
		List<ExamRuleVO> examRuleList = new ArrayList<>();
		// 将questionType为null的进行id填充
		temp.stream().forEach(e -> {
			if (null == e.getQuestionTypeId()) {
				List<Map<String, Object>> maptChoices = subjectChoicesMapper.getTotalChoicesMapBySubjectId(e.getId());
				List<Map<String, Object>>  mapJudgement = subjectJudgementMapper.getTotalJudgementMapBySubjectId(e.getId());
				List<Map<String, Object>>  mapShortAnswer = subjectShortAnswerMapper.getTotalShortAnswerMapBySubjectId(e.getId());
				if  (maptChoices != null && maptChoices.size() > 0) {
					ExamRuleVO examRuleVO = new ExamRuleVO().setId(e.getId()).setExamId(e.getExamId()).setQuestionTypeId(1).setCategoryName(e.getCategoryName());
					examRuleList.add(examRuleVO);
				}

				if  (mapJudgement != null && !mapJudgement.isEmpty()) {
					ExamRuleVO examRuleVO = new ExamRuleVO().setId(e.getId()).setExamId(e.getExamId()).setQuestionTypeId(2).setCategoryName(e.getCategoryName());
					examRuleList.add(examRuleVO);
				}

				if  (mapShortAnswer != null && !mapShortAnswer.isEmpty()) {
					ExamRuleVO examRuleVO = new ExamRuleVO().setId(e.getId()).setExamId(e.getExamId()).setQuestionTypeId(3).setCategoryName(e.getCategoryName());
					examRuleList.add(examRuleVO);
				}
			} else {
				examRuleList.add(e);
			}
		});



		// 查汇总(题型分数及总量)
		List<ExamQuestionExam> ExamQuestionExamList = examQuestionExamMapper.getRuleById(examinationId);
		// 查询题目总数
		List<Map<String, Object>> totalChoicesMap = subjectChoicesMapper.getTotalChoicesMap();
		List<Map<String, Object>> totalJudgementMap = subjectJudgementMapper.getTotalJudgementMap();
		List<Map<String, Object>> totalShortAnswerMap = subjectShortAnswerMapper.getTotalShortAnswerMap();
		// 将各题库题题型提难度的题目数最大值放入examRuleList集合
		examRuleList.stream().forEach(e -> {
			if (!totalChoicesMap.isEmpty()) {
				totalChoicesMap.stream().forEach(r -> {
					if (e.getQuestionTypeId() == 1 && e.getId().equals(Long.valueOf(r.get("categoryId").toString()))) {
							if (StringUtils.equals("1", r.get("level").toString())) {
								e.setTotalsimpleNum(Integer.valueOf(r.get("num").toString()));
							} else if (StringUtils.equals("2", r.get("level").toString())) {
								e.setTotalCommonlyNum(Integer.valueOf(r.get("num").toString()));
							} else if (StringUtils.equals("3", r.get("level").toString())) {
								e.setTotalDifficultyNum(Integer.valueOf(r.get("num").toString()));
							}
						}
				});
			}

			if (!totalJudgementMap.isEmpty()) {
				totalJudgementMap.stream().forEach(r -> {
					if (null == e.getQuestionTypeId()) {
						if (e.getQuestionTypeId() == 2 && e.getId().equals(Long.valueOf(r.get("categoryId").toString()))) {
							if (StringUtils.equals("1", r.get("level").toString())) {
								e.setTotalsimpleNum(Integer.valueOf(r.get("num").toString()));
							} else if (StringUtils.equals("2", r.get("level").toString())) {
								e.setTotalCommonlyNum(Integer.valueOf(r.get("num").toString()));
							} else if (StringUtils.equals("3", r.get("level").toString())) {
								e.setTotalDifficultyNum(Integer.valueOf(r.get("num").toString()));
							}
						}
					}
				});
			}

			if (!totalShortAnswerMap.isEmpty()) {
				totalShortAnswerMap.stream().forEach(r -> {
					if (e.getQuestionTypeId() == 3 && e.getId().equals(Long.valueOf(r.get("categoryId").toString()))) {
						if (StringUtils.equals("1", r.get("level").toString())) {
							e.setTotalsimpleNum(Integer.valueOf(r.get("num").toString()));
						} else if (StringUtils.equals("2", r.get("level").toString())) {
							e.setTotalCommonlyNum(Integer.valueOf(r.get("num").toString()));
						} else if (StringUtils.equals("3", r.get("level").toString())) {
							e.setTotalDifficultyNum(Integer.valueOf(r.get("num").toString()));
						}
					}
				});
			}

		});
		// 将结果返回
		PageInfo<ExamRuleResultVO> subjectDtoPageInfo = new PageInfo<>();
		ExamRuleResultVO examRuleResultVO = new ExamRuleResultVO().setExamRuleVOList(examRuleList).setExamQuestionExamsList(ExamQuestionExamList);
		return examRuleResultVO;


    }

	/**
	 * 获取全部题目
	 * @param subjectDto subjectDto
	 * @return List
	 * @author tangyi
	 * @date 2020/3/12 1:00 下午
	 */
    public List<SubjectDto> allSubjectList(SubjectDto subjectDto) {
		// 查询考试题目关联表
		List<ExaminationSubject> examinationSubjects = examinationSubjectService.findListByExaminationId(subjectDto.getExaminationId());
		if (CollectionUtils.isNotEmpty(examinationSubjects)) {
			return subjectService.findSubjectDtoList(examinationSubjects, true, false);
		}
		return Collections.emptyList();
	}

    /**
     * 根据考试ID查询题目id列表
     *
     * @param examinationId examinationId
     * @return ExaminationSubject
     * @author tangyi
     * @date 2019/06/18 14:34
     */
    public List<ExaminationSubject> findListByExaminationIdAndUserId(Long examinationId, Long userId) {
		List<ExaminationSubject> examinationSubjectList = examinationSubjectService.findListByExaminationIdAndUserId(examinationId, userId);
		List<ExaminationSubject> resultList = new ArrayList<>();
		List<ExaminationSubject> choiceList = new ArrayList<>();
		List<ExaminationSubject> multipleChoiceList = new ArrayList<>();
		List<ExaminationSubject> judgeList = new ArrayList<>();
		List<ExaminationSubject> shortAnswerList = new ArrayList<>();
		examinationSubjectList.stream().forEach(e -> {
			if (0 == e.getType()) {
				// 单选
				choiceList.add(e);
			} else if (1 == e.getType()) {
				// 简答
				shortAnswerList.add(e);
			} else if (2 == e.getType()) {
				// 判断
				judgeList.add(e);
			} else if (3 == e.getType()) {
				// 多选
				multipleChoiceList.add(e);
			}
		});
		resultList.addAll(choiceList);
		resultList.addAll(multipleChoiceList);
		resultList.addAll(judgeList);
		resultList.addAll(shortAnswerList);
        return resultList;
    }

	public List<ExaminationSubject> findListByExaminationId(Long examinationId) {
		return examinationSubjectService.findListByExaminationId(examinationId);
	}

	/**
	 * 查询参与考试人数
	 *
	 * @param examination examination
	 * @return int
	 * @author tangyi
	 * @date 2019/10/27 20:08:58
	 */
	public int findExamUserCount(Examination examination) {
		return this.dao.findExamUserCount(examination);
	}

	/**
	 * 获取考试封面
	 *
	 * @param examinationDto examinationDto
	 * @author tangyi
	 * @date 2020/03/12 22:32:30
	 */
	public void initExaminationLogo(ExaminationDto examinationDto) {
		try {
			if (sysProperties.getLogoUrl() != null && !sysProperties.getLogoUrl().endsWith("/")) {
				sysProperties.setLogoUrl(sysProperties.getLogoUrl() + "/");
			}
			// 获取配置默认头像地址
			if (examinationDto.getAvatarId() != null && examinationDto.getAvatarId() != 0L) {
				Attachment attachment = new Attachment();
				attachment.setId(examinationDto.getAvatarId());
				examinationDto.setLogoUrl(AttachmentConstant.ATTACHMENT_PREVIEW_URL + examinationDto.getAvatarId());
			} else {
				Long index = new Random().nextInt(sysProperties.getLogoCount()) + 1L;
				examinationDto.setLogoUrl(sysProperties.getLogoUrl() + index + sysProperties.getLogoSuffix());
				examinationDto.setAvatarId(index);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * 根据考试ID生成二维码
	 * @param examinationId examinationId
	 * @author tangyi
	 * @date 2020/3/15 1:16 下午
	 */
	public byte[] produceCode(Long examinationId) {
		Examination examination = this.get(examinationId);
		// 调查问卷
		if (examination == null/* || !ExaminationTypeEnum.QUESTIONNAIRE.getValue().equals(examination.getType())*/) {
			return new byte[0];
		}
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		String url = sysProperties.getQrCodeUrl() + "?id=" + examination.getId();
		QRCodeUtils.encoderQRCode(url, outputStream, "png");
		log.info("Share examinationId: {}, url: {}", examinationId, url);
		return outputStream.toByteArray();
	}

	/**
	 * 根据考试ID生成二维码
	 * @param examinationId examinationId
	 * @author tangyi
	 * @date 2020/3/21 5:38 下午
	 */
	public byte[] produceCodeV2(Long examinationId) {
		Examination examination = this.get(examinationId);
		// 调查问卷
		if (examination == null/* || !ExaminationTypeEnum.QUESTIONNAIRE.getValue().equals(examination.getType())*/) {
			return new byte[0];
		}
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		String url = sysProperties.getQrCodeUrl() + "-v2?id=" + examination.getId();
		QRCodeUtils.encoderQRCode(url, outputStream, "png");
		log.info("Share v2 examinationId: {}, url: {}", examinationId, url);
		return outputStream.toByteArray();
	}

	/**
	 * 获取考试时长
	 * @param examId
	 * @return
	 */
	public Integer getExamTime(Long examId) {
		Integer time = examExaminationTimeMapper.getExamTime(examId);
		return time;
	}
}
