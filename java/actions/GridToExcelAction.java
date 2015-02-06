package actions;

import com.google.gson.Gson;
import logic.PubFunc;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import writetoexcel.*;
import youngfriend.common.util.StringUtils;
import youngfriend.common.util.net.ServiceInvokerUtil;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

/**
 * 
 * @author XDY
 * @created 2010-2-22
 */
public class GridToExcelAction extends DispatchAction {
	private final int RECORD_COUNT = 500;// 每次获取的记录条数
	private static final String TEMPDIR = "business/storemanager/customquery2/exceltempdata";

	/**
	 * 从临时文件读取Excel数据
	 * 
	 * @param mapping
	 * @param actionForm
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public void saveExcelData(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String uuid = (String) request.getParameter("uuid");
		if (uuid == null) {
			String strDirPath = this.getServlet().getServletContext().getRealPath(TEMPDIR);
			String accID = (String) request.getSession().getAttribute("sysAccessID");
			String uesrId = getUserIdByAccessId(accID);
			String fileName = (String) request.getParameter("filename");
			String txtfileflag = (String) request.getParameter("txtfileflag");
			if (txtfileflag == null)
				txtfileflag = "0";
			String filetype = ".xls";
			if (txtfileflag.equals("1"))
				filetype = ".txt";
			OutputStream os = response.getOutputStream();
			response.reset();
			response.setHeader("Content-disposition", "attachment; filename=" + fileName + filetype);
			response.setContentType("application/vnd.ms-excel");// 定义输出类型

			FileInputStream from = null;
			try {
				from = new FileInputStream(strDirPath + "/" + uesrId + filetype);
				byte[] buffer = new byte[4096];
				int bytesRead;

				while ((bytesRead = from.read(buffer)) != -1)
					os.write(buffer, 0, bytesRead); // write
			} finally {
				if (from != null)
					from.close();
				File f = new File(strDirPath, uesrId + filetype);
				try {
					if (f.exists() && f.isFile()) {
						f.delete();
					}
				} catch (Exception e) {
					throw new Exception("删除文件[" + uesrId + filetype + "]失败");
				}
			}
		} else {
			String title = (String) request.getParameter("title");
			String fileType = (String) request.getParameter("fileType");
			OutputStream os = response.getOutputStream();
			response.reset();
			response.setHeader("Content-disposition", "attachment; filename=" + title + "." + fileType);
			response.setContentType("application/vnd.ms-excel");// 定义输出类型
			String path = this.getServlet().getServletContext().getRealPath(TEMPDIR) + File.separator + uuid + "." + fileType;
			FileInputStream from = null;
			File file = null;
			try {
				file = new File(path);
				if (!file.exists()) {
					return;
				}
				from = new FileInputStream(path);
				byte[] buffer = new byte[4096];
				int bytesRead;
				while ((bytesRead = from.read(buffer)) != -1) {
					os.write(buffer, 0, bytesRead);
				}
			} finally {
				try {
					if (from != null)
						from.close();
					if (file.exists() && file.isFile()) {
						file.delete();
					}
				} catch (Exception e) {
				}
			}
		}

	}

	/**
	 * 把表格数据生成到Excel中，并存于临时文件夹中
	 * 
	 * @param mapping
	 * @param actionForm
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public void exportGridToExcel(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
		IExportHander hander = null;
		try {
			// 获取程序的全web路径
			// 创建文件夹
			String strDirPath = this.getServlet().getServletContext().getRealPath(TEMPDIR);
			File f = new File(strDirPath);
			if (!f.exists()) {
				f.mkdir();
			}
			String columnStr = request.getParameter("columns");
			GridColumn[] columns = new Gson().fromJson(columnStr, GridColumn[].class);
			String title = (String) request.getParameter("title");
			String fileType = (String) request.getParameter("fileType");
			if (fileType == null)
				fileType = "xls";

			int total = -1;
			int sumPage = 1;
			int pageSize = -1;

			Hashtable<String, String> paramHt = PubFunc.getQueryParams(request);
			String customCondi = request.getParameter("customCondi");
			// 多选，选中哪个导出哪个
			if (!StringUtils.nullOrBlank(customCondi)) {
				customCondi = java.net.URLDecoder.decode(customCondi, "UTF-8");
				paramHt.put("conditionXML", "BASE64ENCODING" + customCondi);
				paramHt.put("treeConditionXml", "");
				paramHt.put("curNode", "");
				paramHt.put("customInputCondi", "");
				paramHt.put("flag", "0");
			} else {
                paramHt.put("flag", "1");
				String totalStr = request.getParameter("total");
				if (StringUtils.isNumberString(totalStr)) {
					total = Integer.parseInt(totalStr);
					sumPage = total / RECORD_COUNT + (total % RECORD_COUNT == 0 ? 0 : 1);
				}
				pageSize = RECORD_COUNT;
			}
			paramHt.put("isToExcel", "1");

			String SumDescDisplayField = request.getParameter("SumDescDisplayField");

			String uuid = UUID.randomUUID().toString();
			if ("txt".equals(fileType)) {
				hander = new TextHandler(columns, title, new File(f, uuid + ".txt"));
			} else {
				hander = new ExcelHandler(columns, title, new File(f, uuid + ".xls"));
			}
			boolean needSum = !StringUtils.nullOrBlank(SumDescDisplayField);
			Element sumEle = null;
			for (int page = 1; page <= sumPage; page++) {
				paramHt.put("page", page + "");
				paramHt.put("pageSize", pageSize + "");
				Hashtable resultData = ServiceInvokerUtil.invoker(paramHt);
				String xml = (String) resultData.get("XML");
				if (xml.indexOf("records") > 0) {
                    break ;
				}else{
                    xml = xml.replaceAll("0x0d", "").replaceAll("0x0a", "").replaceAll("&#2;", "").replaceAll("&#2", "");
                    Document doc = DocumentHelper.parseText(xml);
                    List<Element> datas = doc.selectNodes("//querydata");
                    if (datas == null || datas.isEmpty()) {
                        continue;
                    }
                    if (needSum && sumEle == null) {
                        sumEle = (Element) doc.selectSingleNode("//sumdata");
                    }
                    hander.readDatas(datas);
                    if (page == -1) {
                        break;
                    }
                }
			}
			if (needSum && sumEle != null) {
				hander.setSumRecord(sumEle, SumDescDisplayField);
			}
			hander.writeData();
			response.getWriter().write(uuid);
		} catch (Exception e) {
			e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().write("errorMessage:" + e.getMessage());
		} finally {
			if (hander instanceof TextHandler) {
				TextHandler txtHander = (TextHandler) hander;
				BufferedWriter bw = txtHander.getBw();
				if (bw != null) {
					bw.close();
				}
			}
		}
	}

	public void exportCDBWToExcel(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String strDirPath = this.getServlet().getServletContext().getRealPath(TEMPDIR);

		String accID = (String) request.getSession().getAttribute("sysAccessID");
		String uesrId = getUserIdByAccessId(accID);

		String QueryTitle = request.getParameter("QueryTitle");
		if (null == QueryTitle)
			QueryTitle = "";
		QueryTitle = java.net.URLDecoder.decode(QueryTitle, "UTF-8");
		QueryTitle = java.net.URLDecoder.decode(QueryTitle, "UTF-8");

		String header = request.getParameter("colHeader");
		if (null == header)
			header = "";
		header = java.net.URLDecoder.decode(header, "UTF-8");
		header = java.net.URLDecoder.decode(header, "UTF-8");

		String colWidth = request.getParameter("colWidth");
		if (null == colWidth)
			colWidth = "";
		colWidth = java.net.URLDecoder.decode(colWidth, "UTF-8");
		colWidth = java.net.URLDecoder.decode(colWidth, "UTF-8");

		String colAlign = request.getParameter("colAlign");
		if (null == colAlign)
			colAlign = "";
		colAlign = java.net.URLDecoder.decode(colAlign, "UTF-8");
		colAlign = java.net.URLDecoder.decode(colAlign, "UTF-8");

		String fieldTypes = request.getParameter("fieldTypes");
		if (null == fieldTypes)
			fieldTypes = "";
		fieldTypes = java.net.URLDecoder.decode(fieldTypes, "UTF-8");
		fieldTypes = java.net.URLDecoder.decode(fieldTypes, "UTF-8");

		String fieldNameStr = request.getParameter("fieldNameStr");
		if (null == fieldNameStr)
			fieldNameStr = "";
		fieldNameStr = java.net.URLDecoder.decode(fieldNameStr, "UTF-8");
		fieldNameStr = java.net.URLDecoder.decode(fieldNameStr, "UTF-8");

		String otherDesc = request.getParameter("otherDesc");
		if (null == otherDesc)
			otherDesc = "";
		otherDesc = java.net.URLDecoder.decode(otherDesc, "UTF-8");
		otherDesc = java.net.URLDecoder.decode(otherDesc, "UTF-8");

		List fieldNameLstFormTZ = new ArrayList();
		List fieldNameLst = new ArrayList();
		String[] fs = fieldNameStr.split(",");
		for (int i = 0; i < fs.length; i++)
			fieldNameLst.add(fs[i]);

		StringBuffer fieldLableSb = new StringBuffer();
		StringBuffer fieldAlignSb = new StringBuffer();
		StringBuffer fieldTypeSb = new StringBuffer();
		StringBuffer fieldWidthSb = new StringBuffer();

		String griddata = request.getParameter("gridData");
		if (null == griddata)
			QueryTitle = "";
		griddata = java.net.URLDecoder.decode(griddata, "UTF-8");
		griddata = java.net.URLDecoder.decode(griddata, "UTF-8");

		Document doc = DocumentHelper.parseText(griddata);
		StringBuffer sb = new StringBuffer();
		sb.append("<root>");
		for (int i = 0; i < doc.getRootElement().elements().size(); i++) {
			Element ele = (Element) doc.getRootElement().elements().get(i);
			sb.append("<a>");

			for (int j = 0; j < ele.elements().size(); j++) {
				Element ele2 = (Element) ele.elements().get(j);
				String fieldName = ele2.getName();

				if (fieldNameLst.indexOf(fieldName) > -1)
					sb.append("<b>").append("<![CDATA[" + ele2.getText() + "]]>").append("</b>");
			}
			sb.append("</a>");
			if (i > 0)
				sb.append("\n");
		}
		sb.append("</root>");
		griddata = sb.toString();
		String initWidths = colWidth;

		FileOutputStream fileOut = null;
		// 取得输出流
		OutputStream os = response.getOutputStream();
		try {
			String topic = QueryTitle;

			// 清空输出流
			response.reset();
			// 设定输出文件头
			response.setHeader("Content-disposition", "attachment; filename=" + uesrId + ".xls");
			response.setContentType("application/vnd.ms-excel");// 定义输出类型

			// 通过基本信息建立Excel表格对象
			GridToExcelHandler handler = GridToExcelHandler.writeToExcel(topic, topic, otherDesc, header, "3333333333", initWidths, colAlign, fieldTypes, griddata, os);
			HSSFWorkbook hw = handler.writeAction();

			fileOut = new FileOutputStream(strDirPath + "/" + uesrId + ".xls");
			hw.write(fileOut);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			os.close();
		}
	}

	/**
	 * 根据系统存取ID获取用户IDb
	 * 
	 * @param accessid
	 * @return
	 * @throws Exception
	 */
	private String getUserIdByAccessId(String accessid) throws Exception {
		Hashtable<String, String> paramE = new Hashtable<String, String>();
		paramE.put("service", "useraccess.getOnlineUsers");
		paramE.put("sysAccessID", accessid);

		Hashtable result = ServiceInvokerUtil.invoker(paramE);
		String xml = (String) result.get("onlineUserData");
		Document doc = DocumentHelper.parseText(xml);
		List lst = doc.selectNodes("/root/onlineuser/userid");
		Element ele = (Element) lst.get(0);

		return ele.getTextTrim();
	}
}
