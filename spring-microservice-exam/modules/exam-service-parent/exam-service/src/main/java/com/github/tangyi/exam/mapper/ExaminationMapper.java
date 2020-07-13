package com.github.tangyi.exam.mapper;

import com.github.tangyi.common.core.persistence.CrudMapper;
import com.github.tangyi.exam.api.module.Examination;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 考试Mapper
 *
 * @author tangyi
 * @date 2018/11/8 21:11
 */
@Mapper
public interface ExaminationMapper extends CrudMapper<Examination> {

    /**
     * 查询考试数量
     *
     * @param examination examination
     * @return int
     * @author tangyi
     * @date 2019/3/1 15:32
     */
    int findExaminationCount(Examination examination);

    /**
     * 查询参与考试人数
     *
     * @param examination examination
     * @return int
     * @author tangyi
     * @date 2019/10/27 20:08:58
     */
    int findExamUserCount(Examination examination);

    /**
     * 获取考试总分
     * @param examinationId
     * @return
     */
    Integer getScore(Long examinationId);

    /**
     * 考试页面查询分页数据
     * @param examination
     * @return
     */
    List<Examination> findListExam(Examination examination);
    /**
     * 根据题型名称查询题型id
     * @return
     */
    Integer  selectQuestionId(String typeName);
    /**
     * 当有人开始考试时修改考试记录的状态
     * @param examinationId
     */
    void updateStatusById(Integer status,Long examinationId);
}
