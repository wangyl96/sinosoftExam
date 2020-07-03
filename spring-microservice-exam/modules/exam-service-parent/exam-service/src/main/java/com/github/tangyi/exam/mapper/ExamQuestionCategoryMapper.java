package com.github.tangyi.exam.mapper;

import com.github.tangyi.common.core.persistence.CrudMapper;
import com.github.tangyi.exam.api.module.ExamQuestionCategory;
import com.github.tangyi.exam.api.vo.DifficultyLevelChartVO;
import com.github.tangyi.exam.api.vo.QestionTypesChartVO;
import com.github.tangyi.exam.api.vo.QuestionBankChartVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

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
    int delete(Long examinationId);

    /**
     * 批量插入考试规则
     * @param examRuleList
     * @return
     */
    int insertForeach(List<Map<String, Object>> examRuleList);

    /**
     * 根据考试id获取本考试试题生成规则
     * @param examinationId
     * @return
     */
    List<ExamQuestionCategory> findRuleByExaminationId(Long examinationId);

    /**
     * 获取各题库选题占比饼图
     * @param examinationId
     * @return
     */
    List<QuestionBankChartVO> questionBankChart(Long examinationId);

    /**
     * 获取考试难易程度饼图
     * @param examinationId
     * @return
     */
    DifficultyLevelChartVO difficultyLevelChart(Long examinationId);

    /**
     * 获取题型占比饼图
     * @param examinationId
     * @return
     */
    List<QestionTypesChartVO> questionTypesChart(Long examinationId);

    Integer insertForeachCategory(List<Map<String, Object>> mapList);

    /**
     * 根据id删除已插入的对应类型题目
     * @param id
     * @return
     */
    int deleteById(Long id);
}
