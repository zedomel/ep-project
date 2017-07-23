package ep.db.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ep.db.database.DatabaseService;

public class Configuration {

	/**
	 * Arquivo de configuração
	 */
	public static final String PROP_FILE = "conf/config.properties";

	private static final String DB_HOST = "db.host";
	private static final String DB_NAME = "db.database";
	private static final String DB_PORT = "db.port";
	private static final String DB_PASSWORD = "db.password";
	private static final String DB_USER = "db.user";
	private static final String DB_BATCH_SIZE = "db.batch_size";
	private static final String GROBID_CONFIG = "grobid.properties";
	private static final String MENDELEY_CLIENT_SECRET = "mendeley.client_secret";
	private static final String MENDELEY_HOST = "mendeley.host";
	private static final String MENDELEY_CLIENT_ID = "mendeley.client_id";
	private static final String GROBID_HOME = "grobid.home";
	private static final String MININUM_PERCENT_OF_TERMS = "minimumPercentOfTerms";
	private static final String DOCUMENT_RELEVANCE_FACTOR = "relevance.documents";
	private static final String AUTHORS_RELEVANCE_FACTOR = "relevance.authors";
	private static final String QUADTREE_MAX_DEPTH = "quadtree.max_depth";
	private static final String QUADTREE_MAX_ELEMENTS_PER_BUNCH = "quadtree.max_elements_per_bunch";
	private static final String QUADTREE_MAX_ELEMENTS_PER_LEAF = "quadtree.max_elements_per_leaf";
	private static final String MAX_RADIUS_SIZE = "max_radius";
	private static final String MIN_RADIUS_SIZE = "min_radius";

	private static Logger logger = LoggerFactory.getLogger(Configuration.class);

	private final Properties properties;

	private final String configFile;

	private String dbHost;

	private String dbName;

	private int dbPort;

	private String dbUser;

	private String mendeleyClientId;

	private String mendeleyHost;

	private String grobidHome;

	private String mendeleyClientSecret;

	private String grobidConfig;

	private int dbBatchSize;

	private String dbPassword;

	private float minimumPercentOfTerms;

	private float documentRelevanceFactor;

	private float authorsRelevanceFactor;
	
	private int quadTreeMaxDepth;
	
	private int quadTreeMaxElementsPerBunch;
	
	private int quadTreeMaxElementsPerLeaf;
	
	private float maxRadiusSizePercent;
	
	private float minRadiusSizePercent;

	public Configuration() {
		this(PROP_FILE);
	}

	public Configuration(String configFile) {
		properties = new Properties();
		this.configFile = configFile;
	}

