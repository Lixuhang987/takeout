package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.EmpolyeeInsertFailedException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        //进行md5加密，然后再进行比对
        String password = DigestUtils.md5DigestAsHex(employeeLoginDTO.getPassword().getBytes());

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus().equals(StatusConstant.DISABLE)) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    public void insert(EmployeeDTO employeeDTO) {
        if (employeeDTO.getPhone().length() != 11) {
            throw new EmpolyeeInsertFailedException("非法电话");
        }
        if (employeeDTO.getIdNumber().length() != 18) {
            throw new EmpolyeeInsertFailedException("非法身份证号");
        }
        Employee employee = Employee.builder()
                                    .username(employeeDTO.getUsername())
                                    .name(employeeDTO.getName())
                                    .sex(employeeDTO.getSex())
                                    .idNumber(employeeDTO.getIdNumber())
                                    .phone(employeeDTO.getPhone())
                                    .password(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()))
                                    .build();
        List<Employee> employees = employeeMapper.selectOr(employee);
        if (!employees.isEmpty()) {
            throw new EmpolyeeInsertFailedException("电话，用户名或身份证号重复");
        }
        employeeMapper.insert(employee, BaseContext.getCurrentId());
    }

}
