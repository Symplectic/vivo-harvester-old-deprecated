/*******************************************************************************
 * Copyright (c) 2012 Symplectic Ltd. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 ******************************************************************************/
package org.vivoweb.harvester.util.repo;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.FileAide;
import org.vivoweb.harvester.util.repo.RecordMetaData.RecordMetaDataType;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Read-only record handler for transferring Elements RDF records into a Jena model.
 */
public class ElementsRecordHandler extends RecordHandler {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(ElementsRecordHandler.class);

	/**
	 * The directory to store record files in
	 */
	private String fileDir;

	/**
	 * The directory to store record metadata files in
	 */
	private String metaDir;

	/**
	 * Default Constructor
	 */
	protected ElementsRecordHandler() {
		// Nothing to do here
		// Used by config construction
		// Should only be used in conjunction with setParams()
	}

	/**
	 * Constructor
	 * @param fileDir directory to store records in
	 * @throws java.io.IOException error accessing directory
	 */
	public ElementsRecordHandler(String fileDir) throws IOException {
		setFileDirObj(fileDir);
	}

	/**
	 * Setter for fileDir
	 * @param fileDir the directory path String
	 * @throws java.io.IOException unable to connect
	 */
	private void setFileDirObj(String fileDir) throws IOException {
		if(!FileAide.exists(fileDir)) {
			log.debug("Directory '" + fileDir + "' Does Not Exist, attempting to create");
			FileAide.createFolder(fileDir);
		}
		this.fileDir = fileDir;
		this.metaDir = fileDir+"/.metadata";
		if(!FileAide.exists(this.metaDir)) {
			log.debug("Directory '" + fileDir + "/.metadata' Does Not Exist, attempting to create");
			FileAide.createFolder(this.metaDir);
		}
	}

	@Override
	public void setParams(Map<String, String> params) throws IllegalArgumentException, IOException {
		setFileDirObj(getParam(params, "fileDir", true));
	}

	/**
	 * Sanitizes a record id
	 * @param id the record id
	 * @return null if not needed, else the new id
	 */
	private String sanitizeID(String id) {
		String s = id.replaceAll("\\n", "_-_NEWLINE_-_").replaceAll("\\r", "_-_RETURN_-_").replaceAll("\\t", "_-_TAB_-_").replaceAll(" ", "_-_SPACE_-_").replaceAll("\\\\", "_-_BACKSLASH_-_").replaceAll("/", "_-_FORWARDSLASH_-_").replaceAll(":", "_-_COLON_-_").replaceAll("\\*", "_-_STAR_-_").replaceAll("\\?", "_-_QUESTIONMARK_-_").replaceAll("\"", "_-_DOUBLEQUOTE_-_").replaceAll("<", "_-_LESSTHAN_-_").replaceAll(">", "_-_GREATERTHAN_-_").replaceAll("\\|", "_-_PIPE_-_");
		if(s.equals(id)) {
			return null;
		}
		log.debug("record id sanitized from '" + id + "' to '" + s + "'");
		return s;
	}

	@Override
	public boolean addRecord(Record rec, Class<?> operator, boolean overwrite) throws IOException {
        throw new UnsupportedOperationException();
	}

	/**
	 * Creates the metadata file for a given record
	 * @param recID the record id
	 * @throws java.io.IOException error writing metadata file
	 */
	private void createMetaDataFile(String recID) throws IOException {
        throw new UnsupportedOperationException();
    }

	@Override
	public void delRecord(String recID) throws IOException {
        throw new UnsupportedOperationException();
    }

