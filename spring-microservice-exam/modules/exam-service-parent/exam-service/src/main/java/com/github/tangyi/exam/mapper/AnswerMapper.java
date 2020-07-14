package com.github.tangyi.exam.mapper;

import com.github.tangyi.common.core.persistence.CrudMapper;
import com.github.tangyi.exam.api.module.Answer;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 答题Mapper
 *
 * @author tangyi
 * @date 2018/11/8 21:09
 */
@Mapper
public interface AnswerMapper extends CrudMapper<Answer> {

    /**
     * 根据用户ID、考试ID、考试记录ID、题目ID查找答题
     *
     * @param answer answer
     * @return Answer
     * @author tangyi
     * @date 2019/01/21 19:38
     */
    Answer getAnswer(Answer answer);

    /**
     * 根据examRecordId查询
     * @param examRecordId examRecordId
     * @return List
     * @author tangyi
     * @date 2020/2/21 1:08 下午
     */
    List<Answer> findListByExamRecordId(Long examRecordId);

    /**
     *
     * @param examRecordId
     * @param subjectId
     * @param userId
     * @return
     */
    List<String> findAnswer(Long examRecordId);

    /**
     * 获取某场考试记录中的考题数量
     * @param examRecordId
     * @return
     */
    List<Answer> findQuestionCountByRecordId(String examRecordId);

    /**
     * 获取某考试所有考题数量
     * @param examinationId
     * @return
     */
    Integer findExamTotalCount(String examinationId, String userId);

    /**
     * 根基考试id查询所有的考试记录
     * @param id
     * @return
     */
    List<Answer> findAllRecord(Long id);
}
