package com.github.tangyi.exam.mapper;

import com.github.tangyi.common.core.persistence.CrudMapper;
import com.github.tangyi.exam.api.module.SubjectJudgement;
import com.github.tangyi.exam.api.vo.QuestionCategoryVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 判断题Mapper
 *
 * @author tangyi
 * @date 2019-07-16 13:00
 */
@Mapper
public interface SubjectJudgementMapper extends CrudMapper<SubjectJudgement> {

    /**
     * 物理删除
     *
     * @param subjectJudgement subjectJudgement
     * @return int
     * @author tangyi
     * @date 2019/06/16 22:54
     */
    int physicalDelete(SubjectJudgement subjectJudgement);

    /**
     * 物理批量删除
     *
     * @param ids ids
     * @return int
     * @author tangyi
     * @date 2019/06/16 22:54
     */
    int physicalDeleteAll(Long[] ids);

    /**
     * 获取题目id 及 题目类型 0：选择题，1：简答题，2：判断题，3：多选题
     * @param categoryId
     * @param num
     * @param type
     * @return
     */
    List<QuestionCategoryVO> findQuestions(Long categoryId, Integer num, int type, Long examinationId, Long userId);

    /**
     * 获取选择题答案
     * @param subjectId
     * @return
     */
    String findAnswerById(Long subjectId);

    /**
     * 获取题目总数
     * @return
     */
    List<Map<String, Object>> getTotalJudgementMap();

    /**
     * 根据题库id获取题目总数
     * @param id
     * @return
     */
    List<Map<String, Object>> getTotalJudgementMapBySubjectId(Long id);
}
