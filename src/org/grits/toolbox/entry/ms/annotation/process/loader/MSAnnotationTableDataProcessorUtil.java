package org.grits.toolbox.entry.ms.annotation.process.loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.grits.toolbox.datamodel.ms.annotation.tablemodel.dmtranslate.DMAnnotation;
import org.grits.toolbox.datamodel.ms.annotation.tablemodel.dmtranslate.DMFeature;
import org.grits.toolbox.datamodel.ms.tablemodel.dmtranslate.DMScan;
import org.grits.toolbox.display.control.table.preference.TableViewerColumnSettings;
import org.grits.toolbox.display.control.table.process.TableDataProcessor;
import org.grits.toolbox.entry.ms.process.loader.MassSpecTableDataProcessorUtil;
import org.grits.toolbox.ms.annotation.sugar.GlycanExtraInfo;
import org.grits.toolbox.ms.om.data.Annotation;
import org.grits.toolbox.ms.om.data.CustomExtraData;
import org.grits.toolbox.ms.om.data.Feature;
import org.grits.toolbox.ms.om.data.Ion;
import org.grits.toolbox.ms.om.data.IonAdduct;
import org.grits.toolbox.ms.om.data.IonSettings;
import org.grits.toolbox.ms.om.data.MoleculeSettings;
import org.grits.toolbox.ms.om.data.Scan;

/**
 * @author D Brent Weatherly (dbrentw@uga.edu)
 * 
 * MSAnnotationTableDataProcessorUtil - Fills in rows in a GRITStable with fields appropriate for MS Annotation of MS data
 *
 */
public class MSAnnotationTableDataProcessorUtil
{
	private static final Logger logger = Logger.getLogger(MSAnnotationTableDataProcessorUtil.class);

	/**
	 * Description: adds MS columns appropriate for MS Annotation
	 * 
	 * @param _columnSettings - a TableViewerColumnSettings object that will be filled with column headers
	 * @param _iMSLevel - the MS level of the current table
	 * @return int - the number of columns added
	 */
	public static int fillMassSpecColumnSettingsScan( TableViewerColumnSettings _columnSettings, int _iMSLevel ) {
		_columnSettings.addColumn( DMScan.scan_numannotatedpeaks.getLabel(), DMScan.scan_numannotatedpeaks.name() );
		return 1;
	}

