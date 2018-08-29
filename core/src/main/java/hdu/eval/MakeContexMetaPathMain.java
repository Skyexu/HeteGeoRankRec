package hdu.eval;

import com.google.common.collect.BiMap;
import hdu.metapath.*;
import hdu.util.Utils;
import net.librec.conf.Configuration;
import net.librec.math.structure.DenseMatrix;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @Author: Skye
 * @Date: 16:20 2018/8/29
 * @Description:
 */
public class MakeContexMetaPathMain {
    protected static final Log LOG = LogFactory.getLog(MakeContexMetaPathMain.class);

    public static void run(Configuration conf) throws IOException {
        String path  = conf.get("dfs.data.dir");
        String timetrainPath = path + conf.get("hete.timeslot.dir");

        LOG.info(timetrainPath);
        int timeSlotNum = conf.getInt("hete.timeslot.num");

        String vcFile = path + conf.get("hete.vanuecategory.name");
        // upup 路径 时间上下文加权

        List<MakeMetaPath> upupMakers = new ArrayList<>();
        String upupOutputPath = path + conf.get("dfs.metapath.dir")+"upupt.txt";
        for (int i = 0; i < timeSlotNum; i++) {
            String upFile = timetrainPath + i + "uvc.txt";
            MakeUPUP upupMaker = new MakeUPUP(upFile);
            upupMaker.processData();
            upupMakers.add(upupMaker);
        }

        CombinContexPath c1 = new CombinContexPath(upupMakers,upupOutputPath,conf);
        c1.combin();

        // upcpup 路径 时间上下文加权

        List<MakeMetaPath> upcpupMakers = new ArrayList<>();
        String upcpupOutputPath = path + conf.get("dfs.metapath.dir")+"upcpupt.txt";
        for (int i = 0; i < timeSlotNum; i++) {
            String upFile = timetrainPath + i + "uvc.txt";
            MakeUPCPUP upcpupMaker = new MakeUPCPUP(upFile,vcFile);
            upcpupMaker.processData();
            upcpupMakers.add(upcpupMaker);
        }

        CombinContexPath c2 = new CombinContexPath(upcpupMakers,upcpupOutputPath,conf);
        c2.combin();

    }
}
