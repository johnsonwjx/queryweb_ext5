package logic;

public class SelectOtherDataSource {
	private String serviceName;
	private String idParamName;
	private String condiParamName;
	private String customCondiValue;
	private String isSelectSQL;
	private String returnParamName;
	private String codeField;
	private String nameField;
	private String recordsNodeParam;
	private String recordNodeParam;
	
	public SelectOtherDataSource(String[] dataParam){
		for (int i=0;i<dataParam.length;i++){
			String paramNameAndValue = dataParam[i];
			int index = paramNameAndValue.indexOf("customCondiValue:");
			if (index == 0){//ÌØÊâ´¦Àí
				customCondiValue = paramNameAndValue.substring(index+17,paramNameAndValue.length());
				customCondiValue = customCondiValue.replaceAll(":", "=");
			}else{
				String[] str=dataParam[i].split(":");
				String value="" ;
				if(str.length>=2){
					value=str[1];
				}
				if (str[0].equals("serviceName"))
					serviceName=value; 
				else if (str[0].equals("idParamName"))
					idParamName=value;
				else if (str[0].equals("condiParamName"))
					condiParamName=value;
				else if (str[0].equals("returnParamName"))
					returnParamName=value;
				else if (str[0].equals("codeField"))
					codeField=value;
				else if (str[0].equals("nameField"))
					nameField=value;
				else if (str[0].equals("recordsNodeParam"))
					recordsNodeParam=value;
				else if (str[0].equals("recordNodeParam"))
					recordNodeParam=value;
				else if (str[0].equals("isSelectSQL"))
					isSelectSQL=value;
			}
		}
	}
	
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public String getIdParamName() {
		return idParamName;
	}
	public void setIdParamName(String idParamName) {
		this.idParamName = idParamName;
	}
	public String getCondiParamName() {
		return condiParamName;
	}
	public void setCondiParamName(String condiParamName) {
		this.condiParamName = condiParamName;
	}
	public String getReturnParamName() {
		return returnParamName;
	}
	public void setReturnParamName(String returnParamName) {
		this.returnParamName = returnParamName;
	}
	public String getCodeField() {
		return codeField;
	}
	public void setCodeField(String codeField) {
		this.codeField = codeField;
	}
	public String getNameField() {
		return nameField;
	}
	public void setNameField(String nameField) {
		this.nameField = nameField;
	}
	public String getRecordsNodeParam() {
		return recordsNodeParam;
	}
	public void setRecordsNodeParam(String recordsNodeParam) {
		this.recordsNodeParam = recordsNodeParam;
	}
	public String getRecordNodeParam() {
		return recordNodeParam;
	}
	public void setRecordNodeParam(String recordNodeParam) {
		this.recordNodeParam = recordNodeParam;
	}
	public String getCustomCondiValue() {
		return customCondiValue;
	}
	public void setCustomCondiValue(String customCondiValue) {
		this.customCondiValue = customCondiValue;
	}
	public String getIsSelectSQL() {
		return isSelectSQL;
	}
	public void setIsSelectSQL(String isSelectSQL) {
		this.isSelectSQL = isSelectSQL;
	}
	
}
