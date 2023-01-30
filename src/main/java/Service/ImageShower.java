package Service;

import javax.swing.*;
import java.awt.*;
 
public class ImageShower extends JPanel {
    private static final long serialVersionUID = 1L;
	private Image image;
    private boolean streched = true;
    private int xCoordinate;
    private int yCoordinate;
 
    public ImageShower(){};
 
    public ImageShower(Image image)
    {
        this.image=image;
    }
 
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
 
        if(image!=null){
            if(streched)
                g.drawImage(image,xCoordinate,yCoordinate,getWidth(),getHeight(),this);
        }
        else
            g.drawImage(image,xCoordinate,yCoordinate,this);
    }
 
    public Image getImage()
    {
        return image;
    }
 
    public void setImage(Image image) {
        this.image = image;
        repaint();
    }
 
    public boolean isStreched() {
        return streched;
    }
 
    public void setStreched(boolean streched) {
        this.streched = streched;
        repaint();
    }
 
    public int getxCoordinate() {
        return xCoordinate;
    }
 
    public void setxCoordinate(int x)
    {
        xCoordinate=x;
        repaint();
    }
    public int getyCoordinate() {
        return yCoordinate;
    }
 
    public void setyCoordinate(int y)
    {
        yCoordinate=y;
        repaint();
    }
}
