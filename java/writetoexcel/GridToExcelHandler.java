package writetoexcel;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.util.CellRangeAddress;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import youngfriend.common.util.net.exception.ServiceInvokerException;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * ��ҳ�е�Grid��ʽת����Excel������������
 * 
 * @since 2009-3-2
 * @author XDY
 * 
 */
public class GridToExcelHandler {

	private String filename, topic, otherTableDesc, header, setInitWidths, setColAlign, gridData, fieldTypes;
	private OutputStream os;
	private List tableDescLst = null;
	private String[][] headerArray = null;
	public final static String CSPAN_LABEL = "#cspan";
	public final static String RSPAN_LABEL = "#rspan";
	private HSSFWorkbook wb = null;
	private static final int PER_ROW_SIZE = 60000;
	// private List dataRowLst = null;
	// ��ҳ��ʽ���в�����������
	private String[] webColumnAlign = null;

	private GridToExcelHandler() {
	}

	/**
	 * ��Grid����д��Excel���
	 * 
	 * @param filename
	 *            ���ɵ�Excel���ļ�����
	 * @param topic
	 *            Excel��ı��⣨��һ�У�
	 * @param otherTableDesc
	 *            Excel�������˵������(��ʽΪ:desc1\ndesc2\n.....)
	 * @param setHeader
	 *            ����gridʱ���setHeader������ֵ
	 * @param attachHeader
	 *            ����gridʱ���attachHeader������ֵ
	 * @param setInitWidths
	 *            ����gridʱ���setInitWidths������ֵ
	 * @param setColAlign
	 *            ����gridʱ���setColAlign������ֵ
	 * @param fieldTypes
	 *            ����gridʱ���setFieldTypes������ֵ
	 * @param gridData
	 *            grid��������(��ʽΪ:r1c1\tr1c2\tr1c3\t..\nr2c1\t\r2c2\tr2c3\t...\n...
	 *            rxcy\t) r1 - ��һ�е�һ��, r1c2 - ��һ�еڶ���
	 * @param os
	 */
	public static GridToExcelHandler writeToExcel(String filename, String topic, String otherTableDesc, String setHeader, String attachHeader, String setInitWidths, String setColAlign, String fieldTypes, String gridData, OutputStream os) {

		// String header = (null2Empty(setHeader) + "\n" +
		// null2Empty(attachHeader));

		return writeToExcelWithCommonHeader(filename, topic, otherTableDesc, setHeader, setInitWidths, setColAlign, fieldTypes, gridData, os);
	}

	/**
	 * ��Grid����д��Excel���
	 * 
	 * @param filename
	 *            ���ɵ�Excel���ļ�����
	 * @param topic
	 *            Excel��ı��⣨��һ�У�
	 * @param otherTableDesc
	 *            Excel�������˵������(��ʽΪ:desc1\ndesc2\n.....)
	 * @param header
	 *            ����gridʱ���header������ֵ (ÿ���ö��ŷָ�,ÿ����\n�ָ�;
	 *            ����#cspan����Ϊ����ߵ�һ�кϲ�������Ϊ��������һ����Ԫ�������;
	 *            ����#rspan����Ϊ�������ݸ������һ�е�����һ�� )
	 * @param setInitWidths
	 *            ����gridʱ���setInitWidths������ֵ
	 * @param setColAlign
	 *            ����gridʱ���setColAlign������ֵ
	 * @param gridData
	 *            grid��������(��ʽΪ:r1c1\tr1c2\tr1c3\t..\nr2c1\t\r2c2\tr2c3\t...\n...
	 *            rxcy\t) r1c1 - ��һ�е�һ��, r1c2 - ��һ�еڶ���
	 * @param os
	 */
	public static GridToExcelHandler writeToExcelWithCommonHeader(String filename, String topic, String otherTableDesc, String header, String setInitWidths, String setColAlign, String fieldTypes, String gridData, OutputStream os) {

		GridToExcelHandler g2eHdlr = new GridToExcelHandler();
		g2eHdlr.setFilename(null2Empty(filename));
		g2eHdlr.setTopic(null2Empty(topic).replace("/", "��"));// �滻���Ϊȫ��,������ֲ�֧��
		g2eHdlr.setOtherTableDesc(null2Empty(otherTableDesc));
		g2eHdlr.setHeader(null2Empty(header));
		g2eHdlr.setSetInitWidths(null2Empty(setInitWidths));
		g2eHdlr.setSetColAlign(null2Empty(setColAlign));
		g2eHdlr.setFieldTypes(null2Empty(fieldTypes));
		g2eHdlr.setGridData(null2Empty(gridData));
		g2eHdlr.setOs(os);
		return g2eHdlr;
	}

