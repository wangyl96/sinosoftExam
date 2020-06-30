package com.github.tangyi.exam.mapper;

import com.github.tangyi.common.core.persistence.CrudMapper;
import com.github.tangyi.exam.api.module.ExamExaminationTime;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考试时长Mapper
 *
 * @author wangyl
 * @date 2020年5月10日18:03:30
 */
@Mapper
public interface ExamExaminationTimeMapper extends CrudMapper<ExamExaminationTime> {

    Integer insert(Integer time, Long examId);

    Integer getExamTime(Long examId);
}
