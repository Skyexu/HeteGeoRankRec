package hdu.friend;

import com.google.common.collect.*;
import hdu.metapath.MakeUPUP;
import hdu.util.FileUtil;
import net.librec.math.structure.DenseMatrix;
import net.librec.math.structure.DenseVector;
import net.librec.math.structure.SparseMatrix;
import net.librec.math.structure.SparseVector;
import net.librec.util.StringUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.*;

/**
 * @Author: Skye
 * @Date: 19:17 2018/10/6
 * @Description: ASMF model KDD 2016  location friend    most
 */
public class GetLocationFriend {
    private static final Log LOG = LogFactory.getLog(GetLocationFriend.class);

    private BiMap<String, Integer> userIds, itemIds;
    private int numUsers;
    private int numItems;
    private SparseMatrix UPMatrix;
    private String inputDataPath;
    private DenseMatrix preferenceMatrix;
    private Map<String,List<String>> locationFriend;  // user , locationFriends

    public GetLocationFriend(String inputDataPath){
        this.inputDataPath = inputDataPath;
    }

    public void processData() throws IOException {
        readData(inputDataPath);
        process();
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
            int count = Double.valueOf(data[2]).intValue();
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
    private void process(){
        BiMap<Integer, String> inverseUserIds = userIds.inverse();
        locationFriend = new LinkedHashMap<>();
        /*
        DenseMatrix Wpu_Wput = new DenseMatrix(numUsers,numUsers);
        for (int userIdx = 0; userIdx < numUsers; userIdx++) {
            SparseVector userRow = UPMatrix.row(userIdx);
            for (int userIdx2 = 0; userIdx2 < numUsers; userIdx2++) {
                SparseVector userColumn = UPMatrix.row(userIdx2);
                double value = userRow.inner(userColumn);
                Wpu_Wput.set(userIdx,userIdx2,value);
            }
        }
        /*preferenceMatrix = new DenseMatrix(numUsers, numItems);
        for (int userIdx = 0; userIdx < numUsers; userIdx++) {
            DenseVector userRow = Wpu_Wput.row(userIdx);
            for (int itemIndex = 0; itemIndex < numItems; itemIndex++) {
                SparseVector itemColumn = UPMatrix.column(itemIndex);
                double value = userRow.inner(itemColumn);
                preferenceMatrix.set(userIdx,itemIndex,value);
            }
        }
        */

        for (int userIdx = 0; userIdx < numUsers; userIdx++) {
            PriorityQueue<User> priorityQueue = new PriorityQueue<>(Collections.reverseOrder());
            SparseVector userRow = UPMatrix.row(userIdx);
            for (int userIdx2 = 0; userIdx2 < numUsers; userIdx2++) {
                if (userIdx2 == userIdx)
                    continue;
                SparseVector userRow2 = UPMatrix.row(userIdx2);
                double inner = userRow.inner(userRow2);
                double left = 0,right = 0;
                for (int i = 0;i < userRow.size();i++) {
                    left += Math.pow(userRow.get(i),2);
                }
                for (int i = 0;i < userRow2.size();i++) {
                    right += Math.pow(userRow2.get(i),2);
                }

                double similarity = 0;
                if (left !=0 && right != 0){
                    similarity = inner / (Math.sqrt(left) * Math.sqrt(right));
                }
                User user = new User(inverseUserIds.get(userIdx2),similarity);
                priorityQueue.add(user);
            }
            List<String> list = new ArrayList<>();
            int count = 0;
            while (priorityQueue.peek().getSimilarity() != 0 && count < 10){
                list.add(priorityQueue.poll().getUserID());
                count++;
            }
            System.out.println(userIdx + " " + list.size());
            locationFriend.put(inverseUserIds.get(userIdx),list);
        }
    }

    public Map<String, List<String>> getLocationFriend() {
        return locationFriend;
    }

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

    public static void main(String[] args) throws Exception {
        String path = "D:\\Works\\论文\\dataSet\\experimentData\\Foursquare\\process\\experiment_8_31\\";
        String trainPath = path + "\\ASMF_DATA\\newTrain_uvc.txt";
        String outPath = path + "\\ASMF_DATA\\User_Location_Friends";
        GetLocationFriend getLocationFriend = new GetLocationFriend(trainPath);

        getLocationFriend.processData();

        Map<String,List<String>> locationFriend = getLocationFriend.getLocationFriend();
        List<String> outList = new ArrayList<>();
        for (Map.Entry<String,List<String> > entry:
                locationFriend.entrySet()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(entry.getKey()).append("\t");
            List<String> list = entry.getValue();
            if (list.size() == 0){
                continue;
            }
            for (String friend:
                    list) {
                stringBuilder.append(friend).append("\t");
            }
            outList.add(stringBuilder.toString());
        }
        net.librec.util.FileUtil.writeList(outPath,outList);
    }
}
