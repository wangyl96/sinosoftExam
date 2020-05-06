package com.github.tangyi.exam.mapper;

import com.github.tangyi.common.core.persistence.CrudMapper;
import com.github.tangyi.exam.api.module.QuestionType;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuestionTypeMapper extends CrudMapper<QuestionType> {
}
