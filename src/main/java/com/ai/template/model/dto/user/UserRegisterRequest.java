package com.ai.template.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userAccount;

    private String userPassword;

    private String checkPassword;
}