	public void loadConfiguration() throws IOException {
		try {
			properties.load(new FileInputStream(configFile));
		} catch (IOException e) {
			throw e;
		}

		dbHost = properties.getProperty(DB_HOST);
		dbName = properties.getProperty(DB_NAME);
		dbPort = Integer.parseInt(properties.getProperty(DB_PORT));
		dbUser = properties.getProperty(DB_USER);
		dbPassword = properties.getProperty(DB_PASSWORD);
		try {
			dbBatchSize = Integer.parseInt(properties.getProperty(DB_BATCH_SIZE));
			DatabaseService.batchSize = dbBatchSize;
		}catch (NumberFormatException e) {
			logger.warn("Cannot parse database batch size value: " + properties.getProperty(DB_BATCH_SIZE), e);
		}

		grobidHome = properties.getProperty(GROBID_HOME);
		grobidConfig = properties.getProperty(GROBID_CONFIG);

		mendeleyClientId = properties.getProperty(MENDELEY_CLIENT_ID);
		mendeleyClientSecret = properties.getProperty(MENDELEY_CLIENT_SECRET);
		mendeleyHost = properties.getProperty(MENDELEY_HOST);

		String prop = properties.getProperty(MININUM_PERCENT_OF_TERMS);
		if ( prop != null ){
			try {
				minimumPercentOfTerms = Float.parseFloat(prop);	
				DatabaseService.minimumPercentOfTerms = minimumPercentOfTerms;
			} catch( NumberFormatException e){
				logger.warn("Cannot parse minimum percente of terms value: " + prop, e);
			}
		}

		prop = properties.getProperty(DOCUMENT_RELEVANCE_FACTOR, "1");
		try {
			documentRelevanceFactor = Float.parseFloat(prop);	
			DatabaseService.documentRelevanceFactor = documentRelevanceFactor;
		} catch( NumberFormatException e){
			logger.warn("Cannot parse document relevance factor value: " + prop, e);
		}
		
		prop = properties.getProperty(AUTHORS_RELEVANCE_FACTOR, "0");
		try {
			authorsRelevanceFactor = Float.parseFloat(prop);	
			DatabaseService.authorsRelevanceFactor = authorsRelevanceFactor;
		} catch( NumberFormatException e){
			logger.warn("Cannot parse authors relevance factor value: " + prop, e);
		}
		
		prop = properties.getProperty(QUADTREE_MAX_DEPTH);
		try{
			quadTreeMaxDepth = Integer.parseInt(prop);
		}catch (NumberFormatException e) {
			logger.warn("Cannot parse quadtree max depth value: " + prop, e);
		}
		
		prop = properties.getProperty(QUADTREE_MAX_ELEMENTS_PER_BUNCH);
		try{
			quadTreeMaxElementsPerBunch = Integer.parseInt(prop);
		}catch (NumberFormatException e) {
			logger.warn("Cannot parse quadtree max elements per bunch value: " + prop, e);
		}
		
		prop = properties.getProperty(QUADTREE_MAX_ELEMENTS_PER_LEAF);
		try{
			quadTreeMaxElementsPerLeaf = Integer.parseInt(prop);
		}catch (NumberFormatException e) {
			logger.warn("Cannot parse quadtree max elements per leaf value: " + prop, e);
		}
		
		prop = properties.getProperty(MAX_RADIUS_SIZE);
		try{
			maxRadiusSizePercent = Float.parseFloat(prop);
		}catch (NumberFormatException e) {
			logger.warn("Cannot parse max. radius size value: " + prop, e);
		}
		
		prop = properties.getProperty(MIN_RADIUS_SIZE);
		try{
			minRadiusSizePercent = Float.parseFloat(prop);
		}catch (NumberFormatException e) {
			logger.warn("Cannot parse min. radius size value: " + prop, e);
		}
		
		
	}

	public void save() throws IOException{
		properties.setProperty(DB_HOST, dbHost);
		properties.setProperty(DB_NAME, dbName);
		properties.setProperty(DB_PORT, Integer.toString(dbPort));
		properties.setProperty(DB_USER, dbUser);
		properties.setProperty(DB_PASSWORD, dbPassword);
		properties.setProperty(DB_BATCH_SIZE, Integer.toBinaryString(dbBatchSize));
		
		properties.setProperty(GROBID_CONFIG, grobidConfig);
		properties.setProperty(GROBID_HOME, grobidHome);
		
		properties.setProperty(MENDELEY_CLIENT_ID, mendeleyClientId);
		properties.setProperty(MENDELEY_CLIENT_SECRET, mendeleyClientSecret);
		properties.setProperty(MENDELEY_HOST, mendeleyHost);
		
		properties.setProperty(MININUM_PERCENT_OF_TERMS, Float.toString(minimumPercentOfTerms));
		
		properties.setProperty(DOCUMENT_RELEVANCE_FACTOR, Float.toString(documentRelevanceFactor));
		properties.setProperty(AUTHORS_RELEVANCE_FACTOR, Float.toString(authorsRelevanceFactor));
		
		
		properties.setProperty(QUADTREE_MAX_DEPTH, Integer.toString(quadTreeMaxDepth));
		properties.setProperty(QUADTREE_MAX_ELEMENTS_PER_BUNCH, Integer.toString(quadTreeMaxElementsPerBunch));
		properties.setProperty(QUADTREE_MAX_ELEMENTS_PER_LEAF, Integer.toString(quadTreeMaxElementsPerLeaf));
		
		properties.setProperty(MAX_RADIUS_SIZE, Float.toString(maxRadiusSizePercent));
		properties.setProperty(MIN_RADIUS_SIZE, Float.toString(minRadiusSizePercent));
		
		properties.store(new FileOutputStream(configFile), null);
	}

