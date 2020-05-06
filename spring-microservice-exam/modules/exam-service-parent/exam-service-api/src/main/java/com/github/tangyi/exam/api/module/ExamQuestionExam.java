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
public class ExamQuestionExam {

    private Integer id;

    private Long examId;

    private Integer questionTypeId;

    private Integer score;

    private Integer count;


}
