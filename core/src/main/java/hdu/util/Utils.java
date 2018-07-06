package hdu.util;

import com.google.common.collect.BiMap;
import net.librec.math.structure.DenseMatrix;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

public class Utils {
	// the following is to calculate the distance, given the LatLng
	private static final int EARTH_RADIUS = 6371;
	public static double calDistance(double startLat, double startLng,
						double endLat, double endLng) {
		double lat0 = Math.toRadians(startLat);
		double lat1 = Math.toRadians(endLat);
		double lng0 = Math.toRadians(startLng);
		double lng1 = Math.toRadians(endLng);

		double dlng = Math.abs(lng0 - lng1);
		double dlat = Math.abs(lat0 - lat1);
		double h    = hav(dlat) + Math.cos(lat0) * Math.cos(lat1) * hav(dlng);
		return  2 * EARTH_RADIUS * Math.asin(Math.sqrt(h));
	}

	public static double hav(double theta) {
		double s = Math.sin (theta / 2.0);
		return s * s;
	}
	
	public static double cal1norm(double[] array) {
		double sum = 0;
		for (double v : array) sum += Math.abs(v);
		return sum;
	}
	
	public static double cal2norm(double[] array) {
		double sum = 0;
		for (double v : array) sum += v * v;
		return sum;
	}
	
