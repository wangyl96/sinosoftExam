package com.github.tangyi.exam.mapper;

import com.github.tangyi.common.core.persistence.CrudMapper;
import com.github.tangyi.exam.api.dto.CategoriesListDTO;
import com.github.tangyi.exam.api.module.SubjectCategory;
import com.github.tangyi.exam.api.vo.CategoriesListVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 题目分类Mapper
 *
 * @author tangyi
 * @date 2018/12/4 21:48
 */
@Mapper
public interface SubjectCategoryMapper extends CrudMapper<SubjectCategory> {

    List<CategoriesListVO> getSubjectCategory(CategoriesListDTO categoriesListDTO);
}
