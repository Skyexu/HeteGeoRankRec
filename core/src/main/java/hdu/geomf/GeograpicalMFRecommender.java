package hdu.geomf;

import com.google.common.collect.BiMap;
import hdu.util.Utils;
import net.librec.common.LibrecException;
import net.librec.math.structure.*;
import net.librec.recommender.MatrixFactorizationRecommender;

import java.io.*;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @Author: Skye
 * @Date: 2:56 2018/5/23
 * @Description:
 */
public class GeograpicalMFRecommender extends MatrixFactorizationRecommender {
    protected static final float zeroDistanceDefaultValue = 1f;
    protected HashMap<Integer, double[]> poiLocation;

    /**
     * confidence weight coefficient
     * 置信度权重系数
     */
    protected float weightCoefficient;

    /**
     * power-law parameter
     */
    protected float powerA;
    protected float powerB;

    /**
     * Geographical influence parameter
     */
    protected double alpha;
    /**
     * confindence Minus Identity Matrix{ui} = confidenceMatrix_{ui} - 1 =alpha * r_{ui} or log(1+10^alpha * r_{ui})
     * 置信度矩阵 - 单位阵
     */
    protected SparseMatrix confindenceMinusIdentityMatrix;

    /**
     * preferenceMatrix_{ui} = 1 if {@code r_{ui}>0 or preferenceMatrix_{ui} = 0}
     * 偏好矩阵  1 or 0
     */
    protected SparseMatrix preferenceMatrix;

    @Override
    protected void setup() throws LibrecException {
        super.setup();
        poiLocation = ((GeoDataAppender) getDataModel().getDataAppender()).getPoiLocation();
        userMappingData = getDataModel().getUserMappingData();
        itemMappingData = getDataModel().getItemMappingData();
        weightCoefficient = conf.getFloat("rec.wrmf.weight.coefficient", 4.0f);
        powerA = conf.getFloat("rec.geomf.powerlaw.a");
        powerB = conf.getFloat("rec.geomf.powerlaw.b");
        alpha = conf.getDouble("rec.geomf.alpha", 0.8d);
        confindenceMinusIdentityMatrix = new SparseMatrix(trainMatrix);
        preferenceMatrix = new SparseMatrix(trainMatrix);
        for (MatrixEntry matrixEntry : trainMatrix) {
            int userIdx = matrixEntry.row();
            int itemIdx = matrixEntry.column();
//            confindenceMinusIdentityMatrix.set(userIdx, itemIdx, weightCoefficient * matrixEntry.get());
            confindenceMinusIdentityMatrix.set(userIdx, itemIdx, Math.log(1.0 + Math.pow(10, weightCoefficient) * matrixEntry.get())); //maybe better for poi recommender
            preferenceMatrix.set(userIdx, itemIdx, 1.0d);
        }
    }

