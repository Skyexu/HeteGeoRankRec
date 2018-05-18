package hdu.util;

import java.io.*;
import java.util.Properties;

public class ParamManager {
	public static final String FORMAT_PARAMETERS =
			"#[Environment Setting]"						+ CheckinConstants.LINE_SEPARATOR +
			CheckinConstants.STRING_OUTPUT_PATH		+ "=%s" + CheckinConstants.LINE_SEPARATOR +
															  CheckinConstants.LINE_SEPARATOR +
			CheckinConstants.STRING_TRAIN_FILE		+ "=%s" + CheckinConstants.LINE_SEPARATOR +
			CheckinConstants.STRING_TEST_FILE		+ "=%s" + CheckinConstants.LINE_SEPARATOR +
										  						  CheckinConstants.LINE_SEPARATOR +
			"#[Model Setting]"									+ CheckinConstants.LINE_SEPARATOR +
			CheckinConstants.STRING_MODEL_NAME          + "=%s" + CheckinConstants.LINE_SEPARATOR +
			CheckinConstants.LINE_SEPARATOR +
			CheckinConstants.STRING_CONVERGE_THRESHOLD	+ "=%s"	+ CheckinConstants.LINE_SEPARATOR +
			CheckinConstants.STRING_MAX_ITERATION_NUM	+ "=%s" + CheckinConstants.LINE_SEPARATOR + 
			CheckinConstants.STRING_LEARNING_RATE		+ "=%s" + CheckinConstants.LINE_SEPARATOR +
			CheckinConstants.STRING_MAX_LEARNING_RATE	+ "=%s" + CheckinConstants.LINE_SEPARATOR + 
			CheckinConstants.STRING_MIN_LEARNING_RATE	+ "=%s" + CheckinConstants.LINE_SEPARATOR +
			CheckinConstants.STRING_FEATURE_NUM			+ "=%s" + CheckinConstants.LINE_SEPARATOR +
																  CheckinConstants.LINE_SEPARATOR +
			CheckinConstants.STRING_USER_NUM			+ "=%s" + CheckinConstants.LINE_SEPARATOR +
			CheckinConstants.STRING_ITEM_NUM			+ "=%s" + CheckinConstants.LINE_SEPARATOR +
			CheckinConstants.LINE_SEPARATOR +
			"#[Hyperparameters]"								+ CheckinConstants.LINE_SEPARATOR +
			CheckinConstants.STRING_LAMBDA			+ "=%s" + CheckinConstants.LINE_SEPARATOR +
			CheckinConstants.STRING_LAMBDA_ITEM		+ "=%s" + CheckinConstants.LINE_SEPARATOR +
			CheckinConstants.STRING_LAMBDA_USER		+ "=%s" + CheckinConstants.LINE_SEPARATOR +
			CheckinConstants.LINE_SEPARATOR +
			"#[Init File Setting]"									+ CheckinConstants.LINE_SEPARATOR +
			CheckinConstants.STRING_USER_FEATURE_INIT_FILE  + "=%s" + CheckinConstants.LINE_SEPARATOR +
			CheckinConstants.STRING_ITEM_FEATURE_INIT_FILE  + "=%s" + CheckinConstants.LINE_SEPARATOR +
			CheckinConstants.LINE_SEPARATOR +
			"#[Eval Setting]"					  			+ CheckinConstants.LINE_SEPARATOR +
	        CheckinConstants.STRING_EVAL_THREAD_NUM + "=%s" + CheckinConstants.LINE_SEPARATOR +
	        CheckinConstants.STRING_EVAL_KS 		+ "=%s" + CheckinConstants.LINE_SEPARATOR +
	        												  CheckinConstants.LINE_SEPARATOR +
	        "#[Model-specific Setting]"					  + CheckinConstants.LINE_SEPARATOR +
	        "## -EnhGroupNum <int> {-ItemGroupFile <path> or -GroupNum <int>}"	+ CheckinConstants.LINE_SEPARATOR +
	        CheckinConstants.STRING_MODEL_RRFM  + "=%s" + CheckinConstants.LINE_SEPARATOR
			;
	private File outputPath          = null;
	private File trainFile           = null;
	private File testFile            = null;
	private File userFeatureInitFile = null;
	private File itemFeatureInitFile = null;
	private int userNum              = -1;
	private int itemNum              = -1;
	private int featureNum           = -1;
	private float convergeThreshold  = 1.0e-5f;
	private float lambda             = 0.01f;
	private float lambdaItem         = 0.01f;
	private float lambdaUser         = 0.01f;
	private float learningRate       = 0.001f;
	private Float minLearningRate    = null;
	private Float maxLearningRate    = null;
	private Integer maxIterationNum  = null;
	private String modelName         = null;
	private String evalKArrayStr     = null;
	private String evalThreadNum     = null;
	private String modelRRMF         = null;
	private Properties options       = null;

