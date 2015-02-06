package writetoexcel;

import java.util.ArrayList;
import java.util.List;

public class GridColumn {
	private String text;
	private String type;
	private String align;
	private String needsum;
	private String dataIndex;
	private String width;
	private String webfont;
	private String constValue;
	private String showMyria;
	private String showAmount;
	public String getShowAmount() {
		return showAmount;
	}

	public void setShowAmount(String showAmount) {
		this.showAmount = showAmount;
	}

	private String ContentColor;
	private String BackGround;
	private String BackGroundPic;
	private String buttonName;
	private String ValueMapping;
	private String summaryType;
	private String format;
	private String textAlign;
	private GridColumn parent;

	public GridColumn getParent() {
		return parent;
	}

	public void setParent(GridColumn parent) {
		this.parent = parent;
	}

	private List<GridColumn> columns = new ArrayList<GridColumn>();

	public List<GridColumn> getColumns() {
		return columns;
	}

	public void setColumns(List<GridColumn> columns) {
		this.columns = columns;
	}

	public GridColumn() {
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getAlign() {
		return align;
	}

	public void setAlign(String align) {
		this.align = align;
	}

	public String getNeedsum() {
		return needsum;
	}

	public void setNeedsum(String needsum) {
		this.needsum = needsum;
	}

	public String getDataIndex() {
		return dataIndex;
	}

	public void setDataIndex(String dataIndex) {
		this.dataIndex = dataIndex;
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public String getWebfont() {
		return webfont;
	}

	public void setWebfont(String webfont) {
		this.webfont = webfont;
	}

	public String getConstValue() {
		return constValue;
	}

	public void setConstValue(String constValue) {
		this.constValue = constValue;
	}

	public String getShowMyria() {
		return showMyria;
	}

	public void setShowMyria(String showMyria) {
		this.showMyria = showMyria;
	}

	public String getContentColor() {
		return ContentColor;
	}

	public void setContentColor(String contentColor) {
		ContentColor = contentColor;
	}

	public String getBackGround() {
		return BackGround;
	}

	public void setBackGround(String backGround) {
		BackGround = backGround;
	}

	public String getBackGroundPic() {
		return BackGroundPic;
	}

	public void setBackGroundPic(String backGroundPic) {
		BackGroundPic = backGroundPic;
	}

	public String getButtonName() {
		return buttonName;
	}

	public void setButtonName(String buttonName) {
		this.buttonName = buttonName;
	}

	public String getValueMapping() {
		return ValueMapping;
	}

	public void setValueMapping(String valueMapping) {
		ValueMapping = valueMapping;
	}

	public String getSummaryType() {
		return summaryType;
	}

	public void setSummaryType(String summaryType) {
		this.summaryType = summaryType;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getTextAlign() {
		return textAlign;
	}

	public void setTextAlign(String textAlign) {
		this.textAlign = textAlign;
	}

}
