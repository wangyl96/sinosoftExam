package com.github.tangyi.exam.api.dto;

import lombok.Data;

/**
 * @description:
 * @author: jyf
 * @time: 2020/7/7 17:44
 */
@Data
public class JudgeAnswerStatusDTO {
    private String examRecordId;
    private boolean status;
}
