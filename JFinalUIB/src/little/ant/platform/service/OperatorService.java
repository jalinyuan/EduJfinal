package little.ant.platform.service;

import java.util.ArrayList;
import java.util.List;

import little.ant.platform.annotation.MyTxProxy;
import little.ant.platform.common.DictKeys;
import little.ant.platform.common.SplitPage;
import little.ant.platform.model.Module;
import little.ant.platform.model.Operator;

import org.apache.log4j.Logger;

public class OperatorService extends BaseService {

	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(OperatorService.class);

	public static final OperatorService service = MyTxProxy.newProxy(OperatorService.class);
	
	/**
	 * 保存
	 * @param operator
	 * @return
	 */
	public String save(Operator operator){
		// 保存
		operator.save();
		String ids = operator.getPKValue();
		
		// 缓存
		Operator.dao.cacheAdd(ids);
		
		return ids;
	}

	/**
	 * 更新
	 * @param operator
	 */
	public void update(Operator operator){
		// 更新
		operator.update();
		String ids = operator.getPKValue();
		
		// 缓存
		Operator.dao.cacheAdd(ids);
	}

	/**
	 * 删除
	 * @param ids
	 */
	public void delete(String ids){
		// 缓存
		Operator.dao.cacheRemove(ids);
		
		// 删除
		Operator.dao.deleteById(ids);
	}
	
	/**
	 * 获取子节点数据
	 * @param moduleIds
	 * @return
	 */
	public String childNodeData(String moduleIds){
		List<Module> listModule = new ArrayList<Module>();
		List<Operator> operatorList = new ArrayList<Operator>();
		
		if (null == moduleIds) {
			// 1.模块功能初始化调用
			String sql = getSql("platform.operator.rootModule");
			listModule = Module.dao.find(sql);
			
		} else if (null != moduleIds) {
			moduleIds = moduleIds.replace("module_", "");
			// 2.通用子节点查询
			String sqlModule = getSql("platform.operator.childModule");
			listModule = Module.dao.find(sqlModule, moduleIds);
			
			String sqlOperator = getSql("platform.operator.byModuleIds");
			operatorList = Operator.dao.find(sqlOperator, moduleIds);
		}

		StringBuilder sb = new StringBuilder();
		sb.append("[");

		int operatorSize = operatorList.size();
		int operatorIndexSize = operatorSize - 1;
		for (Operator operator : operatorList) {
			sb.append(" { ");
			sb.append(" id : '").append("operator_").append(operator.getPKValue()).append("', ");
			sb.append(" name : '").append(operator.getStr("names")).append("', ");
			sb.append(" isParent : false, ");

			sb.append(" checked : false, ");

			sb.append(" font : {'font-weight':'bold'}, ");
			sb.append(" icon : '/jsFile/zTree/css/zTreeStyle/img/diy/5.png' ");
			sb.append(" }");
			if (operatorList.indexOf(operator) < operatorIndexSize) {
				sb.append(", ");
			}
		}

		int moduleSize = listModule.size();
		int moduleIndexSize = moduleSize - 1;
		if (operatorSize > 0 && moduleSize > 0) {
			sb.append(", ");
		}
		for (Module module : listModule) {
			sb.append(" { ");
			sb.append(" id : '").append("module_").append(module.getPKValue()).append("', ");
			sb.append(" name : '").append(module.getStr("names")).append("', ");
			sb.append(" isParent : ").append(module.getStr("isparent")).append(", ");
			sb.append(" nocheck : true, ");
			sb.append(" font : {'font-weight':'bold'}, ");
			sb.append(" icon : '/jsFile/zTree/css/zTreeStyle/img/diy/").append(module.getStr("images")).append("' ");
			sb.append(" }");
			if (listModule.indexOf(module) < moduleIndexSize) {
				sb.append(", ");
			}
		}

		sb.append("]");

		return sb.toString();
	}
	
	/**
	 * 分页
	 * @param splitPage
	 */
	public void list(SplitPage splitPage){
		String select = " select o.ids, o.names, o.url, o.rowFilter, o.splitPage, o.formToken, o.privilegess, m.names as modulenames, s.names as systemsnames ";
		splitPageBase(DictKeys.db_dataSource_main, splitPage, select, "platform.operator.splitPage");
	}
	
}
