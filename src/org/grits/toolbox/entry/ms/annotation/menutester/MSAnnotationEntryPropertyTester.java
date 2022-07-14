package org.grits.toolbox.entry.ms.annotation.menutester;

import org.eclipse.core.expressions.PropertyTester;
import org.grits.toolbox.core.datamodel.Entry;

public class MSAnnotationEntryPropertyTester extends PropertyTester {
	protected final static String prop = "org.grits.toolbox.entry.ms.annotation.property.MSAnnotationProperty";	
			
   @Override
   public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
      if (receiver == null || !(receiver instanceof Entry))
         return false;

      Entry entry = (Entry) receiver;
      if ( entry.getProperty().getType().equals( MSAnnotationEntryPropertyTester.prop) )
    	  return true;
      return false;
   }
}