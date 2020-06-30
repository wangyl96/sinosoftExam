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
public class AddSubjectExamDTO {
    private Long userId;

    private Long examinationId;
}