	/**
	 * ���ɵ�excel��д���������
	 * 
	 * @param os
	 * @throws ServiceInvokerException
	 */
	public void writeToOutputStream() throws ServiceInvokerException {
		HSSFWorkbook wb = writeAction();

		try {
			wb.write(this.os);
		} catch (IOException e) {
			throw new ServiceInvokerException(getClass(), e.getMessage(), "д�����ݵ�Excel�����:" + e.toString());
		} finally {
			if (this.os != null)
				try {
					this.os.close();
				} catch (Exception e) {
					throw new ServiceInvokerException(getClass(), e.getMessage(), "�ر������ʧ��:" + e.toString());
				}
		}

	}

	/**
	 * д�������ͬһ��excel����
	 * 
	 * @param hlrs
	 * @return
	 * @throws ServiceInvokerException
	 */
	public static HSSFWorkbook writeMutiSheetAction(GridToExcelHandler[] hlrs) throws ServiceInvokerException {
		HSSFWorkbook workbook = new HSSFWorkbook();
		for (int i = 0; i < hlrs.length; i++) {
			GridToExcelHandler hlr = hlrs[i];
			if (hlr == null)
				continue;
			hlr.writeAction(workbook);
		}
		return workbook;
	}

	/**
	 * д�뵥�����excel����
	 * 
	 * @return
	 * @throws ServiceInvokerException
	 */
	public HSSFWorkbook writeAction() throws ServiceInvokerException {
		return writeAction(new HSSFWorkbook());
	}