	public ParamManager() {}

	public float getConvergeThreshold() {
		return convergeThreshold;
	}

	public String getEvalKArrayStr() {
		return evalKArrayStr;
	}
	
	public String getEvalThreadNum() {
		return evalThreadNum;
	}
	
	public int getFeatureNum() {
		return featureNum;
	}

	public File getItemFeatureInitFile() {
		return itemFeatureInitFile;
	}

	public int getItemNum() {
		return itemNum;
	}

	public float getLambda() {
		return lambda;
	}

	public float getLambdaItem() {
		return lambdaItem;
	}

	public float getLambdaUser() {
		return lambdaUser;
	}
	
	public float getLearningRate() {
		return learningRate;
	}

	public Integer getMaxInterationNum()  {
		return maxIterationNum;
	}
	
	public Float getMaxLearningRate() {
		return maxLearningRate;
	}
	
	public Float getMinLearningRate() {
		return minLearningRate;
	}
	
	public String getModelName() {
		return modelName;
	}
	
	public String getModelRRMF() {
		return modelRRMF;
	}
	
	public Properties getOptions() {
		return options;
	}
	
	public File getOutputPath() {
		return outputPath;
	}

	public File getTestFile() {
		return testFile;
	}

	public File getTrainFile() {
		return trainFile;
	}

	public File getUserFeatureInitFile() {
		return userFeatureInitFile;
	}

	public int getUserNum() {
		return userNum;
	}

	public void setConvergeThreshold(float convergeThreshold) {
		this.convergeThreshold = convergeThreshold;
	}

	public void setEvalKArrayStr(String evalKArrayStr) {
		this.evalKArrayStr = evalKArrayStr;
	}
	
	public void setEvalThreadNum(String evalThreadNum) {
		this.evalThreadNum = evalThreadNum;
	}
	
	public void setFeatureNum(int featureNum) {
		this.featureNum = featureNum;
	}

	public void setItemFeatureInitFile(File itemFeatureInitFile) {
		this.itemFeatureInitFile = itemFeatureInitFile;
	}

	public void setItemNum(int itemNum) {
		this.itemNum = itemNum;
	}

	public void setLambda(float lambda) {
		this.lambda = lambda;
	}

	public void setLambdaItem(float lambdaItem) {
		this.lambdaItem = lambdaItem;
	}

	public void setLambdaUser(float lambdaUser) {
		this.lambdaUser = lambdaUser;
	}

	public void setLearningRate(float learningRate) {
		this.learningRate = learningRate;
	}
	
	public void setMaxIterationNum(Integer maxIterationNum) {
		this.maxIterationNum = maxIterationNum;
	}
	
	public void setMaxLearningRate(float maxLearningRate) {
		this.maxLearningRate = maxLearningRate;
	}
	
	public void setMinLearningRate(float minLearningRate) {
		this.minLearningRate = minLearningRate;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}
	
	public void setModelRRMF(String modelRRMF) {
		this.modelRRMF = modelRRMF;
	}
	
	public void setOptions(Properties options) {
		this.options = options;
	}
	
	public void setOutputPath(File outputPath) {
		this.outputPath = outputPath;
	}

	public void setTestFile(File testFile) throws CheckinException{
		this.testFile = testFile;
		Utils.exists(testFile, "[TestFile]");
	}

	public void setTrainFile(File trainFile) throws CheckinException {
		this.trainFile = trainFile;
		Utils.exists(trainFile, "[TrainFile]");
	}

	public void setUserFeatureInitFile(File userFeatureInitFile) {
		this.userFeatureInitFile = userFeatureInitFile;
	}

	public void setUserNum (int userNum) {
		this.userNum = userNum;
	}

