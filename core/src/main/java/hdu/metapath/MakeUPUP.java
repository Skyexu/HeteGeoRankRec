package hdu.metapath;

import com.google.common.collect.*;
import hdu.util.FileUtil;
import net.librec.data.convertor.TextDataConvertor;
import net.librec.math.structure.SparseMatrix;
import net.librec.util.StringUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * @Author: Skye
 * @Date: 21:12 2018/5/31
 * @Description:  计算元路径 U-P-U-P 的基于计数的相似度
 *
 */
public class MakeUPUP implements MakeMetaPath{
    private static final Log LOG = LogFactory.getLog(MakeUPUP.class);

    private BiMap<String, Integer> userIds, itemIds;
    private SparseMatrix UPMatrix;
    private String inputDataPath;
    private SparseMatrix preferenceMatrix;
    public MakeUPUP(String inputDataPath){
        this.inputDataPath = inputDataPath;
    }
    @Override
    public void processData() throws IOException{
        readData(inputDataPath);
    }
    private void readData(String inputDataPath) throws IOException {
        LOG.info(String.format("Dataset: %s", StringUtil.last(inputDataPath, 38)));
        // Table {row-id, col-id, count}
        Table<Integer, Integer, Integer> dataTable = HashBasedTable.create();
        // Map {col-id, multiple row-id}: used to fast build a rating matrix
        Multimap<Integer, Integer> colMap = HashMultimap.create();
        if (this.userIds == null) {
            this.userIds = HashBiMap.create();
        }
        if (this.itemIds == null) {
            this.itemIds = HashBiMap.create();
        }
        String[] content = FileUtil.read(inputDataPath,null);
        for (int i = 0; i < content.length; i++) {
            String line = content[i];
            String[] data = line.trim().split("[ \t,]+");
            String user = data[0];
            String item = data[1];
            int count = Integer.parseInt(data[2]);
            int row = userIds.containsKey(user) ? userIds.get(user) : userIds.size();
            userIds.put(user, row);

            int col = itemIds.containsKey(item) ? itemIds.get(item) : itemIds.size();
            itemIds.put(item, col);

            dataTable.put(row, col, count);
            colMap.put(col, row);
        }
        int numRows = numUsers(), numCols = numItems();
        // build counting matrix
        UPMatrix = new SparseMatrix(numRows, numCols, dataTable, colMap);

        // release memory of data table
        dataTable = null;
    }
    public void processPreferenceMatrix(){

    }
    @Override
    public SparseMatrix getPreferenceMatrix() {
        return this.preferenceMatrix;
    }

    public int numUsers() {
        return userIds.size();
    }

    public int numItems() {
        return itemIds.size();
    }

    public BiMap<String, Integer> getUserIds() {
        return userIds;
    }

    public BiMap<String, Integer> getItemIds() {
        return itemIds;
    }

    public SparseMatrix getUPMatrix() {
        return UPMatrix;
    }
}
