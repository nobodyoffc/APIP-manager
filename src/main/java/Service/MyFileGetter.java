package Service;

import javax.swing.*;
import java.io.File;
import java.util.Vector;
 
public class MyFileGetter {
    public static void getAllFileNames(File file, Vector<String> fileNames) {
        //获得所有文件名，是不是和listdir一模一样
        File[] files = file.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                getAllFileNames(f, fileNames);
            }
            else {
                String path = f.getAbsolutePath();
                //是图片的话
                if(path.contains("jpg")||path.contains("png"))
                    fileNames.add(f.getAbsolutePath());
            }
        }
    }
    public static File getDirectory(){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        //设置好打开所在文件夹
        fileChooser.setCurrentDirectory(new File("/Users/liuchangyong/Desktop/eclipse-workspace/APIPservice"));
        if(fileChooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION)
        {
            File file = fileChooser.getSelectedFile();
            return file;
        }
        else{
            System.out.println("No return");
            return null;
            //为方便起见不抛出错误，返回一个默认的文件夹
        }
 
    }
}

