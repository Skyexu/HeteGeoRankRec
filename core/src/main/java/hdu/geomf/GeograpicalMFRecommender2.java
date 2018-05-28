/**
 * Copyright (C) 2016 LibRec
 * <p>
 * This file is part of LibRec.
 * LibRec is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * LibRec is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with LibRec. If not, see <http://www.gnu.org/licenses/>.
 */
package hdu.geomf;

import hdu.geomf.GeoDataAppender;
import net.librec.annotation.ModelData;
import net.librec.common.LibrecException;
import net.librec.math.structure.*;
import net.librec.recommender.MatrixFactorizationRecommender;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * <h3>WRMF: Weighted Regularized Matrix Factorization.</h3>
 * <p>
 * This implementation refers to the method proposed by Hu et al. at ICDM 2008.
 * <ul>
 * <li><strong>Binary ratings:</strong> Pan et al., One-class Collaborative Filtering, ICDM 2008.</li>
 * <li><strong>Real ratings:</strong> Hu et al., Collaborative filtering for implicit feedback datasets, ICDM 2008.</li>
 * </ul>
 *
 * @author guoguibing and Keqiang Wang
 */
@ModelData({"isRanking", "wrmf", "userFactors", "itemFactors", "trainMatrix"})
public class GeograpicalMFRecommender2 extends MatrixFactorizationRecommender {
    protected static final float zeroDistanceDefaultValue = 1f;
    protected HashMap<String, double[]> poiLocation;

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
            DenseMatrix Y_ = Y;
            DenseMatrix Yt = Y_.transpose();
            DenseMatrix YtY = Yt.mult(Y_);
            for (int userIdx = 0; userIdx < numUsers; userIdx++) {


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

                // (XtCuX + lambda * itemIdx)^-1
                //lambda * itemIdx can be pre-difined because every time is the same.
                DenseMatrix Wi = (XtCiX.add(itemIdentityMatrix)).inv();
                // Xt * (Ci - itemIdx) * Pu + Xt * Pu
                DenseVector XtCiPu = new DenseVector(numFactors);
                for (int factorIdx = 0; factorIdx < numFactors; factorIdx++) {
                    for (int userIdx : userList) {
                        XtCiPu.add(factorIdx, preferenceMatrix.get(userIdx, itemIdx) * (XtCiI.get(factorIdx, userIdx) + Xt.get(factorIdx, userIdx)));
                    }
                }

                DenseVector yi = Wi.mult(XtCiPu);
                // udpate item factors
                Y.setRow(itemIdx, yi);
            }

            if (verbose) {
                LOG.info(getClass()+" runs at iteration = "+iter+" "+new Date());
            }
        }
    }
}
