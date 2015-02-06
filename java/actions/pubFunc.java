package actions;

import logic.ConverterUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

public class pubFunc {
    /**
	 * 获取查询服务的参数
	 * 
	 * @author XDYΩΩΩΩ
	 * @created 2010-11-12
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public static Hashtable<String, String> getQueryParams(
			HttpServletRequest request) throws Exception {
		Hashtable<String, String> paramHt = new Hashtable<String, String>();
		HashMap<String, String> paramMap = getParamsMap(request);
		String tableAlias = paramMap.get("tableAlias");
		String fields = paramMap.get("fields");
		String accID = (String) request.getSession()
				.getAttribute("sysAccessID");
		String resultCondiXml = paramMap.get("resultCondiXml");
		String conditionXml = paramMap.get("conditionXml");
		if (conditionXml != null && conditionXml.length() > 0) {
			if (resultCondiXml != null && resultCondiXml.length() > 0) {
				conditionXml = unionXml(conditionXml, resultCondiXml);
			}
			conditionXml = "BASE64ENCODING" + conditionXml;
			
		} else {
			if (resultCondiXml != null && resultCondiXml.length() > 0) {
				conditionXml = "BASE64ENCODING" + resultCondiXml;
			}
		}
		String service = paramMap.get("service");
		String pageSize = paramMap.get("limit");
		String nowpage = paramMap.get("page");
		if (nowpage == null || nowpage.length() <= 0)
			nowpage = "1";
		String flag = paramMap.get("flag");
		if (flag == null || !flag.equals("0"))
			flag = "1";

		String sort = paramMap.get("OrderByField");

		String curNode = paramMap.get("node");
		// 获得要sum或avg的字段
		String sumFields = paramMap.get("sumFields").toUpperCase();
		// String avgFields = request.getParameter("avgFields");
		// Add By XDY 2008.07.23 加多取表的参数，调用公用查询服务需要传入表名这个参数 begin
		String tableName = paramMap.get("TableName");
		// Add By XDY 2008.07.23 加多取表的参数，调用公用查询服务需要传入表名这个参数 end
		// 树条件XML add by hjr treeConditionXml
		String treeCondi = paramMap.get("treeConditionXml");
		if (treeCondi != null && treeCondi.length() > 0) {
			treeCondi = URLDecoder.decode(treeCondi, "UTF-8");
			treeCondi = parseTreeCondi(treeCondi);
		}

		// 分类汇总参数
		String groupby = paramMap.get("GroupByField");

		String MinSumParam = paramMap.get("MinSumParam");
		if (MinSumParam != null && MinSumParam.length() > 0) {
			MinSumParam = URLDecoder.decode(MinSumParam, "UTF-8");
			MinSumParam = URLDecoder.decode(MinSumParam, "UTF-8");
		}

		String expression = paramMap.get("Expression");
		if (expression != null && expression.length() > 0) {
			expression = URLDecoder.decode(expression, "UTF-8");
			expression = URLDecoder.decode(expression, "UTF-8");
		}

		String percentage = paramMap.get("Percentage");

		String returnfieldlist = paramMap.get("returnfieldlist");
		returnfieldlist = parseReturnfieldlist(returnfieldlist);

		String CountFieldName = paramMap.get("CountFieldName");
		String CountFieldName_In = paramMap.get("CountFieldName_In");

		String mappedField = paramMap.get("MappedField");

		String isPrint = paramMap.get("isPrint");
		if (isPrint == null || isPrint.length() <= 0)
			isPrint = "0";

		String CurTempTableName = paramMap.get("CurTempTableName");

		String isCondiLoad = paramMap.get("isCondiLoad");
		if (isCondiLoad == null || isCondiLoad.length() <= 0) {
			isCondiLoad = "0";
		}
		
		String autocolumninfo=paramMap.get("autocolumninfo");
		if (autocolumninfo==null)
			autocolumninfo="";
		else
			autocolumninfo=URLDecoder.decode(URLDecoder.decode(autocolumninfo, "UTF-8"), "UTF-8");

		String customInputCondi = paramMap.get("customInputCondi");
		customInputCondi = customInputCondi.replace("大于", " > ")
				.replace("小于", " < ").replace("不等于", " != ")
				.replace("等于", " = ");
		// 级次条件参数
		String levelFilterCondi = paramMap.get("levelFilterCondi");
		// 带数的表格，用于建树的字段名
		String treeGridField = paramMap.get("treeGridField");

		String reQuery = paramMap.get("reQuery");
		String ReportId = paramMap.get("reportid");
		paramHt.put("service", service);
		paramHt.put("page", coverNull(nowpage));
		paramHt.put("pageSize", coverNull(pageSize));
		paramHt.put("conditionXML", coverNull(conditionXml));
		paramHt.put("accid", accID);
		paramHt.put("flag", flag);
		paramHt.put("isCondiLoad", coverNull(isCondiLoad));
		paramHt.put("sortFields", coverNull(sort));
		paramHt.put("curNode", coverNull(curNode));
		paramHt.put("sumFields", coverNull(sumFields));
		paramHt.put("returnfieldlist", coverNull(returnfieldlist));
		paramHt.put("tablename", coverNull(tableName));// Add By XDY 2008.07.23
		// 加多取表的参数，调用公用查询服务需要传入表名这个参数
		paramHt.put("treeConditionXml", coverNull(treeCondi));
		paramHt.put("groupby", coverNull(groupby));
		paramHt.put("MinSumParam", coverNull(MinSumParam));
		paramHt.put("mappedField", coverNull(mappedField));
		paramHt.put("expression", coverNull(expression));
		paramHt.put("percentage", coverNull(percentage));
		paramHt.put("countFieldName", coverNull(CountFieldName));
		paramHt.put("CountFieldName_In", coverNull(CountFieldName_In));
		paramHt.put("fields", coverNull(fields));
		paramHt.put("tableAlias", coverNull(tableAlias));
		paramHt.put("CurTempTableName", coverNull(CurTempTableName));
		paramHt.put("isPrint", coverNull(isPrint));
		paramHt.put("levelFilterCondi", coverNull(levelFilterCondi));
		paramHt.put("treeGridField", coverNull(treeGridField));
		paramHt.put("reQuery", coverNull(reQuery));
		paramHt.put("customInputCondi", coverNull(customInputCondi));
		paramHt.put("ReportId", coverNull(ReportId));
		paramHt.put("autocolumninfo", coverNull(autocolumninfo));
		StringBuilder sb = new StringBuilder();
		for (String key : paramHt.keySet()) {
			sb.append(key).append(":=");
			sb.append(paramHt.get(key)).append("\n");
		}
		
		System.out.print(sb);
		return paramHt;
	}


	public static String unionXml(String desct, String source)
			throws DocumentException, UnsupportedEncodingException {

		desct = URLDecoder.decode(desct, "UTF-8").replaceFirst("\n", "")
				.replaceFirst("\r", "");
		if (desct.trim().equals(""))
			return source;
		Document doc1 = DocumentHelper.parseText(desct);
		Element condifields = doc1.getRootElement().element("condifields");
		Element root1 = condifields.element("condifield");
		source = URLDecoder.decode(source, "UTF-8").replaceFirst("\n", "")
				.replaceFirst("\r", "");
		if (source.trim().equals(""))
			return desct;
		Document doc2 = DocumentHelper.parseText(source);
		List<Element> condifields2 = doc2.getRootElement().element("condifields").elements("condifield");
		for (int i = 0; i < condifields2.size(); i++) {
			Element root2 = condifields2.get(i);
			if (i == 0) {
				for (int j = 0; j < root2.elements().size(); j++) {
					Element ele = (Element) root2.elements().get(j);
					Element ele2 = root1.addElement(ele.getName());
					ele2.setText(ele.getText().toString());
					ele2.addAttribute("type", ele.attributeValue("type"));
				}
			} else {
				condifields.add(root2);
			}

		}

		String retStr = doc1.getRootElement().asXML().toString();
		return URLEncoder.encode(retStr, "UTF-8");
	}

	public static String parseReturnfieldlist(String returnfieldlist)
			throws Exception {
		if (returnfieldlist != null && returnfieldlist != "") {
			String[] fieldArr = returnfieldlist.split(",");
			returnfieldlist = "";
			for (int i = 0; i < fieldArr.length; i++) {
				String[] fieldInfo = fieldArr[i].split(":");
				String fieldName = fieldInfo[0];
				returnfieldlist += fieldName + ",";
			}
			returnfieldlist = returnfieldlist.substring(0,
					returnfieldlist.length() - 1);
		}
		return returnfieldlist;
	}

	/**
	 * 解析树条件,加多了集团的过滤
	 * 
	 * @param treeCondi
	 * @return
	 * @throws Exception
	 */
	public static String parseTreeCondi(String treeCondi) throws Exception {
		if (!treeCondi.equals("")) {
			String[] tc = treeCondi.split(":");
			String opera = ConverterUtils.getENOperatorValueByCNValue(tc[1]);
			String stc = tc[2];
			if ("##".equals(stc)) {
				return "";
			} else {
				String[] stcArr = stc.split("\\*");
				int index = stc.indexOf("*");
				Document doc = DocumentHelper.parseText("<root></root>");
				Element root = doc.getRootElement();
				Element conNode = null;
				if (stcArr.length == 2 && index >= 0) {// 包含集团树，并且此节点有集团信息业务信息,形式为：集团code*业务code
					if (tc[0].indexOf("+") > 0) {
						String newStr = ((String) tc[0]).replaceAll("\\+", "加");
						ConverterUtils.addNewTreeCondiElement(root, conNode,
								newStr, stcArr[1], opera, tc[0]);
						// ConverterUtils.addNewTreeCondiElement(root, conNode,
						// "fieldname", stcArr[1], opera, tc[0]);
					} else
						ConverterUtils.addNewTreeCondiElement(root, conNode,
                                tc[0], stcArr[1], opera, tc[0]);

					ConverterUtils.addNewTreeCondiElement(root, conNode,
							"CORPCODE", stcArr[0], opera, "CORPCODE");
				} else if (stcArr.length == 1 && index >= 0) {// 包含集团树，并且此节点只有集团信息
					ConverterUtils.addNewTreeCondiElement(root, conNode,
							"CORPCODE", stcArr[0], "Llike", "CORPCODE");
				} else if (stcArr.length == 1 && index < 0) {// 不包含集团树的情况
					if (tc[0].indexOf("+") > 0) {
						String newStr = ((String) tc[0]).replaceAll("\\+", "加");
						ConverterUtils.addNewTreeCondiElement(root, conNode,
								newStr, stcArr[0], opera, newStr);
					} else
						ConverterUtils.addNewTreeCondiElement(root, conNode,
								tc[0], stcArr[0], opera, tc[0]);
				}
				return root.asXML().replaceAll("\n", "");
			}
		}
		return "";
	}

	public static String URLDecode(String str)
			throws UnsupportedEncodingException {
		if (str == null || str.length() <= 0) {
			return "";
		}
		return URLDecoder.decode(URLDecoder.decode(str, "UTF-8"), "UTF-8");
	}

	/**
	 * 从request中取出所有参数,以HashMap的形式返回
	 * 
	 * @param request
	 * @return
	 */
	public static HashMap<String, String> getParamsMap(
			HttpServletRequest request) {
		Enumeration names = request.getParameterNames();
		HashMap<String, String> paramsMap = new HashMap<String, String>();
		while (names.hasMoreElements()) {
			String name = (String) names.nextElement();
			String value = (String) request.getParameter(name);
			paramsMap.put(name, coverNull(value));
		}
		return paramsMap;
	}

	public static String coverNull(String str) {
		if (str == null || "undefined".equals(str)) {
			str = "";
		}
		return str;
	}
}