    @Override
    protected void trainModel() throws LibrecException {
        System.out.println(numUsers+"-"+numItems);
        saveTrainTestSet();

        // 以稀疏矩阵存储训练数据，稠密矩阵存储分解后的矩阵
        // 用户、物品对角矩阵，值为正则化参数
        SparseMatrix userIdentityMatrix = DiagMatrix.eye(numFactors).scale(regUser);
        SparseMatrix itemIdentityMatrix = DiagMatrix.eye(numFactors).scale(regItem);

        // To be consistent with the symbols in the paper
        // 用户和物品隐因子矩阵被初始化为标准高斯分布的值 （小的初始值可以更容易地训练模型; 否则可能需要一个非常小的学习速度（特别是在因素数量很大时），这可能导致性能不佳。）
        DenseMatrix X = userFactors, Y = itemFactors;
        // Updating by using alternative least square (ALS)    最小二乘更新参数
        // due to large amount of entries to be processed (SGD will be too slow)   由于要处理大量条目，SGD 会很慢
        // 迭代次数

        for (int iter = 1; iter <= numIterations; iter++) {
            // Step 1: update user factors; 1. 更新用户隐向量
            for (int userIdx = 0; userIdx < numUsers; userIdx++) {
                // 创建 Y~
                List<Integer> items = trainMatrix.getColumns(userIdx);   // 获取 userIdx 对应的 items
                DenseMatrix Y_ = getY_(Y, items);

                // WRMF 原有公式，  更新用户隐向量部分公式相同
//                DenseMatrix Yt = Y.transpose();
//                DenseMatrix YtY = Yt.mult(Y);
                DenseMatrix Yt = Y_.transpose();
                DenseMatrix YtY = Yt.mult(Y_);

                DenseMatrix YtCuI = new DenseMatrix(numFactors, numItems);//actually YtCuI is a sparse matrix
                //Yt * (Cu-itemIdx)
                List<Integer> itemList = trainMatrix.getColumns(userIdx);   // 获取 userIdx 对应的 items
                for (int itemIdx : itemList) {
                    for (int factorIdx = 0; factorIdx < numFactors; factorIdx++) {
                        YtCuI.set(factorIdx, itemIdx, Y_.get(itemIdx, factorIdx) * confindenceMinusIdentityMatrix.get(userIdx, itemIdx));
                    }
                }

                // YtY + Yt * (Cu - itemIdx) * Y   // 论文中的方法，通过计算此式来加快计算 YtCuY
                DenseMatrix YtCuY = new DenseMatrix(numFactors, numFactors);
                for (int factorIdx = 0; factorIdx < numFactors; factorIdx++) {
                    for (int factorIdxIn = 0; factorIdxIn < numFactors; factorIdxIn++) {
                        double value = 0.0;
                        for (int itemIdx : itemList) {
                            value += YtCuI.get(factorIdx, itemIdx) * Y_.get(itemIdx, factorIdxIn);
                        }
                        YtCuY.set(factorIdx, factorIdxIn, value);
                    }
                }
                YtCuY.addEqual(YtY);
                // (YtCuY + lambda * itemIdx)^-1
                //lambda * itemIdx can be pre-difined because every time is the same.
                DenseMatrix Wu = (YtCuY.add(userIdentityMatrix)).inv();
                // Yt * (Cu - itemIdx) * Pu + Yt * Pu
                DenseVector YtCuPu = new DenseVector(numFactors);
                for (int factorIdx = 0; factorIdx < numFactors; factorIdx++) {
                    for (int itemIdx : itemList) {
                        YtCuPu.add(factorIdx, preferenceMatrix.get(userIdx, itemIdx) * (YtCuI.get(factorIdx, itemIdx) + Yt.get(factorIdx, itemIdx)));
                    }
                }

                DenseVector xu = Wu.mult(YtCuPu);
                // udpate user factors    更新用户向量
                X.setRow(userIdx, xu);
                //System.out.println("now update:" + userIdx);
                Y_ = null;
            }

            // Step 2: update item factors;
            DenseMatrix Xt = X.transpose();
            DenseMatrix XtX = Xt.mult(X);

            for (int itemIdx = 0; itemIdx < numItems; itemIdx++) {

                DenseMatrix XtCiI = new DenseMatrix(numFactors, numUsers);
                //actually XtCiI is a sparse matrix
                //Xt * (Ci-itemIdx)
                List<Integer> userList = trainMatrix.getRows(itemIdx);
                for (int userIdx : userList) {
                    for (int factorIdx = 0; factorIdx < numFactors; factorIdx++) {
                        XtCiI.set(factorIdx, userIdx, X.get(userIdx, factorIdx) * confindenceMinusIdentityMatrix.get(userIdx, itemIdx));
                    }
                }

                // XtX + Xt * (Ci - itemIdx) * X
                DenseMatrix XtCiX = new DenseMatrix(numFactors, numFactors);
                for (int factorIdx = 0; factorIdx < numFactors; factorIdx++) {
                    for (int factorIdxIn = 0; factorIdxIn < numFactors; factorIdxIn++) {
                        double value = 0.0;
                        for (int userIdx : userList) {
                            value += XtCiI.get(factorIdx, userIdx) * X.get(userIdx, factorIdxIn);
                        }
                        XtCiX.set(factorIdx, factorIdxIn, value);
                    }
                }
                XtCiX.addEqual(XtX);

                // (XtCuX*alpha*alpha + lambda * itemIdx)^-1
                //lambda * itemIdx can be pre-difined because every time is the same.
                DenseMatrix Wi = (XtCiX.scale(alpha * alpha).add(itemIdentityMatrix)).inv();
                // Xt * (Ci - itemIdx) * Pu + Xt * Pu
                DenseVector XtCiPu = new DenseVector(numFactors);
                for (int factorIdx = 0; factorIdx < numFactors; factorIdx++) {
                    for (int userIdx : userList) {
                        XtCiPu.add(factorIdx, preferenceMatrix.get(userIdx, itemIdx) * (XtCiI.get(factorIdx, userIdx) + Xt.get(factorIdx, userIdx)));
                    }
                }
                // plus right  XtCuX + 右边部分
                DenseVector rightVec = new DenseVector(numFactors);
                for (int userIdx : userList) {
                    DenseVector Py = new DenseVector(numFactors);
                    List<Integer> itemList = trainMatrix.getColumns(userIdx);   // 获取 userIdx 对应的 items
                    if (itemList.size() == 0) {
                        continue;
                    }
                    for (int item : itemList) {
                        double geoProb = getGeoProb(itemIdx, item);
                        Py.addEqual(Y.row(item).scaleEqual(geoProb));
                    }
                    double XtPy = X.row(userIdx).inner(Py) * ((1 - alpha) / itemList.size());
                    DenseVector XtPyX = X.row(userIdx).scaleEqual(XtPy);
                    DenseVector CuiXtPyX = XtPyX.add(XtPyX.scale(confindenceMinusIdentityMatrix.get(userIdx, itemIdx)));
                    rightVec.addEqual(CuiXtPyX);
                }

                DenseVector yi = Wi.mult(XtCiPu.addEqual(rightVec).scale(alpha));
                // udpate item factors
                Y.setRow(itemIdx, yi);
            }

            if (verbose) {
                LOG.info(getClass() + " runs at iteration = " + iter + " " + new Date());
            }
        }

    }

