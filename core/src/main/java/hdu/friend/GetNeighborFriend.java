package hdu.friend;

import net.librec.util.FileUtil;

import java.util.*;

/**
 * @Author: Skye
 * @Date: 21:11 2018/10/6
 * @Description:
 */
public class GetNeighborFriend {
    public static void main(String[] args) throws Exception {
        String path = "D:\\Works\\论文\\dataSet\\experimentData\\Foursquare\\process\\experiment_8_31\\";
        String userHomePath = path + "\\ASMF_DATA\\User_Home";
        String outPath = path + "\\ASMF_DATA\\User_Neighbor_Friends";
        List<String> content = FileUtil.readAsList(userHomePath);
        Map<String,List<String>> friendMap = new LinkedHashMap<>();
        Map<String,double[]> user_loc = new LinkedHashMap<>();
        for (String line:
                content) {
            String[] strings = line.split("\t");
            double[] loc = new double[2];
            loc[0] = Double.parseDouble(strings[1]);
            loc[1] = Double.parseDouble(strings[2]);
            user_loc.put(strings[0],loc);
        }
        for (Map.Entry<String,double[] > entry:
                user_loc.entrySet()) {
            String user = entry.getKey();
            double[] loc = entry.getValue();
            PriorityQueue<User> priorityQueue = new PriorityQueue<>(Collections.reverseOrder());

            for (Map.Entry<String,double[] > entry2:
                    user_loc.entrySet()) {
                String user2 = entry2.getKey();
                if (user2 == user)
                    continue;
                double[] loc2 = entry2.getValue();
                double distance = Math.sqrt(Math.pow(loc2[0] - loc[0],2) + Math.pow(loc2[1] - loc[1] ,2));
                User friend = new User(user2,distance);
                priorityQueue.add(friend);
            }

            List<String> list = new ArrayList<>();
            int count = 0;
            while (priorityQueue.peek().getSimilarity() != 0 && count < 10){
                list.add(priorityQueue.poll().getUserID());
                count++;
            }
            System.out.println(user + " " + list.size());
            friendMap.put(user,list);
        }


        List<String> outList = new ArrayList<>();
        for (Map.Entry<String,List<String> > entry:
                friendMap.entrySet()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(entry.getKey()).append("\t");
            List<String> set = entry.getValue();
            for (String friend:
                    set) {
                stringBuilder.append(friend).append("\t");
            }
            outList.add(stringBuilder.toString());
        }
        FileUtil.writeList(outPath,outList);
    }
}