	public void load(File inputFile) throws CheckinException {
		Utils.exists(inputFile, "Config File");

		BufferedReader confReader = null;
		try {
			confReader = new BufferedReader(new InputStreamReader(
							new FileInputStream(inputFile)));
			options    = new Properties();
			options.load(confReader);

			String sprop = options.getProperty(CheckinConstants.STRING_OUTPUT_PATH);
			if (sprop != null) {
				if (! "".equals(sprop)) setOutputPath(new File(sprop));
				options.remove(CheckinConstants.STRING_OUTPUT_PATH);
			}
		
			sprop = options.getProperty(CheckinConstants.STRING_TRAIN_FILE);
			if (sprop != null) {
				if (! "".equals(sprop)) setTrainFile(new File(sprop));
				options.remove(CheckinConstants.STRING_TRAIN_FILE);
			}

			sprop = options.getProperty(CheckinConstants.STRING_TEST_FILE);
			if (sprop != null) {
				if (! "".equals(sprop)) setTestFile(new File(sprop));
				options.remove(CheckinConstants.STRING_TEST_FILE);
			}
	
			sprop = options.getProperty(CheckinConstants.STRING_USER_FEATURE_INIT_FILE);
			if (sprop != null) {
				if (! "".equals(sprop)) setUserFeatureInitFile(new File(sprop));
				options.remove(CheckinConstants.STRING_USER_FEATURE_INIT_FILE);
			}

			sprop = options.getProperty(CheckinConstants.STRING_ITEM_FEATURE_INIT_FILE);
			if (sprop != null) {
				if (! "".equals(sprop)) setItemFeatureInitFile(new File(sprop));
				options.remove(CheckinConstants.STRING_ITEM_FEATURE_INIT_FILE);
			}

			sprop = options.getProperty(CheckinConstants.STRING_FEATURE_NUM);
			if (sprop != null) {
				if (! "".equals(sprop)) setFeatureNum(Integer.parseInt(sprop));
				options.remove(CheckinConstants.STRING_FEATURE_NUM);
			} else {
				System.err.println("No specified feature num, using default value instead.");
			}
		
			sprop = options.getProperty(CheckinConstants.STRING_ITEM_NUM);
			if (sprop != null) {
				if (! "".equals(sprop)) setItemNum(Integer.parseInt(sprop));
				options.remove(CheckinConstants.STRING_ITEM_NUM);
			} else {
				throw new CheckinException("No item num specified.");
			}
	
			sprop = options.getProperty(CheckinConstants.STRING_USER_NUM);
			if (sprop != null) {
				if (! "".equals(sprop)) setUserNum(Integer.parseInt(sprop));
				options.remove(CheckinConstants.STRING_USER_NUM);
			} else {
				throw new CheckinException("No user num specified.");
			}
	
			sprop = options.getProperty(CheckinConstants.STRING_CONVERGE_THRESHOLD);
			if (sprop != null) {
				if (! "".equals(sprop)) setConvergeThreshold(Float.parseFloat(sprop)); 
				options.remove(CheckinConstants.STRING_CONVERGE_THRESHOLD);
			}
	
			sprop = options.getProperty(CheckinConstants.STRING_MAX_ITERATION_NUM);
			if (sprop != null) {
				if (! "".equals(sprop)) setMaxIterationNum(Integer.parseInt(sprop));
				options.remove(CheckinConstants.STRING_MAX_ITERATION_NUM);
			}
			
			sprop = options.getProperty(CheckinConstants.STRING_LEARNING_RATE);
			if (sprop != null) {
				if (! "".equals(sprop)) setLearningRate(Float.parseFloat(sprop));
				options.remove(CheckinConstants.STRING_LEARNING_RATE);
			}
	
			sprop = options.getProperty(CheckinConstants.STRING_LAMBDA);
			if (sprop != null) {
				if (! "".equals(sprop)) setLambda(Float.parseFloat(sprop));
				options.remove(CheckinConstants.STRING_LAMBDA);
			}
	
			sprop = options.getProperty(CheckinConstants.STRING_LAMBDA_ITEM);
			if (sprop != null) {
				if (! "".equals(sprop)) setLambdaItem(Float.parseFloat(sprop));
				options.remove(CheckinConstants.STRING_LAMBDA_ITEM);
			}
	
			sprop = options.getProperty(CheckinConstants.STRING_LAMBDA_USER);
			if (sprop != null) {
				if (! "".equals(sprop)) setLambdaUser(Float.parseFloat(sprop));
				options.remove(CheckinConstants.STRING_LAMBDA_USER);
			}
			
			sprop = options.getProperty(CheckinConstants.STRING_MODEL_NAME);
			if (sprop != null) {
				if (! "".equals(sprop)) setModelName(sprop);
				options.remove(CheckinConstants.STRING_MODEL_NAME);
			} else {
				throw new CheckinException("Haven't specified model name.");
			}
			
			sprop = options.getProperty(CheckinConstants.STRING_EVAL_KS);
			if (sprop != null) {
				if (! "".equals(sprop)) setEvalKArrayStr(sprop);
				options.remove(CheckinConstants.STRING_EVAL_KS);
			}
			
			sprop = options.getProperty(CheckinConstants.STRING_EVAL_THREAD_NUM);
			if (sprop != null) {
				if (! "".equals(sprop)) setEvalThreadNum(sprop);
				options.remove(CheckinConstants.STRING_EVAL_THREAD_NUM);
			}
			
			sprop = options.getProperty(CheckinConstants.STRING_MODEL_RRFM);
			if (sprop != null) {
				if (! "".equals(sprop)) setModelRRMF(sprop);
				options.remove(CheckinConstants.STRING_MODEL_RRFM);
			}
			
			sprop = options.getProperty(CheckinConstants.STRING_MAX_LEARNING_RATE);
			if (sprop != null) {
				if (! "".equals(sprop)) setMaxLearningRate(Float.parseFloat(sprop));
				options.remove(CheckinConstants.STRING_MAX_LEARNING_RATE);
			}
			
			sprop = options.getProperty(CheckinConstants.STRING_MIN_LEARNING_RATE);
			if (sprop != null) {
				if (! "".equals(sprop)) setMinLearningRate(Float.parseFloat(sprop));
				options.remove(CheckinConstants.STRING_MIN_LEARNING_RATE);
			}
		} catch (NumberFormatException e) {
			throw new CheckinException (e.toString());
		} catch (IOException e) {
			throw new CheckinException (e.toString());
		} finally {
			Utils.cleanup(confReader);
		}
	}

