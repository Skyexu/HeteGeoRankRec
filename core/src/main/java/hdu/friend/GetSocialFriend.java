package hdu.friend;

import net.librec.util.FileUtil;

import java.util.*;

/**
 * @Author: Skye
 * @Date: 16:31 2018/10/6
 * @Description: ASMF model KDD 2016  social friend
 */
public class GetSocialFriend {
    public static void main(String[] args) throws Exception {
        String path = "D:\\Works\\论文\\dataSet\\experimentData\\Foursquare\\process\\experiment_8_31\\";
        String friendPath = path + "friendship.txt";
        String outPath = path + "\\ASMF_DATA\\User_Social_Friendshs";

        String userIndexPath = path + "\\ASMF_DATA\\user_Index.txt";

        List<String> content = FileUtil.readAsList(friendPath);
        List<String> userIdex = FileUtil.readAsList(userIndexPath);
        Map<String,String> userIndexMap = new HashMap<>();

        for (String line:
                userIdex) {
            String[] strings = line.split("\t");
            userIndexMap.put(strings[1],strings[0]);
        }

        Map<Integer,Set<String>> friendMap = new TreeMap<>();
        for (String line:
             content) {
            String[] strings = line.split("\t");
            if (!userIndexMap.containsKey(strings[0]) || !userIndexMap.containsKey(strings[1]))
                continue;
            int user1 = Integer.valueOf(userIndexMap.get(strings[0]));
            String user2 = userIndexMap.get(strings[1]);
            if (friendMap.containsKey(user1)){
                friendMap.get(user1).add(user2);
            }else {
                Set<String> friendSet = new HashSet<>();
                friendSet.add(user2);
                friendMap.put(user1,friendSet);
            }
        }
        List<String> outList = new ArrayList<>();
        for (Map.Entry<Integer,Set<String> > entry:
             friendMap.entrySet()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(entry.getKey()).append("\t");
            Set<String> set = entry.getValue();
            for (String friend:
                 set) {
                stringBuilder.append(friend).append("\t");
            }
            outList.add(stringBuilder.toString());
        }
        FileUtil.writeList(outPath,outList);
    }
}
