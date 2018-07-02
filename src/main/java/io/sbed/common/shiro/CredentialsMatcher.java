package io.sbed.common.shiro;

import io.sbed.modules.sys.entity.SysUserActive;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;
import org.apache.shiro.crypto.hash.Sha256Hash;

/**
 * Description: 凭证匹配器，验证密码 <br>
 * Copyright:DATANG SOFTWARE CO.LTD<br>
 *
 * @author fuxiangming
 * @date 2018/6/14 下午4:09
 */
public class CredentialsMatcher extends SimpleCredentialsMatcher {

    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        JWTToken utoken=(JWTToken) token;
        if(!utoken.isLoginRequest()){
            return true;
        }
        SysUserActive sysUserActive = (SysUserActive) info.getPrincipals().getPrimaryPrincipal();
        //密码错误,使用密码当做加密的盐
        return sysUserActive.getSysUser().getPassword().equals(new Sha256Hash(utoken.getCredentials(), sysUserActive.getSysUser().getSalt()).toHex());
    }

}