	public boolean store(File outputFile) {
		if (outputFile != null) {
			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new OutputStreamWriter(
							new FileOutputStream(outputFile)));
				writer.write(toString());
				writer.newLine();
				if (options.size() > 0) {
					writer.newLine();
					options.store(writer, "#[Other Options]");
				}
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				Utils.cleanup(writer);
			}
		}

		return false;
	}

	@Override
	public String toString() {
		String str = String.format(FORMAT_PARAMETERS,
				Utils.wrapPath(outputPath),
				Utils.wrapPath(trainFile),
				Utils.wrapPath(testFile),
				modelName,
				convergeThreshold,
				maxIterationNum == null ? "": maxIterationNum,
				String.valueOf(learningRate),
				maxLearningRate == null ? "" : maxLearningRate,
				minLearningRate == null ? "" : minLearningRate,
				featureNum,
				userNum,
				itemNum,
				lambda,
				lambdaItem,
				lambdaUser,
				Utils.wrapPath(userFeatureInitFile),
				Utils.wrapPath(itemFeatureInitFile),
				wrap(evalKArrayStr),
				evalThreadNum,
				wrap(modelRRMF)
				);
		str += "\n";
		for (String key : options.stringPropertyNames()) {
			str += key + "=" + options.getProperty(key) + "\n";
		}
		return str;
	}

	@Override
	public ParamManager clone() {
		ParamManager paramManager = new ParamManager();

		try {
			paramManager.setOutputPath(outputPath);
			paramManager.setTrainFile(trainFile);
			paramManager.setTestFile(testFile);
			paramManager.setConvergeThreshold(convergeThreshold);
			paramManager.setFeatureNum(featureNum);
			paramManager.setUserNum(userNum);
			paramManager.setItemNum(itemNum);
			paramManager.setLambda(lambda);
			paramManager.setLambdaItem(lambdaItem);
			paramManager.setLambdaUser(lambdaUser);
			paramManager.setUserFeatureInitFile(userFeatureInitFile);
			paramManager.setItemFeatureInitFile(itemFeatureInitFile);
			paramManager.setMaxIterationNum(maxIterationNum);
			paramManager.setLearningRate(learningRate);
			paramManager.setMaxLearningRate(maxLearningRate);
			paramManager.setMinLearningRate(minLearningRate);
			paramManager.setModelName(modelName);
			paramManager.setEvalKArrayStr(evalKArrayStr);
			paramManager.setEvalThreadNum(evalThreadNum);
			paramManager.setModelRRMF(modelRRMF);
			paramManager.setOptions(options);
		} catch (CheckinException e) {
			// never reaches
			e.printStackTrace();
		}

		return paramManager;
	}
	
	private String wrap(String str) {
		return str == null ? "" : str;
	}
}

