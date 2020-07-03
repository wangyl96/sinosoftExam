package com.github.tangyi.user.service;

import com.github.tangyi.common.core.service.CrudService;
import com.github.tangyi.user.api.module.Station;
import com.github.tangyi.user.mapper.StationMapper;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: hanhm
 * Date: 2020/7/1
 * Time: 16:37
 * Description:
 **/

@AllArgsConstructor
@Service
public class StationService extends CrudService<StationMapper, Station> {

    private final StationMapper stationMapper;

    /**
     * 获取所有的岗位信息
     * @return
     */
    public List<Station> getStation() {

        List<Station> stations = Lists.newArrayList();

        stations = stationMapper.findAllList(new Station());

        return stations;
    }

    /**
     * 获取所有的岗位信息
     * @return
     */
    public String getStationById(Long id) {


        Station stations = stationMapper.getById(id);

        String stationString = stations.getStation() == null ? "" : stations.getStation();

        return stationString;
    }



}
