package little.ant.platform.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.beetl.core.BeetlKit;

/**
 * 简易辅助开发代码生成器
 * 
 * 描述：根据表，生成对应的.sql.xml文件、Model类、Service类、validator类、Controller类，
 * 不包含业务处理逻辑，考虑到开发的业务个性化，通用的生成意义不是太大，只做辅助开发
 * 
 * @author 董华健
 */
public class ToolCodeGenerator {

	/**
	 * 二维数组说明：表名、数据源、是否生成Controller相关、类名（不包含.java）
	 */
	public static String[][] tableArr = {
			{"wx_aa", "DictKeys.db_dataSource_main", "0", "Aa"}, // 生成.sql.xml文件、Model类、Service类、validator类、Controller类
			{"wx_bb", "DictKeys.db_dataSource_main", "0", "Bb"},
			{"wx_cc", "DictKeys.db_dataSource_main", "1", "Cc"} // 生成.sql.xml文件、Model类
		};
	
	/**
	 * 生成的包和类所在的源码根目录，比如src或者是weiXin
	 */
	public static String srcFolder = "weiXin";

	/**
	 * 生成的文件存放的包，公共基础包
	 * 描述：比如
	 * 	platform所在的包就是little.ant.platform
	 * 	weixin所在的包就是little.ant.weixin
	 */
	public static String packageBase = "little.ant.weixin";
	
	/**
	 * controller基础路径，例如
	 * @Controller(controllerKey = "/jf/platform/authImg") 中的platform
	 * @Controller(controllerKey = "/jf/wx/authImg") 中的wx
	 */
	public static String controllerBasePath = "wx";

	/**
	 * render基础路径，例如
	 * /platform/user/add.jsp 中的platform
	 * /weiXin/user/list.jsp 中的weiXin
	 */
	public static String renderBasePath = "weiXin";

	/**
	 * 循环生成文件
	 */
	public static void main(String[] args) {
		for (int i = 0; i < tableArr.length; i++) {
			// 表名
			String tableName = tableArr[i][0]; 
			// 数据源名称
			String dataSource = tableArr[i][1]; 
			// 是否生成Controller相关
			String generController = tableArr[i][2]; 
			// 类名
			String className = tableArr[i][3]; 
			// 类名首字母小写
			String classNameSmall = ToolString.toLowerCaseFirstOne(className); 
			
			// 1.生成sql文件
			sql(classNameSmall); 
			// 2.生成model
			model(className, classNameSmall, dataSource, tableName); 
			
			// 是否生成Controller相关
			if(generController.equals("0")){
				// 3.生成validator
				validator(className, classNameSmall); 
				// 4.生成controller
				controller(className, classNameSmall); 
				// 5.生成service
				service(className, classNameSmall); 
			}
		}
		System.exit(0);
	}

	/**
	 * 生成Model
	 * @param srcFolder
	 * @param packageBase
	 * @param className
	 * @param classNameSmall
	 * @param tableName
	 */
	public static void model(String className, String classNameSmall, String dataSource, String tableName){
		Map<String, Object> paraMap = new HashMap<String, Object>();
		String packages = packageBase + ".model";
		paraMap.put("package", packages);
		paraMap.put("className", className);
		paraMap.put("dataSource", dataSource);
		paraMap.put("tableName", tableName);
		
		String filePath = System.getProperty("user.dir") + "/"+srcFolder+"/" + packages.replace(".", "/") + "/" + className +".java";
		createFileByTemplete("model.html", paraMap, filePath);
	}

	/**
	 * 生成.sql.xml
	 * @param srcFolder
	 * @param packageBase
	 * @param classNameSmall
	 */
	public static void sql(String classNameSmall){
		Map<String, Object> paraMap = new HashMap<String, Object>();
		String packages = packageBase + ".model";
		paraMap.put("namespace", srcFolder + "." + classNameSmall);
		
		String filePath = System.getProperty("user.dir") + "/"+srcFolder+"/" + packages.replace(".", "/") + "/" + classNameSmall + ".sql.xml";
		createFileByTemplete("sql.html", paraMap, filePath);
	}

	/**
	 * 生成Controller
	 * @param srcFolder
	 * @param packageBase
	 * @param className
	 * @param classNameSmall
	 */
	public static void controller(String className, String classNameSmall){
		Map<String, Object> paraMap = new HashMap<String, Object>();
		String packages = packageBase + ".controller";
		paraMap.put("basePackage", packageBase);
		paraMap.put("package", packages);
		paraMap.put("className", className);
		paraMap.put("classNameSmall", classNameSmall);
		paraMap.put("controllerBasePath", controllerBasePath);
		paraMap.put("renderBasePath", renderBasePath);
		
		String filePath = System.getProperty("user.dir") + "/"+srcFolder+"/" + packages.replace(".", "/") + "/" + className + "Controller.java";
		createFileByTemplete("controller.html", paraMap, filePath);
	}

	/**
	 * 生成validator
	 * @param srcFolder
	 * @param packageBase
	 * @param className
	 * @param classNameSmall
	 */
	public static void validator(String className, String classNameSmall){
		Map<String, Object> paraMap = new HashMap<String, Object>();
		String packages = packageBase + ".validator";
		paraMap.put("basePackage", packageBase);
		paraMap.put("package", packages);
		paraMap.put("className", className);
		paraMap.put("classNameSmall", classNameSmall);
		paraMap.put("controllerBasePath", controllerBasePath);
		paraMap.put("renderBasePath", renderBasePath);
		
		String filePath = System.getProperty("user.dir") + "/"+srcFolder+"/" + packages.replace(".", "/") + "/" + className + "Validator.java";
		createFileByTemplete("validator.html", paraMap, filePath);
	}
	
	/**
	 * 生成Service
	 * @param srcFolder
	 * @param packageBase
	 * @param className
	 * @param classNameSmall
	 */
	public static void service(String className, String classNameSmall){
		Map<String, Object> paraMap = new HashMap<String, Object>();
		String packages = packageBase + ".service";
		paraMap.put("package", packages);
		paraMap.put("className", className);
		paraMap.put("classNameSmall", classNameSmall);
		paraMap.put("namespace", srcFolder + "." + classNameSmall);
		
		String filePath = System.getProperty("user.dir") + "/"+srcFolder+"/" + packages.replace(".", "/") + "/" + className + "Service.java";
		createFileByTemplete("service.html", paraMap, filePath);
	}

	/**
	 * 根据具体模板生成文件
	 * @param templateFileName
	 * @param paraMap
	 * @param filePath
	 */
	public static void createFileByTemplete(String templateFileName, Map<String, Object> paraMap, String filePath)  {
		try {
			Class<?> classes = Class.forName("little.ant.platform.tools.ToolCodeGenerator");

			InputStream controllerInputStream = classes.getResourceAsStream(templateFileName);
			int count = 0;
			while (count == 0) {
				count = controllerInputStream.available();
			}
			
			byte[] bytes = new byte[count];
			int readCount = 0; // 已经成功读取的字节的个数
			while (readCount < count) {
				readCount += controllerInputStream.read(bytes, readCount, count - readCount);
			}
			
			String template = new String(bytes);
			
			String javaSrc = BeetlKit.render(template, paraMap);
			
			File file = new File(filePath);
			BufferedWriter output = new BufferedWriter(new FileWriter(file));   
			output.write(javaSrc);   
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
