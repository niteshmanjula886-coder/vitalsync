import java.text.SimpleDateFormat;
import java.util.Date;
public class Main {
    public static void main(String[] args) {
        try {
            SimpleDateFormat f = new SimpleDateFormat("'Today at 'h:mm a");
            System.out.println(f.format(new Date()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
