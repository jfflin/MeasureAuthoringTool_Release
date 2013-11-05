package mat.client.clause.clauseworkspace.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import mat.client.clause.QDSAttributesService;
import mat.client.clause.QDSAttributesServiceAsync;
import mat.client.clause.clauseworkspace.model.CellTreeNode;
import mat.client.clause.clauseworkspace.model.CellTreeNodeImpl;
import mat.client.clause.clauseworkspace.presenter.ClauseConstants;
import mat.client.clause.clauseworkspace.presenter.XmlTreeDisplay;
import mat.model.clause.QDSAttributes;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.OptionElement;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Node;

public class QDMAttributeDialogBox {
	private static final String NEGATION_RATIONALE = "negation rationale";
	private static final String INSTANCE = "instance";
	//Declare all constants
	static final String DATATYPE = "datatype";
	private static final String GREATER_THAN_OR_EQUAL_TO = "-- Greater Than Or Equal To";
	private static final String LESS_THAN_OR_EQUAL_TO = "-- Less Than Or Equal To";
	private static final String EQUAL_TO = "-- Equal To";
	private static final String GREATER_THAN = "-- Greater Than";
	private static final String LESS_THAN = "-- Less Than";
	private static final String COMPARISON = "Comparison";
	private static final String UNIT = "unit";
	private static final String COMPARISON_VALUE = "comparisonValue";
	private static final String QDM_UUID = "qdmUUID";
	private static final String ID = "id";
	private static final String MODE = "mode";
	private static final String VALUE_SET = "Value Set";
	private static final String CHECK_IF_PRESENT = "Check if Present";
	private static final String NAME = "name";
	static final String ATTRIBUTE = "attribute";
	private static final String ATTRIBUTES = "attributes";
	private static final String QDM_ATTRIBUTES_ERROR_TITLE = "QDM Attributes. Please correct the rows below in red.";
	private static final String QDM_ATTRIBUTES_TITLE = "QDM Attributes.";
	private static final String QDM_ATTRIBUTE_INVALID_ROW = "qdm_attribute_invalidRow";
	private static final String QDM_ATTRIBUTE_INVALID_ROW_RIGHT = "qdm_attribute_invalidRow_right";
	private static final String QDM_ATTRIBUTE_INVALID_ROW_LEFT = "qdm_attribute_invalidRow_left";
	private static final String SELECT = "Select";
	private static final String UUID = "uuid";
	
	private static QDSAttributesServiceAsync attributeService = (QDSAttributesServiceAsync) GWT.create(QDSAttributesService.class);
	private static final List<String> unitNames = new ArrayList<String>();
	private static final List<String> attributeList = new  ArrayList<String>();
	private static Grid grid;
	
	private static final class DeleteSelectedClickHandler implements
			ClickHandler {
		private final Grid grid;

		private DeleteSelectedClickHandler(Grid grid) {
			this.grid = grid;
		}

		@Override
		public void onClick(ClickEvent event) {
			int rowCount = grid.getRowCount();
			List<Integer> selectedRowNums = new ArrayList<Integer>();
			//collect all the row nums for checked rows.
			for(int i=0;i<rowCount;i++){
				CheckBox checkBox = (CheckBox)grid.getWidget(i, 0);
				if(checkBox.getValue()){
					selectedRowNums.add(i);
				}
			}
			//Iterate through the selected row nums backwards so that we delete the higher rows before the lower ones.
			for(int i=selectedRowNums.size()-1;i>=0;i--){
				int rowNum = selectedRowNums.get(i);
				grid.removeRow(rowNum);
			}
		}
	}

