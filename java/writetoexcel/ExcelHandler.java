package writetoexcel;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.CellRangeAddress;
import org.dom4j.Element;
import youngfriend.common.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ExcelHandler implements IExportHander {
    private List<GridColumn> bottomColumns = new ArrayList<GridColumn>();
    private String title;
    private HSSFWorkbook book = new HSSFWorkbook();
    private static final int SHEET_SIZE = 60000;
    // 标题为0，内容开始行为1
    private static final int CONTENT_INDEX = 1;
    private static final int DEFAULT_COLUMN_WIDTH = 3500;
    private int dept = 1;
    private File file;

    public ExcelHandler(GridColumn[] columns, String title, File file) {
        this.file = file;
        this.title = title;
        this.bottomColumns = GridUtils.initBottomColumns(columns);
        // 得到列深度
        getMaxColumnDept();
        createSheet();
    }

    private HSSFSheet createSheet() {
        HSSFSheet sheet = book.createSheet();
        setTitle(sheet, title);
        setHeaders(sheet);
        return sheet;
    }

    private void setTitle(HSSFSheet sheet, String title2) {
        HSSFRow row = sheet.createRow(0);
        HSSFCell cell = row.createCell(0);
        CellRangeAddress region = new CellRangeAddress(0, 0, 0, bottomColumns.size() - 1);
        sheet.addMergedRegion(region);
        cell.setCellValue(new HSSFRichTextString(title2));
        // 加粗标题
        HSSFFont font = this.book.createFont();
        // font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setFontHeightInPoints((short) 20); // 字体高度`
        font.setColor(HSSFFont.COLOR_NORMAL); // 字体颜色
        font.setFontName("黑体"); // 字体
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD); // 宽度
        font.setItalic(false); // 是否使用斜体
        HSSFCellStyle style = this.book.createCellStyle();
        style.setFont(font);
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        cell.setCellStyle(style);
    }

    private void setHeaders(HSSFSheet sheet) {
        for (int i = 0; i < dept; i++) {
            createRow(sheet, CONTENT_INDEX + i, true);
        }
        int curentRowIndex = CONTENT_INDEX + dept - 1;
        for (int i = 0; i < bottomColumns.size(); i++) {
            CellStyle style = book.createCellStyle();
            GridColumn col = bottomColumns.get(i);
            short textAlign = -1;
            String textAlignStr = col.getTextAlign();
            boolean isNum = !"string".equalsIgnoreCase(col.getType());
            if (!StringUtils.nullOrBlank(textAlignStr)) {
                if ("right".equalsIgnoreCase(textAlignStr)) {
                    textAlign = HSSFCellStyle.ALIGN_RIGHT;
                } else if ("left".equalsIgnoreCase(textAlignStr)) {
                    textAlign = HSSFCellStyle.ALIGN_LEFT;
                } else {
                    textAlign = HSSFCellStyle.ALIGN_CENTER;
                }
            }
            if (!isNum) {
                if (textAlign == -1) {
                    textAlign = HSSFCellStyle.ALIGN_LEFT;
                }
            } else {
                if (textAlign == -1) {
                    textAlign = HSSFCellStyle.ALIGN_RIGHT;
                }
                if ("true".equalsIgnoreCase(col.getShowAmount())) {
                    // "_ "表示生成的Excel的单元格是数值类型,注意:_后的空格不能少
                    short df = book.createDataFormat().getFormat("#,##0.00_ ");
                    style.setDataFormat(df);
                }
            }
            style.setAlignment(textAlign);
            HSSFRow row = null;
            // 当前列==内容开始列，不需要判断又没parent
            int parentDept = getColDept(col);
            int temp = dept - parentDept;
            if (col.getParent() != null) {
                if (i == 0 || col.getParent().getColumns().get(0).equals(col)) {
                    mergeHeader(sheet, col.getParent(), curentRowIndex - temp - 1, i);
                }
            }
            if (temp > 0) {
                CellRangeAddress region = new CellRangeAddress(curentRowIndex - temp, curentRowIndex, i, i);
                sheet.addMergedRegion(region);
            }
            row = sheet.getRow(curentRowIndex - temp);
            HSSFCell cel = row.getCell(i);
            cel.setCellValue(new HSSFRichTextString(col.getText()));
            sheet.setDefaultColumnStyle(i, style);
        }
    }

    private void mergeHeader(HSSFSheet sheet, GridColumn column, int curentRowIndex, int childColumnIndex) {
        HSSFRow row = null;
        List<GridColumn> bottomGs = new ArrayList<GridColumn>();
        GridUtils.getBottomColumn(column, bottomGs);
        int parentDept = getColDept(column);
        int columnSize = bottomGs.size();
        if (column.getParent() != null) {
            if (childColumnIndex == 0 || column.getParent().getColumns().get(0).equals(column)) {
                mergeHeader(sheet, column.getParent(), curentRowIndex - 1, childColumnIndex);
            }
            CellRangeAddress region = new CellRangeAddress(curentRowIndex, curentRowIndex, childColumnIndex, childColumnIndex + columnSize - 1);
            sheet.addMergedRegion(region);
            row = sheet.getRow(curentRowIndex);
        } else {
            if (parentDept > 1 || columnSize > 1) {
                CellRangeAddress region = new CellRangeAddress(curentRowIndex, curentRowIndex, childColumnIndex, childColumnIndex + columnSize - 1);
                sheet.addMergedRegion(region);
            }
            row = sheet.getRow(curentRowIndex);
        }
        HSSFCell cel = row.getCell(childColumnIndex);
        cel.setCellValue(new HSSFRichTextString(column.getText()));
    }

    // 最大深度
    private void getMaxColumnDept() {
        for (GridColumn c : bottomColumns) {
            int tempDept = getColDept(c);
            if (tempDept > dept) {
                dept = tempDept;
            }
        }
    }

    // 一列的深的
    private int getColDept(GridColumn c) {
        int tempDept = 1;
        GridColumn temp = c;
        while (temp.getParent() != null) {
            tempDept++;
            temp = temp.getParent();
        }
        return tempDept;
    }

    private HSSFRow createRow(HSSFSheet sheet, int rownum, boolean isHeader) {
        HSSFRow row = sheet.createRow(rownum);
        for (int i = 0; i < bottomColumns.size(); i++) {
            GridColumn col = bottomColumns.get(i);
            HSSFCell cel = row.createCell(i);
            if (isHeader) {
                HSSFCellStyle style = this.book.createCellStyle();
                style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
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
                // 列名居中
                style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                cel.setCellStyle(style);
            }
            int width = DEFAULT_COLUMN_WIDTH;
            String widthStr = col.getWidth();
            if (StringUtils.isNumberString(widthStr)) {
                width = Integer.parseInt(widthStr) * 40;
            }
            sheet.setColumnWidth(i, width);
        }
        return row;
    }

    public void readDatas(List<Element> datas) {
        if (datas == null || datas.isEmpty()) {
            return;
        }
        HSSFSheet sheet = book.getSheetAt(book.getNumberOfSheets() - 1);
        for (Element data : datas) {
            if (sheet.getLastRowNum() > SHEET_SIZE) {
                sheet = createSheet();
            }
            HSSFRow row = createRow(sheet, sheet.getLastRowNum() + 1, false);
            for (int i = 0; i < bottomColumns.size(); i++) {
                GridColumn col = bottomColumns.get(i);
                String dataIndex = col.getDataIndex();
                if (StringUtils.nullOrBlank(dataIndex)) {
                    continue;
                }
                Element item = data.element(dataIndex.toLowerCase());
                if (item == null) {
                    item = data.element(dataIndex.toUpperCase());
                    if (item == null) {
                        continue;
                    }
                }
                String value = GridUtils.getNiewValueFromMaping(col, item.getText());
                HSSFCell cel = row.getCell(i);
                setCellValue(cel, col, value);
            }
        }

    }

    private void setCellValue(HSSFCell cel, GridColumn col, String value) {
        if ("string".equalsIgnoreCase(col.getType())) {
            cel.setCellValue(new HSSFRichTextString(value));
        } else {
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
                bd.setScale(scale, 4);
            }
            if ("true".equals(col.getShowMyria())) {
                bd = bd.divide(new BigDecimal(1000));
            }
            cel.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
            cel.setCellValue(bd.doubleValue());
        }

    }

    public void writeData() throws Exception {
        book.write(new FileOutputStream(file));
    }

    public void setSumRecord(Element sumEle, String SumDescDisplayField) {
        if (sumEle == null) {
            return;
        }
        // 每个sheet后面都要一个汇总信息
        for (int i = 0; i < book.getNumberOfSheets(); i++) {
            HSSFSheet sheet = book.getSheetAt(i);
            HSSFRow row = createRow(sheet, sheet.getLastRowNum() + 1, false);
            @SuppressWarnings("unchecked")
            List<Element> fielddata = sumEle.elements();
            for (int j = 0; j < bottomColumns.size(); j++) {
                GridColumn col = bottomColumns.get(j);
                HSSFCell cel = row.getCell(j);
                String dataIndex = col.getDataIndex();
                if (StringUtils.nullOrBlank(dataIndex)) {
                    continue;
                }
                if (dataIndex.equalsIgnoreCase(SumDescDisplayField)) {
                    cel.setCellValue(new HSSFRichTextString("总计"));
                    continue;
                }
                if (!fielddata.isEmpty()) {
                    for (Element field : fielddata) {
                        String filedName = field.elementText("fieldname");
                        if (dataIndex.equalsIgnoreCase(filedName)) {
                            setCellValue(cel, col, field.elementText("fieldvalue"));
                            break;
                        }
                    }

                }

            }
        }

    }
}