    /**
     * @param Y
     * @param items
     * @return
     */
    protected DenseMatrix getY_(DenseMatrix Y, List<Integer> items) {
        DenseMatrix Y_ = new DenseMatrix(numItems, numFactors);

        // 遍历每个 yi, 将每一个行向量更新至 Y_
        for (int itemIndex = 0; itemIndex < numItems; itemIndex++) {
            // 公式右边求和部分
            DenseVector yi_right = new DenseVector(numFactors);
            for (int itemIdxu : items) {
                double geoProb = getGeoProb(itemIndex, itemIdxu);
                //double geoProb = 1;
                yi_right.addEqual(Y.row(itemIdxu).scale(geoProb));
            }
            // 计算当前 yi 向量
            DenseVector yi_alpha = Y.row(itemIndex).scale(alpha);
            DenseVector yi_;
            if (items.size() == 0) {
                yi_ = yi_alpha;
            } else {
                yi_ = yi_alpha.addEqual(yi_right.scaleEqual((1 - alpha) / items.size()));
            }

            Y_.setRow(itemIndex, yi_);
        }
        return Y_;
    }

    protected DenseMatrix getY_2(DenseMatrix Y, List<Integer> items, BiMap<Integer, String> itemMappingInverse) {
        DenseMatrix Y_ = new DenseMatrix(numItems, numFactors);

        // 遍历每个 yi, 将每一个行向量更新至 Y_
        for (int itemIndex = 0; itemIndex < numItems; itemIndex++) {
            // 公式右边求和部分
            DenseVector yi_right = new DenseVector(numFactors);
            for (int factorIdx = 0; factorIdx < numFactors; factorIdx++) {
                for (int itemIdxu : items) {
                    double geoProb = getGeoProb(itemIndex, itemIdxu);
                    //double geoProb = 1;
                    yi_right.add(factorIdx, Y.get(itemIdxu, factorIdx) * geoProb);
                }
            }

            // 计算当前 yi 向量
            DenseVector yi_alpha = Y.row(itemIndex, true).scale(alpha);
            DenseVector yi_;
            if (items.size() == 0) {
                yi_ = yi_alpha;
            } else {
                yi_ = yi_alpha.addEqual(yi_right.scaleEqual((1 - alpha) / items.size()));
            }
            Y_.setRow(itemIndex, yi_);
        }
        return Y_;
    }

    /**
     * 获取地理影响值
     *
     * @param itemIdx            当前地点
     * @param itemIdxu           用户访问过的一个地点
     * @return
     * @TODO 检查正确与否
     */
    protected double getGeoProb(int itemIdx, int itemIdxu) {
        double maxGeoProb = Utils.calPowerLawProb(powerA,
                powerB, zeroDistanceDefaultValue,
                zeroDistanceDefaultValue);
        // 获取当前 item 的 纬经度
        double[] location = poiLocation.get(itemIdx);
        double[] locationu = poiLocation.get(itemIdxu);
        double distance = Utils.calDistance(
                location[0], location[1],
                locationu[0], locationu[1]);
        // 根据 ASMF 除了一个最大值（距离为1时概率最大），进行标准化
        return Utils.calPowerLawProb(
                powerA, powerB,
                zeroDistanceDefaultValue, distance) / maxGeoProb;
    }
    public void saveTrainTestSet(){
        BiMap<Integer, String> inverseUserIds = userMappingData.inverse();
        BiMap<Integer, String> inverseItemIds = itemMappingData.inverse();
        String outPath = conf.get("dfs.data.dir");
        String newTrainPath = outPath + "newTrain_uvc.txt";
        String newTestPath = outPath + "newTest_uvc.txt";
        File trainFile = new File(newTrainPath);
        BufferedWriter writer = null;
        try {
            if (!trainFile.getParentFile().exists())
                trainFile.getParentFile().mkdirs();
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(trainFile)));
            for (int i = 0; i < numUsers; i++) {
                for (int j = 0; j < numItems; j++) {
                    writer.write(inverseUserIds.get(i) + "\t" + inverseItemIds.get(j) + "\t" + trainMatrix.get(i,j) + "\n");
                }
            }
            writer.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
        LOG.info("New train  dataset path is " + outPath);

        File testFile = new File(newTestPath);
        try {
            if (!testFile.getParentFile().exists())
                testFile.getParentFile().mkdirs();
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(testFile)));
            for (int i = 0; i < numUsers; i++) {
                for (int j = 0; j < numItems; j++) {
                    writer.write(inverseUserIds.get(i) + "\t" + inverseItemIds.get(j) + "\t" + testMatrix.get(i,j) + "\n");
                }
            }
            writer.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
        LOG.info("New test  dataset path is " + outPath);

    }

}
