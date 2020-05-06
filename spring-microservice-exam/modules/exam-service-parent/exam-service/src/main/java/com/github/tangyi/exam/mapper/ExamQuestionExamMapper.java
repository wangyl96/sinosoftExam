package com.github.tangyi.exam.mapper;

import com.github.tangyi.common.core.persistence.CrudMapper;
import com.github.tangyi.exam.api.module.ExamQuestionExam;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ExamQuestionExamMapper extends CrudMapper<ExamQuestionExam> {

    /**
     * 获取考试的题型
     * @param examinationId
     * @return
     */
    List<ExamQuestionExam> getExamQuestionExamById(Long examinationId);

    /**
     * 删除题型个数及分数
     * @param examinationId
     * @return
     */
    int delete(Long examinationId, String types);

    /**
     * 创建题型个数及分数
     * @param examQuestionExamList
     * @return
     */
    int insertForeachExamQuestionExam(List<ExamQuestionExam> examQuestionExamList);
}
