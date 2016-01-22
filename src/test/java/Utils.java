import com.mingle.proton.utils.math.Caculator;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.HashMap;

/**
 * Created by Daniel on 2015/12/2.
 *
 * @author Daniel - ymx_gd@163.com
 * @since 0.0.1
 */
public class Utils {
    public static void main(String[] args) {
        System.out.println("nimmm");
        System.out.println(new Utils().newStr(null));
    }

    @Test
    public void test1(){
        String s = "4+54*6-8/9";
        Number result = Caculator.newIntance().caculate(s);
        System.out.println(s + " = " + result);
        HashMap
    }

    @NotNull
    public String newStr(@NotNull String name){
        return name;
    }

}
