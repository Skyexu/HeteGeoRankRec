package hdu.bprranking;

import com.google.common.collect.BiMap;
import hdu.geomf.GeoDataAppender;
import hdu.util.GeoHash;
import net.librec.common.LibrecException;
import net.librec.math.algorithm.Maths;
import net.librec.math.algorithm.Randoms;
import net.librec.math.structure.DenseMatrix;
import net.librec.math.structure.DenseVector;
import net.librec.math.structure.SparseMatrix;
import net.librec.math.structure.SparseVector;
import net.librec.recommender.AbstractRecommender;

import java.util.*;

/**
 * @Author: Skye
 * @Date: 16:57 2018/7/16
 * @Description:
 */
public class MetaPathBPRRecommender extends AbstractRecommender{

    protected HashMap<Integer, double[]> poiLocation;
    /**
     * 特征矩阵
     */
    private DenseMatrix featureMaxtrix;
    /**
     * 特征矩阵行索引映射  user_venue -> features
     */
    private BiMap<String,Integer> uservenueMapping;

    private List<Set<Integer>> userItemsSet;
    /**
     * 需要求解的特征权重向量 theta,维度为特征矩阵的列数
     */
    private DenseVector weightVector;
    /**
     * the number of iterations
     */
    protected int numIterations;

    /**
     * learn rate, maximum learning rate
     */
    protected float learnRate, maxLearnRate;

    /**
     * init mean
     */
    protected float initMean;

    /**
     * init standard deviation
     */
    protected float initStd;

    /**
     * regularization
     */
    protected float reg;

    private BiMap<Integer,String> inverseUserMapping,inverseVenueMapping;
    public MetaPathBPRRecommender(DenseMatrix featureMaxtrix,BiMap<String,Integer> uservenueMapping){
        this.featureMaxtrix = featureMaxtrix;
        this.uservenueMapping = uservenueMapping;
    }
    @Override
    protected void setup() throws LibrecException {
        super.setup();
        poiLocation = ((GeoDataAppender) getDataModel().getDataAppender()).getPoiLocation();
        weightVector = new DenseVector(featureMaxtrix.numColumns);
        numIterations = conf.getInt("rec.iterator.maximum",100);
        learnRate = conf.getFloat("rec.iterator.learnrate", 0.01f);
        maxLearnRate = conf.getFloat("rec.iterator.learnrate.maximum", 1000.0f);

        reg = conf.getFloat("rec.metapathbpr.regularization", 0.01f);

        isBoldDriver = conf.getBoolean("rec.learnrate.bolddriver", false);
        decay = conf.getFloat("rec.learnrate.decay", 1.0f);
        initMean = 0.0f;
        initStd = 0.1f;

        // initialize weightVector
        weightVector.init(initMean, initStd);
        //weightVector.setData(new double[]{0.1,0.1,0.2,0.2,0.4});
        inverseUserMapping = userMappingData.inverse();
        inverseVenueMapping = itemMappingData.inverse();
    }