	private static final class QDMAttributeGridClickHandler implements
			ClickHandler {
		private final Grid grid;
		private int lastRow = -1;
		private int lastCol = -1;
		
		private QDMAttributeGridClickHandler(Grid grid) {
			this.grid = grid;
		}
		
		@Override
		public void onClick(ClickEvent event) {
			HTMLTable.Cell cell = grid.getCellForEvent(event);
			//This check is for clicks which are on the Grid but not necessarily on any grid row.
			//If we dont check for this, then we can have random NullPointer Exceptions in the 
			//code below.
			if(cell == null){
				return;
			}
			
			if(cell.getRowIndex() != lastRow && cell.getCellIndex() != lastCol){
				lastRow = cell.getRowIndex();
				lastCol = cell.getCellIndex();
				return;
			}
			
			Widget widget = grid.getWidget(cell.getRowIndex(), cell.getCellIndex());
			if(cell.getCellIndex() == 1){
				ListBox attributeListBox = (ListBox)widget;
				//updateAttributeListBox(grid, cell.getRowIndex(), attributeListBox);
				updateAttributeListBoxes(grid,cell.getRowIndex());
				String text = SELECT;
				if(attributeListBox.getSelectedIndex() > -1){
					text = attributeListBox.getItemText(attributeListBox.getSelectedIndex());
				}
				int rowNum = cell.getRowIndex();
				
				if(QDMAttributeDialogBox.SELECT.equalsIgnoreCase(text)){
					ListBox modeListBox = (ListBox)grid.getWidget(rowNum, 2);
					modeListBox.setSelectedIndex(0);
					modeListBox.setEnabled(false);
				}else{
					ListBox modeListBox = (ListBox)grid.getWidget(rowNum, 2);
					modeListBox.setEnabled(true);
				}
				
			}else if(cell.getCellIndex() == 2){
				ListBox modeListBox = (ListBox)widget;
								
				String text = modeListBox.getItemText(modeListBox.getSelectedIndex());
				int rowNum = cell.getRowIndex();
				
				if(QDMAttributeDialogBox.SELECT.equalsIgnoreCase(text) || CHECK_IF_PRESENT.equalsIgnoreCase(text) || COMPARISON.equalsIgnoreCase(text)){
					TextBox textBox = new TextBox();
					textBox.setEnabled(false);
					textBox.setWidth("8em");
					grid.setWidget(rowNum, 3, textBox);
				}else if(VALUE_SET.equals(text)){
					ListBox qdmListBox = createQdmListBox();
					setToolTipForEachElementInQdmListBox(qdmListBox);
					grid.setWidget(rowNum, 3, qdmListBox);
				}else {
					HorizontalPanel panel = new HorizontalPanel();
					panel.setWidth("8em");
					panel.setSpacing(0);
					
					TextBox textBox = new TextBox();
					textBox.addKeyPressHandler(new DigitsOnlyKeyPressHandler());
					textBox.setWidth("3em");
					textBox.setHeight("19");
					
					ListBox units = new ListBox(false);
					units.setVisibleItemCount(1);
					units.setWidth("5em");
					
					for(String unitName:unitNames){
						units.addItem(unitName);
					}
					setToolTipForEachElementInListbox(units);
					
					panel.add(textBox);
					panel.add(units);
					
					grid.setWidget(rowNum, 3, panel);					
				}
			}
		}
	}

	private static final class AddNewQDMAttributeClickHandler implements ClickHandler {
		private final List<String> mode;
		private final Grid grid;
		private static CellTreeNode rowNode;

		private AddNewQDMAttributeClickHandler(String qdmDataTypeName,
				List<String> mode, Grid grid) {
			this.mode = mode;
			this.grid = grid;
		}

		@Override
		public void onClick(ClickEvent event) {
			CellTreeNode node = rowNode;
			rowNode = null;
			String attributeName = "";
			if(node != null){
				attributeName = (String) node.getExtraInformation(NAME);
			}
			final ListBox attributeListBox = new ListBox(false);
			grid.resizeRows(grid.getRowCount()+1);
			final int i = grid.getRowCount()-1;
			
			CheckBox checkBox = new CheckBox();
			grid.setWidget(i, 0, checkBox);
			grid.getCellFormatter().setVerticalAlignment(i, 0, HasVerticalAlignment.ALIGN_MIDDLE);
						
			attributeListBox.setVisibleItemCount(1);
			attributeListBox.setWidth("8em");
			attributeListBox.addItem(QDMAttributeDialogBox.SELECT, ""+i);

			for(String attribName:attributeList){
				attributeListBox.addItem(attribName);
			}
			setToolTipForEachElementInListbox(attributeListBox);
			
			//Set the attribute name
			for(int j=0;j<attributeListBox.getItemCount();j++){
				if(attributeListBox.getItemText(j).equals(attributeName)){
					attributeListBox.setSelectedIndex(j);
					break;
				}
			}
			grid.setWidget(i, 1, attributeListBox);
			
			final ListBox modeListBox = new ListBox(false);
			modeListBox.setVisibleItemCount(1);
			modeListBox.setWidth("8em");
			modeListBox.addItem(QDMAttributeDialogBox.SELECT);
			for(String modeName:mode){
				String modeValue = (modeName.startsWith("--"))?modeName.substring(2).trim():modeName;
				modeListBox.addItem(modeName, modeValue);
			}
			modeListBox.setEnabled(false);
			setToolTipForEachElementInListbox(modeListBox);
			grid.setWidget(i, 2, modeListBox);
						
			TextBox textBox = new TextBox();
			textBox.setEnabled(false);
			textBox.setWidth("8em");
			grid.setWidget(i, 3, textBox);	
			
			setExitingAttributeInGrid(node,i);
			updateAttributeListBox(grid, i, attributeListBox);
		}
	}
	
