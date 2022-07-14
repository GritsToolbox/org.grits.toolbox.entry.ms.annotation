package org.grits.toolbox.entry.ms.annotation.property;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.io.PropertyWriter;
import org.grits.toolbox.core.datamodel.property.NotImplementedException;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.core.datamodel.property.PropertyDataFile;
import org.grits.toolbox.core.datamodel.util.DataModelSearch;
import org.grits.toolbox.core.utilShare.DeleteUtils;
import org.grits.toolbox.core.utilShare.XMLUtils;
import org.grits.toolbox.entry.ms.ImageRegistry;
import org.grits.toolbox.entry.ms.annotation.Activator;
import org.grits.toolbox.entry.ms.annotation.property.datamodel.MSAnnotationFileInfo;
import org.grits.toolbox.entry.ms.annotation.property.datamodel.MSAnnotationMetaData;
import org.grits.toolbox.entry.ms.annotation.property.io.MSAnnotationPropertyWriter;
import org.grits.toolbox.entry.ms.property.MassSpecProperty;
import org.grits.toolbox.entry.ms.property.datamodel.ExternalQuantAlias;
import org.grits.toolbox.entry.ms.property.datamodel.ExternalQuantFileToAlias;
import org.grits.toolbox.entry.ms.property.datamodel.MSPropertyDataFile;

/**
 * An extension of the Property class in order to support MSAnnotation data.
 * @author D Brent Weatherly (dbrentw@uga.edu)
 *
 */
public class MSAnnotationProperty extends Property{
	private static final Logger logger = Logger.getLogger(MSAnnotationProperty.class);

	public static final String CURRENT_VERSION = "1.0";
	public static final String TYPE = "org.grits.toolbox.property.ms_annotation";
	protected static PropertyWriter writer = new MSAnnotationPropertyWriter();
	public static final String ARCHIVE_EXTENSION = ".zip";
	private static final String ARCHIVE_FOLDER = "ms-annotation";
	private static final String META_DATA_FILE = "msAnnotMetaData.xml";
	private MSAnnotationMetaData msAnnotationSettings = null;

	@Override
	public boolean equals(Object obj) {
		if ( ! (obj instanceof MSAnnotationProperty) )
			return false;
		MSAnnotationProperty castObj = (MSAnnotationProperty) obj;
		// just not going to handle the case where the meta data is null in either case
		if( getMSAnnotationMetaData() == null || castObj.getMSAnnotationMetaData() == null ) {
			return false;
		}
		return getMSAnnotationMetaData().equals(castObj.getMSAnnotationMetaData());
	}

	public MSAnnotationMetaData getMSAnnotationMetaData() {
		return msAnnotationSettings;
	}

	public void setMSAnnotationMetaData(MSAnnotationMetaData msAnnotationSettings) {
		this.msAnnotationSettings = msAnnotationSettings;
	}

	public static Entry getFirstAnnotEntry( Entry entry ) {
		if ( entry.getParent() != null && entry.getParent().getProperty() instanceof MassSpecProperty ) {
			return entry;
		}
		if ( entry.getParent() != null )
			return MSAnnotationProperty.getFirstAnnotEntry(entry.getParent());
		return null;
	}

	@Override
	public Object clone() {
		MSAnnotationProperty newProp = new MSAnnotationProperty();	
		// TODO: originally not cloning the annotation id. WHY???

		if ( getMSAnnotationMetaData() != null ) {
			MSAnnotationMetaData settings = (MSAnnotationMetaData) getMSAnnotationMetaData().clone();
			newProp.setMSAnnotationMetaData(settings);
		}
		return newProp;
	}

	@Override
	public String getType() {
		return MSAnnotationProperty.TYPE;
	}

	@Override
	public PropertyWriter getWriter() {
		return writer;
	}

	@Override
	public ImageDescriptor getImage() {
		return ImageRegistry.getImageDescriptor(Activator.PLUGIN_ID, ImageRegistry.MSImage.MSANNOTATION_ICON);
	}

	public void adjustPropertyFilePaths() {
		Iterator<PropertyDataFile> itr = getDataFiles().iterator();
		while( itr.hasNext() ) {
			PropertyDataFile file = itr.next();
			if( file.getName().contains("\\") && ! File.separator.equals("\\") ) {
				file.setName( file.getName().replace("\\", File.separator));
			} else if( file.getName().contains("/") && ! File.separator.equals("/") ){
				file.setName( file.getName().replace("/", File.separator));
			}
		}
	}

	public String getAnnotationFolder(Entry entry) {
		String workspaceLocation = PropertyHandler.getVariable("workspace_location");
		String projectName = DataModelSearch.findParentByType(entry, ProjectProperty.TYPE).getDisplayName();
		String annotFolder = workspaceLocation + projectName + File.separator + getArchiveFolder();
		return annotFolder;
	}
	