	public String getDbHost() {
		return dbHost;
	}

	public void setDbHost(String dbHost) {
		this.dbHost = dbHost;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public int getDbPort() {
		return dbPort;
	}

	public void setDbPort(int dbPort) {
		this.dbPort = dbPort;
	}

	public String getDbUser() {
		return dbUser;
	}

	public void setDbUser(String dbUser) {
		this.dbUser = dbUser;
	}

	public String getMendeleyClientId() {
		return mendeleyClientId;
	}

	public void setMendeleyClientId(String mendeleyClientId) {
		this.mendeleyClientId = mendeleyClientId;
	}

	public String getMendeleyHost() {
		return mendeleyHost;
	}

	public void setMendeleyHost(String mendeleyHost) {
		this.mendeleyHost = mendeleyHost;
	}

	public String getGrobidHome() {
		return grobidHome;
	}

	public void setGrobidHome(String grobidHome) {
		this.grobidHome = grobidHome;
	}

	public String getMendeleyClientSecret() {
		return mendeleyClientSecret;
	}

	public void setMendeleyClientSecret(String mendeleyClientSecret) {
		this.mendeleyClientSecret = mendeleyClientSecret;
	}

	public String getGrobidConfig() {
		return grobidConfig;
	}

	public void setGrobidConfig(String grobidConfig) {
		this.grobidConfig = grobidConfig;
	}

	public int getDbBatchSize() {
		return dbBatchSize;
	}

	public void setDbBatchSize(int dbBatchSize) {
		this.dbBatchSize = dbBatchSize;
	}

	public String getDbPassword() {
		return dbPassword;
	}

	public void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}

	public float getMinimumPercentOfTerms() {
		return minimumPercentOfTerms;
	}

	public void setMinimumPercentOfTerms(float minimumPercentOfTerms) {
		this.minimumPercentOfTerms = minimumPercentOfTerms;
		DatabaseService.minimumPercentOfTerms = minimumPercentOfTerms;
	}

	public float getDocumentRelevanceFactor() {
		return documentRelevanceFactor;
	}

	public void setDocumentRelevanceFactor(float documentRelevanceFactor) {
		this.documentRelevanceFactor = documentRelevanceFactor;
	}

	public float getAuthorsRelevanceFactor() {
		return authorsRelevanceFactor;
	}

	public void setAuthorsRelevanceFactor(float authorsRelevanceFactor) {
		this.authorsRelevanceFactor = authorsRelevanceFactor;
	}

	public int getQuadTreeMaxDepth() {
		return quadTreeMaxDepth;
	}

	public void setQuadTreeMaxDepth(int quadTreeMaxDepth) {
		this.quadTreeMaxDepth = quadTreeMaxDepth;
	}

	public int getQuadTreeMaxElementsPerBunch() {
		return quadTreeMaxElementsPerBunch;
	}

	public void setQuadTreeMaxElementsPerBunch(int quadTreeMaxElementsPerBunch) {
		this.quadTreeMaxElementsPerBunch = quadTreeMaxElementsPerBunch;
	}

	public int getQuadTreeMaxElementsPerLeaf() {
		return quadTreeMaxElementsPerLeaf;
	}

	public void setQuadTreeMaxElementsPerLeaf(int quadTreeMaxElementsPerLeaf) {
		this.quadTreeMaxElementsPerLeaf = quadTreeMaxElementsPerLeaf;
	}

	public float getMaxRadiusSizePercent() {
		return maxRadiusSizePercent;
	}

	public void setMaxRadiusSizePercent(float maxRadiusSizePercent) {
		this.maxRadiusSizePercent = maxRadiusSizePercent;
	}

	public float getMinRadiusSizePercent() {
		return minRadiusSizePercent;
	}

	public void setMinRadiusSizePercent(float minRadiusSizePercent) {
		this.minRadiusSizePercent = minRadiusSizePercent;
	}
}
