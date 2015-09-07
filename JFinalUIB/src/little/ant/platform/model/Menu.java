package little.ant.platform.model;

import little.ant.platform.annotation.Table;
import little.ant.platform.common.DictKeys;

import org.apache.log4j.Logger;

/**
 * 菜单model
 * @author 董华健
 */
@SuppressWarnings("unused")
@Table(dataSourceName = DictKeys.db_dataSource_main, tableName = "pt_menu")
public class Menu extends BaseModel<Menu> {

	private static final long serialVersionUID = 6761767368352810428L;

	private static Logger log = Logger.getLogger(Menu.class);
	
	public static final Menu dao = new Menu();
	
	/**
	 * 根据菜单获取对应的功能，可能为空
	 * @return
	 */
	public Operator getOperator(){
		String operatorIds = get("operatorids");
		if(null != operatorIds && !operatorIds.isEmpty()){
			return Operator.dao.findById(operatorIds);
		}
		return null;
	}
	
}