	public String getFullyQualifiedArchiveFileNameByAnnotationID(Entry entry) {
		String annotFolder = getAnnotationFolder(entry);
		String zip = annotFolder + File.separator + getMSAnnotationMetaData().getAnnotationId() + getArchiveExtension();
		return zip;		
	}
	
	public String getFullyQualifiedArchiveFileNameByScanNum(Entry entry, Integer scanNum) {
		String annotFolder = getAnnotationFolder(entry);
		String zip = annotFolder + File.separator + getMSAnnotationMetaData().getAnnotationId() + File.separator + scanNum + getArchiveExtension();
		return zip;		
	}

	public String getArchiveExtension() {
		return MSAnnotationProperty.ARCHIVE_EXTENSION;
	}

	public String getArchiveFolder() {
		return MSAnnotationProperty.ARCHIVE_FOLDER;
	}
	
	/** return the meta data file name without the appended annotation id
	 * 
	 * @return META_DATA_FILE name without the annotation id at the beginning
	 */
	public String getMetaDataFileExtension () {
		return MSAnnotationProperty.META_DATA_FILE;
	}

	public String getMetaDataFileName() {
		return getMSAnnotationMetaData().getAnnotationId() + "." + MSAnnotationProperty.META_DATA_FILE;
	}

	public String getFullyQualifiedMetaDataFileName(Entry entry) {
		String projectFolder = getAnnotationFolder(entry);
		String annotFolder = projectFolder + File.separator + getMetaDataFileName();
		return annotFolder;
	}

	public static PropertyDataFile getNewSettingsFile( String msAnnotDetails, MSAnnotationMetaData metaData ) {
		PropertyDataFile msMetaData = new PropertyDataFile(msAnnotDetails, metaData.getVersion(), MSAnnotationFileInfo.MS_ANNOTATION_METADATA_TYPE);	
		return msMetaData;
	}

	public PropertyDataFile getMetaDataFile() {
		Iterator<PropertyDataFile> itr = getDataFiles().iterator();
		while( itr.hasNext() ) {
			PropertyDataFile file = itr.next();
			if( file.getType().equals( MSAnnotationFileInfo.MS_ANNOTATION_METADATA_TYPE ) ) {
				return file;
			}
		}
		return null;
	}

	public static MSAnnotationMetaData unmarshallSettingsFile( String sFileName ) {
		MSAnnotationMetaData metaData = null;
		try {
			metaData = (MSAnnotationMetaData) XMLUtils.unmarshalObjectXML(sFileName, MSAnnotationMetaData.class);

		} catch (Exception e ) {
			logger.error(e.getMessage(), e);
		}
		return metaData;
	}

