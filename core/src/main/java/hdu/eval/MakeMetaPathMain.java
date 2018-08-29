package hdu.eval;

import hdu.metapath.MakeUPCP;
import hdu.metapath.MakeUPCPUP;
import hdu.metapath.MakeUPUP;
import hdu.metapath.MakeUUP;
import hdu.util.Utils;
import net.librec.conf.Configuration;
import net.librec.math.structure.DenseMatrix;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * @Author: Skye
 * @Date: 14:21 2018/8/28
 * @Description:  创建元路径语义相似度矩阵
 */
public class MakeMetaPathMain {
    protected static final Log LOG = LogFactory.getLog(MakeMetaPathMain.class);

    public static int[] run(Configuration conf) throws IOException {
        String path  = conf.get("dfs.data.dir");
        System.out.println(path);
        String upFile = path + conf.get("hete.train.name");
        String vcFile = path + conf.get("hete.vanuecategory.name");
        MakeUPCP upcpMaker = new MakeUPCP(upFile,vcFile);
        upcpMaker.processData();
        DenseMatrix upcpMatrix = upcpMaker.getPreferenceMatrix();
        String upcpOutputPath = path + conf.get("dfs.metapath.dir")+"upcp.txt";
        boolean upcpDone = Utils.saveDenseMatrix(upcpMatrix,upcpMaker.getUserIds(),upcpMaker.getItemIds(),upcpOutputPath);
        if (upcpDone)
            LOG.info("upcp result path is " + upcpOutputPath);


        MakeUPCPUP upcpupMaker = new MakeUPCPUP(upFile,vcFile);
        upcpupMaker.processData();
        DenseMatrix upcpupMatrix = upcpupMaker.getPreferenceMatrix();
        String upcpupOutputPath = path + conf.get("dfs.metapath.dir")+"upcpup.txt";
        boolean upcpupDone = Utils.saveDenseMatrix(upcpupMatrix,upcpupMaker.getUserIds(),upcpupMaker.getItemIds(),upcpupOutputPath);
        if (upcpupDone)
            LOG.info("upcp result path is " + upcpupOutputPath);

        MakeUPUP upupMaker = new MakeUPUP(upFile);
        upupMaker.processData();
        DenseMatrix upupMatrix = upupMaker.getPreferenceMatrix();
        String upupOutputPath = path + conf.get("dfs.metapath.dir")+"upup.txt";
        boolean upupDone = Utils.saveDenseMatrix(upupMatrix,upupMaker.getUserIds(),upupMaker.getItemIds(),upupOutputPath);
        if (upupDone)
            LOG.info("upcp result path is " + upupOutputPath);

        String uuFile = path + conf.get("hete.friends.name");
        MakeUUP uupMaker = new MakeUUP(upFile,uuFile);
        uupMaker.processData();
        DenseMatrix uupMatrix = uupMaker.getPreferenceMatrix();
        String uupOutputPath = path + conf.get("dfs.metapath.dir")+"uup.txt";
        boolean uupDone = Utils.saveDenseMatrix(uupMatrix,uupMaker.getUserIds(),uupMaker.getItemIds(),uupOutputPath);
        if (uupDone)
            LOG.info("uup result path is " + uupOutputPath);

        return new int[]{uupMatrix.numRows,uupMatrix.numColumns};



    }
}
