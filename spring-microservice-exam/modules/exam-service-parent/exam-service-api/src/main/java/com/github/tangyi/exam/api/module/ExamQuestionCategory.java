package com.github.tangyi.exam.api.module;

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
public class ExamQuestionCategory {

    private Integer id;

    private Integer questionTypeId;

    private Integer questionTypeScore;

    private Long examinationId;

    private Long categoryId;

    private Integer questionSimpleNum;

    private Integer questionCommonlyNum;

    private Integer questionDifficultyNum;


}
