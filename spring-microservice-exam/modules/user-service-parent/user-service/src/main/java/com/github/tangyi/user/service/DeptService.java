package com.github.tangyi.user.service;

import cn.hutool.core.collection.CollUtil;
import com.github.tangyi.common.basic.vo.DeptVo;
import com.github.tangyi.common.core.constant.CommonConstant;
import com.github.tangyi.common.core.service.CrudService;
import com.github.tangyi.common.core.utils.TreeUtil;
import com.github.tangyi.common.security.utils.SysUtil;
import com.github.tangyi.user.api.dto.DeptDto;
import com.github.tangyi.user.api.module.Dept;
import com.github.tangyi.user.api.module.User;
import com.github.tangyi.user.mapper.DeptMapper;
import com.github.tangyi.user.mapper.StationMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 部门service
 *
 * @author tangyi
 * @date 2018/8/26 22:46
 */
@AllArgsConstructor
@Service
public class DeptService extends CrudService<DeptMapper, Dept> {

	private final DeptMapper deptMapper;

    /**
     * 删除部门
     *
     * @param dept dept
     * @return int
     */
    @Transactional
    @Override
    public int delete(Dept dept) {
        // 删除部门
        return super.delete(dept);
    }

    /**
     * 根据用户批量查询
     *
     * @param userList userList
     * @return List
     * @author tangyi
     * @date 2019/07/03 22:06:50
     */
    public List<Dept> getListByUsers(List<User> userList) {
        return this.findListById(userList.stream().filter(tempUser -> tempUser.getDeptId() != null).map(User::getDeptId).distinct().toArray(Long[]::new));
    }

	/**
	 * 查询树形部门集合
	 *
	 * @return List
	 * @author tangyi
	 * @date 2018/10/25 12:57
	 */
	public List<DeptDto> depts() {
		Dept dept = new Dept();
		dept.setApplicationCode(SysUtil.getSysCode());
		dept.setTenantCode(SysUtil.getTenantCode());
		// 查询部门集合
		Stream<Dept> deptStream = findList(dept).stream();
		if (Optional.ofNullable(deptStream).isPresent()) {
			List<DeptDto> deptTreeList = deptStream.map(DeptDto::new).collect(Collectors.toList());
			// 排序、构建树形结构
			return TreeUtil
					.buildTree(CollUtil.sort(deptTreeList, Comparator.comparingInt(DeptDto::getSort)), CommonConstant.ROOT);
		}
		return new ArrayList<>();
	}

	/**
	 * 获取所用的部门数据
	 * @return
	 */
	public List<DeptVo> getDeptDataList() {
		List<DeptVo> deptVoList = new ArrayList<>();

		List<Dept> deptDataList = deptMapper.getDeptDataList();
		for (Dept dept : deptDataList) {
			DeptVo deptVo = new DeptVo();
			deptVo.setDeptId(dept.getId().toString());
			deptVo.setDeptName(dept.getDeptName());
			deptVoList.add(deptVo);
		}

		return deptVoList;
	}
}