	@Override
	public String getRecordData(String recID) throws IllegalArgumentException, IOException {
		String fo = null;
		BufferedReader br = null;
		try {
			StringBuilder sb = new StringBuilder();
			fo = this.fileDir+"/"+recID;
			if(!FileAide.exists(fo)) {
				throw new IllegalArgumentException("Record " + recID + " does not exist!");
			}
			br = new BufferedReader(new InputStreamReader(FileAide.getInputStream(fo)));
			String line;
			while((line = br.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
			br.close();
			return sb.toString().trim();
		} catch(IOException e) {
			if(br != null) {
				try {
					br.close();
				} catch(Exception ignore) {
					// Ignore
				}
			}
			throw e;
		} finally {
			if(br != null) {
				try {
					br.close();
				} catch(Exception ignore) {
					// Ignore
				}
			}
		}
	}

	@Override
	protected void delMetaData(String recID) throws IOException {
        throw new UnsupportedOperationException();
    }

	@Override
	protected void addMetaData(Record rec, RecordMetaData rmd) throws IOException {
        throw new UnsupportedOperationException();
    }

	@Override
	protected SortedSet<RecordMetaData> getRecordMetaData(String recID) throws IOException {
        throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<Record> iterator() {
		return new ElementsFileRecordIterator();
	}

	/**
	 * Iterator for TextFileRecordHandler
	 * @author cah
	 */
	private class ElementsFileRecordIterator implements Iterator<Record> {
		/**
		 * Iterator over the files
		 */
		Iterator<String> fileNameIterator;

		/**
		 * Default Constructor
		 */
		protected ElementsFileRecordIterator() {
			Set<String> allFileListing = new TreeSet<String>();
			log.debug("Compiling list of records");
            addAllFiles(allFileListing, "", new File(fileDir));
			this.fileNameIterator = allFileListing.iterator();
			log.debug("List compiled");
		}

        private void addAllFiles(Set<String> listing, String prefix, File dir) {
            if (dir != null && dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isDirectory()) {
                            addAllFiles(listing, prefix + file.getName() + File.separator, file);
                        } else {
                            if (StringUtils.isEmpty(prefix)) {
                                listing.add(file.getName());
                            } else {
                                listing.add(prefix + file.getName());
                            }
                        }
                    }
                }
            }
        }

		@Override
		public boolean hasNext() {
			return this.fileNameIterator.hasNext();
		}

		@Override
		public Record next() {
			try {
				return getRecord(this.fileNameIterator.next());
			} catch(IOException e) {
				throw new NoSuchElementException(e.getMessage());
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * MetaData File Parser for TextFileRecordHandlers
	 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
	 */
	private static class TextFileMetaDataParser extends DefaultHandler {
		/**
		 * The RecordHandler we are building
		 */
		private SortedSet<RecordMetaData> rmdSet;
		/**
		 * The date of the current rmd
		 */
		private Calendar tempDate;
		/**
		 * Class name of the operator for current rmd
		 */
		private Class<?> tempOperator;
		/**
		 * The rmdType of current rmd
		 */
		private RecordMetaDataType tempOperation;
		/**
		 * The md5hash of current rmd
		 */
		private String tempMD5;
		/**
		 * The value of the current cdata
		 */
		private String tempVal;

		/**
		 * Default Constructor
		 */
		protected TextFileMetaDataParser() {
			this.rmdSet = new TreeSet<RecordMetaData>();
			this.tempDate = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
			this.tempDate.setTimeInMillis(0);
			this.tempOperation = RecordMetaDataType.error;
			this.tempOperator = IllegalArgumentException.class;
		}

		/**
		 * Parses the metadata file and return sorted set of RecordMetaData
		 * @param fmo the file object of the metadata file
		 * @return sorted set of RecordMetaData
		 * @throws javax.xml.parsers.ParserConfigurationException parser configured incorrectly
		 * @throws org.xml.sax.SAXException error parsing xml
		 * @throws java.io.IOException error reading xml
		 */
		protected SortedSet<RecordMetaData> parseMetaData(String fmo) throws ParserConfigurationException, SAXException, IOException {
			SAXParserFactory spf = SAXParserFactory.newInstance(); // get a factory
			SAXParser sp = spf.newSAXParser(); // get a new instance of parser
			sp.parse(FileAide.getInputStream(fmo), this); // parse the file and also register this class for call
			// backs
			return this.rmdSet;
		}
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if(qName.equalsIgnoreCase("MetaDataRecord")) {
				this.tempDate = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
				this.tempDate.setTimeInMillis(0);
				this.tempOperation = RecordMetaDataType.error;
				this.tempOperator = IllegalArgumentException.class;
				this.tempMD5 = "";
			} else if(qName.equalsIgnoreCase("Date")) {
				// Do Nothing, but don't remove so it doesn't go to else clause
			} else if(qName.equalsIgnoreCase("Operation")) {
				// Do Nothing, but don't remove so it doesn't go to else clause
			} else if(qName.equalsIgnoreCase("Operator")) {
				// Do Nothing, but don't remove so it doesn't go to else clause
			} else if(qName.equalsIgnoreCase("MD5")) {
				// Do Nothing, but don't remove so it doesn't go to else clause
			} else if(qName.equalsIgnoreCase("MetaDataRecordList")) {
				// Do Nothing, but don't remove so it doesn't go to else clause
			} else if(qName.equalsIgnoreCase("DateReadable")) {
				// Do Nothing, but don't remove so it doesn't go to else clause
			} else {
				throw new SAXException("Unknown Tag: " + qName);
			}
		}
		
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			this.tempVal = new String(ch, start, length);
		}
		
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if(qName.equalsIgnoreCase("MetaDataRecord")) {
//				RecordMetaData rmd = new RecordMetaData(this.tempDate, this.tempOperator, this.tempOperation, this.tempMD5);
//				this.rmdSet.add(rmd);
			} else if(qName.equalsIgnoreCase("Date")) {
				this.tempDate.setTimeInMillis(Long.parseLong(this.tempVal));
			} else if(qName.equalsIgnoreCase("Operation")) {
				this.tempOperation = RecordMetaDataType.valueOf(this.tempVal);
			} else if(qName.equalsIgnoreCase("Operator")) {
				try {
					this.tempOperator = Class.forName(this.tempVal);
				} catch(ClassNotFoundException e) {
					throw new SAXException(e.getMessage(), e);
				}
			} else if(qName.equalsIgnoreCase("MD5")) {
				this.tempMD5 = this.tempVal;
			} else if(qName.equalsIgnoreCase("MetaDataRecordList")) {
				// Do Nothing, but don't remove so it doesn't go to else clause
			} else if(qName.equalsIgnoreCase("DateReadable")) {
				// Do Nothing, but don't remove so it doesn't go to else clause
			} else {
				throw new SAXException("Unknown Tag: " + qName);
			}
		}
	}
	
	@Override
	public void close() throws IOException {
		// Do nothing
	}
	
	@Override
	public Set<String> find(String idText) {
		Set<String> retVal = new TreeSet<String>();
		for(Record r : this) {
			if(r.getID().contains(idText)) {
				retVal.add(r.getID());
			}
		}
		return retVal;
	}
}
