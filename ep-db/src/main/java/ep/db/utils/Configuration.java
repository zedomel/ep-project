package ep.db.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final String MININUM_PERCENT_OF_DOCUMENTS = "minimumPercentOfDocs";
	private static final String MAXIMUM_PERCENT_OF_DOCUMENTS = "maximumPercentOfDocs";
	private static final String MININUM_NUMBER_OF_TERMS = "minimumNumberOfTerms";
	private static final String MAXIMUM_NUMBER_OF_TERMS = "maximumNumberOfTerms";
	private static final String DOCUMENT_RELEVANCE_FACTOR = "relevance.documents";
	private static final String AUTHORS_RELEVANCE_FACTOR = "relevance.authors";
	private static final String QUADTREE_MAX_DEPTH = "quadtree.max_depth";
	private static final String QUADTREE_MAX_ELEMENTS_PER_BUNCH = "quadtree.max_elements_per_bunch";
	private static final String QUADTREE_MAX_ELEMENTS_PER_LEAF = "quadtree.max_elements_per_leaf";
	private static final String MAX_RADIUS_SIZE = "max_radius";
	private static final String MIN_RADIUS_SIZE = "min_radius";
	private static final String WEIGHT_A = "weight_A";
	private static final String WEIGHT_B = "weight_B";
	private static final String WEIGHT_C = "weight_C";
	private static final String WEIGHT_D = "weight_D";
	private static final String NORMALIZATION = "normalization";
	private static final String USE_PRE_CALCULATED_FREQS = "use_pre_calculated_freqs";
	private static final String RANDOM_CONTROL_POINTS = "random_control_points";
	private static final String DISABLE_OUTLIERS = "disable_outliers";
	private static final String PAGE_RANK_ALPHA = "page_rank_alpha";
	private static final String TFIDF_WEIGHTING_SCHEME = "tfidf_weighting_scheme";
	private static final String DENSITY_MAP_CALCULATION = "density_map";

	public static final int DENSITY_MAP_CLIENT = 1;

	public static final int DENSITY_MAP_SERVER = 0;
	
	private static Logger logger = LoggerFactory.getLogger(Configuration.class);

	private final Properties properties;

	private String configFile;

	private String dbHost;

	private  String dbName;

	private  int dbPort;

	private  String dbUser;

	private  String mendeleyClientId;

	private  String mendeleyHost;

	private  String grobidHome;

	private  String mendeleyClientSecret;

	private  String grobidConfig;

	private  int dbBatchSize;

	private  String dbPassword;

	private  float minimumPercentOfDocuments;
	
	private  float maximumPercentOfDocuments;
	
	private int minimumNumberOfTerms;
	
	private int maximumNumberOfTerms;

	private  float documentRelevanceFactor;

	private  float authorsRelevanceFactor;
	
	private  int quadTreeMaxDepth;
	
	private  int quadTreeMaxElementsPerBunch;
	
	private  int quadTreeMaxElementsPerLeaf;
	
	private  float maxRadiusSizePercent;
	
	private  float minRadiusSizePercent;
	
	private  Float[] weights;

	private  int normalization;
	
	private boolean usePreCalculatedFreqs;
	
	private boolean randomControlPoints;
	
	private boolean disableOutliers;
	
	private float pageRankAlpha;
	
	private int tfidfWeightingScheme;

	private int densityMapCalculation;
	
	private static Configuration instance;

	private Configuration() {
		properties = new Properties();
	}
	
	public static Configuration getInstance() {
		if ( instance == null )
			instance = new Configuration();
		return instance;
	}
	
	public Configuration loadConfiguration() throws IOException{
		return loadConfiguration(PROP_FILE);
	}

	public Configuration loadConfiguration(String configFile) throws IOException {
		
		if ( configFile == null )
			configFile = PROP_FILE;
		this.configFile = configFile;
		
		Locale.setDefault(Locale.ENGLISH);
		
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
			dbBatchSize = Integer.parseInt(properties.getProperty(DB_BATCH_SIZE).trim());
		}catch (NumberFormatException e) {
			logger.warn("Cannot parse database batch size value: " + properties.getProperty(DB_BATCH_SIZE), e);
		}

		grobidHome = properties.getProperty(GROBID_HOME);
		grobidConfig = properties.getProperty(GROBID_CONFIG);

		mendeleyClientId = properties.getProperty(MENDELEY_CLIENT_ID);
		mendeleyClientSecret = properties.getProperty(MENDELEY_CLIENT_SECRET);
		mendeleyHost = properties.getProperty(MENDELEY_HOST);

		String prop = properties.getProperty(MININUM_PERCENT_OF_DOCUMENTS);
		if ( prop != null ){
			try {
				minimumPercentOfDocuments = Float.parseFloat(prop.trim());	
			} catch( NumberFormatException e){
				logger.warn("Cannot parse minimum percent of documents value: " + prop, e);
			}
		}
		
		prop = properties.getProperty(MAXIMUM_PERCENT_OF_DOCUMENTS);
		if ( prop != null ){
			try {
				maximumPercentOfDocuments = Float.parseFloat(prop.trim());	
			} catch( NumberFormatException e){
				logger.warn("Cannot parse maximum percent of documents value: " + prop, e);
			}
		}
		
		prop = properties.getProperty(MININUM_NUMBER_OF_TERMS);
		if ( prop != null ){
			try {
				minimumNumberOfTerms = Integer.parseInt(prop.trim());	
			} catch( NumberFormatException e){
				logger.warn("Cannot parse minimum number of terms value: " + prop, e);
			}
		}
		
		prop = properties.getProperty(MAXIMUM_NUMBER_OF_TERMS);
		if ( prop != null ){
			try {
				maximumNumberOfTerms = Integer.parseInt(prop.trim());	
			} catch( NumberFormatException e){
				logger.warn("Cannot parse maximum number of terms value: " + prop, e);
				maximumNumberOfTerms = Integer.MAX_VALUE;
			}
		}
		
		prop = properties.getProperty(DOCUMENT_RELEVANCE_FACTOR, "1");
		try {
			documentRelevanceFactor = Float.parseFloat(prop.trim());	
		} catch( NumberFormatException e){
			logger.warn("Cannot parse document relevance factor value: " + prop, e);
		}
		
		prop = properties.getProperty(AUTHORS_RELEVANCE_FACTOR, "0");
		try {
			authorsRelevanceFactor = Float.parseFloat(prop.trim());	
		} catch( NumberFormatException e){
			logger.warn("Cannot parse authors relevance factor value: " + prop, e);
		}
		
		prop = properties.getProperty(QUADTREE_MAX_DEPTH);
		try{
			if ( prop.trim().equals("Inf"))
				quadTreeMaxDepth = Integer.MAX_VALUE;
			else
				quadTreeMaxDepth = Integer.parseInt(prop.trim());
		}catch (NumberFormatException e) {
			logger.warn("Cannot parse quadtree max depth value: " + prop, e);
		}
		
		prop = properties.getProperty(QUADTREE_MAX_ELEMENTS_PER_BUNCH);
		try{
			quadTreeMaxElementsPerBunch = Integer.parseInt(prop.trim());
		}catch (NumberFormatException e) {
			logger.warn("Cannot parse quadtree max elements per bunch value: " + prop, e);
		}
		
		prop = properties.getProperty(QUADTREE_MAX_ELEMENTS_PER_LEAF);
		try{
			quadTreeMaxElementsPerLeaf = Integer.parseInt(prop.trim());
		}catch (NumberFormatException e) {
			logger.warn("Cannot parse quadtree max elements per leaf value: " + prop, e);
		}
		
		prop = properties.getProperty(MAX_RADIUS_SIZE);
		try{
			maxRadiusSizePercent = Float.parseFloat(prop.trim());
		}catch (NumberFormatException e) {
			logger.warn("Cannot parse max. radius size value: " + prop, e);
		}
		
		prop = properties.getProperty(MIN_RADIUS_SIZE);
		try{
			minRadiusSizePercent = Float.parseFloat(prop.trim());
		}catch (NumberFormatException e) {
			logger.warn("Cannot parse min. radius size value: " + prop, e);
		}
		
		
		weights = new Float[4];
		int i = 0;
		prop = properties.getProperty(WEIGHT_A);
		try{
			weights[i] = Float.parseFloat(prop.trim());
		}catch (NumberFormatException e) {
			logger.warn("Cannot parse weight A value: " + prop, e);
		}
		++i;
		prop = properties.getProperty(WEIGHT_B);
		try{
			weights[i] = Float.parseFloat(prop.trim());
		}catch (NumberFormatException e) {
			logger.warn("Cannot parse weight B value: " + prop, e);
		}
		++i;
		prop = properties.getProperty(WEIGHT_C);
		try{
			weights[i] = Float.parseFloat(prop.trim());
		}catch (NumberFormatException e) {
			logger.warn("Cannot parse weight C value: " + prop, e);
		}
		++i;
		prop = properties.getProperty(WEIGHT_D);
		try{
			weights[i] = Float.parseFloat(prop.trim());
		}catch (NumberFormatException e) {
			logger.warn("Cannot parse weight D value: " + prop, e);
		}
		
		prop = properties.getProperty(PAGE_RANK_ALPHA);
		if ( prop != null ){
			try {
				pageRankAlpha = Float.parseFloat(prop.trim());	
			} catch( NumberFormatException e){
				logger.warn("Cannot parse Page Rank alpha value: " + prop, e);
			}
		}
		
		prop = properties.getProperty(NORMALIZATION);
		if ( prop != null ){
			try {
				normalization = Integer.parseInt(prop.trim());	
			} catch( NumberFormatException e){
				logger.warn("Cannot parse normalization value: " + prop, e);
			}
		}
		
		prop = properties.getProperty(TFIDF_WEIGHTING_SCHEME);
		if ( prop != null ){
			try {
				tfidfWeightingScheme = Integer.parseInt(prop.trim());	
			} catch( NumberFormatException e){
				logger.warn("Cannot parse TF-IDF weighting scheme: " + prop, e);
			}
		}
		
		prop = properties.getProperty(DENSITY_MAP_CALCULATION);
		if ( prop.trim().equals("client"))
			densityMapCalculation = DENSITY_MAP_CLIENT;
		else 
			densityMapCalculation = DENSITY_MAP_SERVER;
			
		usePreCalculatedFreqs = Boolean.parseBoolean(properties.getProperty(USE_PRE_CALCULATED_FREQS));
		randomControlPoints  = Boolean.parseBoolean(properties.getProperty(RANDOM_CONTROL_POINTS));
		disableOutliers  = Boolean.parseBoolean(properties.getProperty(DISABLE_OUTLIERS));
		
		return this;
	}

	public void save() throws IOException{
		properties.setProperty(DB_HOST, dbHost);
		properties.setProperty(DB_NAME, dbName);
		properties.setProperty(DB_PORT, Integer.toString(dbPort));
		properties.setProperty(DB_USER, dbUser);
		properties.setProperty(DB_PASSWORD, dbPassword);
		properties.setProperty(DB_BATCH_SIZE, Integer.toString(dbBatchSize));
		
		properties.setProperty(GROBID_CONFIG, grobidConfig);
		properties.setProperty(GROBID_HOME, grobidHome);
		
		properties.setProperty(MENDELEY_CLIENT_ID, mendeleyClientId);
		properties.setProperty(MENDELEY_CLIENT_SECRET, mendeleyClientSecret);
		properties.setProperty(MENDELEY_HOST, mendeleyHost);
		
		properties.setProperty(MININUM_PERCENT_OF_DOCUMENTS, Float.toString(minimumPercentOfDocuments));
		properties.setProperty(MAXIMUM_PERCENT_OF_DOCUMENTS, Float.toString(maximumPercentOfDocuments));
		
		properties.setProperty(MININUM_NUMBER_OF_TERMS, Integer.toString(minimumNumberOfTerms));
		properties.setProperty(MAXIMUM_NUMBER_OF_TERMS, Integer.toString(maximumNumberOfTerms));
		
		properties.setProperty(DOCUMENT_RELEVANCE_FACTOR, Float.toString(documentRelevanceFactor));
		properties.setProperty(AUTHORS_RELEVANCE_FACTOR, Float.toString(authorsRelevanceFactor));
		
		
		properties.setProperty(QUADTREE_MAX_DEPTH, Integer.toString(quadTreeMaxDepth));
		properties.setProperty(QUADTREE_MAX_ELEMENTS_PER_BUNCH, Integer.toString(quadTreeMaxElementsPerBunch));
		properties.setProperty(QUADTREE_MAX_ELEMENTS_PER_LEAF, Integer.toString(quadTreeMaxElementsPerLeaf));
		
		properties.setProperty(MAX_RADIUS_SIZE, Float.toString(maxRadiusSizePercent));
		properties.setProperty(MIN_RADIUS_SIZE, Float.toString(minRadiusSizePercent));
		
		properties.setProperty(WEIGHT_A, Float.toString(weights[0]));
		properties.setProperty(WEIGHT_B, Float.toString(weights[1]));
		properties.setProperty(WEIGHT_C, Float.toString(weights[2]));
		properties.setProperty(WEIGHT_D, Float.toString(weights[3]));
		
		properties.setProperty(PAGE_RANK_ALPHA, Float.toString(pageRankAlpha));
		
		properties.setProperty(NORMALIZATION, Integer.toString(normalization));
		
		properties.setProperty(TFIDF_WEIGHTING_SCHEME, Integer.toString(tfidfWeightingScheme));
		
		properties.setProperty(USE_PRE_CALCULATED_FREQS, Boolean.toString(usePreCalculatedFreqs));
		properties.setProperty(RANDOM_CONTROL_POINTS, Boolean.toString(randomControlPoints));
		properties.setProperty(DISABLE_OUTLIERS, Boolean.toString(disableOutliers));
		
		properties.setProperty(DENSITY_MAP_CALCULATION, densityMapCalculation == DENSITY_MAP_CLIENT ? 
				"client" : "server");
		
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

	public float getMinimumPercentOfDocuments() {
		return minimumPercentOfDocuments;
	}

	public void setMinimumPercentOfDocuments(float minimumPercentOfTerms) {
		this.minimumPercentOfDocuments = minimumPercentOfTerms;
	}
	
	public float getMaximumPercentOfDocuments() {
		return maximumPercentOfDocuments;
	}
	
	public void setMaximumPercentOfDocuments(float maximumPercentOfTerms) {
		this.maximumPercentOfDocuments = maximumPercentOfTerms;
	}
	
	public int getMinimumNumberOfTerms() {
		return minimumNumberOfTerms;
	}
	
	public void setMinimumNumberOfTerms(int minimumNumberOfTerms) {
		this.minimumNumberOfTerms = minimumNumberOfTerms;
	}
	
	public int getMaximumNumberOfTerms() {
		return maximumNumberOfTerms;
	}
	
	public void setMaximumNumberOfTerms(int maximumNumberOfTerms) {
		this.maximumNumberOfTerms = maximumNumberOfTerms;
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
	
	public Float[] getWeights() {
		return weights;
	}
	
	public void setWeights(Float[] weights) {
		this.weights = weights;
	}
	
	public int getNormalization() {
		return normalization;
	}
	
	public void setNormalization(int normalization) {
		this.normalization = normalization;
	}

	public boolean isUsePreCalculatedFreqs() {
		return usePreCalculatedFreqs;
	}

	public void setUsePreCalculatedFreqs(boolean usePreCalculatedFreqs) {
		this.usePreCalculatedFreqs = usePreCalculatedFreqs;
	}
	
	public boolean isRandomControlPoints() {
		return randomControlPoints;
	}
	
	public void setRandomControlPoints(boolean randomControlPoints) {
		this.randomControlPoints = randomControlPoints;
	}
	
	public boolean isDisableOutliers() {
		return disableOutliers;
	}
	
	public void setDisableOutliers(boolean disableOutliers) {
		this.disableOutliers = disableOutliers;
	}
	
	public float getPageRankAlpha() {
		return pageRankAlpha;
	}
	
	public void setPageRankAlpha(float pageRankAlpha) {
		this.pageRankAlpha = pageRankAlpha;
	}
	
	public int getTfidfWeightingScheme() {
		return tfidfWeightingScheme;
	}
	
	public void setTfidfWeightingScheme(int tfidfWeightingScheme) {
		this.tfidfWeightingScheme = tfidfWeightingScheme;
	}

	public float getCirclePadding() {
		return 6.0f;
	}
	
	public int getDensityMapCalculation() {
		return densityMapCalculation;
	}
	
	public void setDensityMapCalculation(int densityMapCalculation) {
		this.densityMapCalculation = densityMapCalculation;
	}
}