	public static void marshallSettingsFile( String sFileName, MSAnnotationMetaData metaData  ) {
		try {
			String xmlString = XMLUtils.marshalObjectXML(metaData);
			//write the serialized data to the folder
			FileWriter fileWriter = new FileWriter(sFileName);
			fileWriter.write( xmlString );
			fileWriter.close();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

	}

/*	@Override
	public void delete(Entry entry) throws IOException {
		String annotFolder = getAnnotationFolder(entry);
		
		String sEntryPath = null; // the folder created for each entry under the ms folder
		
		// delete the uploaded files (mzXML, raw, peaklist etc.) from MassSpecMetaData
		if( getMSAnnotationMetaData() != null ) {
			getMSAnnotationMetaData().delete(entry, annotFolder);
		}
		// delete the settings file
		Iterator<PropertyDataFile> itr = getDataFiles().iterator();
		while( itr.hasNext() ) {
			PropertyDataFile file = itr.next();
			if (file.getType().equals(MSFileInfo.MSLOCKFILE_TYPE)) // do not delete the lock file, shared by all ms entries
				continue;
			if( file.getName().trim().equals("") ) {
				continue;
			}
			DeleteUtils.delete(new File( annotFolder + File.separator + file.getName() ) );
		}
		
		// DBW 08/22/17:  need the entry path!
		if( sEntryPath != null ) {
			DeleteUtils.delete(new File(annotFolder + File.separator + sEntryPath) );
		}
	}
	*/

	@Override
	public void delete(Entry entry) throws IOException {	
		try {
			String annotFolder = getAnnotationFolder(entry);
			// find the annotation file if any and remove the lock
			MSPropertyDataFile annotationFile = getMSAnnotationMetaData().getAnnotationFile();
			if (annotationFile != null) {
				if (entry.getParent() != null && entry.getParent().getProperty() instanceof MassSpecProperty) {
					try {
						((MassSpecProperty)entry.getParent().getProperty()).removeLockForFile(entry.getParent(), entry, annotationFile.getName());
					} catch (IOException | JAXBException e) {
						throw new IOException ("Could not remove the lock for the file!", e);
					}
				}
			}
			
			// delete the uploaded files (e.g. GELATO archive)
			if( getMSAnnotationMetaData() != null ) {
				deleteSettings(entry, annotFolder);
			}
			Iterator<PropertyDataFile> itr = getDataFiles().iterator();
			while( itr.hasNext() ) {
				PropertyDataFile pdf = itr.next();
				String sFile = annotFolder + File.separator + pdf.getName();
				File f = new File(sFile);
				DeleteUtils.delete(f);
			}

		} catch( Exception ex ) {
			logger.error(ex.getMessage(), ex);
		}
	}

	/**
	 * @param entry
	 * @param annotFolder
	 * @throws IOException
	 */
	public void deleteSettings(Entry entry, String annotFolder) throws IOException {				
		// delete the result files (e.g. GELATO archive)
		for (MSPropertyDataFile file: getMSAnnotationMetaData().getFileList()) {
			if (file.getName() != null && !file.getName().trim().isEmpty()) {
				DeleteUtils.delete(new File( annotFolder + File.separator + file.getName() ) );
			}
		}
	}
	
	/**
	 * Generate random id
	 * @return
	 */
	public static String getRandomId() {
		Random random = new Random();
		return ((Integer)random.nextInt(10000)).toString();
	}

	@Override
	public Property getParentProperty() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static boolean updateMSSettings( MSAnnotationMetaData metaData, String sSettingsFile ) {
		try {
			MSAnnotationProperty.marshallSettingsFile(sSettingsFile, metaData);
		} catch( Exception e ) {
			logger.error(e.getMessage());
			return false;
		}
		return true;
	}
	
	
	@Override
	public void makeACopy(Entry currentEntry, Entry destinationEntry) throws NotImplementedException, IOException {
		String annotFolder = getAnnotationFolder(currentEntry);
		String workspaceLocation = PropertyHandler.getVariable("workspace_location");
		String newProjectName = destinationEntry.getParent().getParent().getParent().getDisplayName();
		String destinationFolder = workspaceLocation + newProjectName + File.separator + getArchiveFolder();
		
		MSAnnotationProperty newProperty = getNewAnnotationProperty(destinationFolder);
		MSAnnotationMetaData newMetadata = newProperty.getMSAnnotationMetaData();
		String newAnnotId = newMetadata.getAnnotationId();
		
		// find the new msEntryPath from the parent property and replace source file names
		MassSpecProperty parent = (MassSpecProperty) destinationEntry.getParent().getProperty();
		String filename = parent.getMSSettingsFile().getName();
		String msEntryPath = filename.substring(0, filename.lastIndexOf(File.separator));
		
		// copy the uploaded files (e.g. GELATO archive)
		if( getMSAnnotationMetaData() != null ) {
			MSAnnotationMetaData currentMetadata = getMSAnnotationMetaData();
			if (currentMetadata.getFileList() != null) {
				List<MSPropertyDataFile> newFileList = new ArrayList<MSPropertyDataFile>();
				for (MSPropertyDataFile file: currentMetadata.getFileList()) {
					if (file.getName() != null && !file.getName().trim().isEmpty()) {
						// rename file and copy
						MSPropertyDataFile fileCopy = (MSPropertyDataFile) file.clone();
						if (file.getName().endsWith(getArchiveExtension())) {
							fileCopy.setName(newAnnotId + getArchiveExtension());
							File currentFile = new File (annotFolder, file.getName());
							File destinationFile = new File (destinationFolder, fileCopy.getName());
							Files.copy(currentFile.toPath(), destinationFile.toPath());
						} else { // folder
							fileCopy.setName(newAnnotId + File.separator);
							File currentFile = new File (annotFolder, file.getName());
							File destinationFile = new File (destinationFolder, fileCopy.getName());
							Files.copy(currentFile.toPath(), destinationFile.toPath());
							copyDirectory(currentFile, destinationFile);
						}
						
						newFileList.add(fileCopy);
					}
				}
				newMetadata.setFileList(newFileList);
			}
			
			List<MSPropertyDataFile> newSourceFileList = new ArrayList<MSPropertyDataFile>();
			if (currentMetadata.getSourceDataFileList() != null) {
				for (MSPropertyDataFile file: currentMetadata.getSourceDataFileList()) {
					if (file.getName() != null && !file.getName().trim().isEmpty()) {
						MSPropertyDataFile fileCopy = (MSPropertyDataFile) file.clone();	
						
						File f = new File (file.getName());
						String simpleFileName = f.getName();
						// rename file 
						fileCopy.setName(msEntryPath + File.separator + simpleFileName);
						newSourceFileList.add(fileCopy);
					}
				}
				
				newMetadata.setSourceDataFileList(newSourceFileList);
			}
			
			// update any externalQuantAlias source file names
			HashMap<String, ExternalQuantFileToAlias> map = getMSAnnotationMetaData().getQuantTypeToExternalQuant();
			if (map != null) {
				HashMap<String, ExternalQuantFileToAlias> newMap = new HashMap<String, ExternalQuantFileToAlias>();
				for (Map.Entry<String, ExternalQuantFileToAlias> entry: map.entrySet()) {
					ExternalQuantFileToAlias alias = entry.getValue();
					ExternalQuantFileToAlias newAlias = new ExternalQuantFileToAlias();
					HashMap<String, ExternalQuantAlias> fileMap = alias.getSourceDataFileNameToAlias();
					HashMap<String, ExternalQuantAlias> newFileMap = new HashMap<String, ExternalQuantAlias>();
					for (Map.Entry<String, ExternalQuantAlias> fileEntry: fileMap.entrySet()) {
						String fileName = fileEntry.getKey();
						File f = new File (fileName);
						String simpleFileName = f.getName();
						String newFileName = msEntryPath + File.separator + simpleFileName;
						newFileMap.put(newFileName, fileEntry.getValue());
					}
					newAlias.setSourceDataFileNameToAlias(newFileMap);
					newMap.put(entry.getKey(), newAlias);
				}
				newMetadata.setQuantTypeToExternalQuant(newMap);
			}
			
			newMetadata.setDescription(currentMetadata.getDescription());
			newMetadata.setCustomAnnotations(currentMetadata.getCustomAnnotations());
			newMetadata.setCustomAnnotationText(currentMetadata.getCustomAnnotationText());
			newMetadata.setInternalStandardQuantFiles(currentMetadata.getInternalStandardQuantFiles());
			newMetadata.setStandardQuant(currentMetadata.getStandardQuant());
			newMetadata.setStandardQuantText(currentMetadata.getStandardQuantText());
			
			updateMSSettings(newMetadata, destinationFolder + File.separator + newAnnotId + "." + getMetaDataFileExtension());
		}
		
		List<PropertyDataFile> newDataFiles = new ArrayList<PropertyDataFile>();
		for (PropertyDataFile file: dataFiles) {
			PropertyDataFile newFile = new PropertyDataFile();
			newFile.setType(file.getType());
			newFile.setVersion(file.getVersion());
			String name = file.getName().substring(file.getName().indexOf("."));
			// set the new name
			newFile.setName(newAnnotId + name);
			newDataFiles.add(newFile);
		}
		
		newProperty.setDataFiles(newDataFiles);
		destinationEntry.setProperty(newProperty);
	}
	
	public void copyDirectory(File sourceLocation , File targetLocation)
		    throws IOException {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }

            String[] children = sourceLocation.list();
            for (int i=0; i<children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
        } else {
        	Files.copy(sourceLocation.toPath(), targetLocation.toPath());
        }
    }

