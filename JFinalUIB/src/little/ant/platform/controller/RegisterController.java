package little.ant.platform.controller;



import little.ant.platform.annotation.Controller;

import little.ant.platform.model.Group;
import little.ant.platform.model.User;
import little.ant.platform.model.UserInfo;

import little.ant.platform.service.GroupService;
import little.ant.platform.service.RegisterService;
import little.ant.platform.service.UserService;


/**
 * 注册处理
 */
@Controller(controllerKey = "/jf/platform/register")
public class RegisterController extends BaseController{
	
	public void index(){
		render("/platform/register.html");
	}
	public void save() {
		String password = getPara("user.password");
		User user = getModel(User.class);
		UserInfo userInfo = getModel(UserInfo.class);
		
	
		 int verson =0;
		Group group = GroupService.service.getIds(0);
		String name=group.getStr("names");
		String groupIds=group.getStr("ids");
		//分配普通用户的角色 
		user.set("groupids", groupIds);
		UserService.service.save(user, password, userInfo);
		//给用户分组，给用户分配角色才能登陆
		
		
		render("/platform/login.html");
	}
	/**
	 * 验证账号是否存在
	 */
	public void valiUserName(){
		String userName = getPara("userName");
		int count = RegisterService.service.valiUserName(userName);
		renderText(String.valueOf(count));
	}

}