	/**
	 * ��ʼд��
	 * 
	 * @throws ServiceInvokerException
	 */
	private HSSFWorkbook writeAction(HSSFWorkbook inwb) throws ServiceInvokerException {

		wb = inwb;
		HSSFSheet usersheet = wb.createSheet(this.topic);
		List<Element> rowDatas = null;

		setTableDescStrToLst();

		setHeaderToHeaderArray();
		int columnLen = 0;

		if (this.headerArray.length > 0)
			columnLen = (this.headerArray[0]).length;

		setDefColumnWidth(this.setInitWidths, usersheet);

		setTableTopicToXsl(columnLen, usersheet, this.topic);

		setTableDescToXsl(columnLen, usersheet, this.tableDescLst);

		int headStartRowIndex = 1 + this.tableDescLst.size();
		setHeaderToXsl(headStartRowIndex, columnLen, usersheet, this.headerArray);

		try {
			Document doc = DocumentHelper.parseText(this.gridData);
			rowDatas = doc.selectNodes("/root/a");
			int dataSize = rowDatas.size();
			int pageSize = dataSize / PER_ROW_SIZE + (dataSize % PER_ROW_SIZE == 0 ? 0 : 1);
			for (int i = 0; i < pageSize - 1; i++) {
				wb.cloneSheet(0);
			}
			int contentRowNum = 0;
			int sheetIndex = 0;
			HSSFSheet curSheet = null;
			while (contentRowNum < dataSize) {
				sheetIndex = contentRowNum / PER_ROW_SIZE;
				curSheet = wb.getSheetAt(sheetIndex);
				Element rowData = (Element) rowDatas.remove(0);
				if (rowData == null || "<a></a>".equals(rowData.getTextTrim())) {
					contentRowNum++;
					continue;
				} else {
					setGridDataXmlToXslByRow(headStartRowIndex + this.headerArray.length, curSheet, rowData, contentRowNum % PER_ROW_SIZE);
				}
				contentRowNum++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return wb;
	}

	/**
	 * XDY 2009.07.31 д�������ݣ�xml��ʽ
	 * 
	 * @param startRowIndex
	 * @param usersheet
	 * @param data
	 * @return
	 */
	private void setGridDataXmlToXslByRow(int startRowIndex, HSSFSheet usersheet, Element rowData, int contentRowNum) {
		try {
			List<Element> colLst = rowData.selectNodes("b");
			int colLen = this.headerArray[0].length < colLst.size() ? this.headerArray[0].length : colLst.size();

			for (int i = 0; i < colLen; i++) {
				String value = colLst.get(i).getTextTrim();
				String cellValue = null2Empty(value);
				setCellValueToXsl(usersheet, startRowIndex, i, contentRowNum, cellValue, getColumnAlign(this.setColAlign, i), "1");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * XDY 2009.07.31 д�������ݣ�xml��ʽ
	 * 
	 * @param startRowIndex
	 * @param usersheet
	 * @param data
	 * @return
	 */
	private void setGridDataXmlToXsl(int startRowIndex, HSSFSheet usersheet, String data) {
		// List rowLst = new ArrayList();
		if ("".equals(data))
			return;
		try {
			Document doc = DocumentHelper.parseText(data);
			List lst = doc.selectNodes("/root/a");
			for (int i = 0; i < lst.size(); i++) {
				Element ele = (Element) lst.get(i);
				if (ele == null || "<a></a>".equals(ele.getTextTrim()))
					continue;
				// List colValueLst = new ArrayList();
				List colLst = ele.selectNodes("b");
				int colLen = this.headerArray[0].length < colLst.size() ? this.headerArray[0].length : colLst.size();

				for (int j = 0; j < colLen; j++) {
					String value = ((Element) colLst.remove(0)).getTextTrim();
					String cellValue = null2Empty(value);

					setCellValueToXsl(usersheet, startRowIndex, j, i, cellValue, getColumnAlign(this.setColAlign, j), "1");
					// colValueLst.add(cellValue);
				}
				// rowLst.add(colValueLst);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// return rowLst;
	}

	/**
	 * ����Ĭ���п�
	 * 
	 * @param setInitWidths2
	 * @param usersheet
	 */
	private void setDefColumnWidth(String initWidth, HSSFSheet usersheet) {
		if ("".equals(initWidth))
			return;
		String[] arr = initWidth.split(",");
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == null || "".equals(initWidth))
				continue;
			int defWidth = 0;
			try {
				defWidth = Integer.parseInt(arr[i]);
				usersheet.setColumnWidth(i, defWidth * 30);
			} catch (Exception e) {
				usersheet.autoSizeColumn(i);
			}

		}
	}

	/**
	 * д�������(��һ��)
	 * 
	 * @param columnLen
	 * @param sheet
	 * @param topic
	 */
	private void setTableTopicToXsl(int columnLen, HSSFSheet sheet, String topic) {
		CellRangeAddress region = new CellRangeAddress(0, 0, 0, columnLen - 1);
		sheet.addMergedRegion(region);
		HSSFCell hssfCell = setCellValueToXsl(sheet, 0, 0, 0, topic, HSSFCellStyle.ALIGN_CENTER, "");

		// �Ӵֱ���
		HSSFFont font = this.wb.createFont();

		// font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		font.setFontHeightInPoints((short) 20); // ����߶�
		font.setColor(HSSFFont.COLOR_NORMAL); // ������ɫ
		font.setFontName("����"); // ����
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD); // ���
		font.setItalic(false); // �Ƿ�ʹ��б��
		HSSFCellStyle style = this.wb.createCellStyle();
		style.setFont(font);
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		hssfCell.setCellStyle(style);
	}

	/**
	 * ��������������(�ӵڶ��п�ʼ)
	 * 
	 * @param columnLen
	 * @param sheet
	 * @param tableList
	 */
	private void setTableDescToXsl(int columnLen, HSSFSheet sheet, List tableList) {
		int rowIndex = 1;
		for (Object strObj : tableList) {
			HSSFRow row = sheet.createRow(1);
			HSSFCell cell = row.createCell(1);
			CellRangeAddress region = new CellRangeAddress(rowIndex, rowIndex, 0, columnLen - 1);
			sheet.addMergedRegion(region);
			setCellValueToXsl(sheet, 0, 0, rowIndex, (String) strObj, HSSFCellStyle.ALIGN_LEFT, "");
			rowIndex++;
		}
	}

	/**
	 * ���ñ�ͷ��Ϣ
	 * 
	 * @param startRowIndex
	 *            ��ͷ��ʼ��������
	 * @param columnLen
	 *            ��ͷ���г�
	 * @param sheet
	 * @param inHeader
	 *            ����ı�ͷ��ʼ��Ϣ
	 */
	private void setHeaderToXsl(int startRowIndex, int columnLen, HSSFSheet sheet, String[][] inHeader) {

		if (inHeader.length <= 0)
			return;
		// ������ɨ��
		int rowFrom = 0;
		int rowTo = 0;

		for (int yAxis = 0; yAxis < inHeader.length; yAxis++) {
			rowTo = yAxis;
			rowFrom = yAxis;
			int columnTo = 0;
			int columnFrom = 0;
			for (int xAxis = 0; xAxis < columnLen; xAxis++) {

				String cellVal = inHeader[yAxis][xAxis];

				HSSFCell hssfCell = setCellValueToXsl(sheet, startRowIndex, xAxis, yAxis, cellVal, HSSFCellStyle.ALIGN_CENTER, "");
				// ���ñ�ͷ����ɫ
				HSSFCellStyle style = this.wb.createCellStyle();
				style.setFillForegroundColor(HSSFColor.SKY_BLUE.index);
				style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
				style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
				style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
				style.setBorderRight(HSSFCellStyle.BORDER_THIN);
				style.setBorderTop(HSSFCellStyle.BORDER_THIN);
				style.setBottomBorderColor(HSSFColor.BLACK.index);
				style.setLeftBorderColor(HSSFColor.BLACK.index);
				style.setRightBorderColor(HSSFColor.BLACK.index);
				style.setBottomBorderColor(HSSFColor.BLACK.index);
				// ��������
				style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
				style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
				hssfCell.setCellStyle(style);

				columnTo = xAxis;
				if ("".equals(cellVal)) {
					columnFrom = xAxis + 1;
					continue;
				}
				if (xAxis + 1 > columnLen)
					continue;
				if (xAxis + 1 < columnLen && CSPAN_LABEL.equals(inHeader[yAxis][xAxis + 1])) {
					continue;
				}
				rowTo = scanYAxis(inHeader.length, yAxis, xAxis, inHeader);
				setMergedRegion(startRowIndex, sheet, inHeader, rowFrom, rowTo, columnFrom, columnTo);
				columnFrom = xAxis + 1;
				cellVal = inHeader[yAxis][xAxis];

			}
		}
	}

	/**
	 * �ѵ�Ԫ���ֵ����excel��
	 * 
	 * @param sheet
	 * @param startRowIndex
	 * @param xAxis
	 * @param yAxis
	 * @param cellVal
	 */
	private HSSFCell setCellValueToXsl(HSSFSheet sheet, int startRowIndex, int xAxis, int yAxis, String cellVal, int style, String flag) {
		String[] fieldTypeArr = this.fieldTypes.split(",");// �ֶ����ͣ���������Ԫ���ʽ
		int rowIndex = startRowIndex + yAxis;
		if (rowIndex == 2851) {
			System.out.println(1);
		}
		HSSFRow row = sheet.getRow(rowIndex);
		if (row == null) {
			row = sheet.createRow(rowIndex);
		}
		HSSFCell cell = row.getCell(xAxis);
		if (cell == null) {
			cell = row.createCell(xAxis);
		}

		if (!"".equals(flag)) {// flag���ȿ�,˵������ʽ����(������),ֻ������������������ͺ��ַ���
			String fieldtype = fieldTypeArr[xAxis];
			String[] tempArr = fieldtype.split("\\*");
			String deci = "";
			if (fieldtype.split("\\*").length == 2) {
				fieldtype = tempArr[0];
				deci = tempArr[1];
			}

			if ("N".equals(fieldtype)) {
				String format = ".";
				if (!CSPAN_LABEL.equals(cellVal) && !RSPAN_LABEL.equals(cellVal)) {
					if ("".equals(cellVal) || cellVal == null) {
						cell.setCellValue("");
					} else {
						try {
							BigDecimal bd = new BigDecimal(cellVal);
							for (int i = 0; i < Integer.parseInt(deci); i++) {
								format += "0";
							}
							int scale = Integer.parseInt(deci);// ����λ��
							int roundingMode = 4;// ��ʾ�������룬����ѡ��������ֵ��ʽ������ȥβ���ȵ�.
							bd = bd.setScale(scale, roundingMode);
							if (".".equals(format))
								format = "";
							format = "#,##0" + format;// ��ǧ��λ�ĸ�ʽ
							HSSFCellStyle cellStyle = wb.createCellStyle();
							// format = "0"+format;//û��ǧ��λ�ĸ�ʽ

							// ȱ�� ����ȻExcel��Ԫ����ʾ������ֵ��ʽ,����ʵ���ϲ��ǣ����ܽ��л��ܵȲ���
							// DecimalFormat decformat = new
							// DecimalFormat(format);
							// cell.setCellValue(decformat.format(bd));
							// cell.setCellType(HSSFCell.CELL_TYPE_STRING);//�ı䵥Ԫ���ʽ

							// ȱ�� : �ᵼ��һЩ��������ִ����ÿ�ѧ��������ʾ
							cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
							// "_ "��ʾ���ɵ�Excel�ĵ�Ԫ������ֵ����,ע��:_��Ŀո�����
							short df = wb.createDataFormat().getFormat(format + "_ ");
							cellStyle.setDataFormat(df);
							cellStyle.setAlignment((short) style);
							cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
							cell.setCellStyle(cellStyle);
							cell.setCellValue(bd.doubleValue());
						} catch (Exception e) {
							cell.setCellValue(cellVal);
						}
					}
				}
			} else {
				if (!CSPAN_LABEL.equals(cellVal) && !RSPAN_LABEL.equals(cellVal)) {
					cell.setCellValue(new HSSFRichTextString(cellVal));
				}
				HSSFCellStyle cellStyle = cell.getCellStyle();
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);
				cellStyle.setAlignment((short) style);
				cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
			}
		} else {// flag���ڿգ�˵���Ǳ��������˵����Ϣ
			cell.setCellValue(new HSSFRichTextString(cellVal));
		}
		return cell;
	}

	/**
	 * ���úϲ���Ԫ��
	 * 
	 * @param startRowIndex
	 * @param sheet
	 * @param inHeader
	 * @param rowFrom
	 * @param rowTo
	 * @param columnFrom
	 * @param columnTo
	 */
	private void setMergedRegion(int startRowIndex, HSSFSheet sheet, String[][] inHeader, int rowFrom, int rowTo, int columnFrom, int columnTo) {
		CellRangeAddress region = new CellRangeAddress(rowFrom + startRowIndex, rowTo + startRowIndex, columnFrom, columnTo);
		sheet.addMergedRegion(region);
		for (int x = columnFrom; x <= columnTo; x++) {
			for (int y = rowFrom; y <= rowTo; y++) {
				String cellValue = inHeader[y][x];
				if (CSPAN_LABEL.equals(cellValue) || RSPAN_LABEL.equals(cellValue))
					inHeader[y][x] = "";
			}
		}

	}

	/**
	 * ����ɨ��������ĺϲ���Ԫ��������
	 * 
	 * @param rowIndex
	 * @param columnLen
	 * @param columnFrom
	 * @param inHeader
	 * @return
	 */
	private int scanYAxis(int rowLen, int rowIndex, int columnIndex, String[][] inHeader) {
		int rowTo = rowLen - 1;
		for (int i = rowIndex + 1; i < rowLen; i++) {
			String cellValue = inHeader[i][columnIndex];
			if (RSPAN_LABEL.equals(cellValue)) {
				continue;
			} else {
				return i - 1;
			}
		}
		return rowTo;
	}

	/**
	 * ���ñ��Ļ�����������List
	 */
	private void setTableDescStrToLst() {

		this.tableDescLst = new ArrayList();

		String[] arr = this.otherTableDesc.split("\n");

		if (arr == null || arr.length <= 0)
			return;

		for (int i = 0; i < arr.length; i++) {
			String value = arr[i];
			if (value == null || "".equals(value))
				continue;
			tableDescLst.add(value);
		}
	}

	/**
	 * ���ñ�ͷ��ά��
	 * 
	 * @throws ServiceInvokerException
	 */
	private void setHeaderToHeaderArray() throws ServiceInvokerException {

		String[] rowArr = this.header.replaceAll(",", "\t").split("\n");

		if (rowArr == null || rowArr.length <= 0)
			return;

		int rowLen = rowArr.length;
		int colLen = 0;

		this.headerArray = new String[rowLen][];

		String[][] cube = this.headerArray;

		for (int i = 0; i < rowLen; i++) {
			String rowVal = rowArr[i];
			if (rowVal == null || "".equals(rowVal))
				continue;
			String[] colArr = rowVal.split("\t");
			if (colArr == null || colArr.length <= 0)
				continue;
			colLen = colArr.length;
			cube[i] = new String[colLen];
			for (int j = 0; j < colLen; j++) {
				String cellVal = colArr[j];
				cube[i][j] = null2Empty(cellVal);
			}
		}

	}

	/**
	 * ���ص�Ԫ�񲼾�
	 * 
	 * @param initColAlign
	 * @param index
	 * @return
	 */
	private int getColumnAlign(String initColAlign, int index) {
		int result = HSSFCellStyle.ALIGN_LEFT;
		if ("".equals(initColAlign))
			return result;
		if (this.webColumnAlign == null) {
			this.webColumnAlign = initColAlign.split(",");
		}
		if (index < 0 || index > this.webColumnAlign.length)
			return result;
		String align = null2Empty(this.webColumnAlign[index]).toLowerCase();
		if ("left".equals(align)) {
			return HSSFCellStyle.ALIGN_LEFT;
		} else if ("center".equals(align)) {
			return HSSFCellStyle.ALIGN_CENTER;
		} else if ("right".equals(align)) {
			return HSSFCellStyle.ALIGN_RIGHT;
		}

		return result;
	}

	/**
	 * nullת���ɿ��ַ���
	 * 
	 * @param str
	 * @return
	 */
	private static String null2Empty(String str) {
		return str == null ? "" : str;
	}

	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @param filename
	 *            the filename to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * @return the topic
	 */
	public String getTopic() {
		return topic;
	}

	/**
	 * @param topic
	 *            the topic to set
	 */
	public void setTopic(String topic) {
		this.topic = topic;
	}

	/**
	 * @return the otherTableDesc
	 */
	public String getOtherTableDesc() {
		return otherTableDesc;
	}

	/**
	 * @param otherTableDesc
	 *            the otherTableDesc to set
	 */
	public void setOtherTableDesc(String otherTableDesc) {
		this.otherTableDesc = otherTableDesc;
	}

	/**
	 * @return the header
	 */
	public String getHeader() {
		return header;
	}

	/**
	 * @param header
	 *            the header to set
	 */
	public void setHeader(String header) {
		this.header = header;
	}

	/**
	 * @return the setInitWidths
	 */
	public String getSetInitWidths() {
		return setInitWidths;
	}

	/**
	 * @param setInitWidths
	 *            the setInitWidths to set
	 */
	public void setSetInitWidths(String setInitWidths) {
		this.setInitWidths = setInitWidths;
	}

	/**
	 * @return the setColAlign
	 */
	public String getSetColAlign() {
		return setColAlign;
	}

	/**
	 * @param setColAlign
	 *            the setColAlign to set
	 */
	public void setSetColAlign(String setColAlign) {
		this.setColAlign = setColAlign;
	}

	/**
	 * @return the gridData
	 */
	public String getGridData() {
		return gridData;
	}

	/**
	 * @param gridData
	 *            the gridData to set
	 */
	public void setGridData(String gridData) {
		this.gridData = gridData;
	}

	/**
	 * @return the os
	 */
	public OutputStream getOs() {
		return os;
	}

	/**
	 * @param os
	 *            the os to set
	 */
	public void setOs(OutputStream os) {
		this.os = os;
	}

	public String getFieldTypes() {
		return fieldTypes;
	}

	public void setFieldTypes(String fieldTypes) {
		this.fieldTypes = fieldTypes;
	}
}