	/**
	 * Description: fills in the MS-specific data for MS Annotation
	 * 
	 * @param a_scan - the Scan object containing the data
	 * @param _tableRow - The ArrayList<Object> to be filled
	 * @param _columnSettings - the TableViewerColumnSettings object with the positions of the columns in the table row
	 */
	public static void fillMassSpecScanData(Scan a_scan,  ArrayList<Object> _tableRow, 
			TableViewerColumnSettings _columnSettings ) {    
		try {
			MassSpecTableDataProcessorUtil.setRowValue(_columnSettings.getColumnPosition(DMScan.scan_numannotatedpeaks.name()), 
					a_scan.getNumAnnotatedPeaks() != null ? a_scan.getNumAnnotatedPeaks() : -1, _tableRow);
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
	}

	/**
	 * Description: adds the CustomExtraData columns specific for MS Annotation
	 * 
	 * @param _columnSettings - the TableViewerColumnSettings object with the positions of the columns in the table row
	 * @param _lCustomExtraData - List<CustomExtraData> associated with this MS Annotation
	 * @return int - number of columns added
	 */
	public static int fillColumnSettingsCustomExtraData( 
			TableViewerColumnSettings _columnSettings, 
			List<CustomExtraData> _lCustomExtraData) {
		int iNumColumns = 0;
		try {
			if ( _lCustomExtraData != null && ! _lCustomExtraData.isEmpty() ) {
				for( CustomExtraData ced : _lCustomExtraData ) {
					if( ced.getKey().equals( GlycanExtraInfo.GLYCAN_CHARGE ) ) {
						continue;
					}
					_columnSettings.addColumn( ced.getLabel(), ced.getKey() );	
					iNumColumns++;
				}
			}	
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
		return iNumColumns;
	}

	/**
 	 * Description: adds the custom annotation data columns to the TableViewerColumnSettings that were loaded into htKeyToPos.
 	 * Once added, the keys are removed. When complete, if any keys remain in htKeyToPos, they are unrecognized headers found in the project
 	 * but not in persistent preferences.
 	 * 
	 * @param _columnSettings - the TableViewerColumnSettings object with the positions of the columns in the table row
	 * @param _lCustomExtraData - List<CustomExtraData> associated with this MS Annotation
	 * @param htKeyToPos - HashMap<String, Integer> that maps the External Quant label to its column position
	 * @return int - the number of columns added
	 */
	public static int fillColumnSettingsCustomExtraData( 
			TableViewerColumnSettings _columnSettings, 
			List<CustomExtraData> _lCustomExtraData, 
			HashMap<String, Integer> htKeyToPos ) {
		int iNumColumns = 0;
		try {
			if ( _lCustomExtraData != null && ! _lCustomExtraData.isEmpty() ) {
				for( CustomExtraData ced : _lCustomExtraData ) {
					if( ced.getKey().equals( GlycanExtraInfo.GLYCAN_CHARGE ) ) {
						continue;
					}
					if( htKeyToPos != null && htKeyToPos.containsKey(ced.getKey() ) ) {
						_columnSettings.putColumn(ced.getLabel(), ced.getKey(), htKeyToPos.get(ced.getKey()));	
						htKeyToPos.remove(ced.getKey());
						iNumColumns++;
					} else { // just add?
						_columnSettings.addColumn( ced.getLabel(), ced.getKey() );		
						iNumColumns++;
					}
				}
			}		
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
		return iNumColumns;
	}

	/**
	 * Description: Iterates over the list of Annotation-specific CustomExtraData to populate the data into the table row.
	 * 
	 * @param a_annotation - an Annotation object 
	 * @param _tableRow - The ArrayList<Object> to be filled
	 * @param _columnSettings - the TableViewerColumnSettings object with the positions of the columns in the table row
	 * @param _lCustomExtraData - List<CustomExtraData> associated with this MS Annotation
	 */
	public static void fillMSAnnotationCustomExtraData(Annotation a_annotation, ArrayList<Object> _tableRow, 
			TableViewerColumnSettings _columnSettings, List<CustomExtraData> _lCustomExtraData ) {
		try {
			if( a_annotation != null ) {
				if ( _lCustomExtraData != null && ! _lCustomExtraData.isEmpty() ) {
					for( CustomExtraData ced : _lCustomExtraData ) {
						if( ced.getKey().equals( GlycanExtraInfo.GLYCAN_CHARGE ) ) {
							//							logger.debug("Skipping old charge data");
							continue;
						}
						fillAnnotationCustomExtraData(a_annotation, ced, _tableRow, _columnSettings);
					}		
				}
			}
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
	} 

	/**
	 * Description: fills in the Annotation-specific CustomExtraData information into the table row.
	 * 
	 * @param a_annotation - an Annotation object 
	 * @param _cnd - a CustomExtraData object
	 * @param _tableRow - The ArrayList<Object> to be filled
	 * @param _columnSettings - the TableViewerColumnSettings object with the positions of the columns in the table row
	 */
	private static void fillAnnotationCustomExtraData( Annotation a_annotation, CustomExtraData _cnd, ArrayList<Object> _tableRow, 
			TableViewerColumnSettings _columnSettings) {
		try {
			switch (_cnd.getType()) {
			case Double:
				if( a_annotation.getDoubleProp() != null && a_annotation.getDoubleProp().containsKey(_cnd.getKey()) && a_annotation.getDoubleProp().get(_cnd.getKey()) != null ) {
					MassSpecTableDataProcessorUtil.setRowValue(	_columnSettings.getColumnPosition(_cnd.getKey() ), 
							new Double(_cnd.getDoubleFormat().format(a_annotation.getDoubleProp().get(_cnd.getKey()))), _tableRow);    					
				}
				break;
			case Integer:
				if( a_annotation.getIntegerProp() != null && a_annotation.getIntegerProp().containsKey(_cnd.getKey() ) && a_annotation.getIntegerProp().get(_cnd.getKey()) != null ) {
					MassSpecTableDataProcessorUtil.setRowValue(	_columnSettings.getColumnPosition(_cnd.getKey() ), 
							a_annotation.getIntegerProp().get(_cnd.getKey()), _tableRow);    										
				}
				break;
			case Boolean:
				if( a_annotation.getBooleanProp() != null && a_annotation.getBooleanProp().containsKey(_cnd.getKey() ) && a_annotation.getBooleanProp().get(_cnd.getKey()) != null) {
					MassSpecTableDataProcessorUtil.setRowValue(	_columnSettings.getColumnPosition(_cnd.getKey() ), 
							a_annotation.getBooleanProp().get(_cnd.getKey()) ? "Yes" : "No", _tableRow);    										
				} else {
					MassSpecTableDataProcessorUtil.setRowValue(	_columnSettings.getColumnPosition(_cnd.getKey() ), "No", _tableRow);    															
				}
				break;
			case String:
				if( a_annotation.getStringProp() != null && a_annotation.getStringProp().containsKey(_cnd.getKey() ) && a_annotation.getStringProp().get(_cnd.getKey()) != null) {
					MassSpecTableDataProcessorUtil.setRowValue(	_columnSettings.getColumnPosition(_cnd.getKey() ), 
							a_annotation.getStringProp().get(_cnd.getKey()), _tableRow);    										
				}
			default:
				break;
			}
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
	}

	/**
	 * Description: Iterates over the list of Feature-specific CustomExtraData to populate the data into the table row.
	 * 
	 * @param a_feature - a Feature object
	 * @param _tableRow - The ArrayList<Object> to be filled
	 * @param _columnSettings - the TableViewerColumnSettings object with the positions of the columns in the table row
	 * @param _lCustomExtraData - List<CustomExtraData> associated with this MS Annotation
	 */
	public static void fillMSFeatureCustomExtraData(Feature a_feature, ArrayList<Object> _tableRow, 
			TableViewerColumnSettings _columnSettings, List<CustomExtraData> _lCustomExtraData ) {
		try {
			if( a_feature != null ) {
				if ( _lCustomExtraData != null && ! _lCustomExtraData.isEmpty() ) {
					for( CustomExtraData ced : _lCustomExtraData ) {
						if( ced.getKey().equals( GlycanExtraInfo.GLYCAN_CHARGE ) ) {
							continue;
						}
						fillFeatureCustomExtraData(a_feature, ced, _tableRow, _columnSettings);
					}		
				}
			}
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
	} 

	/**
	 * Description: fills in the Feature-specific CustomExtraData information into the table row.
	 * 
	 * @param a_feature - a Feature object
	 * @param _cnd - a CustomExtraData object
	 * @param _tableRow - The ArrayList<Object> to be filled
	 * @param _columnSettings - the TableViewerColumnSettings object with the positions of the columns in the table row
	 */
	private static void fillFeatureCustomExtraData( Feature a_feature, CustomExtraData _cnd, ArrayList<Object> _tableRow, 
			TableViewerColumnSettings _columnSettings) {
		try {
			switch (_cnd.getType()) {
			case Double:
				if( a_feature.getDoubleProp() != null && a_feature.getDoubleProp().containsKey(_cnd.getKey()) ) {
					MassSpecTableDataProcessorUtil.setRowValue(	_columnSettings.getColumnPosition(_cnd.getKey() ), 
							new Double(_cnd.getDoubleFormat().format(a_feature.getDoubleProp().get(_cnd.getKey()))), _tableRow);    					
				}
				break;
			case Integer:
				if( a_feature.getIntegerProp() != null && a_feature.getIntegerProp().containsKey(_cnd.getKey() ) ) {
					MassSpecTableDataProcessorUtil.setRowValue(	_columnSettings.getColumnPosition(_cnd.getKey() ), 
							a_feature.getIntegerProp().get(_cnd.getKey()), _tableRow);    										
				}
				break;
			case Boolean:
				if( a_feature.getBooleanProp() != null && a_feature.getBooleanProp().containsKey(_cnd.getKey() ) ) {
					MassSpecTableDataProcessorUtil.setRowValue(	_columnSettings.getColumnPosition(_cnd.getKey() ), 
							a_feature.getBooleanProp().get(_cnd.getKey()) ? "Yes" : "No", _tableRow);    										
				} else {
					MassSpecTableDataProcessorUtil.setRowValue(	_columnSettings.getColumnPosition(_cnd.getKey() ), "No", _tableRow);    															
				}
				break;
			case String:
				if( a_feature.getStringProp() != null && a_feature.getStringProp().containsKey(_cnd.getKey() ) ) {
					MassSpecTableDataProcessorUtil.setRowValue(	_columnSettings.getColumnPosition(_cnd.getKey() ), 
							a_feature.getStringProp().get(_cnd.getKey()), _tableRow);    										
				}
			default:
				break;
			}
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
	}

	/**
	 * Description: adds the Feature-specific columns to the TableViewerColumnSettings object
	 * @param _columnSettings - a TableViewerColumnSettings object
	 * @return int - the number of columns added
	 */
	public static int fillMSAnnotationColumnSettingsFeature( TableViewerColumnSettings _columnSettings ) {
		try {
			_columnSettings.addColumn( DMFeature.feature_id.getLabel(), DMFeature.feature_id.name());
			_columnSettings.addColumn( DMFeature.feature_sequence.getLabel(), DMFeature.feature_sequence.name());
			_columnSettings.addColumn( DMFeature.feature_mz.getLabel(), DMFeature.feature_mz.name());
			_columnSettings.addColumn( DMFeature.feature_deviation.getLabel(), DMFeature.feature_deviation.name());
			_columnSettings.addColumn( DMFeature.feature_charge.getLabel(), DMFeature.feature_charge.name());
			_columnSettings.addColumn( DMFeature.feature_precursor_id.getLabel(), DMFeature.feature_precursor_id.name());
			_columnSettings.addColumn( DMFeature.feature_ions.getLabel(), DMFeature.feature_ions.name());
			_columnSettings.addColumn( DMFeature.feature_fragmentType.getLabel(), DMFeature.feature_fragmentType.name());
			_columnSettings.addColumn( DMFeature.feature_neutralLosses.getLabel(), DMFeature.feature_neutralLosses.name());
			_columnSettings.addColumn( DMFeature.feature_exchanges.getLabel(), DMFeature.feature_exchanges.name());
			return 11;
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
		return 0;
	}

	/**
	 * @param _lObjects - generic List of objects, here Ion, IonAdduct, MoleculeSettings or IonSettings
	 * @return String - output String for displaying the adducts, neutral gain/loss, exchanges
	 */
	private static String getListFillString(List<?> _lObjects) {
		try {
			if( _lObjects == null || _lObjects.isEmpty() )
				return "";
			StringBuilder sb = new StringBuilder();
			for( Object obj : _lObjects ) {
				if( obj instanceof Ion ) {
					Ion ion = (Ion) obj;
					int iCnt = 1;
					if( obj instanceof IonAdduct ) {
						IonAdduct ionAdduct = (IonAdduct) obj;
						iCnt = ionAdduct.getCount();
					}
					if( ion.getMass() < 0) {
						sb.append("-");
					}
					if( iCnt > 0 ) {
						sb.append(Integer.toString(iCnt));
					}
					sb.append(ion.getLabel());
				} else if ( obj instanceof MoleculeSettings ) {
					if( ! sb.toString().equals("") ) {
						sb.append(", ");
					}
					MoleculeSettings molSettings = (MoleculeSettings) obj;
					int iCnt = molSettings.getCount();
					if( obj instanceof IonSettings ) {
						IonSettings ionSettings = (IonSettings) obj;
					}		
					if( iCnt > 0 ) {
						sb.append(Integer.toString(iCnt));
						sb.append("x");
					}
					sb.append(molSettings.getLabel());
				}
			}
			/*

		StringBuilder sb = new StringBuilder();
		for( Object obj : _lObjects ) {
			if( ! sb.toString().equals("") ) {
				sb.append(", ");
			}
			sb.append("[");
			if( obj instanceof Ion ) {
				Ion ion = (Ion) obj;
				sb.append(ion.getLabel());
				sb.append(", ");
				sb.append(ion.getMass());
				sb.append(", ");
				sb.append(ion.getCharge());
				if( obj instanceof IonAdduct ) {
					IonAdduct ionAdduct = (IonAdduct) obj;
					sb.append(", ");
					sb.append(ionAdduct.getCount());
				}
			} else if ( obj instanceof MoleculeSettings ) {
				MoleculeSettings molSettings = (MoleculeSettings) obj;
				sb.append(molSettings.getLabel());
				sb.append(", ");
				sb.append(molSettings.getMass());
				sb.append(", ");
				sb.append(molSettings.getCount());
				if( obj instanceof IonSettings ) {
					IonSettings ionSettings = (IonSettings) obj;
					sb.append(", ");
					sb.append(ionSettings.getCharge());
					sb.append(", ");
					sb.append(ionSettings.getPolarity());
					if( ionSettings.getCounts() != null ) {
						sb.append(", (");
						int i = 0;
						for( Integer iCnt : ionSettings.getCounts() ) {
							if( i != 0 ) {
								sb.append(",");
							}
							sb.append(iCnt.toString());
							i++;
						}
						sb.append(")");
					}
				}
			} else {
				sb.append(obj.toString());
			}
			sb.append("]");
		}
			 */
			return sb.toString();
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
		return null;
	}

	/**
	 * Description: fills in the Feature-specific information into the table row.
	 * 
	 * @param a_feature - a Feature object
	 * @param _tableRow - The ArrayList<Object> to be filled
	 * @param _columnSettings - the TableViewerColumnSettings object with the positions of the columns in the table row
	 */
	public static void fillFeatureData(Feature a_feature, ArrayList<Object> _tableRow, TableViewerColumnSettings _columnSettings )
	{
		try {
			if ( a_feature != null )
			{
				MassSpecTableDataProcessorUtil.setRowValue(_columnSettings.getColumnPosition( DMFeature.feature_id.name() ), 
						a_feature.getId(), _tableRow);    	
				MassSpecTableDataProcessorUtil.setRowValue(_columnSettings.getColumnPosition( DMFeature.feature_sequence.name() ), 
						a_feature.getSequence(), _tableRow);    	
				MassSpecTableDataProcessorUtil.setRowValue(_columnSettings.getColumnPosition( DMFeature.feature_mz.name() ), 
						new Double(MassSpecTableDataProcessorUtil.formatDec4.format(a_feature.getMz())), _tableRow);    	
				MassSpecTableDataProcessorUtil.setRowValue(_columnSettings.getColumnPosition( DMFeature.feature_deviation.name() ), 
						new Double(MassSpecTableDataProcessorUtil.formatDec4.format(a_feature.getDeviation())), _tableRow);    	
				MassSpecTableDataProcessorUtil.setRowValue(_columnSettings.getColumnPosition( DMFeature.feature_charge.name() ), 
						a_feature.getCharge(), _tableRow);    	
				MassSpecTableDataProcessorUtil.setRowValue(_columnSettings.getColumnPosition( DMFeature.feature_precursor_id.name() ), 
						a_feature.getPrecursor(), _tableRow);    	
				//			MassSpecTableDataProcessorUtil.setRowValue(_columnSettings.getColumnPosition( DMFeature.feature_adduct.name() ), 
				//					MSAnnotationTableDataProcessorUtil.getListFillString(a_feature.getIons()), _tableRow);    	
				MassSpecTableDataProcessorUtil.setRowValue(_columnSettings.getColumnPosition( DMFeature.feature_ions.name() ), 
						MSAnnotationTableDataProcessorUtil.getListFillString(a_feature.getIons()), _tableRow);    	
				MassSpecTableDataProcessorUtil.setRowValue(_columnSettings.getColumnPosition( DMFeature.feature_fragmentType.name() ), 
						a_feature.getFragmentType(), _tableRow);    	
				MassSpecTableDataProcessorUtil.setRowValue(_columnSettings.getColumnPosition( DMFeature.feature_neutralLosses.name() ), 
						MSAnnotationTableDataProcessorUtil.getListFillString(a_feature.getNeutralLoss()), _tableRow);    	
				MassSpecTableDataProcessorUtil.setRowValue(_columnSettings.getColumnPosition( DMFeature.feature_exchanges.name() ), 
						MSAnnotationTableDataProcessorUtil.getListFillString(a_feature.getNeutralexchange()), _tableRow);  
			}
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
	}

	/**
	 * Description: updates the Feature with data from the table row. Not used (yet)!
	 * 
	 * @param a_feature - a Feature object
	 * @param _tableRow - The ArrayList<Object> to be filled
	 * @param _columnSettings - the TableViewerColumnSettings object with the positions of the columns in the table row
	 */
	public static void updateFeatureData(Feature a_feature, ArrayList _tableRow, TableViewerColumnSettings _columnSettings )
	{
		try {
			if ( a_feature != null )
			{
				if ( _tableRow.get( _columnSettings.getColumnPosition(DMFeature.feature_id.name()) ) != null &&
						! _tableRow.get( _columnSettings.getColumnPosition(DMFeature.feature_id.name()) ).equals( a_feature.getId()) ) {
					a_feature.setId( (String) _tableRow.get( _columnSettings.getColumnPosition(DMFeature.feature_id.name()) ));
				}
				if ( _tableRow.get( _columnSettings.getColumnPosition(DMFeature.feature_sequence.name()) ) != null &&
						! _tableRow.get( _columnSettings.getColumnPosition(DMFeature.feature_sequence.name()) ).equals( a_feature.getSequence()) ) {
					a_feature.setSequence( (String) _tableRow.get( _columnSettings.getColumnPosition(DMFeature.feature_sequence.name()) ));
				}
				if ( _tableRow.get( _columnSettings.getColumnPosition(DMFeature.feature_mz.name()) ) != null &&
						! _tableRow.get( _columnSettings.getColumnPosition(DMFeature.feature_mz.name()) ).equals( a_feature.getMz()) ) {
					a_feature.setMz( (Double) _tableRow.get( _columnSettings.getColumnPosition(DMFeature.feature_mz.name()) ));
				}
				if ( _tableRow.get( _columnSettings.getColumnPosition(DMFeature.feature_deviation.name()) ) != null &&
						! _tableRow.get( _columnSettings.getColumnPosition(DMFeature.feature_deviation.name()) ).equals( a_feature.getDeviation()) ) {
					a_feature.setDeviation( (Double) _tableRow.get( _columnSettings.getColumnPosition(DMFeature.feature_deviation.name()) ));
				}
				if ( _tableRow.get( _columnSettings.getColumnPosition(DMFeature.feature_charge.name()) ) != null &&
						! _tableRow.get( _columnSettings.getColumnPosition(DMFeature.feature_charge.name()) ).equals( a_feature.getCharge()) ) {
					a_feature.setCharge( (Integer) _tableRow.get( _columnSettings.getColumnPosition(DMFeature.feature_charge.name()) ));
				}
				if ( _tableRow.get( _columnSettings.getColumnPosition(DMFeature.feature_precursor_id.name()) ) != null &&
						! _tableRow.get( _columnSettings.getColumnPosition(DMFeature.feature_precursor_id.name()) ).equals( a_feature.getPrecursor()) ) {
					a_feature.setPrecursor( (Integer) _tableRow.get( _columnSettings.getColumnPosition(DMFeature.feature_precursor_id.name()) ));
				}
				if ( _tableRow.get( _columnSettings.getColumnPosition(DMFeature.feature_fragmentType.name()) ) != null &&
						! _tableRow.get( _columnSettings.getColumnPosition(DMFeature.feature_fragmentType.name()) ).equals( a_feature.getFragmentType()) ) {
					a_feature.setFragmentType( (String) _tableRow.get( _columnSettings.getColumnPosition(DMFeature.feature_fragmentType.name()) ));
				}
			}
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
	}

	/**
	 * Description: adds the Annotation-specific columns to the TableViewerColumnSettings object
	 * @param _columnSettings - a TableViewerColumnSettings object
	 * @return int - the number of columns added
	 */
	public static int fillMSAnnotationColumnSettingsAnnotation( TableViewerColumnSettings _columnSettings ) {
		try {
			_columnSettings.addColumn(DMAnnotation.annotation_num_candidates.getLabel(), DMAnnotation.annotation_num_candidates.name() );
			_columnSettings.addColumn(TableDataProcessor.filterColHeader.getLabel(),  TableDataProcessor.filterColHeader.getKeyValue());
			_columnSettings.addColumn(TableDataProcessor.commentColHeader.getLabel(),  TableDataProcessor.commentColHeader.getKeyValue());
			_columnSettings.addColumn( DMAnnotation.annotation_id.getLabel(), DMAnnotation.annotation_id.name());
			_columnSettings.addColumn( DMAnnotation.annotation_sequence.getLabel(), DMAnnotation.annotation_sequence.name());
			_columnSettings.addColumn( DMAnnotation.annotation_ratio.getLabel(),  DMAnnotation.annotation_ratio.name());
			return 5;
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
		return 0;
	}

	/**
	 * Description: fills in the Annotation-specific information into the table row.
	 * @param a_annotation - an Annotation object
	 * @param _iNumCandidates - the number of candidate annotations for the current row
	 * @param _tableRow - The ArrayList<Object> to be filled
	 * @param _columnSettings - the TableViewerColumnSettings object with the positions of the columns in the table row
	 */
	public static void fillAnnotationData(Annotation a_annotation, int _iNumCandidates, ArrayList<Object> _tableRow, TableViewerColumnSettings _columnSettings )
	{
		try {
			MassSpecTableDataProcessorUtil.setRowValue(_columnSettings.getColumnPosition( DMAnnotation.annotation_num_candidates.name() ), 
					_iNumCandidates, _tableRow);  
			MassSpecTableDataProcessorUtil.setRowValue(_columnSettings.getColumnPosition( TableDataProcessor.filterColHeader.getKeyValue()), 
					-1, _tableRow);
			if ( a_annotation != null )
			{
				MassSpecTableDataProcessorUtil.setRowValue(_columnSettings.getColumnPosition( DMAnnotation.annotation_id.name() ), 
						a_annotation.getId(), _tableRow);    	
				MassSpecTableDataProcessorUtil.setRowValue(_columnSettings.getColumnPosition( DMAnnotation.annotation_sequence.name() ), 
						a_annotation.getSequence(), _tableRow);  
			}

		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
	}

	/**
	 * Description: updates the Annotation with data from the table row. Not used (yet)!
	 * @param a_annotation - an Annotation object
	 * @param _tableRow - The ArrayList<Object> to be filled
	 * @param _columnSettings - the TableViewerColumnSettings object with the positions of the columns in the table row
	 */
	public static void updateAnnotationData(Annotation a_annotation, ArrayList _tableRow, TableViewerColumnSettings _columnSettings )
	{
		try {
			// annotation data
			if ( a_annotation != null )
			{
				if ( _tableRow.get( _columnSettings.getColumnPosition(DMAnnotation.annotation_id.name()) ) != null &&
						! _tableRow.get( _columnSettings.getColumnPosition(DMAnnotation.annotation_id.name()) ).equals( a_annotation.getId()) ) {
					a_annotation.setId( (Integer) _tableRow.get( _columnSettings.getColumnPosition(DMAnnotation.annotation_id.name()) ));
				}
				if ( _tableRow.get( _columnSettings.getColumnPosition(DMAnnotation.annotation_sequence.name()) ) != null &&
						! _tableRow.get( _columnSettings.getColumnPosition(DMAnnotation.annotation_sequence.name()) ).equals( a_annotation.getSequence()) ) {
					a_annotation.setSequence( (String) _tableRow.get( _columnSettings.getColumnPosition(DMAnnotation.annotation_sequence.name()) ));
				}
			}
		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
	}
}
