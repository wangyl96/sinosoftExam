package com.github.tangyi.exam.mapper;

import com.github.tangyi.common.core.persistence.CrudMapper;
import com.github.tangyi.exam.api.module.ExamQuestionCategory;
import com.github.tangyi.exam.api.vo.QuestionBankChartVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author wangyl
 * @describe
 * @creat
 * @modify
 */
@Mapper
public interface ExamQuestionCategoryMapper extends CrudMapper<ExamQuestionCategory> {

    /**
     * 删除考试规则
     * @param examinationId
     * @return
     */
    int delete(Long examinationId, String types);

    /**
     * 批量插入考试规则
     * @param examQuestionCategoryList
     * @return
     */
    int insertForeach(List<ExamQuestionCategory> examQuestionCategoryList);

    /**
     * 获取各题库选题占比饼图
     * @param examinationId
     * @return
     */
    List<QuestionBankChartVO> questionBankChart(Long examinationId);
}
