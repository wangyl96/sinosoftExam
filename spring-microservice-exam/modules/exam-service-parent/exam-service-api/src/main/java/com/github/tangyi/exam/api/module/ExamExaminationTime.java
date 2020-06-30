package com.github.tangyi.exam.api.module;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author wangyl
 * @describe
 * @creat
 * @modify
 */
@Data
@Accessors(chain = true)
public class ExamExaminationTime {

    private Integer id;

    /**
     * 考试id
     */
    private Long examId;

    /**
     * 时长(分钟)
     */
    private Integer time;
}
