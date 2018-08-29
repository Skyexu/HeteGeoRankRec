package hdu.eval;

import hdu.geomf.GeograpicalMetaPathMFRecommender;
import net.librec.common.LibrecException;
import net.librec.conf.Configuration;
import net.librec.data.model.TextDataModel;
import net.librec.eval.RecommenderEvaluator;
import net.librec.eval.ranking.AveragePrecisionEvaluator;
import net.librec.eval.ranking.NoveltyEvaluator;
import net.librec.eval.ranking.PrecisionEvaluator;
import net.librec.eval.ranking.RecallEvaluator;
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
 * @Date: 16:13 2018/8/21
 * @Description:
 */
public class GeoMetaPathMFMain {
    protected static final Log LOG = LogFactory.getLog(GeoMetaPathMFMain.class);

    public static void main(String[] args) throws LibrecException, IOException, ClassNotFoundException {
        LocalDateTime time = LocalDateTime.now();
        String timeString = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH_mm_ss"));

        String path  = "D:\\Works\\论文\\dataSet\\experimentData\\Foursquare\\process\\实验数据8_21\\";
        String metaPath = "up" ;
        // build data model
        Configuration conf = new Configuration();
        conf.set("dfs.data.dir", path);
        //conf.set("data.input.path","metapath/"+ metaPath + ".txt");
        conf.set("data.input.path", "newTrain_uvc" + ".txt");
        conf.set("dfs.result.dir",path+"result");

        String outputPath  =  conf.get("dfs.result.dir") + "/" + metaPath + timeString;

        conf.set("data.appender.class", "geoup");
        conf.set("data.appender.poilatlon", "venue_lat_lon.txt");
        conf.set("data.appender.up", "newTrain_uvc.txt");
        conf.set("data.model.splitter","ratio");
        // 置 1 ，使 按比率切分无效，再按比率切分的代码中加入代码，使 ratio 为 1 时，训练集为输入矩阵，测试集也为输入矩阵，因为次步骤无需测试集
        conf.set("data.splitter.trainset.ratio","1");

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

        Recommender recommender = new GeograpicalMetaPathMFRecommender();
        recommender.setContext(context);

        // run recommender algorithm
        // 调用 recommend 方法后，会调用 setup 方法初始化 contex 等信息
        recommender.recommend(context);

        // evaluate the recommended result

        RecommenderEvaluator PRECISION = new PrecisionEvaluator();
        PRECISION.setTopN(10);
        System.out.println("PRECISION:" + recommender.evaluate(PRECISION));
        RecommenderEvaluator RECALL = new RecallEvaluator();
        RECALL.setTopN(10);
        System.out.println("RECALL:" + recommender.evaluate(RECALL));
        RecommenderEvaluator AP = new AveragePrecisionEvaluator();
        AP.setTopN(10);
        System.out.println("AP:" + recommender.evaluate(AP));
        RecommenderEvaluator Novelty = new NoveltyEvaluator();
        Novelty.setTopN(10);
        System.out.println("Novelty:" + recommender.evaluate(Novelty));

        List<RecommendedItem> recommendedList = recommender.getRecommendedList();


        // 输出所有用户对所有地点的评分
        String allResultOutputPath = conf.get("dfs.result.dir") + "/" + "metapath_feature_result/"+ metaPath;
        ((GeograpicalMetaPathMFRecommender)recommender).saveAllPredict(allResultOutputPath);
    }

    /**
     *
     * @param inputConf
     * @param metaPath   输入的元路径矩阵名称
     * @throws IOException
     */
    public static void run(Configuration inputConf,String metaPath) throws IOException, LibrecException {

        String path  = inputConf.get("dfs.data.dir");
        // build data model
        Configuration conf = new Configuration();
        conf.set("dfs.data.dir", path);
        //conf.set("data.input.path","metapath/"+ metaPath + ".txt");
        conf.set("data.input.path", inputConf.get("dfs.metapath.dir") + metaPath + ".txt");
        conf.set("dfs.result.dir",inputConf.get("dfs.result.dir"));


        conf.set("data.appender.class", "geoup");
        conf.set("data.appender.poilatlon", inputConf.get("hete.vanuelatlon.name"));
        conf.set("data.appender.up", inputConf.get("hete.train.name"));
        conf.set("data.model.splitter","ratio");
        // 置 1 ，使 按比率切分无效，再按比率切分的代码中加入代码，使 ratio 为 1 时，训练集为输入矩阵，测试集也为输入矩阵，因为次步骤无需测试集
        conf.set("data.splitter.trainset.ratio","1");

        TextDataModel dataModel = new TextDataModel(conf);
        dataModel.buildDataModel();


        // build recommender context
        RecommenderContext context = new RecommenderContext(conf, dataModel);

        // build recommender
        conf.set("rec.iterator.maximum", inputConf.get("metapath.rec.iterator.maximum"));
        conf.set("rec.user.regularization", inputConf.get("metapath.rec.user.regularization"));
        conf.set("rec.item.regularization", inputConf.get("metapath.rec.item.regularization"));
        conf.set("rec.factor.number", inputConf.get("metapath.rec.factor.number"));
        conf.set("rec.recommender.isranking", inputConf.get("metapath.rec.recommender.isranking"));
        conf.set("rec.recommender.ranking.topn", inputConf.get("metapath.rec.recommender.ranking.topn"));
        conf.set("rec.wrmf.weight.coefficient", inputConf.get("metapath.rec.wrmf.weight.coefficient"));



        // set power-law parameter
        conf.set("rec.geomf.powerlaw.a",inputConf.get("rec.geomf.powerlaw.a"));
        conf.set("rec.geomf.powerlaw.b",inputConf.get("rec.geomf.powerlaw.b"));
        conf.set("rec.geomf.alpha",inputConf.get("rec.geomf.alpha"));

        Recommender recommender = new GeograpicalMetaPathMFRecommender();
        recommender.setContext(context);

        // run recommender algorithm
        // 调用 recommend 方法后，会调用 setup 方法初始化 contex 等信息
        recommender.recommend(context);

        // 输出所有用户对所有地点的评分
        String allResultOutputPath = inputConf.get("dfs.metapathfeature.dir") + metaPath;
        ((GeograpicalMetaPathMFRecommender)recommender).saveAllPredict(allResultOutputPath);

    }

}
