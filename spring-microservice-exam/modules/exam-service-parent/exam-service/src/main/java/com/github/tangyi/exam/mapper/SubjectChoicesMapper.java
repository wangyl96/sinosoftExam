package com.github.tangyi.exam.mapper;

import com.github.tangyi.common.core.persistence.CrudMapper;
import com.github.tangyi.exam.api.module.SubjectChoices;
import com.github.tangyi.exam.api.vo.QuestionCategoryVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 选择题Mapper
 *
 * @author tangyi
 * @date 2018/11/8 21:15
 */
@Mapper
public interface SubjectChoicesMapper extends CrudMapper<SubjectChoices> {

    /**
     * 物理删除
     *
     * @param subjectChoices subjectChoices
     * @return int
     * @author tangyi
     * @date 2019/06/16 22:44
     */
    int physicalDelete(SubjectChoices subjectChoices);

    /**
     * 物理批量删除
     *
     * @param ids ids
     * @return int
     * @author tangyi
     * @date 2019/06/16 22:44
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
    List<Map<String, Object>> getTotalChoicesMap();

    /**
     * 根据题库id获取题目总数
     * @param id
     * @return
     */
    List<Map<String, Object>> getTotalChoicesMapBySubjectId(Long id);


    List<SubjectChoices> findListByCondition(SubjectChoices subjectChoices);
}
