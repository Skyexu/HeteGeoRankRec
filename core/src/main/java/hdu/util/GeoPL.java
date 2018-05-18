package hdu.util;

import java.io.*;
import java.util.*;

public class GeoPL {
	private final static double LAMBDA    = 0.001;
	private final static double THRESHOLD = 1.0e-5;
	
	private static double calLogLikelihood(double[][] data,
											double w0, double w1) {
		double logLikelihood = 0;
		for (int i = 0; i < data.length; i ++) {
			double value = w1 * data[i][0] + w0 - data[i][1];
			logLikelihood += value * value;
		}
		return -1 * logLikelihood - LAMBDA * (w0 * w0 + w1 * w1);
	}
	
	// Assume the trainRatingFile is sorted by userID
	// The format is: UserID \t PlaceID \t Rating
	public static void createLogDistanceProbData(File trainRatingFile,
			File locationLatLngMapFile, final int decimalDigitNum,
				final double zeroDefaultValue, File distanceOutputFile,
					File logDistanceProbOutputFile) throws CheckinException{
		Utils.exists(trainRatingFile);
		Utils.exists(locationLatLngMapFile);
		BufferedReader reader = null;
		BufferedWriter writer = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
						new FileInputStream(trainRatingFile)));
			List<Integer> userIDList      = new ArrayList<Integer>();
			List<Integer> userLineNumList = new ArrayList<Integer>();
			Set<Integer> existedUserIDs   = new HashSet<Integer>();
			String line                   = null;
			int totalRecordNum            = 0;
			while ((line = reader.readLine()) != null) {
				String[] array = Utils.parseLine(line, 3);
				int userID  = Integer.parseInt(array[0]);
				//int placeID = Integer.parseInt(array[1]);
				Integer lastUserID = null;
				if (userIDList.size() > 0) {
					lastUserID = userIDList.get(userIDList.size() - 1);
				}
				if (lastUserID == null || lastUserID != userID) {
					userIDList.add(userID);
					userLineNumList.add(0);
					if (existedUserIDs.contains(userID)) {
						reader.close(); reader = null;
						throw new CheckinException(
							"Rating file not sort by userID. UserID=%s",userID);
					}
					existedUserIDs.add(userID);
					lastUserID = userID;
				}
				int lastIndex = userLineNumList.size() - 1;
				userLineNumList.set(lastIndex, userLineNumList
						.get(lastIndex) + 1);
				totalRecordNum ++;
			}
			reader.close();
			System.out.println("TotalRecordNum = " + totalRecordNum);
			System.out.println("TotalUserNum   = " + existedUserIDs.size());
			Map<Integer, double[]> locationLatLngMap =
				Utils.loadLocationDic(locationLatLngMapFile,
					CheckinConstants.DELIMITER, 0, 1, 2);
			if (locationLatLngMap == null ||
					locationLatLngMap.size() == 0) {
				throw new CheckinException("No location - LatLng found.");
			}
			
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(trainRatingFile)));
			if (! distanceOutputFile.getParentFile().exists())
				distanceOutputFile.getParentFile().mkdirs();
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(distanceOutputFile)));
			for (int userIndex = 0; userIndex < userIDList.size(); userIndex ++) {
				int userID    = userIDList.get(userIndex);
				int recordNum = userLineNumList.get(userIndex);
				if (recordNum == 1) continue;
				List<Integer> placeIDList = new ArrayList<Integer>(recordNum);
				for (int recordIndex = 0; recordIndex < recordNum; recordIndex ++) {
					line = reader.readLine();
					String[] array   = Utils.parseLine(line, 3);
					int parsedUserID = Integer.parseInt(array[0]);
					int locationID   = Integer.parseInt(array[1]);
					if (parsedUserID != userID) {
						reader.close(); reader = null;
						writer.close(); writer = null;
						throw new CheckinException(
							"PaserID != userID: parsed=%s, userID=%s",
							parsedUserID, userID);
					}
					placeIDList.add(locationID);
				}
				for (int index = 0; index < placeIDList.size(); index ++) {
					int currLocationID  = placeIDList.get(index);
					double[] currLatLng = locationLatLngMap.get(currLocationID);
					for (int futureIndex = index + 1; futureIndex <
							placeIDList.size(); futureIndex ++) {
						int fID = placeIDList.get(futureIndex);
						double fLatLng[] = locationLatLngMap.get(fID);
						double distance  = Utils.calDistance(
							currLatLng[0], currLatLng[1],
							fLatLng[0], fLatLng[1]);
						writer.write(String.format("%s\t1", distance));
						writer.newLine();
					}
				}
			}
			writer.close();
			if (reader.readLine() != null) {
				throw new CheckinException("Inner Error : havenot finished reading lines.");
			}
			reader.close();
			calLogDistanceProb(distanceOutputFile, decimalDigitNum,
				zeroDefaultValue, logDistanceProbOutputFile);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			Utils.cleanup(reader);
			Utils.cleanup(writer);
		}
	}
	
	// Assume the trainRatingFile is sorted by userID
	// The format is: UserID \t PlaceID \t Rating
	// 计算用户 home 与其签到的所有地点的距离  保存  distance rating(count)
	//
	public static void createLogDistanceProbForHomeData(File trainRatingFile,
			File locationLatLngMapFile, File userHomeFile,
			final int decimalDigitNum, final double zeroDefaultValue,
			File distanceOutputFile, File logDistanceProbOutputFile)
						throws CheckinException{
		Utils.exists(trainRatingFile);
		Utils.exists(locationLatLngMapFile);
		BufferedReader reader = null;
		BufferedWriter writer = null;
		try {
			Map<Integer, double[]> userHomeDicMap =
					Utils.loadLocationDic(userHomeFile, "\t", 0, 1, 2);
			Map<Integer, double[]> locationLatLngMap =
				Utils.loadLocationDic(locationLatLngMapFile,
					CheckinConstants.DELIMITER, 0, 1, 2);
			if (locationLatLngMap == null ||
					locationLatLngMap.size() == 0) {
				throw new CheckinException("No location - LatLng found.");
			}
			if (! distanceOutputFile.getParentFile().exists())
				distanceOutputFile.getParentFile().mkdirs();
			writer = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(distanceOutputFile)));
			reader = new BufferedReader(new InputStreamReader(
						new FileInputStream(trainRatingFile)));
			int totalRecordNum          = 0;
			String line                 = null;
			Set<Integer> existedUserIDs = new HashSet<Integer>();
			while ((line = reader.readLine()) != null) {
				String[] array      = Utils.parseLine(line, 3);
				int userID          = Integer.parseInt(array[0]);
				int placeID         = Integer.parseInt(array[1]);
				int rating          = Integer.parseInt(array[2]);
				double[] homeLatLng = userHomeDicMap.get(userID);
				existedUserIDs.add(userID);
				if (homeLatLng == null) {
					reader.close(); reader = null;
					writer.close(); writer = null;
					throw new CheckinException(
						"Canot find homeLatLng for userID[%s].", userID);
				}
				double[] placeLatLng = locationLatLngMap.get(placeID);
				double distance      = Utils.calDistance(
										homeLatLng[0], homeLatLng[1],
										placeLatLng[0], placeLatLng[1]);
				writer.write(String.format("%s\t%s", distance, rating));
				writer.newLine();
				totalRecordNum ++;
			}
			writer.close();
			reader.close();
			System.out.println("TotalRecordNum = " + totalRecordNum);
			System.out.println("TotalUserNum   = " + existedUserIDs.size());
			calLogDistanceProb(distanceOutputFile, decimalDigitNum,
				zeroDefaultValue, logDistanceProbOutputFile);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			Utils.cleanup(reader);
			Utils.cleanup(writer);
		}
	}
	
	// format: distance \t count
	// output: distance \t prob
	private static void calLogDistanceProb(File distanceFile,
			final int decimalDigitNum, final double zeroDefaultValue,
				File outputFile) throws CheckinException {
		BufferedReader reader = null;
		BufferedWriter writer = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(distanceFile)));
			Map<String, Integer> disCountMap =
					new HashMap<String, Integer>();
			String line = null;
			int totalInstanceNum = 0;
			while ((line = reader.readLine()) != null) {
				String array[] = Utils.parseLine(line, 2);
				double distance = Double.parseDouble(array[0]);
				int rating      = Integer.parseInt(array[1]);
				/*double logDis   = Math.log(distance);
				if (distance <= zeroDefaultValue) {
					logDis = Math.log(zeroDefaultValue);
				}
				logDis /= Math.log(10);
				String roundValue = Utils.getRoundStringValue(
						logDis, decimalDigitNum);
				if (Double.parseDouble(roundValue) < -1) {
					 System.out.println(String.format(
						"Distance=%s, logDIs=%s, roundValue=%s", distance, logDis, roundValue));
				}*/
				if (distance < zeroDefaultValue)
					distance = zeroDefaultValue;
				// 距离保存几位小数
				String roundDis = Utils.getRoundStringValue(
									distance, decimalDigitNum);
				Integer count = disCountMap.get(roundDis);
				if (count == null) count = rating;
				else count += rating;
				disCountMap.put(roundDis, count);
				totalInstanceNum += rating;
			}
			reader.close();
			Map<Double, Double> logDisProbMap = new HashMap<Double, Double>();
			int num = 0;
			for (String keyDis : disCountMap.keySet()) {
				int count      = disCountMap.get(keyDis);
				double logDis  = Math.log10(Double.parseDouble(
						keyDis));
				// 概率计算，即当前距离的次数/总的签到次数
				double logProb = Math.log10(count /
						(totalInstanceNum + 0.0));
				num += count;
				logDisProbMap.put(logDis, logProb);
			}
			if (num != totalInstanceNum) {
				throw new CheckinException(
					"Inner Error: InstanceNum not match: totalInstaceNum=%s,Num=%s",
					totalInstanceNum, num);
			}
			disCountMap = null;
			double[] keys = Utils.getSortedDoubleKeys(
								logDisProbMap.keySet());
			if (! outputFile.getParentFile().exists()) {
				outputFile.getParentFile().mkdirs();
			}
			writer = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(outputFile)));
			for (double key : keys) {
				writer.write(String.format("%s%s%s", key,
					CheckinConstants.DELIMITER, logDisProbMap.get(key)));
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			Utils.cleanup(reader);
			Utils.cleanup(writer);
		}
	}
	
	// dataInputFile: format as: logDistance \t  logProb

	public static void learnPowerLawParams(File dataInputFile, File outputFile)
						throws CheckinException {
		double[][] data = Utils.load2DoubleArray(dataInputFile,
				CheckinConstants.DELIMITER);
		System.out.println("[Info] DataSize = " + data.length);
		double w0 = Math.random();
		double w1 = Math.random();
		double oldLogLikelihood = calLogLikelihood(data, w0, w1);
		double error            = 0;
		double paramDiff        = Double.MAX_VALUE;
		int iter                = 0;
		double logProbSum       = 0;
		double logDisSum        = 0;
		double logDisSquareSum = 0;
		double logProb_x_logDis = 0;
		for (int i = 0; i < data.length; i ++) {
			logDisSum  += data[i][0];
			logProbSum += data[i][1];
			logProb_x_logDis += data[i][0] * data[i][1];
			logDisSquareSum += data[i][0] * data[i][0];
		}
		double oldW0 = w0;
		double oldW1 = w1;
		do {
			// update w0  w1
			w0 = (logProbSum - w1 * logDisSum) / (data.length + LAMBDA);
			w1 = (logProb_x_logDis - w0 * logDisSum) /
					(LAMBDA + logDisSquareSum);
			double newLogLikelihood = calLogLikelihood(data, w0, w1);
			error     = (oldLogLikelihood - newLogLikelihood) / oldLogLikelihood;
			paramDiff = Math.abs(oldW0 - w0) + Math.abs(oldW1 - w1);

			oldW0 = w0;
			oldW1 = w1;
			oldLogLikelihood = newLogLikelihood;

			iter ++;
			System.out.println(String.format(
				"Iter=%s, Error=%s, LogLikelihood=%s, w0=%s, w1=%s",
					iter, error, newLogLikelihood, w0, w1));
		} while (error > THRESHOLD || paramDiff < 1e-12);
		storePowerLawParams(w0, w1, outputFile);
	}
	
	private static void storePowerLawParams(double w0, double w1,
			File outputFile) {
		if (! outputFile.getParentFile().exists()) {
			outputFile.getParentFile().mkdirs();
		}
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(outputFile)));
			writer.write(String.format("W0=%s, W1=%s", w0, w1));
			writer.newLine();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			Utils.cleanup(writer);
		}
	}
}
