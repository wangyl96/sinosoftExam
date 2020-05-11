package com.github.tangyi.exam.api.dto;

import com.github.tangyi.exam.api.module.Course;
import com.github.tangyi.exam.api.module.Examination;
import com.github.tangyi.exam.api.module.QuestionType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author tangyi
 * @date 2018/11/20 22:02
 */
@Data
@NoArgsConstructor
public class ExaminationDto extends Examination {

    private Course course;

    /**
     * 封面地址
     */
    private String logoUrl;

    private Integer totalTime;

}