	public static int calLineNumInFile(File inputFile) throws CheckinException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
						new FileInputStream(inputFile)));
			int lineNum = 0;
			while (reader.readLine() != null) lineNum++;
			reader.close();
			return lineNum;
		} catch (IOException e) {
			throw new CheckinException(e);
		} finally {
			cleanup(reader);
		}
	}

	public static void cleanup(InputStream in) {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				//e.printStackTrace();
			}
		}
	}

	public static void cleanup(Reader reader) {
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
				// to do nothing
			}
		}
	}

	public static void cleanup(Writer writer) {
		if (writer != null) {
			try {
				writer.close();
			} catch (IOException e) {
				// to do nothing
			}
		}
	}
	
	public static void check(Properties options, String key) throws CheckinException {
		if (options.get(key) == null) {
			throw new CheckinException("You have not specified option -" + key);
		}
	}

	public static String convertToString(double[] array, String spacer) {
		StringBuffer buffer = new StringBuffer();
		String sp = (spacer == null ? " " : spacer);
		for (int i = 0; i < array.length; i ++) {
			buffer.append(array[i]);
			if (i != array.length - 1) {
				buffer.append(spacer == null ? " " : sp);
			}
		}
		return buffer.toString();
	}

	public static float[] convert2DTo1D(float[][] matrix) {
		if (matrix == null) return null;
		float[] array = new float[matrix.length * matrix[0].length];
		int index     = 0;
		for (int row = 0; row < matrix.length; row ++) {
			for (int column = 0; column < matrix[row].length; column ++) {
				array[index] = matrix[row][column];
				index ++;
			}
		}
		return array;
	}

	public static double[] convert2DTo1D(double[][] matrix) {
		if (matrix == null) return null;
		double[] array = new double[matrix.length * matrix[0].length];
		int index      = 0;
		for (int row = 0; row < matrix.length; row ++) {
			for (int column = 0; column < matrix[row].length; column ++) {
				array[index] = matrix[row][column];
				index ++;
			}
		}
		return array;
	}

	// convert as 1 / (1 + x^-1)
	public static void convertRatingFile(File inputRatingFile,int columnNum, 
			int userIDIndex, int placeIDIndex, int ratingIndex,
			File outputRatingFile) throws CheckinException {
		Utils.exists(inputRatingFile);
		BufferedReader reader = null;
		BufferedWriter writer = null;
		try {
			if (! outputRatingFile.getParentFile().exists()) {
				outputRatingFile.getParentFile().mkdirs();
			}
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputRatingFile)));
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(inputRatingFile)));
			String line     = null;
			int instanceNum = 0;
			while ((line = reader.readLine()) != null) {
				String[] array = Utils.parseLine(line, columnNum);
				int userID  = Integer.parseInt(array[userIDIndex]);
				int placeID = Integer.parseInt(array[placeIDIndex]);
				float rating = Float.parseFloat(array[ratingIndex]);
				float convertedRating = 1.0f / (1 + 1.0f / rating);
				if (convertedRating <= 0 || convertedRating >= 1 ||
					Float.isInfinite(convertedRating) ||
					Float.isNaN(convertedRating)) {
					reader.close(); reader = null;
					writer.close(); writer = null;
					throw new CheckinException(
						"Invalid Converted Rating: Rating=%s, ConvertedRating=%s",
						rating, convertedRating);
				}
				writer.write(String.format("%s\t%s\t%s",
						userID, placeID, convertedRating));
				writer.newLine();
				instanceNum ++;
			}
			writer.close();
			reader.close();
			System.out.println("InstanceNum=" +instanceNum);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			Utils.cleanup(reader);
		}
	}

	public static double[] copy(double[] array) {
		if (array == null) return null;
		
		double[] newArr = new double[array.length];
		for (int i = array.length - 1; i >= 0; i --) {
			newArr[i] = array[i];
		}
		return newArr;
	}
	
	public static boolean copyFile(File from, File dest) {
		if (from == null || dest == null || ! from.exists()) return false;

		BufferedReader reader = null;
		BufferedWriter writer = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(from)));
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(dest)));
			int ch = -1;
			while ((ch = reader.read()) != -1) {
				writer.write(ch);
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			Utils.cleanup(reader);
			Utils.cleanup(writer);
		}
		return false;
	}

	// Assume placeID_1 and placeID_2 have been sorted.
	public static double cosineSimilarity(int[] placeID_1,
				int[] checkinNum_1, int[] placeID_2,
						int[] checkinNum_2) {
		if (placeID_1 == null || placeID_1.length == 0 ||
				placeID_2 == null || placeID_2.length == 0) {
			return 0;
		}
		int index1      = 0;
		int index2      = 0;
		double sum      = 0;
		double square_1 = 0;
		double square_2 = 0;
		while (index1 < placeID_1.length && index2 < placeID_2.length) {
			if (placeID_1[index1] < placeID_2[index2]) {
				square_1 += checkinNum_1[index1] *
						checkinNum_1[index1];
				index1 ++;
			} else
			if (placeID_2[index2] < placeID_1[index1]) {
				square_2 += checkinNum_2[index2] *
						checkinNum_2[index2];
				index2 ++;
			} else {
				sum      += checkinNum_1[index1] *
						checkinNum_2[index2];
				square_1 += checkinNum_1[index1] *
						checkinNum_1[index1];
				square_2 += checkinNum_2[index2] *
						checkinNum_2[index2];
				index1 ++;
				index2 ++;
			}
		}
		if (sum == 0) return 0;
		if (index1 < placeID_1.length) {
			for (; index1 < placeID_1.length; index1 ++) {
				square_1 += checkinNum_1[index1] *
						checkinNum_1[index1];
			}
		}
		if (index2 < placeID_2.length) {
			for (; index2 < placeID_2.length; index2 ++) {
				square_2 += checkinNum_2[index2] *
						checkinNum_2[index2];
			}
		}
		return sum / Math.sqrt(square_1) / Math.sqrt(square_2);
	}
	
	// Assume placeID_1 and placeID_2 have been sorted.
	public static double cosineSimilarity(int[] placeID_1, int[] placeID_2) {
		if (placeID_1 == null || placeID_1.length == 0 ||
				placeID_2 == null || placeID_2.length == 0) {
			return 0;
		}
		int commonNum = 0;
		int index1    = 0;
		int index2    = 0;
		while (index1 < placeID_1.length && index2 < placeID_2.length) {
			if (placeID_1[index1] < placeID_2[index2]) {
				index1 ++;
			} else
			if (placeID_2[index2] < placeID_1[index1]) {
				index2 ++;
			} else {
				index1 ++;
				index2 ++;
				commonNum++;
			}
		}
		if (commonNum == 0) return 0;
		return commonNum / Math.sqrt(placeID_1.length) /
				Math.sqrt(placeID_2.length);
	}

	public static double cosineSimilarity(Set<Integer> placeSet_1,
				Set<Integer> placeSet_2) {
		if (placeSet_1 == null || placeSet_1.size() == 0 ||
			placeSet_2 == null || placeSet_2.size() == 0)
			return 0;
		Set<Integer> smallSet = placeSet_1;
		Set<Integer> largeSet = placeSet_2;
		if (placeSet_1.size() > placeSet_2.size()) {
			smallSet = placeSet_2;
			largeSet = placeSet_1;
		}
		int commonNum = 0;
		for (int ID : smallSet) {
			if (largeSet.contains(ID)) commonNum ++;
		}
		if (commonNum == 0) return 0;
		return commonNum / Math.sqrt(smallSet.size()) /
				Math.sqrt(largeSet.size()); 
	}

	public static boolean delete (File file) {
		if (file.isDirectory()) {
			boolean res = true;
			File[] files = file.listFiles();
			for (File subFile : files) {
				boolean r = delete(subFile);
				if (res && ! r) res = r;
			}
			file.delete();
			return res;
		} else {
			return file.delete();
		}
	}

	// Format: userId itemId rating
	public static void encode(File[] inputFiles, File[] outputFiles,
				String delimiter, boolean isSorted) throws CheckinException {
		BufferedReader reader = null;
		BufferedWriter writer = null;
		try {
			Map<Integer, Integer> userDicMap = new HashMap<Integer, Integer>();
			Map<Integer, Integer> itemDicMap = new HashMap<Integer, Integer>();
			for (int i = 0; i < inputFiles.length; i ++) {
				File inputFile  = inputFiles[i];
				File outputFile = outputFiles[i];
				if (inputFile == null || !inputFile.exists()) {
					System.err.println("File not exist: " + 
						(inputFile==null ? "null":inputFile.getAbsolutePath()));
					continue;
				}
				if (outputFile == null) {
					throw new CheckinException("No output file specified.");
				}
				if (!outputFile.getParentFile().exists())
					outputFile.getParentFile().mkdirs();
				reader = new BufferedReader(new InputStreamReader(
							new FileInputStream(inputFile)));
				writer = new BufferedWriter(new OutputStreamWriter(
							new FileOutputStream(outputFile)));
				String line     = null;
				int dupRecodNum = 0;
				Map<Integer, Map<Integer, String>> userItemMap =
						new HashMap<Integer, Map<Integer, String>>();
				while ((line = reader.readLine()) != null) {
					String[] array = line.split(delimiter);
					if (array.length != 3) throw new CheckinException(
							"Incorrect format: " + line);
					int userId = Integer.parseInt(array[0]);
					int itemId = Integer.parseInt(array[1]);
					
					int encodedUserId = userDicMap.size();
					if (userDicMap.containsKey(userId)) {
						encodedUserId = userDicMap.get(userId);
					} else {
						userDicMap.put(userId, encodedUserId);
					}
					int encodedItemId = itemDicMap.size();
					if (itemDicMap.containsKey(itemId)) {
						encodedItemId = itemDicMap.get(itemId);
					} else {
						itemDicMap.put(itemId, encodedItemId);
					}
					
					// check duplicate
					Map<Integer, String> encodedItemSet =
										userItemMap.get(encodedUserId);
					if (encodedItemSet == null) {
						encodedItemSet = new HashMap<Integer, String>();
						userItemMap.put(encodedUserId, encodedItemSet);
					}
					if (encodedItemSet.containsKey(encodedItemId)) {
						dupRecodNum ++;
						continue;
					} else {
						encodedItemSet.put(encodedItemId, array[2]);
					}
					
					if (! isSorted) {
						writer.write(String.format("%s\t%s\t%s",
								encodedUserId, encodedItemId, array[2]));
						writer.newLine();
					}
				}
				reader.close();
				if (isSorted) {
					int[] sortedUserArr = getSortedKeys(userItemMap.keySet());
					for (int userId : sortedUserArr) {
						Map<Integer, String> itemMap = userItemMap.get(userId);
						int[] itemArr = Utils.getSortedKeys(itemMap.keySet());
						for (int itemId : itemArr) {
							writer.write(String.format("%s\t%s\t%s",
									userId, itemId, itemMap.get(itemId)));
							writer.newLine();
						}
					}
				}
				writer.close();
				if (dupRecodNum > 0) {
					System.err.println(i + "th file: duplicateRecodNum = " + dupRecodNum);
				}
			}
			System.out.println("UserNum : " + userDicMap.size());
			System.out.println("ItemNum : " + itemDicMap.size());
		} catch (IOException e) {
			throw new CheckinException(e);
		} finally {
			Utils.cleanup(reader);
			Utils.cleanup(writer);
		}
	}
	
	public static boolean exists(File file) throws CheckinException {
		if (file == null) throw new CheckinException("File is null");
		if (! file.exists()) throw new CheckinException(
				"File does not exist: " + file.getAbsolutePath());
		return true;
	}

	public static boolean exists(File file, String prefix) throws CheckinException {
		if (file == null) throw new CheckinException("%s is null", prefix);
		if (! file.exists()) throw new CheckinException(
				"%s does not exist:%s ", prefix, file.getAbsolutePath());
		return true;
	}

	public static void fills(double[] array, double value) {
		for (int i = 0; i < array.length; i ++)
			array[i] = value;
	}

	// Return the number of digits after the decimal point
	public static int getDecimalDigitNum(String value) {
		Double.parseDouble(value);
		int index = value.indexOf('.');
		if (index > 0) {
			return value.length() - index - 1;
		}
		return 0;
	}

	public static int getDecimalDigitNum(double value) {
		return getDecimalDigitNum(String.valueOf(value));
	}

	public static double getRoundValue(double value, int scale) {
		return new BigDecimal(value).setScale(scale,
				BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	// keepDecimalDigitNum must be larger than or equal to 0
	public static String getRoundStringValue(double value, int keepDecimalDigitNum) {
		String zero = "";
		for (int i = 0; i < keepDecimalDigitNum; i ++)
			zero += "0";
		DecimalFormat df = new DecimalFormat("0." + zero);
		df.setRoundingMode(RoundingMode.HALF_UP);
		String str = df.format(value);
		
		int dotIndex   = str.indexOf(".");
		int decimalNum = str.length() - dotIndex - 1;
		if (dotIndex == - 1 || decimalNum != keepDecimalDigitNum) {
			throw new RuntimeException(String.format(
					"Value error : value=%s, result=%s",
					value, str));
		}
		
		return str;
	}

	public static double getFloorValue(double value, int scale) {
		double origValue = value;
		int base         = 1;
		for (int i = scale; i >0; i --)
			base *= 10;
		value  = (int) (value * base);
		value /= base;
		String str = String.valueOf(value);
		int s      = str.indexOf(".");
		if (str.length() - s - 1 > 2) {
			System.err.println(String.format(
				"Precision error : scale=%s, value=%s, floorres=%s, str=%s",
				scale, origValue, value, str));
			str = String.format("%." + scale + "f", value);
		}
		return value;
	}

	@SuppressWarnings("unchecked")
	public static Set<Integer>[] getNotFoundIds(Map<Integer, Map<Integer, Double>>
				trainUserRatingMap, int userNum, int itemNum) {
		if (trainUserRatingMap == null || trainUserRatingMap.size() == 0)
				return null;
		boolean userStatus[] = new boolean[userNum];
		boolean itemStatus[] = new boolean[itemNum];
		for (int userId : trainUserRatingMap.keySet()) {
			userStatus[userId] = true;
			Map<Integer, Double> itemRatingMap = trainUserRatingMap.get(userId);
			for (int itemId : itemRatingMap.keySet()) {
				if (! itemStatus[itemId])
					itemStatus[itemId] = true;
			}
		}
		Set<Integer> notFoundUserSet = new HashSet<Integer>();
		Set<Integer> notFoundItemSet = new HashSet<Integer>();
		for (int i = 0; i < userStatus.length; i ++) {
			if (! userStatus[i]) notFoundUserSet.add(i);
		}
		for (int i = 0; i < itemStatus.length; i ++) {
			if (! itemStatus[i]) notFoundItemSet.add(i);
		}
		System.out.println("UserNotFoundNum = " + notFoundUserSet.size());
		System.out.println("ItemNotFoundNum = " + notFoundItemSet.size());

		Set<Integer>[] result = new Set[2];
		if (notFoundUserSet.size() == 0) result[0] = null;
		else result[0] = notFoundUserSet;
		
		if (notFoundItemSet.size() == 0) result[1] = null;
		else result[1] = notFoundItemSet;
		return result;
	}

	public static double getMeanValue(Map<Integer, Map<Integer, Double>> trainUserRatingMap) {
		double mean = 0;
		int count   = 0;
		for (int userId : trainUserRatingMap.keySet()) {
			Map<Integer, Double> itemRatingMap = trainUserRatingMap
								.get(userId);
			for (int itemId : itemRatingMap.keySet()) {
				mean += itemRatingMap.get(itemId);
				count ++;
			}
		}
		return mean / (count + 0.0);
	}

	public static String[] getSortedNumberString(Set<String> set) {
		if (set == null || set.size() == 0) return null;
		String sortedArray[] = new String[set.size()];
		int index = 0;
		for (String value : set)
			sortedArray[index ++] = value;
		Arrays.sort(sortedArray, new Comparator<String>() {
			@Override
			public int compare(String str1, String str2) {
				double v1 = Double.parseDouble(str1);
				double v2 = Double.parseDouble(str2);
				if (v1  > v2) {
					return 1;
				} else
				if (v1 < v2) {
					return -1;
				} else {
					return 0;
				}
			}
		});
		return sortedArray;
	}

	public static int[] getSortedKeys(Set<Integer> keys) {
		int[] array = new int[keys.size()];
		int index   = 0;
		for (Integer key : keys) {
			array[index] = key;
			index ++;
		}
		Arrays.sort(array);
		return array;
	}

	public static long[] getSortedKeysLong(Set<Long> keys) {
		long[] array = new long[keys.size()];
		int index    = 0;
		for (long key : keys) {
			array[index] = key;
			index ++;
		}
		Arrays.sort(array);
		return array;
	}

	public static double[] getSortedDoubleKeys(Set<Double> keys) {
		double[] array = new double[keys.size()];
		int index      = 0;
		for (double key : keys) {
			array[index] = key;
			index ++;
		}
		Arrays.sort(array);
		return array;
	}

	public static int getStoredFileCount(int userNum, int maxUserNumPerFile) {
		int count = userNum / maxUserNumPerFile;
		if (userNum % maxUserNumPerFile != 0) count ++;
		return count;
	}

	public static int getStoredFileIndex(int userId, int maxUserNumPerFile) {
		return (int)(userId / maxUserNumPerFile); 
	}

	public static int getStoredLineNum(int userId, int locationId,
					int maxUserNumPerFile, int locationNum) {
		int fileIndex = getStoredFileIndex(userId, maxUserNumPerFile);
		return getStoredLineNum(userId, locationId, fileIndex,
				maxUserNumPerFile, locationNum);
	}

	public static int getStoredLineNum(int userId, int locationId,
			int fileIndex, int maxUserNumPerFile, int locationNum) {
		int convertedUserIndex = userId - fileIndex * maxUserNumPerFile;
		return convertedUserIndex * locationNum + locationId + 1;
	}

	public static boolean[] increase(boolean[] array, int incrementSize) {
		return increase(array, incrementSize, false);
	}

	public static boolean[] increase(boolean[] array, int incrementSize,
					boolean incrementStatus) {
		if (incrementSize <= 0) return array;
		if (array == null) {
			if (! incrementStatus) return new boolean[incrementSize];
			boolean[] newarray = new boolean[incrementSize];
			for (int i = 0; i < newarray.length; i ++)
				newarray[i] = true;
			return newarray;
		} else {
			boolean[] newarray = new boolean[array.length + incrementSize];
			for (int i = 0; i < array.length; i ++)
				newarray[i] = array[i];
			if (incrementStatus) {
				for (int i = 0; i < incrementSize; i ++)
					newarray[i + array.length] = true;
			}
			return newarray;
		}
	}

	public static double[][] inverse(double[][] matrix) {
		return inverse(new BlockRealMatrix(matrix)).getData();
	}

	public static RealMatrix inverse(RealMatrix matrix) {
		int splitIndex = matrix.getColumnDimension() > matrix.getRowDimension() ?
					matrix.getRowDimension() : matrix.getColumnDimension();
		splitIndex = splitIndex == 2 ? 0 : splitIndex / 2;
		return MatrixUtils.blockInverse(matrix, splitIndex);
	}

	public static Properties loadParams(File paramFile) throws CheckinException {
		if (! Utils.exists(paramFile)) {
			throw new CheckinException("Param file does not exist.[%s]",
				paramFile == null ? null : paramFile.getAbsolutePath());
		}
		try {
			Properties props = new Properties();
			props.load(new FileInputStream(paramFile));
			return props;
		} catch (IOException e) {
			throw new CheckinException(e.toString());
		}
	}

	public static String loadString(File inputFile) throws CheckinException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(inputFile)));
			String line = reader.readLine();
			if (reader.readLine() != null) {
				reader.close();
				reader = null;
				throw new CheckinException("Has more than one lines for file %s",
						inputFile.getAbsolutePath());
			}
			reader.close();
			return line;
		} catch (IOException e) {
			throw new CheckinException(e);
		}
	}

	public static int[] loadIntArray(File inputFile, String delimiter)
						throws CheckinException {
		Utils.exists(inputFile);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(inputFile)));
			String line  = null;
			int[] result = null;
			while ((line = reader.readLine())!= null) {
				if ("".equals(line = line.trim())) continue;
				String[] array = line.split(delimiter);
				result         = new int[array.length];
				for (int i = 0; i < result.length; i ++) {
					result[i] = Integer.parseInt(array[i]);
				}
				break;
			}
			return result;
		} catch (IOException e) {
			throw new CheckinException(e);
		} finally {
			Utils.cleanup(reader);
		}
	}

	// each line is a double value
	public static double[] loadDoubleArray(File inputFile)
					throws CheckinException {
		Utils.exists(inputFile);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(inputFile)));
			String line       = null;
			List<Double> list = new ArrayList<Double>();
			while ((line = reader.readLine())!= null) {
				if ("".equals(line = line.trim())) continue;
				list.add(Double.parseDouble(line));
			}
			double result[] = new double[list.size()];
			for (int i = 0; i < list.size(); i ++)
				result[i] = list.get(i);
			return result;
		} catch (IOException e) {
			throw new CheckinException(e);
		} finally {
			Utils.cleanup(reader);
		}
	}

	public static int[][] load2IntArray(File inputFile, String spacer)
						throws CheckinException {
		Utils.exists(inputFile);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(inputFile)));
			String line = null;
			List<int[]> list  = new ArrayList<int[]>();
			Integer columnNum = null;
			int lineNum       = 0;
			while ((line = reader.readLine()) != null) {
				lineNum += 1;
				if ("".equals(line = line.trim())) continue;
				String[] array = line.split(spacer);
				int[] intarr   = new int[array.length];
				for (int i = 0; i < intarr.length; i ++)
					intarr[i] = Integer.parseInt(array[i]);
				if (columnNum == null) columnNum = array.length;
				else if (columnNum != array.length) {
					reader.close();
					reader = null;
					throw new CheckinException(
						"LineNum %s: Format is incorrect: columnNum=%s, parsedNum=%s",
						lineNum, columnNum, array.length);
				}
				list.add(intarr);
			}
			int[][] result = new int[list.size()][columnNum];
			for (int i = 0; i < list.size(); i ++) {
				int[] array = list.get(i);
				for (int j = 0; j < columnNum; j ++) {
					result[i][j] = array[j];
				}
			}
			return result;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			Utils.cleanup(reader);
		}
		return null;
	}

	public static float[][] load2FloatArray(File inputFile, String spacer)
				throws CheckinException {
		List<float[]> arrayList = loadFloatArrayList(inputFile, spacer);
		if (arrayList != null && arrayList.size() > 0) {
			int rowLen      = arrayList.size();
			int columnLen   = 0;
			float[][] res  = null;
			for (int i = 0; i < arrayList.size(); i ++) {
			if (arrayList.get(i) == null) throw new CheckinException("Array format it not correct");
				if (i == 0) {
					columnLen = arrayList.get(i).length;
					res       = new float[rowLen][columnLen];
				}
				if (columnLen != arrayList.get(i).length) {
					throw new CheckinException ("Array format is not correct.");
				}
				for (int column = 0; column < columnLen; column ++) {
					res[i][column] = arrayList.get(i)[column];
				}
			}
			return res;
		}
		return null;
	}

	public static double[][] load2DoubleArray(File inputFile,
			String delimiter) throws CheckinException{
		exists(inputFile);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(inputFile)));
			String line   = null;
			int lineNum   = 0;
			int rowNum    = 0;
			int columnNum = -1;
			while ((line =reader.readLine()) != null) {
				if ("".equals(line = line.trim())) continue;
				String[] array = line.split(delimiter);
				if (columnNum < 0) columnNum = array.length;
				else if (columnNum != array.length) {
					reader.close();
					reader = null;
					throw new CheckinException("LineNum %s: column num is not equal to the one in the first line - ColNumInFirst=%s. ColNumCurr=%s",
						lineNum, columnNum, array.length);
				}
				rowNum = rowNum + 1;
			}
			reader.close();
			reader    = new BufferedReader(new InputStreamReader(
					new FileInputStream(inputFile)));
			line      = null;
			double[][] result = new double[rowNum][columnNum];
			rowNum    = 0;
			while ((line = reader.readLine()) != null) {
				if ("".equals(line = line.trim())) continue;
				String array[] = line.split(delimiter);
				for (int i = 0; i < array.length; i ++) {
					result[rowNum][i] = Double.parseDouble(array[i]);
				}
				rowNum ++;
			}
			reader.close();
			return result;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			cleanup(reader);
		}
		return null;
	}

	public static Map<Integer, Integer> loadMap(File inputFile,
					String delimeter) throws CheckinException {
		Utils.exists(inputFile);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(inputFile)));
			String line = null;
			Map<Integer, Integer> map = new HashMap<Integer, Integer>();
			while ((line = reader.readLine()) != null) {
				if ("".equals(line = line.trim())) continue;
				String array[] = line.split(delimeter);
				if (array.length != 2) {
					reader.close();
					reader = null;
					throw new CheckinException(
						"Format is incorrect: requiredsize=%s, parsedsize=%s\n%s",
						2, array.length, line);
				}
				int key   = Integer.parseInt(array[0]);
				int value = Integer.parseInt(array[1]); 
				if (map.containsKey(key)) {
					reader.close();
					reader = null;
					throw new CheckinException("Key %s has existed in %s",
						key, inputFile.getAbsolutePath());
				}
				map.put(key, value);
			}
			return map;
		} catch (IOException e) {
			throw new CheckinException(e);
		} finally {
			Utils.cleanup(reader);
		}
	}

	public static Map<Integer, Double> loadMapWithIntDoube(File inputFile,
			String delimeter) throws CheckinException {
		Utils.exists(inputFile);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(inputFile)));
			String line = null;
			Map<Integer, Double> map = new HashMap<Integer, Double>();
			while ((line = reader.readLine()) != null) {
				if ("".equals(line = line.trim())) continue;
				String array[] = line.split(delimeter);
				if (array.length != 2) {
					reader.close();
					reader = null;
					throw new CheckinException(
						"Format is incorrect: requiredsize=%s, parsedsize=%s\n%s",
						2, array.length);
				}
				int key      = Integer.parseInt(array[0]);
				double value = Double.parseDouble(array[1]); 
				if (map.containsKey(key)) {
					reader.close();
					reader = null;
					throw new CheckinException("Key %s has existed in %s",
						key, inputFile.getAbsolutePath());
				}
				map.put(key, value);
			}
			return map;
		} catch (IOException e) {
			throw new CheckinException(e);
		} finally {
			Utils.cleanup(reader);
		}
	}

	public static Map<String, Integer> loadMapWithStringInt(File inputFile,
					String delimeter) throws CheckinException {
		Utils.exists(inputFile);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(inputFile)));
			String line = null;
			Map<String, Integer> map = new HashMap<String, Integer>();
			while ((line = reader.readLine()) != null) {
				if ("".equals(line = line.trim())) continue;
				String array[] = line.split(delimeter);
				if (array.length != 2) {
					reader.close();
					reader = null;
					throw new CheckinException(
						"Format is incorrect: requiredsize=%s, parsedsize=%s\n%s",
						2, array.length);
				}
				String key = array[0];
				int value  = Integer.parseInt(array[1]); 
				if (map.containsKey(key)) {
					reader.close();
					reader = null;
					throw new CheckinException("Key %s has existed in %s",
						key, inputFile.getAbsolutePath());
				}
				map.put(key, value);
			}
			return map;
		} catch (IOException e) {
			throw new CheckinException(e);
		} finally {
			Utils.cleanup(reader);
		}
	}

	public static Map<String, String> loadMapWithStringString(File inputFile,
				String delimeter) throws CheckinException {
		Utils.exists(inputFile);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(inputFile)));
			String line = null;
			Map<String, String> map = new HashMap<String, String>();
			while ((line = reader.readLine()) != null) {
				if ("".equals(line = line.trim())) continue;
				String array[] = line.split(delimeter);
				if (array.length != 2) {
					reader.close();
					reader = null;
					throw new CheckinException(
						"Format is incorrect: requiredsize=%s, parsedsize=%s\n%s",
						2, array.length, line);
				}
				String key   = array[0];
				String value = array[1];
				if (map.containsKey(key)) {
					reader.close();
					reader = null;
					throw new CheckinException("Key %s has existed in %s",
						key, inputFile.getAbsolutePath());
				}
				map.put(key, value);
			}
			return map;
		} catch (IOException e) {
			throw new CheckinException(e);
		} finally {
			Utils.cleanup(reader);
		}
	}

	/*
	 * The file format is like: userId(locationId) \t lat \t lng
	 */
	public static Map<Integer, double[]> loadLocationDic(File userHomeDicFile,
			String delimiter, int indexId, int indexLat, int indexLng)
						throws CheckinException{
		Utils.exists(userHomeDicFile);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(userHomeDicFile)));
			String line = null;
			Map<Integer, double[]> userHomeDic =
					new HashMap<Integer, double[]>();
			while ((line = reader.readLine()) != null) {
				if ("".equals(line = line.trim())) continue;
				String[] array = line.split(delimiter);
				int userId     = Integer.parseInt(array[indexId]);
				double lat     = Double.parseDouble(array[indexLat]);
				double lng     = Double.parseDouble(array[indexLng]);
				if (userHomeDic.containsKey(userId)) {
					reader.close();
					reader = null;
					throw new CheckinException(
						"Failed to loadUserHomeDic: UserId %s has existed.",
						userId);
				}
				userHomeDic.put(userId, new double[]{lat, lng});
			}
			reader.close();
			return userHomeDic;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			Utils.cleanup(reader);
		}
		return null;
	}
	
	public static double[][] loadLatLngArr(File locationLatLngFile,
			int itemNum, boolean requireAllItem)
						throws CheckinException {
		Map<Integer, double[]> locationLatLngMap =
		Utils.loadLocationDic(locationLatLngFile,
			CheckinConstants.DELIMITER, 0, 1, 2);
		
		double[][] latlngArr = new double[itemNum][];
		for (int itemId = 0; itemId < itemNum; itemId ++) {
			double[] latlngs = locationLatLngMap.get(itemId);
			if (requireAllItem && latlngs == null) {
				throw new CheckinException(
					"Cannot find LatLng for itemId[%s]", itemId);
			}
			latlngArr[itemId]=latlngs;
		}
		return latlngArr;
	}


	public static double[][] loadLatLngArr(File locationLatLngFile,
					int itemNum) throws CheckinException {
		return loadLatLngArr(locationLatLngFile, itemNum, true);
	}

	public static Map<Integer, Map<Integer, Double>> loadRatingTuple2(
			File ratingFile, final int USER_NUM, final int ITEM_NUM)
					throws CheckinException {
		Utils.exists(ratingFile);
		BufferedReader reader = null;
		try {
			reader      = new BufferedReader(new InputStreamReader(
					new FileInputStream(ratingFile)));
			String line = null;
			Map<Integer, Map<Integer, Double>> userItemRatingMap =
					new HashMap<Integer, Map<Integer, Double>>();
			while ((line = reader.readLine()) != null) {
				if ((line = line.trim()).equals("")) continue;
				String[] array = line.split(CheckinConstants.DELIMITER);
				if (array.length != 3) {
					reader.close();
					reader = null;
					throw new CheckinException("Failed to parse test file : " + line);
				}
				int userId    = Integer.parseInt(array[0]);
				int itemId    = Integer.parseInt(array[1]);
				double rating = Double.parseDouble(array[2]);
				if (userId < 0 || userId >= USER_NUM) {
					reader.close();
					reader = null;
					throw new CheckinException(
						"Out of the user ids.[UserId=%s][UserNum=%s]",
						userId, USER_NUM);
				}
				if (itemId < 0 || itemId >= ITEM_NUM) {
					reader.close();
					reader = null;
					throw new CheckinException(
						"Out of item ids.[ItemId=%s][ItemNum=%s]",
						itemId, ITEM_NUM);
				}
				Map<Integer, Double> itemRatingMap =
						userItemRatingMap.get(userId);
				if (itemRatingMap == null) {
					itemRatingMap = new HashMap<Integer, Double>();
					userItemRatingMap.put(userId, itemRatingMap);
				}
				if (itemRatingMap.containsKey(itemId)) {
					reader.close();
					reader = null;
					throw new CheckinException("UserId=%s, ItemId=%s has existed in %s",
						userId, itemId, ratingFile.getAbsolutePath());
				}
				itemRatingMap.put(itemId, rating);
			}
			reader.close();
			return userItemRatingMap;
		} catch (IOException e) {
			throw new CheckinException(e);
		} finally {
			Utils.cleanup(reader);
		}
	}
	
	public static int[][] loadUserFriendPlaces(File userFriendPlacesFile,
			int userNum, int placeNum) throws CheckinException{
		return loadUserFriendPlaces(userFriendPlacesFile,userNum,placeNum,null);
	}

	public static int[][] loadUserFriendPlaces(File userFriendPlacesFile,
			int userNum, int placeNum, Set<Integer> validUserSet)
					throws CheckinException {
		Utils.exists(userFriendPlacesFile);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(userFriendPlacesFile)));
			String line      = null;
			int meanPlaceNum = 0;
			int nUser        = 0;
			int[][]userFriendPlaceIDs = new int[userNum][];
			while ((line = reader.readLine()) != null) {
				String[] array = line.split(CheckinConstants.DELIMITER);
				if (array.length < 1) {
					reader.close(); reader = null;
					throw new CheckinException(
						"Format Error: "+ line);
				}
				int userID = Integer.parseInt(array[0]);
				if (userID >= userNum) {
					reader.close(); reader = null;
					throw new CheckinException(
						"UserID[%s] >= userNum[%s]",
						userID, userNum);
				}
				if (validUserSet != null &&
						! validUserSet.contains(userID)) continue;
				if (userFriendPlaceIDs[userID] != null) {
					reader.close(); reader = null;
					throw new CheckinException(
						"UserFriendPlaceFile: userID dumplicated: ");
				}
				Set<Integer> placeIDs = new HashSet<Integer>();
				for (int i = 1; i < array.length; i ++) {
					int placeID = Integer.parseInt(array[i]);
					if (placeIDs.contains(placeID)) {
						reader.close(); reader = null;
						throw new CheckinException(
							"Dumplicated placeID[%s] for userID[%s]",
							placeID, userID);
					}
					if (placeID >= placeNum) {
						reader.close(); reader = null;
						throw new CheckinException(
							"placeID[%s] > placeNum[%s].",
							placeID, placeNum);
					}
					placeIDs.add(placeID);
				} // end for (int i = 1 ...)
				meanPlaceNum += placeIDs.size();
				userFriendPlaceIDs[userID] = getSortedKeys(placeIDs);
				nUser ++;
			} // end while
			reader.close();
			System.out.println("UserFriendMap:: UserNum = " + nUser);
			System.out.println("UserFriendMap:: MeanFriendPlaceNum = " +
												(meanPlaceNum + 0.0) / nUser);
			return userFriendPlaceIDs;
		} catch (IOException e) {
			e.printStackTrace();
			throw new CheckinException(e);
		} finally {
			Utils.cleanup(reader);
		}
	}
	
	public static Object[] loadUserDynamicCanPool(File userDynamicCanPool,
						int userNum, int placeNum, Set<Integer> validUserSet)
											throws CheckinException {
		Utils.exists(userDynamicCanPool);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(userDynamicCanPool)));
			String line      = null;
			int nUser        = 0;
			int meanPlaceNum = 0;
			List<int[]> dataList = new ArrayList<int[]>();
			@SuppressWarnings("unchecked")
			Map<Integer, Integer>[] userDataMap = new HashMap[userNum];
			while ((line = reader.readLine()) != null) {
				String[] array = line.split(CheckinConstants.DELIMITER);
				if (array.length <= 2) {
					reader.close(); reader = null;
					throw new CheckinException("Format Error: "+ line);
				}
				int userID = Integer.parseInt(array[0]);
				if (userID >= userNum) {
					reader.close(); reader = null;
					throw new CheckinException("UserID[%s] >= userNum[%s]",
						userID, userNum);
				}
				if (validUserSet != null &&
						! validUserSet.contains(userID)) continue;
				int itemID = Integer.parseInt(array[1]);
				Map<Integer, Integer> map = userDataMap[userID];
				if (map == null) {
					map = new HashMap<Integer, Integer>();
					userDataMap[userID] = map;
				}
				if (map.containsKey(itemID)) {
					reader.close(); reader = null;
					throw new CheckinException(
						"UserFriendPlaceFile: userID dumplicated: ");
				}
				map.put(itemID, dataList.size());
				Set<Integer> placeIDs = new HashSet<Integer>();
				for (int i = 2; i < array.length; i ++) {
					int placeID = Integer.parseInt(array[i]);
					if (placeIDs.contains(placeID)) {
						reader.close(); reader = null;
						throw new CheckinException(
							"Dumplicated placeID[%s] for userID[%s]",
								placeID, userID);
					}
					if (placeID >= placeNum) {
						reader.close(); reader = null;
						throw new CheckinException("placeID[%s]>placeNum[%s].",
							placeID, placeNum);
					}
					placeIDs.add(placeID);
					
				} // end for (int i = 1 ...)
				meanPlaceNum += placeIDs.size();
				dataList.add(getSortedKeys(placeIDs));
				nUser ++;
			} // end while
			reader.close();
			System.out.println("UserFriendMap:: UserNum = " + nUser);
			System.out.println("UserFriendMap:: MeanDynamicPlaceNum = " +
												(meanPlaceNum + 0.0) / nUser);
			int[][] dataArr = new int[dataList.size()][];
			for (int i = 0; i < dataList.size(); i ++) {
				dataArr[i] = dataList.get(i);
			}
			return new Object[]{userDataMap, dataArr};
		} catch (IOException e) {
			e.printStackTrace();
			throw new CheckinException(e);
		} finally {
			Utils.cleanup(reader);
		}
	}

	// Each line (after trim()) is an element
	public static Set<String> loadSet(File inputFile) {
		if (inputFile != null && inputFile.exists()) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(
						new FileInputStream(inputFile)));
				String line     = null;
				Set<String> set = new HashSet<String>();
				while ((line = reader.readLine()) != null) {
					if ("".equals(line = line.trim())) continue;
					if (set.contains(line)) {
						System.err.println(String.format(
							"%s has existed.", line));
					} else {
						set.add(line);
					}
				}
				return set;
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				Utils.cleanup(reader);
			}
		}
		return null;
	}

	public static Map<Integer, Set<Integer>> loadUserLocationMap(File inputFile,
			final int columnNum, final int userIDIndex,
			final int locationIDIndex) throws CheckinException {
		Utils.exists(inputFile);
		BufferedReader reader = null;
		try {
			reader      = new BufferedReader(new InputStreamReader(
					new FileInputStream(inputFile)));
			String line = null;
			Map<Integer, Set<Integer>> userLocationMap =
					new HashMap<Integer, Set<Integer>>();
			while ((line = reader.readLine()) != null) {
				String[] array = Utils.parseLine(line, columnNum);
				int userID     = Integer.parseInt(array
						[userIDIndex]);
				int locationID = Integer.parseInt(array
						[locationIDIndex]);
				Set<Integer> locationSet = userLocationMap
						.get(userID);
				if (locationSet == null) {
					locationSet = new HashSet<Integer>();
					userLocationMap.put(userID, locationSet);
				}
				if (locationSet.contains(locationID)) {
					reader.close(); reader=null;
					throw new CheckinException("LocationID[%s] repeated for userID[%s].",
						locationID, userID);
				}
				locationSet.add(locationID);
			}
			reader.close();
			return userLocationMap;
		} catch (IOException e) {
			e.printStackTrace();
			throw new CheckinException(e.toString());
		} finally {
			Utils.cleanup(reader);
		}
	}
	
	// Return {userPlaceMap, userPlaceRatingMap, placeUserMap} 
	public static Map<Integer, Map<Integer, Integer>>loadUserLocationRatingMap(
			File ratingFile, final int requiredColNum,
			final int userIDIndex, final int placeIDIndex,
			final int ratingIndex) throws CheckinException {
		Utils.exists(ratingFile);
		BufferedReader reader = null;
		try {
			reader      = new BufferedReader(new InputStreamReader(
					new FileInputStream(ratingFile)));
			String line = null;
			Map<Integer, Map<Integer, Integer>> userPlaceRatingMap =
				new HashMap<Integer, Map<Integer, Integer>>();
			Set<Integer> placeSet = new HashSet<Integer>();
			int recordNum         = 0;
			while ((line = reader.readLine()) != null) {
				String[] array = Utils.parseLine(line,requiredColNum);
				int userID     = Integer.parseInt(array[userIDIndex]);
				int placeID    = Integer.parseInt(array[placeIDIndex]);
				int rating     = Integer.parseInt(array[ratingIndex]);
	
				Map<Integer, Integer> placeRatingMap =
						userPlaceRatingMap.get(userID);
				if (placeRatingMap == null) {
					placeRatingMap = new HashMap<Integer, Integer>();
					userPlaceRatingMap.put(userID,
							placeRatingMap);
				}
				if (placeRatingMap.containsKey(placeID)) {
					reader.close(); reader=null;
					throw new CheckinException(
						"UserID[%s], PlaceID[%s] has exited.",
						userID, placeID);
				} else {
					placeRatingMap.put(placeID, rating);
				}
				if (! placeSet.contains(placeID))
					placeSet.add(placeID);
				recordNum ++;
			}
			reader.close();
			System.out.println();
			System.out.println("Load userPlaceRating*");
			System.out.println("UserNum  : " + userPlaceRatingMap.size());
			System.out.println("PlaceNum : " + placeSet.size());
			System.out.println("recordNum: " + recordNum);
			System.out.println();
			return userPlaceRatingMap;
		} catch (IOException e) {
			e.printStackTrace();
			throw new CheckinException(e.toString());
		} finally {
			Utils.cleanup(reader);
		}
	}

	// The format is: UserID \t PlaceID \t Rating
	// {userID array (int[]), placeID (int[][]), placeRating (float[][]), placeUserData(int[][])}
	public static Object[] loadUserRatingData(File ratingFile, String prompt,
			BufferedWriter infoWriter) throws CheckinException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(ratingFile)));
			String line        = null;
			Integer lastUserID = null;
			int totalRecordNum = 0;
			Set<Integer> existedUserIDs           =
					new HashSet<Integer>();
			Map<Integer, Map<Integer, Float>> userHisMap =
					new HashMap<Integer, Map<Integer, Float>>();
			Map<Integer, Set<Integer>> placeUserMap =
					new HashMap<Integer, Set<Integer>>();
			Set<Integer> totalPlaceSet = new HashSet<Integer>();
			int maxUserID  = -1;
			int minUserID  = Integer.MAX_VALUE;
			int maxPlaceID = -1;
			int minPlaceID = Integer.MAX_VALUE;
			while ((line = reader.readLine()) != null) {
				String[] array = Utils.parseLine(line, 3);
				int userID   = Integer.parseInt(array[0]);
				int placeID  = Integer.parseInt(array[1]);
				float rating = Float.parseFloat(array[2]);

				if (lastUserID == null || lastUserID != userID) {
					if (existedUserIDs.contains(userID)) {
						reader.close(); reader = null;
						throw new CheckinException(
						"Rating file not sort by userID. UserID=%s",
							userID);
					}
					existedUserIDs.add(userID);
					if (lastUserID != null && userID < lastUserID) {
						reader.close(); reader = null;
						throw new CheckinException(
						"Rating file not sort by userId in increasing order. UserID=%s",
							userID);
					}
					lastUserID = userID;
				}
				Map<Integer, Float> placeIDRatingMap =
						userHisMap.get(userID);
				if (placeIDRatingMap == null) {
					placeIDRatingMap = new HashMap<Integer, Float>();
					userHisMap.put(userID, placeIDRatingMap);
				}
				if (placeIDRatingMap.containsKey(placeID)) {
					reader.close(); reader = null;
					throw new CheckinException(
						"Repeated Place ID. UserID=%s, PlaceID=%s",
						userID, placeID);
				}
				placeIDRatingMap.put(placeID, rating);
				
				Set<Integer> userPlaceIDSet = placeUserMap.get(placeID);
				if (userPlaceIDSet == null) {
					userPlaceIDSet = new HashSet<Integer>();
					placeUserMap.put(placeID, userPlaceIDSet);
				}
				userPlaceIDSet.add(userID);
				
				totalRecordNum ++;
				if (! totalPlaceSet.contains(placeID))
					totalPlaceSet.add(placeID);
				if (userID > maxUserID) maxUserID = userID;
				if (userID < minUserID) minUserID = userID;
				if (placeID > maxPlaceID) maxPlaceID = placeID;
				if (placeID < minPlaceID) minPlaceID = placeID;
			}
			reader.close();
			Utils.writeAndPrint(infoWriter, prompt + "TotalRecordNum=" + totalRecordNum, true);
			Utils.writeAndPrint(infoWriter, prompt + "PlaceNum      =" + totalPlaceSet.size(), true);
			Utils.writeAndPrint(infoWriter, prompt + "MinPlaceID    =" + minPlaceID, true);
			Utils.writeAndPrint(infoWriter, prompt + "MaxPlaceID    =" + maxPlaceID, true);
			Utils.writeAndPrint(infoWriter, prompt + "UserNum       = " + userHisMap.size(), true);
			Utils.writeAndPrint(infoWriter, prompt + "MinUserID     =" + minUserID, true);
			Utils.writeAndPrint(infoWriter, prompt + "MaxUserID     =" + maxUserID , true);
			Utils.writeAndPrint(infoWriter, prompt + "NumOfUserWhoHasCheckins=" + userHisMap.size(), true);
			
			int[][] userPlaceData     = new int[userHisMap.size()][];
			float[][] userPlaceRating = new float[userHisMap.size()][];
			int[] userIDs           = Utils.getSortedKeys(userHisMap.keySet());
			int userNumHasCheckins  = 0;
			int index               = 0;
			for (int userID : userIDs) {
				Map<Integer, Float> placeIDRatingMap =
						userHisMap.get(userID);
				int[] sortedPlaceIDs = Utils.getSortedKeys(
						placeIDRatingMap.keySet());
				if (sortedPlaceIDs == null ||
						sortedPlaceIDs.length == 0) {
					userPlaceData[index]   = null;
					userPlaceRating[index] = null;
				} else {
					userPlaceData[index]   = sortedPlaceIDs;
					userPlaceRating[index] = new float[sortedPlaceIDs.length];
					for (int i = 0; i < sortedPlaceIDs.length; i ++) {
						userPlaceRating[index][i] = placeIDRatingMap
							.get(sortedPlaceIDs[i]);
					}
				}
				userNumHasCheckins ++;
				index ++;
			}
			if (userNumHasCheckins != userHisMap.size()) {
				throw new CheckinException(
					"UserNumHasCheckins=%s, UserHisMapSize=%s",
					userNumHasCheckins, userHisMap.size());
			}
			Utils.writeAndPrint(infoWriter, "", true);
			for (int i = 0; i < userPlaceData.length; i ++) {
				if (userPlaceData[i].length != userPlaceRating[i].length) {
					throw new RuntimeException(String.format(
						"User[%s] placeLen[%s] != ratingLen[%s]",
						i, userPlaceData[i].length, userPlaceRating[i].length));
				}
			}
			int[][] placeUserData = new int[maxPlaceID + 1][];
			for (int placeID = 0; placeID <= maxPlaceID; placeID ++) {
				if (placeUserMap.containsKey(placeID)) {
					Set<Integer> userIDSet = placeUserMap.get(placeID);
					int[] sortedUserIDs    = Utils.getSortedKeys(userIDSet);
					placeUserData[placeID] = sortedUserIDs;
				} else {
					placeUserData[placeID] = null;
				}
			}
			return new Object[] {userIDs, userPlaceData, userPlaceRating, placeUserData};
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Object[] loadUserTrainPlaceData(File trainRatingFile,
					int userNum) throws CheckinException {
		Utils.exists(trainRatingFile);
		
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(trainRatingFile)));
			String line     = null;
			int instanceNum = 0;
			int maxUserID   = -1;
			Map<Integer, Map<Integer, Float>> userTrainPlaceMap =
					new HashMap<Integer, Map<Integer, Float>>();
			while ((line = reader.readLine()) != null) {
				String[] array = Utils.parseLine(line, 3);
				int userId = Integer.parseInt(array[0]);
				int itemId = Integer.parseInt(array[1]);
				float rating = Integer.parseInt(array[1]);
				Map<Integer, Float> placeRatingMap =
						userTrainPlaceMap.get(userId);
				if (placeRatingMap == null) {
					placeRatingMap = new HashMap<Integer, Float>();
					userTrainPlaceMap.put(userId, placeRatingMap);
				}
				placeRatingMap.put(itemId, rating);
				if (userId > maxUserID) maxUserID = userId;
				instanceNum++;
			}
			reader.close();
			int[][] userTrainPlaceIDs       = new int[userNum][];
			float[][] userTrainPlaceRatings = new float[userNum][];
			for (int userId  = 0; userId < userNum; userId ++) {
				if (userTrainPlaceMap.containsKey(userId)) {
					Map<Integer, Float> placeRatingMap =
						userTrainPlaceMap.get(userId);
					int sortedPlaceIDs[] =
						Utils.getSortedKeys(
							placeRatingMap.keySet());
					userTrainPlaceIDs[userId] = sortedPlaceIDs;
					userTrainPlaceRatings[userId] =
						new float[placeRatingMap.size()];
					for (int i = 0; i < sortedPlaceIDs.length; i ++) {
						userTrainPlaceRatings[userId][i] =
							placeRatingMap.get(sortedPlaceIDs[i]);
					}
				} else {
					userTrainPlaceIDs[userId] = null;
					userTrainPlaceRatings[userId] = null;
				}
			}
			System.out.println(String.format("MaxUserID=%s, UserNum=%s, InstanceNum =%s ",
					maxUserID, userTrainPlaceMap.size(), instanceNum));
			return new Object[]{userTrainPlaceIDs, userTrainPlaceRatings};
		} catch (IOException e) {
			e.printStackTrace();
		throw new CheckinException(e);
		} finally {
			Utils.cleanup(reader);
		}
	}

	public static int[][] loadPlaceUserIDs(File trainRatingFile, int placeNum)
								throws CheckinException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(trainRatingFile)));
			String line = null;
			Map<Integer, Set<Integer>> placeUserIDMap =
				new HashMap<Integer, Set<Integer>>();
			int maxPlaceID = -1;
			while ((line = reader.readLine()) != null) {
				String[] array = Utils.parseLine(line, 3);
				int userID  = Integer.parseInt(array[0]);
				int placeID = Integer.parseInt(array[1]);
				Set<Integer> userSet = placeUserIDMap
						.get(placeID);
				if (userSet == null) {
					userSet = new HashSet<Integer>();
					placeUserIDMap.put(placeID, userSet);
				}
				if (! userSet.contains(userID))
					userSet.add(userID);
				if (placeID > maxPlaceID)
					maxPlaceID = placeID;
			}
			reader.close();
			System.out.println(String.format(
				"MaxPlaceID=%s, PlaceNum=%s",
				maxPlaceID, placeNum));
			int[][] placeUserIDs = new int[placeNum][];
			for (int placeID =0; placeID < placeNum; placeID ++) {
				Set<Integer> userSet = 
					placeUserIDMap.get(placeID);
				if (userSet != null) {
					placeUserIDs[placeID] =
						Utils.getSortedKeys(userSet);
				} else {
					placeUserIDs[placeID] = null;
				}
			}
			return placeUserIDs;
		} catch (IOException e) {
			e.printStackTrace();
			throw new CheckinException(e);
		} finally {
			Utils.cleanup(reader);
		}
	}
	
	public static int[][] loadUserTrainPlaceIDs(File trainRatingFile,
				int userNum) throws CheckinException {
		Utils.exists(trainRatingFile);
		
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(trainRatingFile)));
			String line     = null;
			int instanceNum = 0;
			int maxUserID   = -1;
			Map<Integer, Set<Integer>> userTrainPlaceMap =
				new HashMap<Integer, Set<Integer>>();
			while ((line = reader.readLine()) != null) {
				String[] array = Utils.parseLine(line, 3);
				int userId = Integer.parseInt(array[0]);
				int itemId = Integer.parseInt(array[1]);
				//float rating = Integer.parseInt(array[1]);
				Set<Integer> placeSet = userTrainPlaceMap.get(userId);
				if (placeSet == null) {
					placeSet = new HashSet<Integer>();
					userTrainPlaceMap.put(userId, placeSet);
				}
				placeSet.add(itemId);
				if (userId > maxUserID) maxUserID = userId;
			}
			reader.close();
			int[][] userTrainPlaceIDs = new int[userNum][];
			for (int userId  = 0; userId < userNum; userId ++) {
				if (userTrainPlaceMap.containsKey(userId)) {
					userTrainPlaceIDs[userId] =
						Utils.getSortedKeys(
							userTrainPlaceMap.get(userId));
				} else {
					userTrainPlaceIDs[userId] = null;
				}
			}
			System.out.println(String.format("MaxUserID=%s, UserNum=%s, InstanceNum =%s ",
				maxUserID, userTrainPlaceMap.size(), instanceNum));
			return userTrainPlaceIDs;
		} catch (IOException e) {
			e.printStackTrace();
		throw new CheckinException(e);
		} finally {
			Utils.cleanup(reader);
		}
	}

	public static int[][] loadFriends(File userFriendshipFile, int userNum)
						throws CheckinException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(userFriendshipFile)));
			String line = null;
			Map<Integer, int[]> map = new HashMap<Integer, int[]>();
			int maxUserID = -1;
			while ((line = reader.readLine()) != null) {
				String[] array = line.split(CheckinConstants.DELIMITER);
				if (array.length <= 1) {
					reader.close(); reader = null;
					throw new CheckinException(
							"LoadFriends:: Incorrect format:" + line);
				}
				int userID = Integer.parseInt(array[0]);
				if (userID >= userNum) {
					reader.close(); reader = null;
					throw new CheckinException(
							"UserID[%s] > UserNum[%s].",
							userID, userNum);
				}
				int[] friends = new int[array.length - 1];
				for (int i = 1; i < array.length; i ++) {
					int friendID = Integer.parseInt(array[i]);
					if (friendID >= userNum) {
						reader.close(); reader = null;
						throw new CheckinException(
							"FriendID[%s] > userNum[%s] for user[%s]",
							friendID, userNum, userID);
					}
					friends[i - 1] = friendID;
				}
				Arrays.sort(friends);
				map.put(userID, friends);
				if (userID > maxUserID) maxUserID = userID;
			}
			reader.close();
			System.out.println(String.format("MaxUserID = %s, TotalUserNum = %s",
					maxUserID, userNum));
			int[][] userFriends = new int[maxUserID + 1][];
			for (int userID = 0; userID < userFriends.length; userID ++) {
				if (map.containsKey(userID)) {
					userFriends[userID] = map.get(userID);
				} else {
					userFriends[userID] = null;
				}
			}
			return userFriends;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			Utils.cleanup(reader);
		}
		return null;
	}
	
	public static int[][] loadPairFriends(File userFriendshipFile, int userNum)
				throws CheckinException {
		BufferedReader reader = null;
		try {
		reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(userFriendshipFile)));
		String line = null;
		Map<Integer, Set<Integer>> map = new HashMap<Integer, Set<Integer>>();
		int maxUserID                  = -1;
		int recordNum                  = 0;
		while ((line = reader.readLine()) != null) {
			String[] array = parseLine(line, 3);
			int userID     = Integer.parseInt(array[0]);
			if (userID >= userNum) {
				reader.close(); reader = null;
				throw new CheckinException(
						"UserID[%s] > UserNum[%s].",
						userID, userNum);
			}
			int friendID     = Integer.parseInt(array[1]);
			Set<Integer> set = map.get(userID);
			if (set == null) {
				set = new HashSet<Integer>();
				map.put(userID, set);
			}
			if (set.contains(friendID)) {
				reader.close(); reader = null;
				throw new CheckinException("UserID:%s, FriendID:%s",
					userID, friendID);
			}
			set.add(friendID);
			if (userID > maxUserID) maxUserID = userID;
			recordNum ++;
		}
		reader.close();
		System.out.println(String.format("MaxUserID=%s, TotalUserNum=%s, RecordNum:%s",
				maxUserID, userNum, recordNum));
		int[][] userFriends = new int[maxUserID + 1][];
		for (int userID = 0; userID < userFriends.length; userID ++) {
			if (map.containsKey(userID)) {
				userFriends[userID] = Utils.getSortedKeys(
						map.get(userID));
			} else {
				userFriends[userID] = null;
			}
		}
		return userFriends;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			Utils.cleanup(reader);
		}
		return null;
	}

	public static double[] loadPairwisePowerLawParams(File trainRatingFile,
			File locationLatLngFile, double zeroDistanceDefaultValue,
			File outputPath, String pairwisePowerlawParamName)
					throws CheckinException {
		File pairwisePowerLawParamFile = new File(outputPath,
				pairwisePowerlawParamName);
		if (! pairwisePowerLawParamFile.exists()) {
			File  logDistanceProbOutputFile =
				new File(outputPath, "Pairwise_LogDisProb");
			File distanceOutputFile         =
				new File(outputPath, "Pairwise_Distance");
			if (! logDistanceProbOutputFile.exists()) {
				GeoPL.createLogDistanceProbData(trainRatingFile,
					locationLatLngFile, 1, zeroDistanceDefaultValue,
					distanceOutputFile, logDistanceProbOutputFile);
			}
			System.out.println("[Info] Learn PairwisePowLaw Params");
			GeoPL.learnPowerLawParams(logDistanceProbOutputFile,
					pairwisePowerLawParamFile);
		}
		System.out.println("Pairwise Power-Law *");
		return loadPowerLawParams(pairwisePowerLawParamFile);
	}

	public static double[] loadUserHomePowerLawParams(File userHomeLatLngFile,
			File trainRatingFile, File locationLatLngFile,
			double zeroDistanceDefaultValue, File outputPath,
			String homePowerLawParamName) throws CheckinException {
		File userHomePowerLawParamFile = new File(outputPath,
											homePowerLawParamName);
		if (! userHomePowerLawParamFile.exists()) {
			File  logDistanceProbOutputFile =
					new File(outputPath, "UserHome_LogDisProb");
			File distanceOutputFile         =
					new File(outputPath, "UserHome_Distance");
			//if (! logDistanceProbOutputFile.exists()) {
			GeoPL.createLogDistanceProbForHomeData(trainRatingFile,
					locationLatLngFile, userHomeLatLngFile,
					1, zeroDistanceDefaultValue,
					distanceOutputFile,
					logDistanceProbOutputFile);
			//}
			System.out.println("[Info] Learn UserHomePowLaw Params");
			GeoPL.learnPowerLawParams(logDistanceProbOutputFile,
					userHomePowerLawParamFile);
		}
		System.out.println("UserHome Power-Law *");
		return loadPowerLawParams(userHomePowerLawParamFile);
	}

	// [w0, w1]
	public static double[] loadPowerLawParams(File paramFile)
						throws CheckinException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(paramFile)));
			String line = reader.readLine();
			reader.close();
			String[] keys   = {"W0=", "W1="};
			double[] params = new double[keys.length];
			for (int i = 0; i < keys.length; i ++) {
				String key = keys[i];
				int s = line.indexOf(key);
				if (s < -1) throw new CheckinException(
					"Cannot find key[%s] in %s",
					keys[i], paramFile.getAbsolutePath());
				int e = line.indexOf(",", s);
				if (e < 0) e = line.length();
				params[i] = Double.parseDouble(line.substring(
						s + key.length(), e));
			}
			double paramA   = Math.pow(10, params[0]);
			double paramB   = params[1];
			System.out.println(String.format("PowerLawParams: W0=%s, W1=%s",
				params[0], params[1]));
			System.out.println(String.format("PowerLawParams: a=%s, b=%s",
				paramA, paramB));
			return new double[]{paramA, paramB};
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static double calPowerLawProb(double paramA, double paramB,
			double zeroDefaultValue, double distance) {
		if (distance < zeroDefaultValue) distance = zeroDefaultValue;
		return paramA * Math.pow(distance, paramB);
	}

	public static double logisticFunction(double value) {
		return 1.0 / (1 + Math.exp(-1.0 * value));
	}

	public static double normalize(double value, double mean, double stdvar,
			Double zeroValue) {
		if (stdvar == 0) {
			if (zeroValue != null) return zeroValue;
			else return (value - mean);
		} else {
			return (value - mean) / stdvar;
		}
	}

	public static void outputOneLine(BufferedWriter writer, int[] array)
			throws IOException {
		for (int i = 0; i < array.length; i ++) {
			writer.write(i + ":" + array[i]);
			if (i != array.length - 1)
				writer.write(CheckinConstants.DELIMITER);
		}
		writer.newLine();
	}

	public static boolean parseBoolean(String str) throws CheckinException {
		if (str.equalsIgnoreCase("true")) {
			return true;
		} else
		if (str.equalsIgnoreCase("false")) {
			return false;
		}
		throw new CheckinException("InvalidFormat: " + str);
	}
	
	public static int[] parseIntArray(String str, String delimiter)
								throws NumberFormatException {
		String[] array = str.split(delimiter);
		int[] result   = new int[array.length];
		for (int i = 0; i < array.length; i ++) {
			result[i] = Integer.parseInt(array[i]);
		}
		return result;
	}

	public static String[] parseLine(String line, int requiredColumnNum) {
		String[] array = line.split(CheckinConstants.DELIMITER);
		if (array.length != requiredColumnNum) {
			throw new RuntimeException(String.format(
				"Column num does not match: parsedNum=%s,requiredNum=%s. [%s]",
				array.length, requiredColumnNum, line));
		}
		return array;
	}
	
	public static Properties parseCMD(String arg) throws CheckinException {
		if (arg == null) return null;
		
		Properties props = new Properties();
		StringTokenizer tokenizer = new StringTokenizer(arg);
		while (tokenizer.hasMoreElements()) {
			String key = tokenizer.nextToken();
			if (! key.startsWith("-")) {
				throw new CheckinException("Incorrect Format: " + arg);
			}
			key = key.substring(1);
			String value = tokenizer.nextToken();
			if (value == null || value.startsWith("-")) {
				throw new CheckinException("Incorrect Format: " + arg);
			}
			props.put(key, value);
		}
		return props;
	}
	
	public static void outputOneLine(BufferedWriter writer, double[] array)
				throws IOException {
		for (int i = 0; i < array.length; i ++) {
			writer.write(i + ":" + array[i]);
			if (i != array.length - 1)
				writer.write(CheckinConstants.DELIMITER);
		}
		writer.newLine();
	}

	public static void print(double[] matrix, int rowNum, int columnNum,
						String delimiter) {
		for (int row = 0; row < rowNum; row ++) {
			for (int column = 0; column < columnNum; column ++) {
				System.out.print(matrix[row * columnNum + column]);
				if (column != columnNum - 1)
					System.out.print(delimiter);
			}
			System.out.println();
		}
	}
	
	public static void print(int[]array) {
		System.out.print(array[0]);
		for (int i = 1; i < array.length; i ++)
			System.out.print(", " + array[i]);
		System.out.println();
	}

	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// igore
		}
	}

	public static List<float[]> loadFloatArrayList(File inputFile,
					String spacer) throws CheckinException {
		exists(inputFile);
		if (spacer == null || "".equals(spacer))
			throw new CheckinException("No spacer specified.");
		
		BufferedReader reader = null;
		try {
			reader      = new BufferedReader(new InputStreamReader(
					new FileInputStream(inputFile)));
			String line = null;
			ArrayList<float[]> list = new ArrayList<float[]>(); 
			while ((line = reader.readLine()) != null) {
				if ("".equals(line = line.trim())) continue;
				
				ArrayList<Float> arrayList = new ArrayList<Float>();
				if ("".equals(spacer.trim())) {
					StringTokenizer tokenizer = new StringTokenizer(line);
					while (tokenizer.hasMoreElements()) {
						arrayList.add(Float.parseFloat(tokenizer.nextToken()));
					}
				} else {
					String[] array = line.split(spacer);
					for (String element : array) {
						if (! "".equals((element = element.trim()))) {
							arrayList.add(Float.parseFloat(element));
						}
					}
				}
				if (! arrayList.isEmpty()) {
					float[] arrayRes = new float[arrayList.size()];
					for (int i = 0; i < arrayList.size(); i ++) {
						arrayRes[i] = arrayList.get(i);
					}
					list.add(arrayRes);
				}
			}
			return list;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			cleanup(reader);
		}
		
		return null;
	}

	public static void shuffle(boolean[] array) {
		if (array == null || array.length == 0) return;

		for (int i = 0; i < array.length; i ++) {
			int nextIndex    = new Random().nextInt(array.length);
			boolean tmp      = array[i];
			array[i]         = array[nextIndex];
			array[nextIndex] = tmp;
		}
	}

	public static void shuffle(int[] array) {
		if (array == null || array.length == 0) return;

		for (int i = 0; i < array.length; i ++) {
			int nextIndex    = new Random().nextInt(array.length);
			int tmp          = array[i];
			array[i]         = array[nextIndex];
			array[nextIndex] = tmp;
		}
	}

	public static boolean save(String content, File outputFile) {
		BufferedWriter writer= null;
		try {
			if (! outputFile.getParentFile().exists())
				outputFile.getParentFile().mkdirs();
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputFile)));
			writer.write(content);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			Utils.cleanup(writer);
		}
		return false;
	}

	public static boolean save(int[] array, String spacer,
			File outputFile) {
		BufferedWriter writer = null;
		try {
			if (! outputFile.getParentFile().exists())
				outputFile.getParentFile().mkdirs();
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputFile)));
			if (array != null) {
				writer.write(String.valueOf(array[0]));
				for (int i = 1; i < array.length; i ++)
					writer.write(spacer + String.valueOf(array[i]));
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			cleanup(writer);
		}
		return false;
	}

	public static boolean save(float[] array, String spacer,
		File outputFile) {
		BufferedWriter writer = null;
		try {
			if (! outputFile.getParentFile().exists())
				outputFile.getParentFile().mkdirs();
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputFile)));
			if (array != null) {
				writer.write(String.valueOf(array[0]));
				for (int i = 1; i < array.length; i ++)
					writer.write(spacer + String.valueOf(array[i]));
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			cleanup(writer);
		}
		return false;
	}

	public static boolean save(double[] array, String spacer,
							File outputFile) {
		BufferedWriter writer = null;
		try {
			if (! outputFile.getParentFile().exists())
				outputFile.getParentFile().mkdirs();
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputFile)));
			if (array != null) {
				writer.write(String.valueOf(array[0]));
				for (int i = 1; i < array.length; i ++)
					writer.write(spacer + String.valueOf(array[i]));
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			cleanup(writer);
		}
		return false;
	}

	public static <T extends Comparable<? super T>> void save(Set<T> set, File outputFile) {
		BufferedWriter writer = null;
		try {
			List<T> list = new ArrayList<T>(set.size());
			for (T elem : set) {
				list.add(elem);
			}
			Collections.sort(list);
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputFile)));
			for (T elem : list) {
				writer.write(elem.toString());
				writer.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			Utils.cleanup(writer);
		}
	}

	public static <K extends Comparable<? super K>> void saveBySortKey(
			final Map<K, ?> map, String delimiter, File outputFile) {
		BufferedWriter writer = null;
		try {
			if (! outputFile.getParentFile().exists())
				outputFile.getParentFile().mkdirs();
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputFile)));
			List<K> keys = new ArrayList<K>();
			for (K elem : map.keySet()) {
				keys.add(elem);
			}
			Collections.sort(keys);
			for (K key : keys) {
				writer.write(String.format("%s%s%s", key.toString(),
						delimiter, map.get(key).toString()));
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			Utils.cleanup(writer);
		}
	}

	public static <K extends Comparable<? super K>> void saveBySortKeyInDecentOrder(
			final Map<K, ?> map, String delimiter, File outputFile) {
		BufferedWriter writer = null;
		try {
			if (! outputFile.getParentFile().exists())
				outputFile.getParentFile().mkdirs();
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputFile)));
			List<K> keys = new ArrayList<K>();
			for (K elem : map.keySet()) {
				keys.add(elem);
			}
			Collections.sort(keys, new Comparator<K>() {
				@Override
				public int compare(K k1, K k2) {
					return k2.compareTo(k1);
				}
			});
			for (K key : keys) {
				writer.write(String.format("%s%s%s", key.toString(),
						delimiter, map.get(key).toString()));
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			Utils.cleanup(writer);
		}
	}

	public static <V extends Comparable<? super V>> void saveBySortValue(
			final Map<?, V> map, String delimiter, File outputFile) {
		BufferedWriter writer = null;
		try {
			if (! outputFile.getParentFile().exists())
				outputFile.getParentFile().mkdirs();
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputFile)));
			Object[] keys = map.keySet().toArray(new Object[0]);
			Arrays.sort(keys, new Comparator<Object>() {
				@Override
				public int compare(Object s1, Object s2) {
					return map.get(s1).compareTo(map.get(s2));
				}
			});
			for (Object key : keys) {
				writer.write(String.format("%s%s%s", key.toString(),
						delimiter, map.get(key)));
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			Utils.cleanup(writer);
		}
	}
	public static <V extends Comparable<? super V>> void saveBySortValueInDecentOrder(
			final Map<?, V> map, String delimiter, File outputFile) {
		BufferedWriter writer = null;
		try {
			if (! outputFile.getParentFile().exists())
				outputFile.getParentFile().mkdirs();
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputFile)));
			Object[] keys = map.keySet().toArray(new Object[0]);
			Arrays.sort(keys, new Comparator<Object>() {
				@Override
				public int compare(Object s1, Object s2) {
					return map.get(s2).compareTo(map.get(s1));
				}
			});
			for (Object key : keys) {
				writer.write(String.format("%s%s%s", key.toString(),
						delimiter, map.get(key)));
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			Utils.cleanup(writer);
		}
	}

	public static void store2DArray(float[] array, int columnNum, File outputFile) {
		BufferedWriter writer = null;
		try {
			if (! outputFile.getParentFile().exists())
				outputFile.getParentFile().mkdirs();
			writer    = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputFile)));
			int count = 0;
			for (int i = 0; i < array.length; i ++) {
				writer.write(String.valueOf(array[i]));
				count ++;
				if (i != array.length - 1) {
					if (count == columnNum) {
						count = 0;
						writer.newLine();
					} else {
						writer.write(CheckinConstants.DELIMITER);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			Utils.cleanup(writer);
		}
	}

	public static void store2DArray(double[] array, int columnNum, File outputFile) {
		BufferedWriter writer = null;
		try {
			if (! outputFile.getParentFile().exists())
				outputFile.getParentFile().mkdirs();
			writer    = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputFile)));
			int count = 0;
			for (int i = 0; i < array.length; i ++) {
				writer.write(String.valueOf(array[i]));
				count ++;
				if (i != array.length - 1) {
					if (count == columnNum) {
						count = 0;
						writer.newLine();
					} else {
						writer.write(CheckinConstants.DELIMITER);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			Utils.cleanup(writer);
		}
	}

	public static void store2DArray(double[][] array, String delemiter,
							File outputFile) {
		BufferedWriter writer = null;
		try {
			if (! outputFile.getParentFile().exists())
				outputFile.getParentFile().mkdirs();
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputFile)));
			for (int i = 0; i < array.length; i ++) {
				if (array[i].length == 0) continue;
				writer.write(String.valueOf(array[i][0]));
				for (int j = 1; j < array[i].length; j ++) {
					writer.write(String.format("%s%s",
						delemiter, array[i][j]));
				}
				writer.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			Utils.cleanup(writer);
		}
	}

	public static void storeFriendship(Map<Integer, Set<Integer>> totalUserFriendsMap,
			File outputFile) {
		BufferedWriter writer = null;
		try {
			if (! outputFile.getParentFile().exists())
				outputFile.getParentFile().mkdirs();
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputFile)));
			int[] sortedUserIDs   = Utils.getSortedKeys(
					totalUserFriendsMap.keySet());
			int uniquePairNum     = 0;
			for (int userID : sortedUserIDs) {
				Set<Integer>set = totalUserFriendsMap.get(userID);
				if (set.size() == 0) continue;
				int[] sortedFriendIDs = Utils.getSortedKeys(set);
				writer.write(String.valueOf(userID));
				for (int friendID : sortedFriendIDs) {
					writer.write(String.format("\t%s", friendID));
					uniquePairNum ++;
				}
				writer.newLine();
			}
			writer.close();
			System.out.println();
			System.out.println("TotalUserNum  = " + sortedUserIDs.length);
			System.out.println("UniquePairNum = " + uniquePairNum);
			System.out.println();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			Utils.cleanup(writer);
		}
	}
	
	public static void storeVectorInRow(double[] vector, File outputFile) {
		BufferedWriter writer = null;
		try {
			if (! outputFile.getParentFile().exists()) {
				outputFile.getParentFile().mkdirs();
			}
			writer = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(outputFile)));
			for (double value : vector) {
				writer.write(String.valueOf(value));
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			Utils.cleanup(writer);
		}
	}
	
	public static double sumOfDiffAbs(double[] array, double[] array_old) {
		double sum = 0.0;
		for (int i = 0; i < array.length; i ++) {
			sum += Math.abs(array[i] - array_old[i]);
		}
		return sum;
	}

	/*
	 * Transpose matrix which is rowNum x columnNum to the matrix
	 * which is columnNum x rowNum
	 */
	public static double[] transpose(double[] matrix, int rowNum, int columnNum) {
		double newmatrix[] = new double[matrix.length];
		int index          = 0;
		for (int column = 0; column < columnNum; column ++) {
			for (int row = 0; row < rowNum; row ++) {
				newmatrix[index] = matrix[row * columnNum + column];
				index ++;
			}
		}
		return newmatrix;
	}

	public static int[] toArray(List<Integer> list) {
		int[] array = new int[list.size()];
		for (int i = list.size() - 1; i >= 0; i --)
			array[i] = list.get(i);
		return array;
	}

	public static String wrapPath(File file) {
		if (file == null) return "";

		String spath         = file.getAbsolutePath();
		StringBuilder buffer = new StringBuilder();
		for (int pathIndex = 0; pathIndex < spath.length(); pathIndex ++) {
			buffer.append(spath.charAt(pathIndex));
			if (spath.charAt(pathIndex) == '\\') {
				buffer.append('\\');
			}
		}
		return buffer.toString();
	}

	public static void write(BufferedWriter writer, String content,
			boolean newline) throws IOException {
		writer.write(content);
		if (newline) writer.newLine();
	}

	public static void write(BufferedWriter writer, String[] array,
			String delimiter) throws IOException {
		writer.write(array[0]);
		for (int i = 1; i < array.length; i ++) {
			writer.write(delimiter);
			writer.write(array[i]);
		}
		writer.newLine();
	}

	public static void writeAndPrint(BufferedWriter writer, String content,
			boolean newline) throws IOException {
		write(writer, content, newline);
		System.out.println(content);
	}
	public static boolean saveDenseMatrix(DenseMatrix matrix, BiMap<String, Integer> userIds, BiMap<String, Integer> itemIds, String outputFile){
		BiMap<Integer, String> inverseUserIds = userIds.inverse();
		BiMap<Integer, String> inverseItemIds = itemIds.inverse();
		File file = new File(outputFile);
		BufferedWriter writer = null;
		try {
			if (! file.getParentFile().exists())
				file.getParentFile().mkdirs();
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file)));
			if (matrix != null) {
				String userId,itemId;
				int data;
				for (int i = 0; i < matrix.numRows(); i++) {
					if (!inverseUserIds.containsKey(i))
						continue;
					userId = inverseUserIds.get(i);
					for (int j = 0; j < matrix.numColumns(); j++) {
						if (!inverseItemIds.containsKey(j))
							continue;
						itemId = inverseItemIds.get(j);
						data = (int)matrix.get(i,j);
						if (data > 0)
							writer.write(userId + "\t" + itemId + "\t" + data +"\n");
					}
				}

				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			cleanup(writer);
		}
		return false;
	}
}
