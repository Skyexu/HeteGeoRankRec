package hdu.metapath;

import com.google.common.collect.*;
import hdu.util.FileUtil;
import net.librec.math.structure.DenseMatrix;
import net.librec.math.structure.DenseVector;
import net.librec.math.structure.SparseMatrix;
import net.librec.math.structure.SparseVector;
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
    private int numUsers;
    private int numItems;
    private SparseMatrix UPMatrix;
    private String inputDataPath;
    private DenseMatrix preferenceMatrix;
    public MakeUPUP(String inputDataPath){
        this.inputDataPath = inputDataPath;
    }
    @Override
    public void processData() throws IOException{
        readData(inputDataPath);
        processPreferenceMatrix();
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
        numUsers = numUsers();
        numItems = numItems();
        // release memory of data table
        dataTable = null;
    }
    private void processPreferenceMatrix(){
        DenseMatrix Wpu_Wput = new DenseMatrix(numUsers,numUsers);
        for (int userIdx = 0; userIdx < numUsers; userIdx++) {
            SparseVector userRow = UPMatrix.row(userIdx);
            for (int userIdx2 = 0; userIdx2 < numUsers; userIdx2++) {
                SparseVector userColumn = UPMatrix.row(userIdx2);
                Integer value = (int)userRow.inner(userColumn);
                Wpu_Wput.set(userIdx,userIdx2,value);
            }
        }
        preferenceMatrix = new DenseMatrix(numUsers, numItems);
        for (int userIdx = 0; userIdx < numUsers; userIdx++) {
            DenseVector userRow = Wpu_Wput.row(userIdx);
            for (int itemIndex = 0; itemIndex < numItems; itemIndex++) {
                SparseVector itemColumn = UPMatrix.column(itemIndex);
                Integer value = (int) userRow.inner(itemColumn);
                preferenceMatrix.set(userIdx,itemIndex,value);
            }
        }


    }
    @Override
    public DenseMatrix getPreferenceMatrix() {
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
