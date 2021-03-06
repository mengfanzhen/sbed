package io.sbed.common.shiro;

import io.sbed.common.utils.JWTUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authc.AuthenticationToken;

/**
 * Description: 认证token <br>
 * Copyright:DATANG SOFTWARE CO.LTD<br>
 *
 * @author fuxiangming
 * @date 2018/6/14 下午4:09
 */
public class JWTToken implements AuthenticationToken {

    private String username;
    private String password;
    private String token;
    private boolean isLoginRequest = true;


    public JWTToken(String username, String password, String token) {
        this.username = username;
        this.password = password;
        this.token = token;
    }

    public JWTToken(String username, String password, String token, boolean isLoginRequest) {
        this.username = username;
        this.password = password;
        this.token = token;
        this.isLoginRequest = isLoginRequest;
    }

    @Override
    public Object getPrincipal() {
        return StringUtils.isNotBlank(this.username) ? this.username : JWTUtil.getUsername(this.token);
    }

    @Override
    public Object getCredentials() {
        return this.password;
    }


    public void setUsername(String username) {
        this.username = username;
    }


    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isLoginRequest() {
        return isLoginRequest;
    }

    public void setLoginRequest(boolean loginRequest) {
        isLoginRequest = loginRequest;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
