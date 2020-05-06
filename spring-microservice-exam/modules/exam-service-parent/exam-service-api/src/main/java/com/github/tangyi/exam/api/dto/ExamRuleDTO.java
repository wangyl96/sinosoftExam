package com.github.tangyi.exam.api.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author wangyl
 * @describe
 * @creat
 * @modify
 */
@Data
public class ExamRuleDTO {

    /**
     * 题库id
     */
    @NotNull(message = "题库类型不能为空")
    private Long id;

    /**
     * 题型id
     */
    @NotNull(message = "题型不能为空")
    private Integer questionTypeId;

    /**
     * 题型分数
     */
    private Integer score;

    /**
     * 简单难度题数量
     */
    private Integer simpleNum;

    /**
     * 一般难度题数量
     */
    private Integer commonlyNum;

    /**
     * 困难难度题数量
     */
    private Integer difficultyNum;
}
