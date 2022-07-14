package org.grits.toolbox.entry.ms.annotation.tablehelpers;

import org.eclipse.nebula.widgets.nattable.data.convert.ConversionFailedException;
import org.eclipse.nebula.widgets.nattable.data.validate.DataValidator;

public class DoubleDataValidator extends DataValidator {

	@Override
	public boolean validate(int columnIndex, int rowIndex, Object newValue) {
		if (newValue != null) {
			try {
	            Double.valueOf(newValue.toString())
	                    .doubleValue();
	            return true;
	        } catch (Exception ex) {
	            throw new ConversionFailedException("Not a double value");
	        }
		}
		return true;
	}

}
