import org.junit.Test;

import java.util.Random;

/**
 * Created by Daniel on 2015/12/2.
 *
 * @author Daniel - ymx_gd@163.com
 * @since 0.0.1
 */
public class Utils {

    /** 冒泡排序 */
    @Test
    public void babbleSort(){
        int[] array = randomArray(20,100);
        printArray(array);
        int  checks= 0;
        for(int i=0;i<array.length;i++){
            for(int j=i+1;j<array.length;j++){
                checks++;
                int t1 = array[i];
                int t2 = array[j];
                if(t1>t2){
                    array[j] = t1;
                    array[i] = t2;
                }
            }
        }
        printArray(array);
        System.out.println("冒泡排序步骤数：" + checks);
    }

    /** 直接插入排序 从小到大排*/
    @Test
    public void straightInsertionSort(){

        int[] array = randomArray(20,100);
        printArray(array);
        int checks = 0;
        for(int i=1; i<array.length; i++){
            int t = array[i];
            int l = i-1;
            while(l>=0 && array[l] > t){
                array[l+1] = array[l];
                checks++;
                l--;
            }
            array[l+1] = t;
        }
        printArray(array);
        System.out.println("直接插入排序步骤数："+checks);
    }

    public static void printArray(int[] array){
        if(array == null){
            System.out.println("null");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for(int i : array){
            sb.append(i);
            sb.append(",");
        }
        System.out.println(sb.toString());
        return ;
    }

    public static int[] randomArray(int size, int max){
        //初始化目标数组
        int[] array = new int[size];
        Random random = new Random(System.nanoTime());

        for(int i=0;i<size;i++){
            array[i] = random.nextInt(max);
        }
        return array;
    }
}