	private static final class DigitsOnlyKeyPressHandler implements
	KeyPressHandler {
		@Override
		public void onKeyPress(KeyPressEvent event) {
			TextBox sender = (TextBox)event.getSource();
			if (sender.isReadOnly() || !sender.isEnabled()) {
		        return;
		    }
		
		    Character charCode = event.getCharCode();
		    int unicodeCharCode = event.getUnicodeCharCode();
		    // allow digits, and non-characters
		    if (!(Character.isDigit(charCode) || unicodeCharCode == 0 || charCode == '.')){
		       sender.cancelKey();
		    }
		}
	}

	public static void showQDMAttributeDialogBox(XmlTreeDisplay xmlTreeDisplay, CellTreeNode cellTreeNode) {
		//If the CellTreeNode type isn't CellTreeNode.ELEMENT_REF_NODE then return without doing anything.
		if(cellTreeNode.getNodeType() != CellTreeNode.ELEMENT_REF_NODE){
			return;
		}
		unitNames.clear();
		attributeList.clear();
		
		String qdmName = ClauseConstants.getElementLookUpName().get(cellTreeNode.getUUID());
		Node qdmNode = ClauseConstants.getElementLookUpNode().get(qdmName + "~" + cellTreeNode.getUUID());
		//Could not find the qdm node in elemenentLookup tag 
		if(qdmNode == null){
			return;
		}
		String qdmDataType = qdmNode.getAttributes().getNamedItem(DATATYPE).getNodeValue();
		boolean isOccuranceQDM = false;
		if(qdmNode.getAttributes().getNamedItem(INSTANCE) != null){
			isOccuranceQDM = true;
		}
//		findAttributesForDataType(qdmDataType,isOccuranceQDM);
		grid = new Grid(0,4);
		grid.addClickHandler(new QDMAttributeGridClickHandler(grid));
			
		//unitNames.addAll(getUnitNameList());
		unitNames.add("");
		unitNames.addAll(ClauseConstants.units);
		
		List<String> mode = getModeList();
		findAttributesForDataType(qdmDataType,isOccuranceQDM, mode, xmlTreeDisplay, cellTreeNode);
		//buildAndDisplayDialogBox(qdmDataType, mode,xmlTreeDisplay, cellTreeNode);
	}
	
	/**
	 * Remove all attribute names used in other rows from modeListBox.
	 * @param table
	 * @param rowIndex
	 * @param attributeListBox
	 */
	public static void updateAttributeListBox(Grid table, int rowIndex, ListBox attributeListBox) {
		for(int i=0;i<grid.getRowCount();i++){
			if(i == rowIndex){
				continue;
			}
			ListBox rowListBox = (ListBox)table.getWidget(i, 1);
			if(rowListBox.getSelectedIndex() > -1){
				String alreadySelectedAttribute = rowListBox.getItemText(rowListBox.getSelectedIndex());
				if(SELECT.equals(alreadySelectedAttribute)){
					continue;
				}
				for(int j=0;j<attributeListBox.getItemCount();j++){
					if(attributeListBox.getItemText(j).equals(alreadySelectedAttribute)){
						attributeListBox.removeItem(j);
						break;
					}
				}
			}
		}
	}
	
