package little.ant.platform.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import little.ant.platform.common.DictKeys;
import little.ant.platform.model.Group;
import little.ant.platform.model.Operator;
import little.ant.platform.model.Role;
import little.ant.platform.model.Station;
import little.ant.platform.model.User;
import little.ant.platform.plugin.PropertiesPlugin;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

/**
 * WEB上下文工具类
 * 
 * @author 董华健 2012-9-7 下午1:51:04
 */
public class ToolContext {

	private static Logger log = Logger.getLogger(ToolContext.class);

	public static boolean hasPrivilegeUrl(String url, String userIds) {
		// 基于缓存查询operator
		Operator operator = Operator.dao.cacheGet(url);
		if (null == operator) {
			log.error("URL缓存不存在：" + url);
			return false;
		}

		// 基于缓存查询user
		Object userObj = User.dao.cacheGet(userIds);
		if (null == userObj) {
			log.error("用户缓存不存在：" + userIds);
			return false;
		}
		User user = (User) userObj;

		// 权限验证对象
		String operatorIds = operator.getPKValue() + ",";
		String groupIds = user.getStr("groupids");
		String stationIds = user.getStr("stationids");

		// 根据分组查询权限
		if (null != groupIds) {
			String[] groupIdsArr = groupIds.split(",");
			for (String groupIdsTemp : groupIdsArr) {
				Group group = Group.dao.cacheGet(groupIdsTemp);
				String roleIdsStr = group.getStr("roleids");
				if(null == roleIdsStr || roleIdsStr.equals("")){
					continue;
				}
				String[] roleIdsArr = roleIdsStr.split(",");
				for (String roleIdsTemp : roleIdsArr) {
					Role role = Role.dao.cacheGet(roleIdsTemp);
					String operatorIdsStr = role.getStr("operatorids");
					if (operatorIdsStr.indexOf(operatorIds) != -1) {
						return true;
					}
				}
			}
		}

		// 根据岗位查询权限
		if (null != stationIds) {
			String[] stationIdsArr = stationIds.split(",");
			for (String ids : stationIdsArr) {
				Station station = Station.dao.cacheGet(ids);
				String operatorIdsStr = station.getStr("operatorids");
				if(null == operatorIdsStr || operatorIdsStr.equals("")){
					continue;
				}
				if (operatorIdsStr.indexOf(operatorIds) != -1) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * 判断用户是否对某个功能具有操作权限
	 * 
	 * @param operator
	 * @param user
	 * @return
	 */
	public static boolean hasPrivilegeOperator(Operator operator, User user) {
		// 基于缓存查询
		String operatorIds = operator.getPKValue() + ",";

		// 根据分组查询权限
		String groupIds = user.getStr("groupids");
		if (null != groupIds) {
			String[] groupIdsArr = groupIds.split(",");
			for (String groupIdsTemp : groupIdsArr) {
				Group group = Group.dao.cacheGet(groupIdsTemp);
				String roleIdsStr = group.getStr("roleids");
				if(null == roleIdsStr || roleIdsStr.equals("")){
					continue;
				}
				String[] roleIdsArr = roleIdsStr.split(",");
				for (String roleIdsTemp : roleIdsArr) {
					Role role = Role.dao.cacheGet(roleIdsTemp);
					String operatorIdsStr = role.getStr("operatorids");
					if (operatorIdsStr.indexOf(operatorIds) != -1) {
						return true;
					}
				}
			}
		}

		// 根据岗位查询权限
		String stationIds = user.getStr("stationids");
		if (null != stationIds) {
			String[] stationIdsArr = stationIds.split(",");
			for (String ids : stationIdsArr) {
				Station station = Station.dao.cacheGet(ids);;
				String operatorIdsStr = station.getStr("operatorids");
				if(null == operatorIdsStr || operatorIdsStr.equals("")){
					continue;
				}
				if (operatorIdsStr.indexOf(operatorIds) != -1) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * 输出servlet文本内容
	 * 
	 * @author 董华健 2012-9-14 下午8:04:01
	 * @param response
	 * @param content
	 */
	public static void outPage(HttpServletResponse response, String content) {
		response.setContentType("text/html; charset=UTF-8");
		response.setCharacterEncoding(ToolString.encoding);
		// PrintWriter out = response.getWriter();
		// out.print(content);
		try {
			response.getOutputStream().write(content.getBytes(ToolString.encoding));// char to byte 性能提升
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 输出CSV文件下载
	 * 
	 * @author 董华健 2012-9-14 下午8:02:33
	 * @param response
	 * @param content CSV内容
	 */
	public static void outDownCsv(HttpServletResponse response, String content) {
		response.setContentType("application/download; charset=gb18030");
		try {
			response.setHeader("Content-Disposition", "attachment; filename=" + java.net.URLEncoder.encode(ToolDateTime.format(ToolDateTime.getDate(), ToolDateTime.pattern_ymd_hms_s) + ".csv", ToolString.encoding));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		// PrintWriter out = response.getWriter();
		// out.write(content);
		try {
			response.getOutputStream().write(content.getBytes(ToolString.encoding));
		} catch (IOException e) {
			e.printStackTrace();
		}// char to byte 性能提升
			// out.flush();
			// out.close();
	}

	/**
	 * 获取当前登录用户
	 * @param request
	 * @param userAgentVali 是否验证 User-Agent
	 * @return
	 */
	public static User getCurrentUser(HttpServletRequest request, boolean userAgentVali) {
		String loginCookie = ToolWeb.getCookieValueByName(request, "authmark");
		if (null != loginCookie && !loginCookie.equals("")) {
			// 1. Base64解码cookie令牌
			try {
				loginCookie = ToolString.decode(loginCookie);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// 2. 解密cookie令牌
			byte[] securityByte = Base64.decodeBase64(loginCookie);

			String securityKey = (String) PropertiesPlugin.getParamMapValue(DictKeys.config_securityKey_key);
			byte[] keyByte = Base64.decodeBase64(securityKey);

			byte[] dataByte = null;
			try {
				dataByte = ToolSecurityIDEA.decrypt(securityByte, keyByte);
			} catch (Exception e) {
				e.printStackTrace();
			}
			String data = new String(dataByte);
			String[] datas = data.split(".#.");	//arr[0]：时间戳，arr[1]：USERID，arr[2]：USER_IP， arr[3]：USER_AGENT
			
			// 3. 获取数据
			long loginDateTimes = Long.parseLong(datas[0]);// 时间戳
			String userIds = datas[1];// 用户id
			String ips = datas[2];// ip地址
			String userAgent = datas[3];// USER_AGENT

			String newIp = ToolWeb.getIpAddr(request);
			String newUserAgent = request.getHeader("User-Agent");

			Date start = ToolDateTime.getDate();
			start.setTime(loginDateTimes);
			int day = ToolDateTime.getDateDaySpace(start, ToolDateTime.getDate());
			
			// 4. 验证数据有效性
			if (ips.equals(newIp) && (userAgentVali ? userAgent.equals(newUserAgent) : true) && day <= 365) {
				Object userObj = User.dao.cacheGet(userIds);
				if (null != userObj) {
					User user = (User) userObj;
					return user;
				}
			}
		}

		return null;
	}

	/**
	 * 设置当前登录用户
	 * @param request
	 * @param response
	 * @param user
	 * @param autoLogin
	 */
	public static void setCurrentUser(HttpServletRequest request, HttpServletResponse response, User user, boolean autoLogin) {
		// 1.设置cookie有效时间
		int maxAgeTemp = -1;
		if (autoLogin) {
			maxAgeTemp = ((Integer) PropertiesPlugin.getParamMapValue(DictKeys.config_maxAge_key)).intValue();
		}

		// 2.设置用户名到cookie
		ToolWeb.addCookie(response, "", "/", true, "userName", user.getStr("username"), maxAgeTemp);

		// 3.生成登陆认证cookie
		String userIds = user.getPKValue();
		String ips = ToolWeb.getIpAddr(request);
		String userAgent = request.getHeader("User-Agent");
		long date = ToolDateTime.getDateByTime();

		StringBuilder token = new StringBuilder();// 时间戳#USERID#USER_IP#USER_AGENT
		token.append(date).append(".#.").append(userIds).append(".#.").append(ips).append(".#.").append(userAgent);
		String authToken = token.toString();
		byte[] authTokenByte = null;
		try {
			authTokenByte = authToken.getBytes(ToolString.encoding);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String securityKey = (String) PropertiesPlugin.getParamMapValue(DictKeys.config_securityKey_key);
		byte[] keyByte = Base64.decodeBase64(securityKey);

		// 4. 认证cookie加密
		byte[] securityByte = null;
		try {
			securityByte = ToolSecurityIDEA.encrypt(authTokenByte, keyByte);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String securityCookie = Base64.encodeBase64String(securityByte);

		// 5. 认证cookie Base64编码
		try {
			securityCookie = ToolString.encode(securityCookie);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 6. 添加到Cookie
		ToolWeb.addCookie(response,  "", "/", true, "authmark", securityCookie, maxAgeTemp);
	}
	
	/**
	 * 设置验证码
	 * @param response
	 * @param authCode
	 */
	public static void setAuthCode(HttpServletResponse response, String authCode){
		byte[] authTokenByte = null;
		try {
			authTokenByte = authCode.getBytes(ToolString.encoding);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String securityKey = (String) PropertiesPlugin.getParamMapValue(DictKeys.config_securityKey_key);
		byte[] keyByte = Base64.decodeBase64(securityKey);

		// 加密
		byte[] securityByte = null;
		try {
			securityByte = ToolSecurityIDEA.encrypt(authTokenByte, keyByte);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String securityCookie = Base64.encodeBase64String(securityByte);

		// Base64编码
		try {
			securityCookie = ToolString.encode(securityCookie);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 登陆认证cookie
		int maxAgeTemp = ((Integer) PropertiesPlugin.getParamMapValue(DictKeys.config_maxAge_key)).intValue();
		ToolWeb.addCookie(response,  "", "/", true, "authCode", securityCookie, maxAgeTemp);
	}

	/**
	 * 获取验证码
	 * @param request
	 * @return
	 */
	public static String getAuthCode(HttpServletRequest request){
		String authCode = ToolWeb.getCookieValueByName(request, "authCode");
		if (null != authCode && !authCode.equals("")) {
			// Base64解码
			try {
				authCode = ToolString.decode(authCode);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// 解密
			byte[] securityByte = Base64.decodeBase64(authCode);

			String securityKey = (String) PropertiesPlugin.getParamMapValue(DictKeys.config_securityKey_key);
			byte[] keyByte = Base64.decodeBase64(securityKey);

			byte[] dataByte = null;
			try {
				dataByte = ToolSecurityIDEA.decrypt(securityByte, keyByte);
			} catch (Exception e) {
				e.printStackTrace();
			}
			authCode = new String(dataByte);
		}
		return authCode;
	}

	/**
	 * 获取请求参数
	 * 
	 * @param request
	 * @param name
	 * @return
	 */
	public static String getParam(HttpServletRequest request, String name) {
		String value = request.getParameter(name);
		if (null != value && !value.isEmpty()) {
			try {
				value = URLDecoder.decode(value, ToolString.encoding).trim();
			} catch (UnsupportedEncodingException e) {
				log.error("decode异常：" + value);
			}
		}
		return value;
	}

	/**
	 * 请求流转字符串
	 * 
	 * @param request
	 * @return
	 */
	public static String requestStream(HttpServletRequest request) {
		InputStream inputStream = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader bufferedReader = null;
		try {
			request.setCharacterEncoding(ToolString.encoding);
			inputStream = (ServletInputStream) request.getInputStream();
			inputStreamReader = new InputStreamReader(inputStream, ToolString.encoding);
			bufferedReader = new BufferedReader(inputStreamReader);
			String line = null;
			StringBuilder sb = new StringBuilder();
			while ((line = bufferedReader.readLine()) != null) {
				sb.append(line);
			}
			return sb.toString();
		} catch (IOException e) {
			log.error("request.getInputStream() to String 异常", e);
			return null;
		} finally { // 释放资源
			if(null != bufferedReader){
				try {
					bufferedReader.close();
				} catch (IOException e) {
					log.error("bufferedReader.close()异常", e);
				}
				bufferedReader = null;
			}
			
			if(null != inputStreamReader){
				try {
					inputStreamReader.close();
				} catch (IOException e) {
					log.error("inputStreamReader.close()异常", e);
				}
				inputStreamReader = null;
			}
			
			if(null != inputStream){
				try {
					inputStream.close();
				} catch (IOException e) {
					log.error("inputStream.close()异常", e);
				}
				inputStream = null;
			}
		}
	}
}
