package hdu.eval.experiment02;

import net.librec.common.LibrecException;
import net.librec.conf.Configuration;
import net.librec.data.model.TextDataModel;
import net.librec.eval.RecommenderEvaluator;
import net.librec.eval.ranking.*;
import net.librec.recommender.Recommender;
import net.librec.recommender.RecommenderContext;
import net.librec.recommender.cf.ranking.WRMFRecommender;
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
 * @Date: 8:23 2018/9/6
 * @Description: WRMF 对比实验测试
 */
public class TestWMF  {
    protected static final Log LOG = LogFactory.getLog(TestWMF.class);

    public static void main(String[] args) throws LibrecException, IOException, ClassNotFoundException {
        LocalDateTime time = LocalDateTime.now();
        String timeString = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH_mm_ss"));
        Configuration conf = new Configuration();
        Configuration.Resource resource = new Configuration.Resource("rec/skye/heterankgeomf.properties");
        conf.addResource(resource);

        //String path  = "D:\\Works\\论文\\dataSet\\experimentData\\Foursquare\\process\\实验数据8_21\\";
        String path  = conf.get("dfs.data.dir");
        // build data model

        conf.set("dfs.data.dir", path);
        // 这里需要传切分测试集与训练集之前的数据
        conf.set("data.input.path",conf.get("hete.preference.name"));
        conf.set("dfs.result.dir",conf.get("dfs.result.dir"));

        // split train test
        conf.set("data.model.splitter","testset");
        conf.set("data.testset.path",conf.get("hete.test.name"));
//        conf.set("data.model.splitter","ratio");
//        conf.set("data.splitter.trainset.ratio","0.8");

        String outputPath  =  conf.get("dfs.result.dir") + "\\" + "WRMF" + timeString;
        TextDataModel dataModel = new TextDataModel(conf);
        dataModel.buildDataModel();

        // build recommender context
        RecommenderContext context = new RecommenderContext(conf, dataModel);

        // build recommender
        conf.set("rec.iterator.maximum", "10");
        conf.set("rec.user.regularization", "0.01");
        conf.set("rec.item.regularization", "0.01");
        conf.set("rec.factor.number", "30");
        conf.set("rec.recommender.isranking", "true");
        conf.set("rec.recommender.ranking.topn", "50");
        conf.set("rec.wrmf.weight.coefficient", "40.0");


        Recommender recommender = new WRMFRecommender();
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

        String evalOutPutPath = conf.get("dfs.result.dir") + "\\eval\\" + "WRMFeval" + timeString;
        try {
            FileUtil.writeString(evalOutPutPath, evalResult.toString());
            LOG.info("Eval path is " + evalOutPutPath);
        } catch (Exception e) {
            e.printStackTrace();
        }

       // List<RecommendedItem> recommendedList = recommender.getRecommendedList();
      //  saveResult(recommendedList,outputPath);
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
