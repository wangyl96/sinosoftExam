package com.github.tangyi.exam.api.vo;

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
public class QuestionCategoryVO {

    private Long examinationId;
    private Long subjectId;
    private Long categoryId;
    private String applicationCode;
    private String tenantCode;
    private Long userId;
    /**
    * 题目类型，0：选择题，1：简答题，2：判断题，3：多选题
    */
    private Integer type;

}
