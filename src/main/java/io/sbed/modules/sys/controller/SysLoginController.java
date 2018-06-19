package io.sbed.modules.sys.controller;

import com.google.code.kaptcha.Producer;
import io.sbed.common.Constant;
import io.sbed.common.cache.RedisUtils;
import io.sbed.common.utils.JWTUtil;
import io.sbed.common.utils.Result;
import io.sbed.modules.sys.entity.SysUser;
import io.sbed.modules.sys.service.SysUserService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author
 * @Description: (登录相关)
 * @date 2017-6-23 15:07
 */
@RestController
public class SysLoginController extends AbstractController {

	public static final String CAPTCHA_TEXT = "captcha-text-";
	public static final String CAPTCHA_ERROR_TIMES = "captcha-errorTimes-";
	@Autowired
	private Producer producer;
	@Autowired
	private SysUserService sysUserService;

	@RequestMapping("/captcha.jpg")
	public void captcha(HttpServletRequest request,HttpServletResponse response, String captchaT)throws ServletException, IOException {
		response.setHeader("Cache-Control", "no-store, no-cache");
		response.setContentType("image/jpeg");

		//生成文字验证码
		String text = producer.createText();
//		String text = "1234";
		//生成图片验证码
		BufferedImage image = producer.createImage(text);
		//保存到shiro session
//		ShiroUtils.setSessionAttribute(Constants.KAPTCHA_SESSION_KEY, text);
		//redis中保存（用于其他地方判断是否输入正确）
		RedisUtils.set(CAPTCHA_TEXT +captchaT,text,60 * 60 * 5);
		ServletOutputStream out = response.getOutputStream();
		ImageIO.write(image, "jpg", out);
		IOUtils.closeQuietly(out);
	}

	/**
	 * 获取登录错误次数
	 */
	@RequestMapping(value = "/sys/getLoginErrorTimes", method = RequestMethod.GET)
	public Result getLoginErrorTimes(String captchaT){
//		Object errorTimes = ShiroUtils.getSessionAttribute(Constant.LOGIN_ERROR_TIMES);
		int errorTimes = NumberUtils.toInt(RedisUtils.get(CAPTCHA_ERROR_TIMES +captchaT),0);
		//redis中获取登录错误次数
		return Result.ok().put("errorTimes", errorTimes);
	}

	/**
	 * 登录
	 */
	@RequestMapping(value = "/sys/login", method = RequestMethod.POST)
	public Result login(String username, String password, String captcha,String captchaT)throws IOException {
		Object _login_errors = 3;
			//redis中获取登录错误次数
//			_login_errors = ShiroUtils.getSessionAttribute(Constant.LOGIN_ERROR_TIMES);
			_login_errors = NumberUtils.toInt(RedisUtils.get(CAPTCHA_ERROR_TIMES+captchaT),0);

		if(_login_errors == null){
			_login_errors = 3;
		}
		long errorTimes = Long.valueOf(_login_errors.toString());

		//用户信息
		SysUser user = sysUserService.queryByUserName(username);

		//账号不存在
		if(user == null) {
//			ShiroUtils.setSessionAttribute(Constant.LOGIN_ERROR_TIMES, ++errorTimes);
			//redis中获取登录错误次数
			RedisUtils.set(CAPTCHA_ERROR_TIMES+captchaT,++errorTimes);
			return Result.error("账号不存在").put("errorTimes", errorTimes);
		}

		//密码错误,使用密码当做加密的盐
		if(!user.getPassword().equals(new Sha256Hash(password, password).toHex())) {
//			ShiroUtils.setSessionAttribute(Constant.LOGIN_ERROR_TIMES, ++errorTimes);
			//redis中获取登录错误次数
			RedisUtils.set(CAPTCHA_ERROR_TIMES+captchaT,++errorTimes);
			return Result.error("密码不正确").put("errorTimes", errorTimes);
		}

		//验证码
		if(errorTimes >= 3){
//			String kaptcha = getKaptcha(Constants.KAPTCHA_SESSION_KEY);
			String kaptcha = getKaptcha(captchaT);
			if(!captcha.equalsIgnoreCase(kaptcha)){
//				ShiroUtils.setSessionAttribute(Constant.LOGIN_ERROR_TIMES, ++errorTimes);
				// redis中获取登录错误次数
				RedisUtils.set(CAPTCHA_ERROR_TIMES+captchaT,++errorTimes);
				return Result.error("验证码不正确").put("errorTimes", errorTimes);
			}
		}

		//账号锁定
		if(Constant.UserStatus.DISABLE.getValue() == user.getStatus()){
//			ShiroUtils.setSessionAttribute(Constant.LOGIN_ERROR_TIMES, ++errorTimes);
			// redis中获取登录错误次数
			RedisUtils.set(CAPTCHA_ERROR_TIMES+captchaT,++errorTimes);
			return Result.error("账号已被锁定,请联系管理员").put("errorTimes", errorTimes);
		}

		//生成token，并保存到数据库redis
//				sysUserTokenService.createToken(user);
		String token  = JWTUtil.sign(user.getId()+"",user.getPassword()+"");
		RedisUtils.set(Constant.TOKEN_IN_HEADER+"-"+user.getId(),token);
		Map<String, Object> result = new HashMap<>();
		result.put(Constant.TOKEN_IN_HEADER, token);
		result.put("expire", RedisUtils.DEFAULT_EXPIRE);
		RedisUtils.set(user.getId()+"",result);
		Result r =Result.ok().put(result);
		return r;
	}

	/**
	 * 退出
	 */
	@RequestMapping(value = "/sys/logout", method = RequestMethod.POST)
	public Result logout(HttpServletRequest request) {
		// redis中删除token信息
//		sysUserTokenService.logout(getUserId());
		String token = request.getHeader(Constant.TOKEN_IN_HEADER);
		RedisUtils.delete(Constant.TOKEN_IN_HEADER+"-"+JWTUtil.getUsername(token));
		return Result.ok();
	}

	/**
	 *从session中获取记录的验证码
	 */
	private String getKaptcha(String captchaT) {
//		Object kaptcha = ShiroUtils.getSessionAttribute(key);
//		if(kaptcha == null){
//			throw new SbedException("验证码已失效");
//		}
//		ShiroUtils.getSession().removeAttribute(key);
//		return kaptcha.toString();
		//redis中获取
		return RedisUtils.get(CAPTCHA_TEXT+captchaT);
	}
	
}
