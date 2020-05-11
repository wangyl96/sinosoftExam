package com.github.tangyi.exam.api.module;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long examId;

    private Integer questionTypeId;

    private Integer score;

    private Integer count;


}
