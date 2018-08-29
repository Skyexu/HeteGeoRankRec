package hdu.metapath;

import com.google.common.collect.*;
import hdu.util.FileUtil;
import net.librec.conf.Configuration;
import net.librec.math.structure.DenseMatrix;
import net.librec.math.structure.SparseMatrix;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.List;

/**
 * @Author: Skye
 * @Date: 16:43 2018/8/29
 * @Description:
 */
public class CombinContexPath {
    private static final Log LOG = LogFactory.getLog(CombinContexPath.class);
    private BiMap<String, Integer> userIds, itemIds;
    private int numUsers;
    private int numItems;
    private SparseMatrix preferenceMatrix;
    private List<MakeMetaPath> makers;
    private String outputPath;
    private Configuration conf;
    public CombinContexPath(List<MakeMetaPath> makers, String outputpath, Configuration conf){
        this.makers = makers;
        this.outputPath = outputpath;
        this.conf = conf;
    }

    public void combin(){
        // 通过 preference 文件来获取所有的 用户和地点 ID
        getTotalUserVenueByPreference();
        // Table {row-id, col-id, count}
        Table<Integer, Integer, Double> dataTable = HashBasedTable.create();
        // Map {col-id, multiple row-id}: used to fast build a rating matrix
        Multimap<Integer, Integer> colMap = HashMultimap.create();

        for (int i = 0; i < makers.size(); i++) {
            DenseMatrix preferenceMatrix = makers.get(i).getPreferenceMatrix();

            BiMap<Integer, String> inveseUserIdsNow = makers.get(i).getUserIds().inverse();
            BiMap<Integer, String> itemIdsNow = makers.get(i).getItemIds().inverse();

            for (int j = 0; j < preferenceMatrix.numRows(); j++) {
                String user = inveseUserIdsNow.get(j);
                int row = userIds.containsKey(user) ? userIds.get(user) : userIds.size();
                userIds.put(user, row);
                for (int k = 0; k < preferenceMatrix.numColumns(); k++) {
                    String item = itemIdsNow.get(k);
                    double count = preferenceMatrix.get(j, k);

                    int col = itemIds.containsKey(item) ? itemIds.get(item) : itemIds.size();
                    itemIds.put(item, col);

                    dataTable.put(row, col, count);
                    colMap.put(col, row);
                }
            }
        }
        numUsers = numUsers();
        numItems = numItems();
        preferenceMatrix = new SparseMatrix(numUsers(), numItems(), dataTable, colMap);
        // release memory of data table
        dataTable = null;

        saveTrainTestSet();
    }
    public void saveTrainTestSet(){
        BiMap<Integer, String> inverseUserIds = userIds.inverse();
        BiMap<Integer, String> inverseItemIds = itemIds.inverse();

        File trainFile = new File(outputPath);
        BufferedWriter writer = null;
        try {
            if (!trainFile.getParentFile().exists())
                trainFile.getParentFile().mkdirs();
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(trainFile)));
            for (int i = 0; i < numUsers; i++) {
                for (int j = 0; j < numItems; j++) {
                    writer.write(inverseUserIds.get(i) + "\t" + inverseItemIds.get(j) + "\t" + preferenceMatrix.get(i,j) + "\n");
                }
            }
            writer.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
        LOG.info("New combin contexpath  dataset path is " + outputPath);


    }
    private void getTotalUserVenueByPreference(){
        String path = conf.get("dfs.data.dir");
        String preferencePath = path + conf.get("hete.preference.name");

        if (this.userIds == null) {
            this.userIds = HashBiMap.create();
        }
        if (this.itemIds == null) {
            this.itemIds = HashBiMap.create();
        }
        String[] content = FileUtil.read(preferencePath,null);
        for (int i = 0; i < content.length; i++) {
            String line = content[i];
            String[] data = line.trim().split("[ \t,]+");
            String user = data[0];
            String item = data[1];
            int row = userIds.containsKey(user) ? userIds.get(user) : userIds.size();
            userIds.put(user, row);
            int col = itemIds.containsKey(item) ? itemIds.get(item) : itemIds.size();
            itemIds.put(item, col);
        }

    }
    public int numUsers() {
        return userIds.size();
    }

    public int numItems() {
        return itemIds.size();
    }
}
