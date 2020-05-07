package com.github.tangyi.exam.mapper;

import com.github.tangyi.common.core.persistence.CrudMapper;
import com.github.tangyi.exam.api.module.SubjectJudgement;
import com.github.tangyi.exam.api.vo.QuestionCategoryVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

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
     * @param simpleNum
     * @param type
     * @return
     */
    List<QuestionCategoryVO> findQuestions(Long categoryId, Integer num, int type);
}
