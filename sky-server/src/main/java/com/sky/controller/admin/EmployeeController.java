package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Api(tags = "员工相关接口")//swagger注解   tags可以省略
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @ApiOperation(value = "员工登陆")//value也可以省略
    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();
        log.info("登陆成功");
        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @ApiOperation(value = "员工退出")
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }

    /**
     * 1新增员工
     * <p>
     * 解决的问题：sql异常的捕获，操作人id的获取
     *
     * @param employeeDTO
     * @return
     */
    @ApiOperation("新增员工")
    @PostMapping
    public Result create(@RequestBody EmployeeDTO employeeDTO) {
        log.info("新增员工信息:{}", employeeDTO);
        System.out.println("当前线程id:" + Thread.currentThread().getId());
        employeeService.create(employeeDTO);
        return Result.success();
    }

    /**
     * 2员工分页查询
     *
     * @param employeePageQueryDTO
     * @return
     */
    @ApiOperation("员工分页查询")
    @GetMapping("/page")
    public Result<PageResult> pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        //也是用DTO来封装请求参数
        log.info("分页查询参数：{}", employeePageQueryDTO);
        PageResult pageResult = employeeService.pageQuery(employeePageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 3启用禁用员工账号
     *
     * @param status
     * @param id
     * @return
     */
    @ApiOperation("启用禁用员工账号")
    @PostMapping("/status/{status}")//两种参数 路径参数和query参数
    public Result startOrStop(@PathVariable Integer status, Long id) {
        log.info("id{}的账号状态更改为：{}", id, status);
        employeeService.startOrStop(status, id);
        return Result.success();
    }

    /**
     * 4根据id查询员工信息
     *
     * @param id
     * @return
     */
    @ApiOperation("根据id查询员工信息")
    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable Long id) {
        log.info("查询员工id:{}", id);
        Employee employee = employeeService.getById(id);
        return Result.success(employee);
    }

    /**
     * 4根据id修改员工信息
     *
     * @param employeeDTO
     * @return
     */
    @ApiOperation("根据id修改员工信息")
    @PutMapping("")
    public Result updateById(@RequestBody EmployeeDTO employeeDTO) {
        //注意也要使用DTO
        log.info("修改的员工id:{}", employeeDTO.getId());
        employeeService.updateById(employeeDTO);
        return Result.success();
    }
}
