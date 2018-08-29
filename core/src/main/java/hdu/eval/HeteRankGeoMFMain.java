package hdu.eval;

import com.google.common.collect.BiMap;
import hdu.bprranking.CombineFeature;
import hdu.bprranking.MetaPathBPRRecommender;
import hdu.metapath.MakeUPCP;
import net.librec.common.LibrecException;
import net.librec.conf.Configuration;
import net.librec.math.structure.DenseMatrix;
import net.librec.util.FileUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Skye
 * @Date: 15:49 2018/8/28
 * @Description: HeteRankGeoMF 测试入口
 *
 */
public class HeteRankGeoMFMain {
    private static final Log LOG = LogFactory.getLog(HeteRankGeoMFMain.class);

    public static void main(String[] args) throws Exception {
        // 读取配置文件
        Configuration conf = new Configuration();
        Configuration.Resource resource = new Configuration.Resource("rec/skye/heterankgeomf.properties");
        conf.addResource(resource);
        String[] metaPaths = new String[]{"up","upcp","upcpup","upup","uup","upupt","upcpupt"};
/*
        //1. 构造元路径语义相似度矩阵
        int[] userVenueNum = MakeMetaPathMain.run(conf);
        System.out.println("numUsers:" + userVenueNum[0] + " numItems: " + userVenueNum[1]);
        FileUtil.writeString(conf.get("hete.uservenuenum.path"),userVenueNum[0] + "," + userVenueNum[1]);

        // 上下文加权元路径
        MakeContexMetaPathMain.run(conf);

        */

        //2. 构建元路径特征

        for (String metaPath:
             metaPaths) {
            GeoMetaPathMFMain.run(conf,metaPath);
        }


        //3. 合并元路径特征
        List<String> inputFiles = new ArrayList<>();
        for (String metaPath:
                metaPaths) {
            inputFiles.add(conf.get("dfs.metapathfeature.dir")+metaPath);
        }
        String[] userVenue = FileUtil.readAsString(conf.get("hete.uservenuenum.path")).trim().split(",");
        int[] uv = new int[]{Integer.valueOf(userVenue[0]),Integer.valueOf(userVenue[1])};
        CombineFeature combineFeature = new CombineFeature(inputFiles,uv[0],uv[1],metaPaths.length);
        combineFeature.process();
        DenseMatrix denseMatrix = combineFeature.getFeatureMatix();
        BiMap<String,Integer> map = combineFeature.getUservenueMapping();


        //4. BPR 训练并验证结果
        MetaPathBPRRecommenderMain.run(conf,denseMatrix,map);

    }
}
