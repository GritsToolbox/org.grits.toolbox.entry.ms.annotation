package org.grits.toolbox.entry.ms.annotation.tablehelpers;

import java.util.HashMap;

public class SharedCheckboxWidget {
	protected HashMap<String, ExtCheckBoxPainter> htGlycanToCheckBox = null;		

	public SharedCheckboxWidget() {
		htGlycanToCheckBox = new HashMap<>();
	}
	
	public HashMap<String, ExtCheckBoxPainter> getHtGlycanToCheckBox() {
		return htGlycanToCheckBox;
	}
	
	public ExtCheckBoxPainter createCheckBoxPainter( String _sKey, String _sLabel, boolean _bCurStatus ) {
		ExtCheckBoxPainter ecbp = null;
		if( ! htGlycanToCheckBox.containsKey(_sKey) ) {
			ecbp = new ExtCheckBoxPainter(_sLabel, _bCurStatus);
			htGlycanToCheckBox.put(_sKey, ecbp);
		} else {
			htGlycanToCheckBox.get(_sKey);
		}
		return ecbp;
	}

}