	protected MSAnnotationProperty getNewAnnotationProperty(String msAnnotationFolder) {
		MSAnnotationProperty t_property = new MSAnnotationProperty();
		MSAnnotationMetaData metaData = new MSAnnotationMetaData();		
		t_property.setMSAnnotationMetaData(metaData);
		try {
			metaData.setAnnotationId(MSAnnotationProperty.createRandomId(msAnnotationFolder));
			metaData.setVersion(MSAnnotationMetaData.CURRENT_VERSION);
			metaData.setName(metaData.getAnnotationId() + "." + getMetaDataFileExtension());
		} catch (IOException e2) {
			logger.error(e2.getMessage(), e2);
			return null;
		}

		return t_property;
	}
	
	/**
	 * @param msAnnotation - String representation of the annotation folder
	 * @return String - an integer value for the entry's id
	 * @throws IOException
	 */
	public static String createRandomId(String msAnnotation) throws IOException {
		File simFolder = new File(msAnnotation);
		if(!simFolder.exists())
		{
			simFolder.mkdirs();
			//get a random id for the folder name
			String entryId = MSAnnotationProperty.getRandomId();
			return entryId;
		} else {
			// make sure the new random id is unique
			while (true) {
				String entryId = MSAnnotationProperty.getRandomId();
				boolean found = false;
				for (File file: simFolder.listFiles()) {
					if (file.isDirectory()) {
						if (file.getName().equals(entryId))
							found = true;
					} else {
						if (file.getName().substring (0, file.getName().lastIndexOf(".")).equals(entryId)) 
							found = true;
					}
				}
				if (!found)
					return entryId;
			}
		}
		
	}
	
	@Override
	public boolean directCopyEnabled() {
		return false;
	}

}
