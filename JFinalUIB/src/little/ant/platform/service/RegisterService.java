package little.ant.platform.service;

import java.util.HashMap;
import java.util.Map;

import little.ant.platform.annotation.MyTxProxy;

import org.apache.log4j.Logger;

import com.jfinal.plugin.activerecord.Db;

public class RegisterService extends BaseService {
	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(LoginService.class);
	
	public static final RegisterService service = MyTxProxy.newProxy(RegisterService.class);
	/**
	 * 验证账号是否存在
	 * @param mailbox
	 * @return
	 */
	public int valiUserName(String userName){
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("table", "pt_user");
		param.put("column", "username");
		String sql = getSql("platform.baseModel.selectCount", param);
		int count = Db.queryLong(sql, userName).intValue();
		return count;
	}
}
