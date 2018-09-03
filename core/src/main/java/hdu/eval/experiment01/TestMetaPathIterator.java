package hdu.eval.experiment01;

import com.google.common.collect.BiMap;
import hdu.bprranking.CombineFeature;
import hdu.eval.GeoMetaPathMFMain;
import hdu.eval.MakeContexMetaPathMain;
import hdu.eval.MakeMetaPathMain;
import hdu.eval.MetaPathBPRRecommenderMain;
import net.librec.conf.Configuration;
import net.librec.math.structure.DenseMatrix;
import net.librec.util.FileUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Skye
 * @Date: 16:20 2018/8/31
 * @Description: 测试 HeteGeoRankRec 迭代次数
 */

public class TestMetaPathIterator {
    private static final Log LOG = LogFactory.getLog(TestMetaPathIterator.class);

    public static void main(String[] args) throws Exception {
        // 读取配置文件
        Configuration conf = new Configuration();
        Configuration.Resource resource = new Configuration.Resource("rec/skye/heterankgeomf.properties");
        conf.addResource(resource);
        String[] metaPaths = new String[]{"up", "upcp", "upcpup", "upup", "uup", "upupt", "upcpupt",
                "upupw_cloudCover", "upupw_humidity", "upupw_temperature",
                "upcpupw_cloudCover", "upcpupw_humidity", "upcpupw_temperature"};
/*
        //1. 构造元路径语义相似度矩阵
        int[] userVenueNum = MakeMetaPathMain.run(conf);
        System.out.println("numUsers:" + userVenueNum[0] + " numItems: " + userVenueNum[1]);
        FileUtil.writeString(conf.get("hete.uservenuenum.path"),userVenueNum[0] + "," + userVenueNum[1]);

        // 上下文加权元路径
        MakeContexMetaPathMain.run(conf);


*/
        //2. 构建元路径特征
        List<String> evalList = new ArrayList<>();

        for (int i = 10; i <= 200; i += 10) {
            conf.set("metapath.rec.iterator.maximum", i + "");

            for (String metaPath :
                    metaPaths) {
                GeoMetaPathMFMain.run(conf, metaPath);
            }

            //3. 合并元路径特征
            List<String> inputFiles = new ArrayList<>();
            for (String metaPath :
                    metaPaths) {
                inputFiles.add(conf.get("dfs.metapathfeature.dir") + metaPath);
            }
            String[] userVenue = FileUtil.readAsString(conf.get("hete.uservenuenum.path")).trim().split(",");
            int[] uv = new int[]{Integer.valueOf(userVenue[0]), Integer.valueOf(userVenue[1])};
            CombineFeature combineFeature = new CombineFeature(inputFiles, uv[0], uv[1], metaPaths.length);
            combineFeature.process();
            DenseMatrix denseMatrix = combineFeature.getFeatureMatix();
            BiMap<String, Integer> map = combineFeature.getUservenueMapping();

            //4. BPR 训练并验证结果
            String eval = MetaPathBPRRecommenderMain.run(conf, denseMatrix, map);
            evalList.add(eval);

            combineFeature = null;

        }
        LocalDateTime time = LocalDateTime.now();
        String timeString = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH_mm_ss"));
        String outPath = conf.get("dfs.eval.dir") + "testMetaPathIterator/testMetaPathIterator" + timeString;
        FileUtil.writeList(outPath,evalList);
    }
}
