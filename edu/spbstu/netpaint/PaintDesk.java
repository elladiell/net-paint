package edu.spbstu.netpaint;


import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

public class PaintDesk extends JPanel {

    public enum DrawMode{CURVE, LINE, RECT, ELLIPSE, ERASER}

    private int lineThickness = 1;
    private Color lineColor = Color.BLACK;
    private Color fillColor = Color.BLACK;


    private DrawMode drawMode = DrawMode.CURVE;
    private Image image;
    private Image imageTemp;
    private Graphics2D graphics2D, graphics2DTemp;
    private int currentX, currentY, oldX, oldY;
    private boolean wasResized = false;

    public static final int DEFAULT_WIDTH = 400;
    public static final int DEFAULT_HEIGHT = 400;

    private Dimension drawZoneDimension = new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);


    private void makeImageTransparent(Graphics2D g2d){
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, getDrawZoneDimension().width, getDrawZoneDimension().height);
        g2d.setComposite(AlphaComposite.SrcOver);
    }

    public PaintDesk() {
        setDoubleBuffered(false);
        setPreferredSize(drawZoneDimension);
        addMouseListener(new MouseAdapter(){
            public void mousePressed(MouseEvent e){
                oldX = e.getX();
                oldY = e.getY();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                graphics2D.setComposite(AlphaComposite.SrcOver);
                //copy imageTemp to image
                graphics2D.drawImage(imageTemp, 0, 0, null);
                makeImageTransparent(graphics2DTemp);
            }
        });
        addMouseMotionListener(new MouseMotionAdapter(){
            public void mouseDragged(MouseEvent e){
                currentX = e.getX();
                currentY = e.getY();
                if(graphics2DTemp != null) {
                    if(getDrawMode() != DrawMode.CURVE && getDrawMode() != DrawMode.ERASER) {
                        makeImageTransparent(graphics2DTemp);
                    }
                    Stroke stroke = new BasicStroke(getLineThickness(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
                    if(getDrawMode() == DrawMode.ERASER){
                        graphics2DTemp.setColor(Color.white);
//                        stroke = new BasicStroke(getLineThickness(),
//                                BasicStroke.CAP_BUTT,
//                                BasicStroke.JOIN_MITER,
//                                10.0f, new float[]{10.0f}, 0.0f);
                    }else {
                        graphics2DTemp.setColor(getLineColor());
                    }
                    graphics2DTemp.setStroke(stroke);
                    if(getDrawMode() == DrawMode.RECT || getDrawMode() == DrawMode.ELLIPSE){
                        int topLeftX = oldX;
                        int topLeftY = oldY;
                        int width = Math.abs(currentX - oldX);
                        int height = Math.abs(currentY -  oldY);
                        if(oldX > currentX){
                            topLeftX = currentX;
                        }
                        if(oldY > currentY){
                            topLeftY = currentY;
                        }
                        if(getDrawMode() == DrawMode.RECT) {
                            graphics2DTemp.setColor(getFillColor());
                            graphics2DTemp.fillRect(topLeftX, topLeftY, width, height);
                            graphics2DTemp.setColor(getLineColor());
                            graphics2DTemp.drawRect(topLeftX, topLeftY, width, height);
                        }else {
                            graphics2DTemp.setColor(getFillColor());
                            graphics2DTemp.fillOval(topLeftX, topLeftY, width, height);
                            graphics2DTemp.setColor(getLineColor());
                            graphics2DTemp.drawOval(topLeftX, topLeftY, width, height);
                        }
                    }else {
                        graphics2DTemp.drawLine(oldX, oldY, currentX, currentY);
                    }
                }
                repaint();
                if(getDrawMode() == DrawMode.CURVE || getDrawMode() == DrawMode.ERASER) {
                    oldX = currentX;
                    oldY = currentY;
                }
            }
        });

    }

    public int getLineThickness() {
        return lineThickness;
    }

    public void setLineThickness(int lineThickness) {
        this.lineThickness = lineThickness;
    }

    public Color getLineColor() {
        return lineColor;
    }

    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
    }

    public Color getFillColor() {
        return fillColor;
    }

    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }

    public Dimension getDrawZoneDimension() {
        return drawZoneDimension;
    }

    public void setDrawZoneDimension(Dimension drawZoneDimension) {
        this.drawZoneDimension = drawZoneDimension;
        wasResized = true;
        setPreferredSize(drawZoneDimension);
        revalidate();
        repaint();
    }


    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
        setDrawZoneDimension(new Dimension(image.getWidth(null), image.getHeight(null)));
    }

    public DrawMode getDrawMode() {
        return drawMode;
    }

    public void setDrawMode(DrawMode drawMode) {
        this.drawMode = drawMode;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(image == null || wasResized){
            Image oldImage = image;
            imageTemp = new BufferedImage(getDrawZoneDimension().width, getDrawZoneDimension().height, BufferedImage.TYPE_INT_ARGB);
            graphics2DTemp = (Graphics2D) imageTemp.getGraphics();
            graphics2DTemp.setComposite(AlphaComposite.SrcOver);
            graphics2DTemp.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


            image = createImage(getDrawZoneDimension().width, getDrawZoneDimension().height);
            graphics2D = (Graphics2D)image.getGraphics();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.setBackground(Color.white);

            makeImageWhite(graphics2D);
            if(oldImage != null) {
                graphics2D.drawImage(oldImage, 0, 0, null);
            }
            wasResized = false;
        }
        g.drawImage(image, 0, 0, null);
        g.drawImage(imageTemp, 0, 0, null);

    }


    private void makeImageWhite(Graphics2D g2d){
        g2d.clearRect(0, 0, getSize().width, getSize().height);
        repaint();
    }
}