    @Override
    protected void trainModel() throws LibrecException {
        userItemsSet = getUserItemsSet(trainMatrix);
        int maxSample = trainMatrix.size();
        GeoHash geoHash = new GeoHash(4);
        for (int iter = 1; iter <= numIterations; iter++) {

            loss = 0.0d;
            for (int sampleCount = 0; sampleCount < maxSample; sampleCount++) {

                // randomly draw (userIdx, posVenueIdx, negVenueIdx)

                int userIdx, posVenueIdx, negVenueIdx;
                while (true) {
                    userIdx = Randoms.uniform(numUsers);
                    // 用户访问过的地点为正样本
                    Set<Integer> itemSet = userItemsSet.get(userIdx);
                    if (itemSet.size() == 0 || itemSet.size() == numItems)
                        continue;
                    Set<String> userGeoHash = new HashSet<>();
                    for (int item:
                         itemSet) {
                        userGeoHash.add(geoHash.encode(poiLocation.get(item)[0],poiLocation.get(item)[1]));
                    }

                    List<Integer> itemList = trainMatrix.getColumns(userIdx);
                    posVenueIdx = itemList.get(Randoms.uniform(itemList.size()));
                    String negItemGeoHash;
                    do {
                        //1. 没访问过的地点
                        negVenueIdx = Randoms.uniform(numItems);
                        // 2. 与已访问过的地点相距离不超过 20KM 的为候选样本
                        negItemGeoHash = geoHash.encode(poiLocation.get(negVenueIdx)[0],poiLocation.get(negVenueIdx)[1]);
                    } while (itemSet.contains(negVenueIdx) && !userGeoHash.contains(negItemGeoHash));
                   // } while (itemSet.contains(negVenueIdx));
//                    String posKey = inverseUserMapping.get(userIdx) +"_"+ inverseVenueMapping.get(posVenueIdx);
//                    String negKey = inverseUserMapping.get(userIdx) +"_"+ inverseVenueMapping.get(negVenueIdx);
//                    if (!uservenueMapping.containsKey(posKey) || !uservenueMapping.containsKey(negKey))
//                        continue;

                    break;
                }

                // update parameters
                double posPredictRating = predict(userIdx, posVenueIdx);
                double negPredictRating = predict(userIdx, negVenueIdx);
                double diffValue = posPredictRating - negPredictRating;
                double lossValue = -Math.log(Maths.logistic(diffValue));
//                if (Double.isInfinite(lossValue))
//                    lossValue = 0;
                loss += lossValue;

                // = Math.exp(-diffValue) * Maths.logistic(diffValue)
                double deriValue = Maths.logistic(-diffValue);

                String posKey = inverseUserMapping.get(userIdx) +"_"+ inverseVenueMapping.get(posVenueIdx);
                String negKey = inverseUserMapping.get(userIdx) +"_"+ inverseVenueMapping.get(negVenueIdx);

                DenseVector posVector = featureMaxtrix.row(uservenueMapping.get(posKey));
                DenseVector negVector = featureMaxtrix.row(uservenueMapping.get(negKey));


                // 负梯度方向更新 theta
                DenseVector tempweightVector = weightVector;
                weightVector = posVector.minus(negVector).scaleEqual(-deriValue).add(weightVector.scale(reg));
                weightVector = weightVector.scale(-learnRate).add(tempweightVector);




//                if (weightVector.sum() > 5){
//                    weightVector.init(initMean,initStd);
//                }

                if (Double.isNaN(weightVector.get(0))){
                    System.out.println("nan");
                }
                loss += weightVector.inner(weightVector) * reg;
            }
            if (isConverged(iter) && earlyStop) {
                break;
            }
            updateLRate(iter);
        }
        //weightVector.setData(new double[]{0.1,0.1,0.2,0.2,0.4});
    }

    @Override
    protected double predict(int userIdx, int itemIdx) throws LibrecException {
        String rowKey = inverseUserMapping.get(userIdx) +"_"+ inverseVenueMapping.get(itemIdx);
        int rowIndex = uservenueMapping.get(rowKey);
        return weightVector.inner(featureMaxtrix.row(rowIndex));
    }

    private List<Set<Integer>> getUserItemsSet(SparseMatrix sparseMatrix) {
        List<Set<Integer>> userItemsSet = new ArrayList<>();
        for (int userIdx = 0; userIdx < numUsers; ++userIdx) {
            userItemsSet.add(new HashSet(sparseMatrix.getColumns(userIdx)));
        }
        return userItemsSet;
    }

    protected void updateLRate(int iter) {
        if (learnRate < 0.0) {
            return;
        }

        if (isBoldDriver && iter > 1) {
            learnRate = Math.abs(lastLoss) > Math.abs(loss) ? learnRate * 1.05f : learnRate * 0.5f;
        } else if (decay > 0 && decay < 1) {
            learnRate *= decay;
        }

        // limit to max-learn-rate after update
        if (maxLearnRate > 0 && learnRate > maxLearnRate) {
            learnRate = maxLearnRate;
        }
        lastLoss = loss;

    }
}
