package com.github.tangyi.exam.api.dto;

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
public class CategoriesListDTO {

    private Long examinationId;

    private Integer questionTypeId;
}
