package com.github.tangyi.exam.api.vo;

import com.github.tangyi.exam.api.module.ExamQuestionExam;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author wangyl
 * @describe
 * @creat
 * @modify
 */
@Data
@Accessors(chain = true)
public class ExamRuleResultVO {

    private List<ExamRuleVO> examRuleVOList;

    private List<ExamQuestionExam> examQuestionExamsList;

}
