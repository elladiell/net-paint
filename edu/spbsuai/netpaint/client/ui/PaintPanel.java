package edu.spbsuai.netpaint.client.ui;


import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

public class PaintPanel extends JPanel {

    private static final int RESIZE_RECT_SIZE = 10;


    public enum DrawMode {CURVE, LINE, RECT, ELLIPSE, ERASER}

    private int lineThickness = 1;
    private Color lineColor = Color.CYAN;
    private Color fillColor = Color.PINK;


    private DrawMode drawMode = DrawMode.CURVE;
    private Image image;
    private Image imageTemp;
    private Graphics2D graphics2D, graphics2DTemp;
    private int currentX, currentY, oldX, oldY;
    private boolean wasResized = false;
    private boolean imgWasChanged = false;

    public static final int DEFAULT_WIDTH = 900;
    public static final int DEFAULT_HEIGHT = 500;


    private Dimension drawZoneDimension = new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    private int resizingHeight = drawZoneDimension.height + 1;
    private int resizingWidth = drawZoneDimension.width + 1;
    private int resizingDx, resizingDy;

    private boolean isResizing = false;


    private void makeImageTransparent(Graphics2D g2d) {
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, getDrawZoneDimension().width, getDrawZoneDimension().height);
        g2d.setComposite(AlphaComposite.SrcOver);
    }

    private boolean isResizeRectHovered(int x, int y) {
        if (x > getDrawZoneDimension().width && x <= getDrawZoneDimension().width + RESIZE_RECT_SIZE + 1
                && y > getDrawZoneDimension().height && y <= getDrawZoneDimension().height + RESIZE_RECT_SIZE + 5) {
            return true;
        }
        return false;
    }

    public PaintPanel() {
        setDoubleBuffered(false);
        setBackground(Color.gray);
        setPreferredSize(new Dimension(this.drawZoneDimension.width + 2 * RESIZE_RECT_SIZE, this.drawZoneDimension.height + 3 * RESIZE_RECT_SIZE));
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                oldX = e.getX();
                oldY = e.getY();
                if (isResizeRectHovered(oldX, oldY)) {
                    isResizing = true;
                    resizingDx = oldX - resizingWidth;
                    resizingDy = oldY - resizingHeight;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                graphics2D.setComposite(AlphaComposite.SrcOver);
                //copy imageTemp to image
                graphics2D.drawImage(imageTemp, 0, 0, null);
                makeImageTransparent(graphics2DTemp);
                if (isResizing) {
                    isResizing = false;
                    setDrawZoneDimension(new Dimension(resizingWidth - 1, resizingHeight - 1));
                } else {
                    firePropertyChange("image", null, getImage());
                }

            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (isResizeRectHovered(e.getX(), e.getY())) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                } else {
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }

            public void mouseDragged(MouseEvent e) {
                currentX = e.getX();
                currentY = e.getY();
                if (isResizing) {
                    resizingWidth = currentX - resizingDx;
                    resizingHeight = currentY - resizingDy;
                    firePropertyChange("drawZoneResizing", null, new Dimension(resizingWidth - 1, resizingHeight - 1));
                } else if (graphics2DTemp != null) {
                    if (getDrawMode() != DrawMode.CURVE && getDrawMode() != DrawMode.ERASER) {
                        makeImageTransparent(graphics2DTemp);
                    }
                    Stroke stroke = new BasicStroke(getLineThickness(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
                    if (getDrawMode() == DrawMode.ERASER) {
                        graphics2DTemp.setColor(Color.white);
//                        stroke = new BasicStroke(getLineThickness(),
//                                BasicStroke.CAP_BUTT,
//                                BasicStroke.JOIN_MITER,
//                                10.0f, new float[]{10.0f}, 0.0f);
                    } else {
                        graphics2DTemp.setColor(getLineColor());
                    }
                    graphics2DTemp.setStroke(stroke);
                    if (getDrawMode() == DrawMode.RECT || getDrawMode() == DrawMode.ELLIPSE) {
                        int topLeftX = oldX;
                        int topLeftY = oldY;
                        int width = Math.abs(currentX - oldX);
                        int height = Math.abs(currentY - oldY);
                        if (oldX > currentX) {
                            topLeftX = currentX;
                        }
                        if (oldY > currentY) {
                            topLeftY = currentY;
                        }
                        if (getDrawMode() == DrawMode.RECT) {
                            graphics2DTemp.setColor(getFillColor());
                            graphics2DTemp.fillRect(topLeftX, topLeftY, width, height);
                            graphics2DTemp.setColor(getLineColor());
                            graphics2DTemp.drawRect(topLeftX, topLeftY, width, height);
                        } else {
                            graphics2DTemp.setColor(getFillColor());
                            graphics2DTemp.fillOval(topLeftX, topLeftY, width, height);
                            graphics2DTemp.setColor(getLineColor());
                            graphics2DTemp.drawOval(topLeftX, topLeftY, width, height);
                        }
                    } else {
                        graphics2DTemp.drawLine(oldX, oldY, currentX, currentY);
                    }

                    if (getDrawMode() == DrawMode.CURVE || getDrawMode() == DrawMode.ERASER) {
                        oldX = currentX;
                        oldY = currentY;
                    }
                }
                repaint();
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
        Dimension oldValue = this.drawZoneDimension;
        if (!oldValue.equals(drawZoneDimension)) {
            resizingHeight = drawZoneDimension.height + 1;
            resizingWidth = drawZoneDimension.width + 1;
            this.drawZoneDimension = drawZoneDimension;
            wasResized = true;

            setPreferredSize(new Dimension(this.drawZoneDimension.width + RESIZE_RECT_SIZE, this.drawZoneDimension.height + RESIZE_RECT_SIZE));
            revalidate();
            repaint();
            firePropertyChange("drawZoneDimension", oldValue, drawZoneDimension);
        }
   }


    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
        setDrawZoneDimension(new Dimension(image.getWidth(null), image.getHeight(null)));
        imgWasChanged = true;
        repaint();
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
        if (image == null || wasResized || imgWasChanged) {
            if(imgWasChanged){
                graphics2D = (Graphics2D) image.getGraphics();
                imgWasChanged = false;
            }else {
                Image oldImage = image;
                imageTemp = new BufferedImage(getDrawZoneDimension().width, getDrawZoneDimension().height, BufferedImage.TYPE_INT_ARGB);
                graphics2DTemp = (Graphics2D) imageTemp.getGraphics();
                graphics2DTemp.setComposite(AlphaComposite.SrcOver);
                graphics2DTemp.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


                image = createImage(getDrawZoneDimension().width, getDrawZoneDimension().height);
                graphics2D = (Graphics2D) image.getGraphics();
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics2D.setBackground(Color.white);

                makeImageWhite(graphics2D);
                if (oldImage != null) {
                    graphics2D.drawImage(oldImage, 0, 0, null);
                }
                if (wasResized) {
                    firePropertyChange("image", null, image);
                    System.out.println("paintComponent !!!!!");
                }
                wasResized = false;
            }
        }
        g.drawImage(image, 0, 0, null);
        g.drawImage(imageTemp, 0, 0, null);
        g.setColor(Color.BLUE);
        g.drawLine(0, resizingHeight, resizingWidth, resizingHeight);
        g.drawLine(resizingWidth, 0, resizingWidth, resizingHeight);
        g.fillRect(resizingWidth, resizingHeight, RESIZE_RECT_SIZE, RESIZE_RECT_SIZE);

    }


    private void makeImageWhite(Graphics2D g2d) {
        g2d.clearRect(0, 0, getSize().width, getSize().height);
        repaint();
    }
}
