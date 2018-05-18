package hdu.util;

import java.io.File;

public class CheckinConstants {
	private static final String FILE_NAME_OUTPUT       = "output";
	private static final String FILE_NAME_CONFIG       = "Config";
	private static final File PATH_CURRENT_RUNNING     =
			new File(System.getProperty("user.dir"));
	public static final File DEFAULT_PATH_OUTPUT       = 
			new File((PATH_CURRENT_RUNNING.getParentFile().exists() ?
				new File(PATH_CURRENT_RUNNING.getParentFile(), FILE_NAME_OUTPUT) :
				new File(PATH_CURRENT_RUNNING, FILE_NAME_OUTPUT)),
				"Checkin");

	public static final String STRING_OUTPUT_PATH = "OutputPath";
	public static final String STRING_TRAIN_FILE  = "TrainFile";
	public static final String STRING_TEST_FILE   = "TestFile";
	public static final String STRING_ITEM_FEATURE_INIT_FILE =
						"ItemFeatureInitFile";
	public static final String STRING_USER_FEATURE_INIT_FILE =
						"UserFeatureInitFile";
	public static final String STRING_CONVERGE_THRESHOLD     =
						"ConvergeThreshold";
	public static final String STRING_MODEL_NAME         = "Model";
	public static final String STRING_MODEL_LRT          = "LRT";
	public static final String STRING_MODEL_IRENMF       = "IRenMF";
	public static final String STRING_MODEL_SOCPMF       = "SocPMF";
	public static final String STRING_MODEL_LGSOCPMF     = "LGSocPMF";
	public static final String STRING_MODEL_WRMF         = "WRMF";
	public static final String STRING_MODEL_RRFM         = "RRFM";
	public static final String STRING_PARAM              = "Param";
	public static final String STRING_PREDECT_RATING     = "PredictedRating";
	public static final String STRING_ITEM_FEATURE       = "ItemFeature";
	public static final String STRING_USER_FEATURE       = "UserFeature";
	public static final String STRING_PROGRAM_NAME       = "MethodName";
	public static final String STRING_MEAN_TRAIN_RATING  = "MeanTrainRating";
	public static final String STRING_RATING_RECORD_NUM  = "RatingRecordNum";
	public static final String STRING_FEATURE_NUM        = "FeatureNum";
	public static final String STRING_USER_NUM           = "UserNum";
	public static final String STRING_ITEM_NUM           = "ItemNum";
	public static final String STRING_LAMBDA             = "Lambda";
	public static final String STRING_LAMBDA_USER        = "LambdaUser";
	public static final String STRING_LAMBDA_ITEM        = "LambdaItem";
	public static final String STRING_MAX_ITERATION_NUM  = "MaxIterationNum";
	public static final String STRING_EVAL_THREAD_NUM    = "ThreadNum";
	public static final String STRING_EVAL_KS            = "Ks";
	public static final String STRING_LEARNING_RATE      = "LearningRate";
	public static final String STRING_MIN_LEARNING_RATE  = "MinLearningRate";
	public static final String STRING_MAX_LEARNING_RATE  = "MaxLearningRate";
	public static final String LINE_SEPARATOR =
			System.getProperty("line.separator", "\n");

	public static final String DELIMITER   = "\t";
	public static final String CHARSET     = "UTF-8";
	public static final char CSV_DELIMITER = ',';

	private static File FILE_CONFIG = null;

	public static ParamManager CONFIG_MANAGER = null;

	static {
		String sprop = System.getProperty("PATH_CONFIG");
		if (sprop != null && ! "".equals(sprop)) {
			FILE_CONFIG = new File(new File(sprop), FILE_NAME_CONFIG);
		} else {
			FILE_CONFIG = new File(PATH_CURRENT_RUNNING, FILE_NAME_CONFIG);
		}
		initConfig();
	}

	private static void initConfig() {
		String sprop = System.getProperty("PATH_CONFIGURE");
		if (sprop != null && ! "".equals(sprop)) {
			FILE_CONFIG = new File(new File(sprop), FILE_NAME_CONFIG);
		} else {
			FILE_CONFIG = new File(PATH_CURRENT_RUNNING, FILE_NAME_CONFIG);
		}

		if (! FILE_CONFIG.exists()) {
			CONFIG_MANAGER = new ParamManager();

			CONFIG_MANAGER.setFeatureNum(10);
			CONFIG_MANAGER.setOutputPath(DEFAULT_PATH_OUTPUT);

			if (! CONFIG_MANAGER.store(FILE_CONFIG)) {
				System.err.println("Failed to store configure file.");
			}
		} else {
			CONFIG_MANAGER = new ParamManager();
			try {
				CONFIG_MANAGER.load(FILE_CONFIG);
			} catch (CheckinException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}

		// override some properties
		try {
			sprop = System.getProperty("FEATURE_NUM");
			if (sprop != null) {
				CONFIG_MANAGER.setFeatureNum(Integer.parseInt(sprop));
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		System.out.println("================== Configure ==================");
		System.out.println(CONFIG_MANAGER);
		System.out.println("===============================================");
	}
}
