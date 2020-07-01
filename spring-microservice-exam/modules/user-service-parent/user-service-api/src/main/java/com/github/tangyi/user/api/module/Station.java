package com.github.tangyi.user.api.module;

import com.github.tangyi.common.core.persistence.BaseEntity;
import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 * User: hanhm
 * Date: 2020/7/1
 * Time: 16:19
 * Description:
 **/

@Data
public class Station extends BaseEntity<Station> {

    /**
     * 岗位id
     */
    private Long id;

    /**
     * 岗位名称
     */
    private String station;


}
