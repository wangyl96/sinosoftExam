package com.github.tangyi.exam.mapper;

import com.github.tangyi.common.core.persistence.CrudMapper;
import com.github.tangyi.exam.api.module.ExamQuestionExam;
import com.github.tangyi.exam.api.vo.ExamRuleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ExamQuestionExamMapper extends CrudMapper<ExamQuestionExam> {

    /**
     * 获取考试的题型
     * @param examinationId
     * @return
     */
    List<ExamRuleVO> getExamQuestionExamById(Long examinationId);

    /**
     * 数量及题目汇总
     */
    List<ExamQuestionExam> getRuleById(Long examinationId);

    /**
     * 删除题型个数及分数
     * @param examinationId
     * @return
     */
    int delete(Long examinationId);

    /**
     * 创建题型个数及分数
     * @param examQuestionExamsList
     * @return
     */
    int insertForeachExamQuestionExam(List<Map<String, Object>> examQuestionExamsList);

    /**
     * 插入考试与题型
     * @param examQuestionExamList
     * @return
     */
    int insertForeach(List<ExamQuestionExam> examQuestionExamList);

    /**
     *根据考试id删除已插入的考试类型
     * @return
     */
    int deleteById(Long id);
    /**
     * 获取指定考试考题的分数
     * @param type
     * @param examinationId
     * @return
     */
    Integer getScoreByExamIdAndTypeId(Integer type, Long examinationId);

    /**
     * 根据答题id和考试id获取该题实际分数
     * @param examinationId
     * @return
     */
    Integer getAnswerScoreByExamId(Long recordId,Long subjectId);

    /**
     * 获取临时分数
     * @param recordId
     * @param subjectId
     * @return
     */
    Integer getAnswerTemporaryScore(Long recordId,Long subjectId);
    /**
     * 获取题型分数
     * @param examRecordId
     * @return
     */
    List<ExamQuestionExam> getListByRecordId(Long examRecordId);

    Integer examQuestionFlag(Long id);
}
