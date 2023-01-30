package Test;

import javax.swing.*;

import Service.ImageShower;
import Service.MyFileGetter;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Random;
import java.util.Vector;
 
public class Tester extends JFrame{
    private Image image1;//image对象
    private ImageIcon imageicon1;
    private ImageShower imgShower;//用于显示图片
    public int imgWidth;
    public int imgHeight;
    private double muliple;
    private int currentPos = 0;
    private Random r = new Random();
    private Vector<String> filenames = new Vector<String>(30);
    public Tester(Vector<String> filenames){
        this.filenames = filenames;
        currentPos = -1;
        setNewImage(1);
 
        setLayout(new GridLayout(1,2,5,5));
        imgShower = new ImageShower(image1);
        add(imgShower);
 
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                switch (e.getKeyCode()){
                    //下一张
                    case KeyEvent.VK_RIGHT:
                        setNewImage(1);
                        changeImage();
                        break;
                    //上一张
                    case KeyEvent.VK_LEFT:
                        setNewImage(-1);
                        changeImage();
                        break;
                    //随机一张
                    case KeyEvent.VK_UP:
                        setNewImage(0);
                        changeImage();
                        break;
                }
            }
        });
    }
    private void changeImage(){
        //设置新的图片
        imgShower.setImage(image1);
        //窗口改变大小
        setSize(imgWidth,imgHeight);
    }
    private void setNewImage(int mode)
    {
        if(mode==1)//下一张
            currentPos = (currentPos+1)%filenames.size();
        else if(mode==-1)//上一张
            currentPos = currentPos-1>=0?currentPos-1:filenames.size()-1;
        else if(mode==0)//随机一张
            currentPos = r.nextInt(0,filenames.size());
        String path = filenames.get(currentPos);
        imageicon1 = new ImageIcon(path);
        image1 =  imageicon1.getImage();
        imgWidth = imageicon1.getIconWidth();
        imgHeight = imageicon1.getIconHeight();
 
        //为了保证长宽不超过屏幕我们提前设置一个合适的尺寸，使图片只能在此大小
        muliple = (double)Math.min(1000.0/imgWidth,800.0/imgHeight);
        imgWidth *= muliple;
        imgHeight *= muliple;
 
        //设置标题
        int tmp = path.lastIndexOf('\\');
        setTitle(path.substring(tmp+1));
    }
 
    public static void main(String[] args) throws Exception {
        File director = MyFileGetter.getDirectory();
        Vector<String> filenames = new Vector<String>();
        MyFileGetter.getAllFileNames(director,filenames);
 
        Tester frame = new Tester(filenames);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(frame.imgWidth, frame.imgHeight);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