	public static void updateAttributeListBoxes(Grid table, int row){
		for(int i=0;i<grid.getRowCount();i++){
			if(i == row){
				continue;
			}
			ListBox rowListBox = (ListBox)table.getWidget(i, 1);
			if(rowListBox.getSelectedIndex() > -1){
				String selected = rowListBox.getItemText(rowListBox.getSelectedIndex());
				rowListBox.clear();
				rowListBox.addItem(SELECT);
				for(int g=0;g<attributeList.size();g++){
					String att = attributeList.get(g); 
					rowListBox.addItem(att);
				}
				updateAttributeListBox(grid, i, rowListBox);
				for(int j=0;j<rowListBox.getItemCount();j++){
					String item = rowListBox.getItemText(j);
					if(item.equals(selected)){
						rowListBox.setSelectedIndex(j);
						break;
					}
				}
			}
		}
	}

	private static void buildAndDisplayDialogBox(String qdmDataType,
			List<String> mode, final XmlTreeDisplay xmlTreeDisplay, final CellTreeNode cellTreeNode) {
		
		final DialogBox qdmAttributeDialogBox = new DialogBox(false,true);
		qdmAttributeDialogBox.getElement().setId("qdmAttributeDialog");
		qdmAttributeDialogBox.setGlassEnabled(true);
		qdmAttributeDialogBox.setAnimationEnabled(true);
	    qdmAttributeDialogBox.setText(QDM_ATTRIBUTES_TITLE);
	    
		// Create a table to layout the content
	    VerticalPanel dialogContents = new VerticalPanel();
	        
	    qdmAttributeDialogBox.setWidget(dialogContents);
	    
	    //Add a Add New button
	    Button addNewButton = new Button("Add New");
	    //Handler to Add New rows to the attribute table.
		addNewButton.addClickHandler(new AddNewQDMAttributeClickHandler(qdmDataType, mode, grid));
		//Add a Delete Selected button
	    Button deleteSelectedButton = new Button("Delete Selected");
	    deleteSelectedButton.addClickHandler(new DeleteSelectedClickHandler(grid));
	    
	    HorizontalPanel horizontalDeleteAddNewPanel = new HorizontalPanel();
	    horizontalDeleteAddNewPanel.setSpacing(5);
	    	    
	    horizontalDeleteAddNewPanel.add(addNewButton);
	    horizontalDeleteAddNewPanel.setCellHorizontalAlignment(addNewButton, HasHorizontalAlignment.ALIGN_LEFT);
	        
	    horizontalDeleteAddNewPanel.add(deleteSelectedButton);
	    horizontalDeleteAddNewPanel.setCellHorizontalAlignment(deleteSelectedButton, HasHorizontalAlignment.ALIGN_LEFT);
	    	    	    
	    //Add a Close button at the bottom of the dialog
	    Button closeButton = new Button("Cancel", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				qdmAttributeDialogBox.hide();		
			}
		});
	    
	    //Add a Save button at the bottom of the dialog
	    Button saveButton = new Button("OK", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				//TODO:Validate the table rows.
				if(validateRows(qdmAttributeDialogBox)){									
					saveToModel(xmlTreeDisplay);
					xmlTreeDisplay.editNode(cellTreeNode.getName(), cellTreeNode.getName());
					xmlTreeDisplay.setDirty(true);
					qdmAttributeDialogBox.hide();
				}
			}
		});
	    
	    HorizontalPanel horizontalSaveClosePanel = new HorizontalPanel();
	    horizontalSaveClosePanel.setSpacing(5);
	    
	    horizontalSaveClosePanel.add(saveButton);
	    horizontalSaveClosePanel.setCellHorizontalAlignment(saveButton, HasHorizontalAlignment.ALIGN_RIGHT);
	    
	    horizontalSaveClosePanel.add(closeButton);
	    horizontalSaveClosePanel.setCellHorizontalAlignment(closeButton, HasHorizontalAlignment.ALIGN_RIGHT);
	  	  
	    addTableToPanel(dialogContents,qdmDataType,mode,xmlTreeDisplay, cellTreeNode,deleteSelectedButton,addNewButton);
	    
	    dialogContents.add(horizontalDeleteAddNewPanel);
	    dialogContents.setCellHorizontalAlignment(horizontalDeleteAddNewPanel, HasHorizontalAlignment.ALIGN_LEFT);
	    
	    dialogContents.add(horizontalSaveClosePanel);
	    dialogContents.setCellHorizontalAlignment(horizontalSaveClosePanel, HasHorizontalAlignment.ALIGN_RIGHT);
	    
	    dialogContents.setHeight("21em");
	    qdmAttributeDialogBox.center();	    
	}
	
	protected static boolean validateRows(DialogBox qdmAttributeDialogBox) {
		int rowCount = grid.getRowCount();
		List<Integer> inValidRows = new ArrayList<Integer>();
		
		//Go through all the rows & collect all the invalid row numbers in a list 'inValidRows'
		for(int i=0;i<rowCount;i++){
			ListBox attributeListBox = ((ListBox)grid.getWidget(i, 1));
			if(!QDMAttributeDialogBox.SELECT.equals(attributeListBox.getItemText(attributeListBox.getSelectedIndex()))){
				ListBox modeListBox = ((ListBox)grid.getWidget(i, 2));
				if(!QDMAttributeDialogBox.SELECT.equals(modeListBox.getItemText(modeListBox.getSelectedIndex()))){
					String modeName = modeListBox.getValue(modeListBox.getSelectedIndex());
					if(!CHECK_IF_PRESENT.equals(modeName)){
						if(VALUE_SET.equals(modeName)){
							ListBox qdmListBox = ((ListBox)grid.getWidget(i, 3));
							if(qdmListBox.getItemCount() == 0){
								inValidRows.add(i);
								continue;
							}
							String qdmName = qdmListBox.getItemText(qdmListBox.getSelectedIndex());
							if(qdmName == null || qdmName.length() == 0){
								inValidRows.add(i);
								continue;
							}
						}else if(COMPARISON.equals(modeName)){
							inValidRows.add(i);
							continue;
						}else{
							HorizontalPanel hPanel = (HorizontalPanel) grid.getWidget(i, 3);
							TextBox valueBox = (TextBox) hPanel.getWidget(0);
							ListBox unitBox =  (ListBox) hPanel.getWidget(1);
							
							String value = valueBox.getText(); 
							if(value == null || value.length() == 0){
								inValidRows.add(i);
								continue;
							}
							/*else{
								String unit = unitBox.getItemText(unitBox.getSelectedIndex());
								if(unit == null || unit.length() == 0){
									inValidRows.add(i);
									continue;
								}
							}*/
						}
					}
				}else{
					inValidRows.add(i);
					continue;
				}
			}else{
				inValidRows.add(i);
				continue;
			}
		}		
		setGridRowStyles(qdmAttributeDialogBox, rowCount, inValidRows);
		return (inValidRows.size() == 0);
	}

	/**
	 * @param qdmAttributeDialogBox
	 * @param rowCount
	 * @param inValidRows
	 */
	private static void setGridRowStyles(DialogBox qdmAttributeDialogBox,
			int rowCount, List<Integer> inValidRows) {
		if(inValidRows.size() > 0){
			qdmAttributeDialogBox.setText(QDM_ATTRIBUTES_ERROR_TITLE);
		}else{
			qdmAttributeDialogBox.setText(QDM_ATTRIBUTES_TITLE);
		}			
		CellFormatter cellFormatter = grid.getCellFormatter();
		for(int i=0;i<rowCount;i++){
			if(inValidRows.contains(i)){
				for(int j=0;j<4;j++){
					if(j == 0){
						cellFormatter.addStyleName(i, j, QDM_ATTRIBUTE_INVALID_ROW_LEFT);
					}else if(j == 3){
						cellFormatter.addStyleName(i, j, QDM_ATTRIBUTE_INVALID_ROW_RIGHT);
					}
					cellFormatter.addStyleName(i, j, QDM_ATTRIBUTE_INVALID_ROW);
				}
			}else{
				for(int j=0;j<4;j++){
					cellFormatter.removeStyleName(i, j, QDM_ATTRIBUTE_INVALID_ROW);
					cellFormatter.removeStyleName(i, j, QDM_ATTRIBUTE_INVALID_ROW_LEFT);
					cellFormatter.removeStyleName(i, j, QDM_ATTRIBUTE_INVALID_ROW_RIGHT);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected static void saveToModel(XmlTreeDisplay xmlTreeDisplay) {
		int rowCount = grid.getRowCount();
		List<CellTreeNode> attributeList = (List<CellTreeNode>)xmlTreeDisplay.getSelectedNode().getExtraInformation(ATTRIBUTES);
		if(attributeList == null){
			attributeList = new ArrayList<CellTreeNode>();
		}
		attributeList.clear();
		
		for(int i=0;i<rowCount;i++){
			CellTreeNode attributeNode = new CellTreeNodeImpl();
			attributeNode.setName(ATTRIBUTE);
			attributeNode.setNodeType(CellTreeNode.ATTRIBUTE_NODE);
								
			ListBox attributeListBox = ((ListBox)grid.getWidget(i, 1));
			String attributeName = attributeListBox.getItemText(attributeListBox.getSelectedIndex());
			attributeNode.setExtraInformation(NAME, attributeName);
			
			ListBox modeListBox = ((ListBox)grid.getWidget(i, 2));
			//String modeName = modeListBox.getItemText(modeListBox.getSelectedIndex());
			String modeName = modeListBox.getValue(modeListBox.getSelectedIndex());
			
			if(CHECK_IF_PRESENT.equals(modeName) || VALUE_SET.equals(modeName)){
				attributeNode.setExtraInformation(MODE, modeName);
				if(VALUE_SET.equals(modeName)){
					ListBox qdmListBox = ((ListBox)grid.getWidget(i, 3));
					String uuid = qdmListBox.getValue(qdmListBox.getSelectedIndex());
					attributeNode.setExtraInformation(QDM_UUID, uuid);
				}
			}else{
				attributeNode.setExtraInformation(MODE, modeName);
				
				HorizontalPanel hPanel = (HorizontalPanel) grid.getWidget(i, 3);
				TextBox valueBox = (TextBox) hPanel.getWidget(0);
				ListBox unitBox =  (ListBox) hPanel.getWidget(1);
				
				attributeNode.setExtraInformation(COMPARISON_VALUE, valueBox.getText());
				String unitName = unitBox.getItemText(unitBox.getSelectedIndex());
				if(unitName.trim().length() > 0){
					attributeNode.setExtraInformation(UNIT, unitName);
				}
			}
			attributeList.add(attributeNode);	
		}
		xmlTreeDisplay.getSelectedNode().setExtraInformation(ATTRIBUTES, attributeList);
		
	}

	/**
	 * This method will create the actual table to be placed and shown in the pop-up DialogBox.
	 * @param dialogContents
	 * @param attributeList
	 * @param mode
	 * @param cellTreeNode 
	 * @param xmlTreeDisplay 
	 * @param addNewButton 
	 * @param deleteSelectedButton 
	 */
	private static void addTableToPanel(VerticalPanel dialogContents,
			final String qdmDataType, final List<String> mode, XmlTreeDisplay xmlTreeDisplay, CellTreeNode cellTreeNode, Button deleteSelectedButton, final Button addNewButton) {
		
		ScrollPanel scrollPanel = new ScrollPanel();
		scrollPanel.setSize("28em", "19em");
		
		DecoratorPanel decoratorPanel = new DecoratorPanel();
		scrollPanel.setWidget(grid);
		decoratorPanel.setWidget(scrollPanel);
		dialogContents.add(decoratorPanel);
		
		dialogContents.setCellHorizontalAlignment(decoratorPanel, HasHorizontalAlignment.ALIGN_LEFT);
		
		final List<CellTreeNode> attributeNodeList = (List<CellTreeNode>) cellTreeNode.getExtraInformation(ATTRIBUTES);
		final int rows = (attributeNodeList == null)?0:attributeNodeList.size();
	    
		Timer t = new Timer() {
			public void run(){
				if(rows == 0){
					//Add a blank attribute row to the table for the user to fill in.
					AddNewQDMAttributeClickHandler.rowNode = null;
					DomEvent.fireNativeEvent(Document.get().createClickEvent(0, 0, 0, 0, 0, false, false, false, false), addNewButton);
				}else{
					for(int i=0;i<rows;i++){
						//Add a blank row to the table.
						CellTreeNode rowNode = attributeNodeList.get(i);
						AddNewQDMAttributeClickHandler.rowNode = rowNode;
						DomEvent.fireNativeEvent(Document.get().createClickEvent(0, 0, 0, 0, 0, false, false, false, false), addNewButton);
					}
					for(int j=0;j<grid.getRowCount();j++){
						updateAttributeListBox(grid, j, (ListBox) grid.getWidget(j, 1));
					}
				}
			}
		};
		t.schedule(500);
	}
	
	private static void setExitingAttributeInGrid(CellTreeNode attributenode, int row){
			if(attributenode == null){
				return;
			}
					
			//Set the attribute name
			String attributeName = (String) attributenode.getExtraInformation(NAME);
			ListBox attributeNameListBox = (ListBox) grid.getWidget(row, 1);
			attributeNameListBox.setEnabled(true);
			for(int j=0;j<attributeNameListBox.getItemCount();j++){
				if(attributeNameListBox.getItemText(j).equals(attributeName)){
					attributeNameListBox.setSelectedIndex(j);
					break;
				}
			}
			//Set the mode name
			String modeName = (String) attributenode.getExtraInformation(MODE);
			ListBox modeNameListBox = (ListBox) grid.getWidget(row, 2);
			modeNameListBox.setEnabled(true);
			for(int j=0;j<modeNameListBox.getItemCount();j++){
				if(modeNameListBox.getItemText(j).equalsIgnoreCase(modeName) || modeNameListBox.getItemText(j).equalsIgnoreCase("-- "+modeName)){
					modeNameListBox.setSelectedIndex(j);
					break;
				}
			}
			if(!CHECK_IF_PRESENT.equalsIgnoreCase(modeName)){
				if(VALUE_SET.equalsIgnoreCase(modeName)){
					ListBox qdmListBox = createQdmListBox();
					setToolTipForEachElementInQdmListBox(qdmListBox);
					grid.setWidget(row, 3, qdmListBox);
					
					String qdmId = (String) attributenode.getExtraInformation(QDM_UUID);
					if(ClauseConstants.getElementLookUpName().containsKey(qdmId)){
						for(int r=0;r<qdmListBox.getItemCount();r++){
							if(qdmId.equals(qdmListBox.getValue(r))){
								qdmListBox.setSelectedIndex(r);
								break;
							}
						}
					}
				}
				//If this is a Comparison operator
				else{
					HorizontalPanel panel = new HorizontalPanel();
					panel.setWidth("8em");
					panel.setSpacing(0);
					
					TextBox textBox = new TextBox();
					textBox.addKeyPressHandler(new DigitsOnlyKeyPressHandler());
					textBox.setWidth("3em");
					textBox.setHeight("19");
					textBox.setValue((String) attributenode.getExtraInformation(COMPARISON_VALUE));
					
					ListBox units = new ListBox(false);
					units.setVisibleItemCount(1);
					units.setWidth("5em");
					
					for(String unitName:unitNames){
						units.addItem(unitName);
					}
					setToolTipForEachElementInListbox(units);
					String unit = (String) attributenode.getExtraInformation(UNIT);
					for(int g=0;g<units.getItemCount();g++){
						if(units.getItemText(g).equals(unit)){
							units.setSelectedIndex(g);
							break;
						}
					}					
					panel.add(textBox);
					panel.add(units);
					
					grid.setWidget(row, 3, panel);					
				}
			}
	}

	private static ListBox createQdmListBox() {
		ListBox qdmListBox = new ListBox(false);
		qdmListBox.setVisibleItemCount(1);
		qdmListBox.setWidth("8em");
		for (Entry<String, Node> qdm : ClauseConstants.getElementLookUpNode().entrySet()) {
			Node qdmNode = qdm.getValue();
			String dataType = qdmNode.getAttributes().getNamedItem(DATATYPE).getNodeValue();
			String uuid = qdmNode.getAttributes().getNamedItem(UUID).getNodeValue();
			if(ATTRIBUTE.equals(dataType)){
				qdmListBox.addItem(ClauseConstants.getElementLookUpName().get(uuid), uuid);	
			}
		}
		return qdmListBox;
	}

	private static List<String> getModeList() {
		List<String> modeList = new ArrayList<String>();
		
		modeList.add(CHECK_IF_PRESENT);
		modeList.add(COMPARISON);
		modeList.add(LESS_THAN);
		modeList.add(GREATER_THAN);
		modeList.add(EQUAL_TO);
		modeList.add(LESS_THAN_OR_EQUAL_TO);
		modeList.add(GREATER_THAN_OR_EQUAL_TO);
		modeList.add(VALUE_SET);
		
		return modeList;
	}
	
	/**
	 * This method will check all the QDM elements in ElementLookup node
	 * and return the names of QDM elements of datatype 'attribute'.
	 * @param cellTreeNode 
	 * @param xmlTreeDisplay 
	 * @param mode2 
	 * @return
	 */
	/*private static List<String> getQDMElementNames(){
		List<String> qdmNameList = new ArrayList<String>();
		Set<String> qdmNames = ClauseConstants.getElementLookUpNode().keySet();
		for(String qdmName:qdmNames){
			com.google.gwt.xml.client.Node qdmNode = ClauseConstants.getElementLookUpNode().get(qdmName);
			String dataType = qdmNode.getAttributes().getNamedItem(DATATYPE).getNodeValue();
			if(ATTRIBUTE.equals(dataType)){
				String uuid = qdmName.substring(qdmName.lastIndexOf("~") + 1);
				qdmNameList.add(ClauseConstants.getElementLookUpName().get(uuid));
			}
		}
		return qdmNameList;
	}*/
	
	private static void findAttributesForDataType(final String dataType, final boolean isOccuranceQDM, final List<String> mode2, final XmlTreeDisplay xmlTreeDisplay, final CellTreeNode cellTreeNode){
		attributeService.getAllAttributesByDataType(dataType, new AsyncCallback<List<QDSAttributes>>() {

			@Override
			public void onFailure(Throwable caught) {
				caught.printStackTrace();
				System.out.println("Error retrieving data type attributes. " + caught.getMessage());
			}
			@Override
			public void onSuccess(List<QDSAttributes> result) {
				for(QDSAttributes qdsAttributes:result){
					if(isOccuranceQDM && NEGATION_RATIONALE.equals(qdsAttributes.getName())){
						continue;
					}
					attributeList.add(qdsAttributes.getName());
				}
				buildAndDisplayDialogBox(dataType, mode2,xmlTreeDisplay, cellTreeNode);
			}
		});
	}

/*	private static Node findElementLookUpNode(String name) {
		Set<Entry<String, Node>> qdmNames = ClauseConstants.getElementLookUpNode().entrySet();
		for(Entry qdmName:qdmNames){
			if(qdmName.getKey().equals(name)){
				com.google.gwt.xml.client.Node qdmNode = ClauseConstants.getElementLookUpNode().get(qdmName.getKey());
				return qdmNode;
			}
		}
		return null;
	}*/
	
	private static void setToolTipForEachElementInListbox(ListBox listBox){
		//Set tooltips for each element in listbox
		SelectElement selectElement = SelectElement.as(listBox.getElement());
		com.google.gwt.dom.client.NodeList<OptionElement> options = selectElement.getOptions();
		for (int i = 0; i < options.getLength(); i++) {
			OptionElement optionElement = options.getItem(i);
	        optionElement.setTitle(optionElement.getInnerText());
	    }
	}
	
	
	private static void setToolTipForEachElementInQdmListBox(ListBox listBox){
		SelectElement selectElement = SelectElement.as(listBox.getElement());
		com.google.gwt.dom.client.NodeList<OptionElement> options = selectElement.getOptions();
		for (int i = 0; i < options.getLength(); i++) {
			String text = options.getItem(i).getText();
			String uuid = options.getItem(i).getAttribute("value");
			String oid = ClauseConstants.getElementLookUpNode().get(text + "~" + uuid).getAttributes().getNamedItem("oid").getNodeValue();
			String title = text + " (" + oid + ")";
			OptionElement optionElement = options.getItem(i);
	        optionElement.setTitle(title);
	    }
	}
}