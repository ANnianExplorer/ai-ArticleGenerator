package com.ai.template.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.ai.template.model.dto.user.UserQueryRequest;
import com.ai.template.model.entity.User;
import com.ai.template.model.vo.LoginUserVO;
import com.ai.template.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface UserService extends IService<User> {

    long userRegister(String userAccount, String userPassword, String checkPassword);

    LoginUserVO getLoginUserVO(User user);

    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    User getLoginUser(HttpServletRequest request);

    UserVO getUserVO(User user);

    List<UserVO> getUserVOList(List<User> userList);

    boolean userLogout(HttpServletRequest request);

    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);

    String getEncryptPassword(String userPassword);
}
