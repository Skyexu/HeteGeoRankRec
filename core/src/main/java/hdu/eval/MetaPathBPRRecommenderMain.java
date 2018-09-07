package hdu.eval;

import com.google.common.collect.BiMap;
import hdu.bprranking.CombineFeature;
import hdu.bprranking.MetaPathBPRRecommender;
import net.librec.common.LibrecException;
import net.librec.conf.Configuration;
import net.librec.data.model.TextDataModel;
import net.librec.eval.RecommenderEvaluator;
import net.librec.eval.ranking.*;
import net.librec.math.structure.DenseMatrix;
import net.librec.recommender.RecommenderContext;
import net.librec.util.FileUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Skye
 * @Date: 22:39 2018/8/21
 * @Description:
 */
public class MetaPathBPRRecommenderMain {
    protected static final Log LOG = LogFactory.getLog(MetaPathBPRRecommenderMain.class);

    public static void main(String[] args) throws IOException, LibrecException {
        String test1 = "D:\\Works\\论文\\dataSet\\experimentData\\Foursquare\\process\\实验数据8_21\\result\\metapath_feature_result\\upcp";
        String test2 = "D:\\Works\\论文\\dataSet\\experimentData\\Foursquare\\process\\实验数据8_21\\result\\metapath_feature_result\\upcpup";
        String test3 = "D:\\Works\\论文\\dataSet\\experimentData\\Foursquare\\process\\实验数据8_21\\result\\metapath_feature_result\\upup";
        String test4 = "D:\\Works\\论文\\dataSet\\experimentData\\Foursquare\\process\\实验数据8_21\\result\\metapath_feature_result\\uup";
        String test5 = "D:\\Works\\论文\\dataSet\\experimentData\\Foursquare\\process\\实验数据8_21\\result\\metapath_feature_result\\up";
        List<String> inputFiles = new ArrayList<>();
        inputFiles.add(test1);
        inputFiles.add(test2);
        inputFiles.add(test3);
        inputFiles.add(test4);
        inputFiles.add(test5);

        CombineFeature combineFeature = new CombineFeature(inputFiles,671,1898,5);

        combineFeature.process();

        DenseMatrix denseMatrix = combineFeature.getFeatureMatix();
        BiMap<String,Integer> map = combineFeature.getUservenueMapping();


        LocalDateTime time = LocalDateTime.now();
        String timeString = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH_mm_ss"));

        String path  = "D:\\Works\\论文\\dataSet\\experimentData\\Foursquare\\process\\实验数据8_21\\";
        // 输入地点

        // build data model
        Configuration conf = new Configuration();
        conf.set("dfs.data.dir", path);
        conf.set("data.input.path","preference_uvc.txt");
        conf.set("dfs.result.dir",path+"result");
        // split train test
        conf.set("data.model.splitter","testset");
        conf.set("data.testset.path","test_uvc.txt");
        // 传入地点信息，用于 GeoHash
        conf.set("data.appender.class", "geo");
        conf.set("data.appender.path", "venue_lat_lon.txt");


        TextDataModel dataModel = new TextDataModel(conf);
        dataModel.buildDataModel();

        // build recommender context
        RecommenderContext context = new RecommenderContext(conf, dataModel);

        // build recommender
        conf.set("rec.iterator.maximum", "20");
        conf.set("rec.metapathbpr.regularization", "0.01");
        conf.set("rec.recommender.isranking", "true");
        conf.set("rec.recommender.ranking.topn", "10");



        MetaPathBPRRecommender recommender = new MetaPathBPRRecommender(denseMatrix,map);
        recommender.setContext(context);

        // run recommender algorithm
        // 调用 recommend 方法后，会调用 setup 方法初始化 contex 等信息
        recommender.recommend(context);

        // evaluate the recommended result
        StringBuilder evalResult = new StringBuilder();

        RecommenderEvaluator PRECISION = new PrecisionEvaluator();
        PRECISION.setTopN(10);
        System.out.println("PRECISION:" + recommender.evaluate(PRECISION));
        evalResult.append("PRECISION:" + recommender.evaluate(PRECISION) + "\n");

        RecommenderEvaluator RECALL = new RecallEvaluator();
        RECALL.setTopN(10);
        System.out.println("RECALL:" + recommender.evaluate(RECALL));
        evalResult.append("RECALL:" + recommender.evaluate(RECALL)+"\n");

        RecommenderEvaluator AP = new AveragePrecisionEvaluator();
        AP.setTopN(10);
        System.out.println("AP:" + recommender.evaluate(AP));
        evalResult.append("AP:" + recommender.evaluate(AP)+"\n");

        RecommenderEvaluator AUC = new AUCEvaluator();
        AUC.setTopN(10);
        System.out.println("AUC:" + recommender.evaluate(AUC));
        evalResult.append("AUC:" + recommender.evaluate(AUC)+"\n");

        RecommenderEvaluator Novelty = new NoveltyEvaluator();
        Novelty.setTopN(10);
        System.out.println("Novelty:" + recommender.evaluate(Novelty));
        evalResult.append("Novelty:" + recommender.evaluate(Novelty)+"\n");

        String evalOutPutPath = conf.get("dfs.result.dir") + "\\eval\\" + "MetaPathBPRRecommender" + timeString;
        try {
            FileUtil.writeString(evalOutPutPath, evalResult.toString());
            LOG.info("Eval path is " + evalOutPutPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String run(Configuration inputConf, DenseMatrix featureMatrix,BiMap<String,Integer> map) throws IOException, LibrecException {
        LocalDateTime time = LocalDateTime.now();
        String timeString = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH_mm_ss"));

        String path  = inputConf.get("dfs.data.dir");
        // 输入地点

        // build data model
        Configuration conf = new Configuration();
        conf.set("dfs.data.dir", path);
        conf.set("data.input.path",inputConf.get("hete.preference.name"));
        conf.set("dfs.result.dir",inputConf.get("dfs.result.dir"));

        // split train test
        conf.set("data.model.splitter","testset");
        conf.set("data.testset.path",inputConf.get("hete.test.name"));
        // 传入地点信息，用于 GeoHash
        conf.set("data.appender.class", "geo");
        conf.set("data.appender.path", inputConf.get("hete.vanuelatlon.name"));


        TextDataModel dataModel = new TextDataModel(conf);
        dataModel.buildDataModel();

        // build recommender context
        RecommenderContext context = new RecommenderContext(conf, dataModel);

        // build recommender
        conf.set("rec.iterator.maximum", inputConf.get("bpr.rec.iterator.maximum"));
        conf.set("rec.metapathbpr.regularization", inputConf.get("bpr.rec.metapathbpr.regularization"));
        conf.set("rec.recommender.isranking", inputConf.get("bpr.rec.recommender.isranking"));
        conf.set("rec.recommender.ranking.topn", inputConf.get("bpr.rec.recommender.ranking.topn"));

        int topN = inputConf.getInt("bpr.rec.recommender.ranking.topn");

        MetaPathBPRRecommender recommender = new MetaPathBPRRecommender(featureMatrix,map);
        recommender.setContext(context);

        // run recommender algorithm
        // 调用 recommend 方法后，会调用 setup 方法初始化 contex 等信息
        recommender.recommend(context);

        // evaluate the recommended result
        StringBuilder evalResult = new StringBuilder();
        evalResult.append("rec.iterator.maximum: " + conf.get("rec.iterator.maximum")+"\n")
        .append("rec.metapathbpr.regularization: " + conf.get("rec.metapathbpr.regularization")+"\n")
        .append("rec.recommender.ranking.topn: " + conf.get("rec.recommender.ranking.topn")+"\n")
        .append("metapath.rec.iterator.maximum: " + inputConf.get("metapath.rec.iterator.maximum")+"\n")
        .append("metapath.rec.factor.number: "+ inputConf.get("metapath.rec.factor.number")+"\n")
        .append("rec.geomf.alpha" + inputConf.get("rec.geomf.alpha")+"\n");

        RecommenderEvaluator PRECISION = new PrecisionEvaluator();
        PRECISION.setTopN(topN);
        System.out.println("PRECISION:" + recommender.evaluate(PRECISION));
        evalResult.append("PRECISION:" + recommender.evaluate(PRECISION) + "\n");

        RecommenderEvaluator RECALL = new RecallEvaluator();
        RECALL.setTopN(topN);
        System.out.println("RECALL:" + recommender.evaluate(RECALL));
        evalResult.append("RECALL:" + recommender.evaluate(RECALL)+"\n");

        RecommenderEvaluator AP = new AveragePrecisionEvaluator();
        AP.setTopN(topN);
        System.out.println("AP:" + recommender.evaluate(AP));
        evalResult.append("AP:" + recommender.evaluate(AP)+"\n");

        RecommenderEvaluator AUC = new AUCEvaluator();
        AUC.setTopN(topN);
        System.out.println("AUC:" + recommender.evaluate(AUC));
        evalResult.append("AUC:" + recommender.evaluate(AUC)+"\n");

        RecommenderEvaluator Novelty = new NoveltyEvaluator();
        Novelty.setTopN(topN);
        System.out.println("Novelty:" + recommender.evaluate(Novelty));
        evalResult.append("Novelty:" + recommender.evaluate(Novelty)+"\n");

        String evalOutPutPath = inputConf.get("dfs.eval.dir") + "MetaPathBPRRecommender" + timeString;
        try {
            FileUtil.writeString(evalOutPutPath, evalResult.toString());
            LOG.info("Eval path is " + evalOutPutPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return evalResult.toString();
    }
}
