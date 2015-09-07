package little.ant.platform.junit;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import little.ant.platform.common.DictKeys;
import little.ant.platform.plugin.I18NPlugin;
import little.ant.platform.plugin.PropertiesPlugin;
import little.ant.platform.plugin.SqlXmlPlugin;
import little.ant.platform.plugin.TablePlugin;
import little.ant.platform.thread.ThreadParamInit;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.BeforeClass;

import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.CaseInsensitiveContainerFactory;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.activerecord.dialect.OracleDialect;
import com.jfinal.plugin.activerecord.dialect.PostgreSqlDialect;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.ehcache.EhCachePlugin;

public class TestBase {

	private static Logger log = Logger.getLogger(TestBase.class);
	
	protected static DruidPlugin druidPlugin;
    protected static ActiveRecordPlugin arpMain;
    protected static I18NPlugin i18NPlugin;
    protected static EhCachePlugin ehCachePlugin;
    protected static SqlXmlPlugin sqlXmlPlugin;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    	Properties properties = new Properties();
    	properties.load(TestBase.class.getResourceAsStream("/init.properties"));
		new PropertiesPlugin(properties).start();
		
		log.info("configPlugin 配置Druid数据库连接池连接属性");
		druidPlugin = new DruidPlugin(
				(String)PropertiesPlugin.getParamMapValue(DictKeys.db_connection_jdbcUrl), 
				(String)PropertiesPlugin.getParamMapValue(DictKeys.db_connection_userName), 
				(String)PropertiesPlugin.getParamMapValue(DictKeys.db_connection_passWord), 
				(String)PropertiesPlugin.getParamMapValue(DictKeys.db_connection_driverClass));

		log.info("configPlugin 配置Druid数据库连接池大小");
		druidPlugin.set(
				(Integer)PropertiesPlugin.getParamMapValue(DictKeys.db_initialSize), 
				(Integer)PropertiesPlugin.getParamMapValue(DictKeys.db_minIdle), 
				(Integer)PropertiesPlugin.getParamMapValue(DictKeys.db_maxActive));
		
		log.info("configPlugin 配置ActiveRecord插件");
		arpMain = new ActiveRecordPlugin(DictKeys.db_dataSource_main, druidPlugin);
		//arp.setTransactionLevel(4);//事务隔离级别
		arpMain.setDevMode(Boolean.parseBoolean((String) PropertiesPlugin.getParamMapValue(DictKeys.config_devMode))); // 设置开发模式
		arpMain.setShowSql(Boolean.parseBoolean((String) PropertiesPlugin.getParamMapValue(DictKeys.config_devMode))); // 是否显示SQL

		log.info("configPlugin 数据库类型判断");
		String db_type = (String) PropertiesPlugin.getParamMapValue(DictKeys.db_type_key);
		if(db_type.equals(DictKeys.db_type_postgresql)){
			log.info("configPlugin 使用数据库类型是 postgresql");
			arpMain.setDialect(new PostgreSqlDialect());
			arpMain.setContainerFactory(new CaseInsensitiveContainerFactory(true));// 配置属性名(字段名)大小写不敏感容器工厂
			
		}else if(db_type.equals(DictKeys.db_type_mysql)){
			log.info("configPlugin 使用数据库类型是 mysql");
			arpMain.setDialect(new MysqlDialect());
			arpMain.setContainerFactory(new CaseInsensitiveContainerFactory(true));// 配置属性名(字段名)大小写不敏感容器工厂
		
		}else if(db_type.equals(DictKeys.db_type_oracle)){
			log.info("configPlugin 使用数据库类型是 oracle");
			druidPlugin.setValidationQuery("select 1 FROM DUAL"); //指定连接验证语句(用于保存数据库连接池), 这里不加会报错误:invalid oracle validationQuery. select 1, may should be : select 1 FROM DUAL 
			arpMain.setDialect(new OracleDialect());
			arpMain.setContainerFactory(new CaseInsensitiveContainerFactory(true));// 配置属性名(字段名)大小写不敏感容器工厂
		}
		
		druidPlugin.start();
		
		log.info("configPlugin 表扫描注册");
		Map<String, ActiveRecordPlugin> arpMap = new HashMap<String, ActiveRecordPlugin>();
		arpMap.put(DictKeys.db_dataSource_main, arpMain); // 多数据源继续添加
		new TablePlugin(arpMap).start();
		
		arpMain.start();
		
		log.info("I18NPlugin 国际化键值对加载");
		i18NPlugin = new I18NPlugin();
		i18NPlugin.start();
		
		log.info("EhCachePlugin EhCache缓存");
		ehCachePlugin = new EhCachePlugin();
		ehCachePlugin.start();

		log.info("SqlXmlPlugin 解析并缓存 xml sql");
		sqlXmlPlugin = new SqlXmlPlugin();
		sqlXmlPlugin.start();
		
		ThreadParamInit.cacheAll();
    }
 
    @After
    public void tearDown() throws Exception {
//    	i18NPlugin.stop();
//    	ehCachePlugin.stop();
//    	sqlXmlPlugin.stop();
//    	
//    	druidPlugin.stop();
//    	arpMain.stop();
//    	
//    	System.exit(0);
    }
    
}
