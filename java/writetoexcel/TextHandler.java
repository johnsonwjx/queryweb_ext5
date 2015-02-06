package writetoexcel;

import org.dom4j.Element;
import youngfriend.common.util.StringUtils;

import java.io.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TextHandler implements IExportHander {
	private Map<GridColumn, String> bottomColumnsAndWdith = new LinkedHashMap<GridColumn, String>();
	private String title;
	private String encoding;
	private BufferedWriter bw;
	private File file;
	private static final String PX1_00 = "          ";

	public TextHandler(GridColumn[] columns, String title, File file) {
		this.title = title;
		this.file = file;
		List<GridColumn> bottomColumns = GridUtils.initBottomColumns(columns);
		for (GridColumn c : bottomColumns) {
			String widthStr = c.getWidth();
			if (StringUtils.isNumberString(widthStr)) {
				widthStr = getWidth(Integer.parseInt(widthStr));
			} else {
				widthStr = PX1_00;
			}
			bottomColumnsAndWdith.put(c, widthStr);
		}

	}

	private String getWidth(int parseInt) {
		int temp = parseInt / 100;
		if (temp == 0) {
			return PX1_00;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < temp; i++) {
			sb.append(PX1_00);
		}
		return sb.toString();
	}

	public void readDatas(List<Element> datas) throws Exception {
		if (datas == null || datas.isEmpty()) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < datas.size(); i++) {
			Element data = datas.get(i);
			if (bw == null) {
				encoding = data.getDocument().getXMLEncoding();
				bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), encoding));
				title = title + "\n";
				bw.write(title, 0, title.length());
				setHeaders();
			}
			for (GridColumn col : bottomColumnsAndWdith.keySet()) {
				String widthStr = bottomColumnsAndWdith.get(col);
				String dataIndex = col.getDataIndex();
				if (StringUtils.nullOrBlank(dataIndex)) {
					continue;
				}
				Element item = data.element(dataIndex.toLowerCase());
				if (item == null) {
					item=data.element(dataIndex.toUpperCase());
					if(item==null){
						continue;
					}
				}
				String value = item.getText();
				if (StringUtils.nullOrBlank(value)) {
					sb.append(widthStr);
					continue;
				}
				value = GridUtils.getNiewValueFromMaping(col, value);

				if (!"string".equalsIgnoreCase(col.getType())) {
					BigDecimal bd;
					if (StringUtils.isNumberString(value)) {
						bd = new BigDecimal(value);
					} else {
						bd = new BigDecimal(0);
					}
					String format = col.getFormat();
					if (!StringUtils.nullOrBlank(format)) {
						int scale = format.lastIndexOf(".");
						if (scale > 0) {
							scale = format.length() - scale - 1;
						}
						// 表示四舍五入，可以选择其他舍值方式，例如去尾，等等.
						bd.setScale(scale, BigDecimal.ROUND_HALF_DOWN);
					}
					if ("true".equalsIgnoreCase(col.getShowAmount())) {
						// "_ "表示生成的Excel的单元格是数值类型,注意:_后的空格不能少
						format = "#,###.00";
						DecimalFormat df = new DecimalFormat(format);
						value = df.format(bd.doubleValue());
					} else {
						if ("true".equals(col.getShowMyria())) {
							bd = bd.divide(new BigDecimal(1000));
						}
						value = bd.intValue() + "";
					}
				}
				if (value.length() >= widthStr.length()) {
					sb.append(value).append(" ");
				} else {
					sb.append(value).append(widthStr.substring(value.length() - 1));
				}
			}
			sb.append("\n");
		}
		bw.write(sb.toString(), 0, sb.length());
	}

	private void setHeaders() throws IOException {
		StringBuffer sb = new StringBuffer();
		for (GridColumn col : bottomColumnsAndWdith.keySet()) {
			String widthStr = bottomColumnsAndWdith.get(col);
			String header = col.getText();
			if (header.length() > widthStr.length()) {
				sb.append(header).append(" ");
			} else {
				sb.append(header).append(widthStr.substring(header.length() - 1));
			}

		}
		sb.append("\n");
		bw.write(sb.toString(), 0, sb.length());
	}

	public void writeData() throws Exception {
		bw.flush();
		bw.close();
	}

	public void setSumRecord(Element sumEle, String sumDescDisplayField) throws Exception {
		if (sumEle == null) {
			return;
		}
		List<Element> fielddata = sumEle.elements();
		StringBuilder sb = new StringBuilder();
		for (GridColumn col : bottomColumnsAndWdith.keySet()) {
			String width = bottomColumnsAndWdith.get(col);
			String dataIndex = col.getDataIndex();
			if (StringUtils.nullOrBlank(dataIndex)) {
				sb.append(width);
				continue;
			}
			if (dataIndex.equalsIgnoreCase(sumDescDisplayField)) {
				sb.append("总计").append(width.substring(2));
				continue;
			}
			boolean setValue = false;
			if (!fielddata.isEmpty()) {
				for (Element field : fielddata) {
					String filedName = field.elementText("fieldname");
					if (dataIndex.equalsIgnoreCase(filedName)) {
						String value = GridUtils.getNiewValueFromMaping(col, field.elementText("fieldvalue"));
						if (value.length() > width.length()) {
							sb.append(value).append(" ");
						} else {
							sb.append(value).append(width.substring(value.length() - 1));
						}
						setValue = true;
						break;
					}
				}

			}
			if (!setValue) {
				sb.append(width);
			}
		}
		sb.append("\n");
		bw.write(sb.toString(), 0, sb.length());
	}


    public BufferedWriter getBw() {
		return bw;
	}

}
