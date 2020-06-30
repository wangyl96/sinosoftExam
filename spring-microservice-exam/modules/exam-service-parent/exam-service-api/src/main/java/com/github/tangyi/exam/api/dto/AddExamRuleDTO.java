package com.github.tangyi.exam.api.dto;

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
public class AddExamRuleDTO {

    private Long examinationId;

    private Map<String, Object> examRule;

}
