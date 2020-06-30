package com.github.tangyi.exam.api.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * DifficultyLevelChartVO
 *
 * @author wangyl
 * Description:
 * Created in: 2020/5/7
 * Modified by:
 */
@Data
@Accessors(chain = true)
public class DifficultyLevelChartVO {

    private Integer simpleNum;

    private Integer commonlyNum;

    private Integer difficultyNum;

}
