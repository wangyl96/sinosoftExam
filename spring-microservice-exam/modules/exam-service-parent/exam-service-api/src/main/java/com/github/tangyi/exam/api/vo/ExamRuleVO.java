package com.github.tangyi.exam.api.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.tangyi.exam.api.module.ExamQuestionExam;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author wangyl
 * @describe
 * @creat
 * @modify
 */
@Data
@Accessors(chain = true)
public class ExamRuleVO {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long examId;
    private Integer questionTypeId;
    private String categoryName;
    private Integer simpleNum;
    private Integer commonlyNum;
    private Integer difficultyNum;

    /**
     * 各题库各类型题目总数
     */
    private Integer totalsimpleNum = 0;

    private Integer totalCommonlyNum = 0;

    private Integer totalDifficultyNum = 0;
}
