package com.github.tangyi.exam.api.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * @author wangyl
 * @describe
 * @creat
 * @modify
 */
@Data
@Accessors(chain = true)
public class QuestionBankChartVO {

    /**
     * 题库名称
     */
    private String name;
    /**
     * 每题库所使用的题量
     */
    private String value;
}
