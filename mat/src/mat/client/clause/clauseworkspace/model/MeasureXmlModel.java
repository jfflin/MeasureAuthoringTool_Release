package mat.client.clause.clauseworkspace.model;

import com.google.gwt.user.client.rpc.IsSerializable;

public class MeasureXmlModel implements IsSerializable{
	
	private String meausreExportId;
	
	private String measureId;
	
	private String xml;
	
	private String toReplaceNode;
	
	private String parentNode;

	

	/**
	 * @return the meausreExportId
	 */
	public String getMeausreExportId() {
		return meausreExportId;
	}

	/**
	 * @param meausreExportId the meausreExportId to set
	 */
	public void setMeausreExportId(String meausreExportId) {
		this.meausreExportId = meausreExportId;
	}

	/**
	 * @return the measureId
	 */
	public String getMeasureId() {
		return measureId;
	}

	/**
	 * @param measureId the measureId to set
	 */
	public void setMeasureId(String measureId) {
		this.measureId = measureId;
	}

	/**
	 * @return the xml
	 */
	public String getXml() {
		return xml;
	}

	/**
	 * @param xml the xml to set
	 */
	public void setXml(String xml) {
		this.xml = xml;
	}
	
	public String getToReplaceNode() {
		return toReplaceNode;
	}

	public void setToReplaceNode(String toReplaceNode) {
		this.toReplaceNode = toReplaceNode;
	}

	/**
	 * @return the parentNode
	 */
	public String getParentNode() {
		return parentNode;
	}

	/**
	 * @param parentNode the parentNode to set
	 */
	public void setParentNode(String parentNode) {
		this.parentNode = parentNode;
	}

}