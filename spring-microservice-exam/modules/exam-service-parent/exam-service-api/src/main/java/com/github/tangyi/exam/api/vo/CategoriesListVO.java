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
public class CategoriesListVO {

    private String id;

    /**
     * 题库名称
     */
    private String categoryName;

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
