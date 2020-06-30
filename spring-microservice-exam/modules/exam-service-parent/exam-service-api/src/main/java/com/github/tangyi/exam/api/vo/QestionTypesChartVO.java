package com.github.tangyi.exam.api.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * QestionTypesChartVO
 *
 * @author wangyl
 * Description:
 * Created in: 2020/5/7
 * Modified by:
 */
@Data
@Accessors(chain = true)
public class QestionTypesChartVO {

    /**
     * 题库名称
     */
    private String name;
    /**
     * 每题库所使用的题量
     */
    private String value;
}
