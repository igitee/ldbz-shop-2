package cn.ldbz.sso.controller;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.dubbo.config.annotation.Reference;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;

import cn.ldbz.constant.Const;
import cn.ldbz.notify.service.NotifyUserService;
import cn.ldbz.pojo.LdbzResult;
import cn.ldbz.pojo.LdbzUser;
import cn.ldbz.sso.service.UserService;
import cn.ldbz.utils.CookieUtils;

/**
 * 用户登录注册 Controller
 */
@Controller
@RequestMapping("sso")
public class UserController {
	
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Reference(version = Const.LDBZ_SHOP_SSO_VERSION, timeout=30000)
    private UserService userService;

    @Reference(version = Const.LDBZ_SHOP_NOTIFY_VERSION , timeout=30000)
    private NotifyUserService notifyUserService;

    @Value("${portal_path}")
    private String PORTAL_PATH;
    //图片的URL路径
    @Value("${redisKey.nginxImage.url.key}")
    private String INDEX_NGINX_IMAGE_URL;
    
    /**
     * 监听配置项是否有修改
     */
    @ApolloConfigChangeListener
	public void onChange(ConfigChangeEvent changeEvent) {
		for (String key : changeEvent.changedKeys()) {
			ConfigChange change = changeEvent.getChange(key);
			logger.debug("Found change - key: {}, oldValue: {}, newValue: {}, changeType: {}",
					change.getPropertyName(), change.getOldValue(), change.getNewValue(), change.getChangeType());
			switch(key) {
				case "portal_path" : 
					PORTAL_PATH = change.getNewValue();
				case "redisKey.nginxImage.url.key" : 
					INDEX_NGINX_IMAGE_URL = change.getNewValue();
			}
		}
	}

    /**
     * 显示注册页面
     * @param model
     * @param ReturnUrl 返回跳转URL
     * @return
     */
    @RequestMapping(value = "/register",method = RequestMethod.GET)
    public String showRegister(HttpServletRequest request, Model model, String returnUrl) {

        String token = CookieUtils.getCookieValue(request, Const.TOKEN_LOGIN);
        if(StringUtils.isNoneEmpty(token)) {
        	if(userService.token(token)!=null) {
        		//已经在线
        		return "redirect:" + PORTAL_PATH;
        	}
        }
        //尚未登录
        model.addAttribute("uid", UUID.randomUUID().toString());
    	model.addAttribute("nginxImage", INDEX_NGINX_IMAGE_URL);
        return "register";
    }

    /**
     * 显示登录页面
     * @param model
     * @param returnUrl 返回跳转URL
     * @return
     */
    @RequestMapping(value = "/login",method = RequestMethod.GET)
    public String showLogin(HttpServletRequest request, Model model, String returnUrl) {

        String token = CookieUtils.getCookieValue(request, Const.TOKEN_LOGIN);
        if(StringUtils.isNoneEmpty(token)) {
        	if(userService.token(token)!=null) {
        		//已经在线
        		return "redirect:" + PORTAL_PATH;
        	}
        }
        //尚未登录
        model.addAttribute("returnUrl", returnUrl);
    	model.addAttribute("nginxImage", INDEX_NGINX_IMAGE_URL);
        return "login";
    }

    /**
     * 用户登录
     * @param user POJO
     * @param returnUrl 返回跳转URL
     * @param response
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/user/login", method = RequestMethod.POST)
    public String login(LdbzUser user, String returnUrl, HttpServletResponse response, HttpServletRequest request) {
        LdbzResult result = userService.login(user);
        if (result.getStatus() == 200) {
            CookieUtils.setCookie(request, response, Const.TOKEN_LOGIN, result.getData().toString());
            //有返回URL 跳转
            if (StringUtils.isNotBlank(returnUrl)) {
                return "{\"success\":true , \"info\":\"" + returnUrl + "\"}";
            }
            return "{\"success\":true , \"info\":\"" + PORTAL_PATH + "\"}";
        }
        if (result.getStatus() == 400) {
            return "{\"success\":false , \"info\":\"用户不存在\"}";
        }
        if (result.getStatus() == 500) {
        	return "{\"success\":false , \"info\":\"用户名密码不能为空\"}";
        }
        return "{\"success\":false , \"info\":\"用户名密码有误\"}";
    }

    @ResponseBody
    @RequestMapping(value = "/loginservice")
    public String valida(String callback, String method, Integer uid) {
        return callback + "({\"Identity\":{\"Unick\":\"\",\"Name\":\"\",\"IsAuthenticated\":false}})";
    }

    /**
     * 验证用户名、邮箱是否重复
     * @param isEngaged 检测的名称
     * @param regName 用户名
     * @param email 邮箱
     * @return
     */
    @ResponseBody
    @RequestMapping("/validateuser/{isEngaged}")
    public String validateUser(@PathVariable String isEngaged, @RequestParam(defaultValue = "") String regName, 
    		@RequestParam(defaultValue = "") String email) {
        return userService.validateUser(isEngaged, regName, email);
    }

    /**
     * 发送邮件验证码
     * @param email 邮箱
     * @return
     */
    @ResponseBody
    @RequestMapping("/notifyuser/emailCode")
    public String emailCode(String email) {
        return notifyUserService.emailNotify(email);
    }

    /**
     * 请求格式 POST
     * 注册 使用邮箱注册
     *
     * @param regName       注册名
     * @param pwd           第一次密码
     * @param pwdRepeat     第二次密码
     * @param emailCode    邮箱验证码
     * @param email         邮箱
     * @param authCode      输入的验证码
     * @param uuid          Redis验证码uuid
     * @return
     */
    @ResponseBody
    @RequestMapping("/registerByEmail")
    public String sendRegEmail(String regName, String pwdRepeat, String pwd, String emailCode, String uuid, String authCode, String email) {
        return userService.register(regName, pwd, pwdRepeat, emailCode, uuid, authCode, email);
    }
    
    /**
     * 用户注销
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
    	String token = CookieUtils.getCookieValue(request, Const.TOKEN_LOGIN);
    	userService.logout(token, null);
    	CookieUtils.setCookie(request, response, Const.TOKEN_LOGIN, "", -1);
        return "redirect:" + PORTAL_PATH;
    }

}
