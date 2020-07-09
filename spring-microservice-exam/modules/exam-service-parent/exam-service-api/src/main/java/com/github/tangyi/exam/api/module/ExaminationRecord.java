package com.github.tangyi.exam.api.module;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.tangyi.common.core.persistence.BaseEntity;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.List;

/**
 * 考试记录
 *
 * @author tangyi
 * @date 2018/11/8 21:05
 */
@Data
public class ExaminationRecord extends BaseEntity<ExaminationRecord> {

    /**
     * 考生ID
     */
    @NotBlank(message = "用户ID不能为空")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long userId;

    /**
     * 考试ID
     */
    @NotBlank(message = "考试ID不能为空")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long examinationId;

    /**
     * 考试名称
     */
    private String examinationName;

    private String creator;
    /**
     * 姓名或用户名
     */
    private String name;

    /**
     * 选择id集合
     */
    private Long[] ids;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 成绩
     */
    private Double score;

    /**
     * 错误题目数量
     */
    private Integer inCorrectNumber;

    /**
     * 正确题目数量
     */
    private Integer correctNumber;

    /**
     * 提交状态 1-已提交 0-未提交
     */
    @NotBlank(message = "状态不能为空")
    private Integer submitStatus;
}
