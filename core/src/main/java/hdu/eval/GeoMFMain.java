package hdu.eval;

import hdu.geomf.GeograpicalMFRecommender;
import net.librec.common.LibrecException;
import net.librec.conf.Configuration;
import net.librec.data.model.TextDataModel;
import net.librec.eval.RecommenderEvaluator;
import net.librec.eval.ranking.*;
import net.librec.recommender.Recommender;
import net.librec.recommender.RecommenderContext;
import net.librec.recommender.item.RecommendedItem;
import net.librec.util.FileUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @Author: Skye
 * @Date: 15:54 2018/8/21
 * @Description:  GeoMF 测试
 */
public class GeoMFMain {
    protected static final Log LOG = LogFactory.getLog(GeoMFMain.class);

    public static void main(String[] args) throws LibrecException, IOException, ClassNotFoundException {
        LocalDateTime time = LocalDateTime.now();
        String timeString = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH_mm_ss"));

        String path  = "D:\\Works\\论文\\dataSet\\experimentData\\Foursquare\\process\\实验数据8_21\\";
        // build data model
        Configuration conf = new Configuration();
        conf.set("dfs.data.dir", path);
        // 这里需要传切分测试集与训练集之前的数据
        conf.set("data.input.path","preference_uvc.txt");
        conf.set("dfs.result.dir",path+"result");
        // split train test
        conf.set("data.model.splitter","testset");
        conf.set("data.testset.path","test_uvc.txt");

        conf.set("data.appender.class", "geo");
        conf.set("data.appender.path", "venue_lat_lon.txt");

        String outputPath  =  conf.get("dfs.result.dir") + "\\" + "GeoMF" + timeString;
        TextDataModel dataModel = new TextDataModel(conf);
        dataModel.buildDataModel();

        // build recommender context
        RecommenderContext context = new RecommenderContext(conf, dataModel);


        // build recommender
        conf.set("rec.iterator.maximum", "20");
        conf.set("rec.user.regularization", "0.01");
        conf.set("rec.item.regularization", "0.01");
        conf.set("rec.factor.number", "10");
        conf.set("rec.recommender.isranking", "true");
        conf.set("rec.recommender.ranking.topn", "10");
        conf.set("rec.wrmf.weight.coefficient", "4.0");


        // set power-law parameter
        conf.set("rec.geomf.powerlaw.a","3.52855505698074E-4");
        conf.set("rec.geomf.powerlaw.b","-0.5152437346326175");
        conf.set("rec.geomf.alpha","0.8");

        Recommender recommender = new GeograpicalMFRecommender();
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

        RecommenderEvaluator NDCG = new NormalizedDCGEvaluator();
        Novelty.setTopN(10);
        System.out.println("NDCG:" + recommender.evaluate(NDCG));
        evalResult.append("NDCG:" + recommender.evaluate(NDCG)+"\n");

        String evalOutPutPath = conf.get("dfs.result.dir") + "\\eval\\" + "GeoMFeval" + timeString;
        try {
            FileUtil.writeString(evalOutPutPath, evalResult.toString());
            LOG.info("Eval path is " + evalOutPutPath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<RecommendedItem> recommendedList = recommender.getRecommendedList();

        saveResult(recommendedList,outputPath);
    }

    public static void  saveResult(List<RecommendedItem> recommendedList,String outputPath) throws LibrecException, IOException, ClassNotFoundException {
        if (recommendedList != null && recommendedList.size() > 0) {
            // make output path


            // convert itemList to string
            StringBuilder sb = new StringBuilder();
            for (RecommendedItem recItem : recommendedList) {
                String userId = recItem.getUserId();
                String itemId = recItem.getItemId();
                String value = String.valueOf(recItem.getValue());
                sb.append(userId).append(",").append(itemId).append(",").append(value).append("\n");
            }
            String resultData = sb.toString();
            // save resultData
            try {
                FileUtil.writeString(outputPath, resultData);
                LOG.info("Result path is " + outputPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
