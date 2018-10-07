package hdu.friend;

/**
 * @Author: Skye
 * @Date: 20:15 2018/10/6
 * @Description:
 */
public class User implements Comparable<User>{
    private String userID;
    private double similarity;
    public User(String userID,double similarity){
        this.userID = userID;
        this.similarity = similarity;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }

    @Override
    public int compareTo(User o) {
        if (this.similarity - o.similarity > 0){
            return 1;
        }else {
            return -1;
        }
    }
}
