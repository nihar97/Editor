package editor;

import javafx.geometry.VPos;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.HashMap;

/**
 * Created by We on 01-03-2016.
 */
public class Render {
    public final double LEFT_MARGIN = 5.0;
    public final double RIGHT_MARGIN = 5.0;
    public TextList fileList;
    private TextList.TextNode[] fileNodes;
    private Text[] fileContents;
    private HashMap<Double, TextList.TextNode> characterMappings;
    public double windowWidth;
    public double windowHeight;
    private String fontName;
    private int fontSize;
    private double standardHeight = Math.round(new Text("").getLayoutBounds().getHeight());
    private int startOfWord;
    public Render(TextList t, double windowWidth, double windowHeight, String fontName, int fontSize) {
        fileList = t;
        fileContents = t.makeArray();
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        characterMappings = new HashMap<Double, TextList.TextNode>((int) (this.windowHeight / standardHeight));
        fileNodes = t.makeArrayofNodes();
        this.fontName = fontName;
        this.fontSize = fontSize;
        startOfWord = 0;
    }

    public Text[] coordinate() {
        startOfWord = 0;
        boolean cantWrap = false;
        fileContents[0].setX(LEFT_MARGIN);
        fileContents[0].setY(0);
        fileContents[0].setTextOrigin(VPos.TOP);
        fileContents[0].setFont(Font.font(fontName, fontSize));
        addThisTextNodeToMap(fileNodes[0], 0);
        for (int i = 1; i < fileContents.length; i++) {
            if(fileContents[i].getText().equals(" ")){
                startOfWord = i + 1;
            }
            fileContents[i].setX(fileContents[i - 1].getX() + Math.round(fileContents[i - 1].getLayoutBounds().getWidth()));
            if ((int) fileContents[i].getX() + fileContents[i].getLayoutBounds().getWidth() > windowWidth - RIGHT_MARGIN
                    && fileContents[startOfWord].getX() != LEFT_MARGIN)
            {
                wordWrapping(i);

            } else if (fileContents[i].getText().equals("\n") || fileContents[i].getText().equals("\r")
                    || (int) fileContents[i].getX() + fileContents[i].getLayoutBounds().getWidth() > windowWidth - RIGHT_MARGIN){
                fileContents[i].setY(fileContents[i - 1].getY() + standardHeight);
                fileContents[i].setX(LEFT_MARGIN);
                addThisTextNodeToMap(fileNodes[i], fileContents[i].getY());
            } else {
                fileContents[i].setY(fileContents[i - 1].getY());
            }
            fileContents[i].setTextOrigin(VPos.TOP);
            fileContents[i].setFont(Font.font(fontName, fontSize));
        }
        return fileContents;
    }

    public void addThisTextNodeToMap(TextList.TextNode t, double Y) {
        characterMappings.put(Y, t);
    }

    public void setFileList(TextList t) {
        this.fileList = t;
        this.fileContents = t.makeArray();
        this.fileNodes = t.makeArrayofNodes();
    }

    public HashMap<Double, TextList.TextNode> getCharacterMappings() {
        return characterMappings;
    }

//    public TextList.TextNode findClosestTextObject(double X, double Y) {
//        int lineNum = (int) (Y/standardHeight);
//        double roundedY = lineNum * standardHeight;
//        TextList.TextNode toReturn;
//        TextList.TextNode temp = characterMappings.get(roundedY);
//        toReturn = temp;
//        double min = 1000;
//        while(temp.ch.getY() == roundedY){
//            if(Math.abs(temp.ch.getX()-X) <= min){
//                toReturn = temp;
//                min = Math.abs(temp.ch.getX() - X);
//            }
//            temp = temp.next;
//        }
//        fileList.cursor.next = toReturn;
//        return toReturn;
//    }

    public TextList.TextNode findClosestTextObject(double X, double Y) {
        int lineNum;
        if (Y == 0) {
            lineNum = 0;
        } else {
            lineNum = (int) (Math.ceil(Y / standardHeight)) - 1;
        }
        double roundedY = lineNum * standardHeight;
        boolean found = false;
        TextList.TextNode toReturn;
        TextList.TextNode temp = characterMappings.get(roundedY);
        toReturn = temp;
        while (temp != null) {
            double spanOfChar = temp.ch.getX() + Math.round(temp.ch.getLayoutBounds().getWidth());
            if (spanOfChar > X) {
                found = true;
                if (Math.abs(X - temp.ch.getX()) >= Math.round(temp.ch.getLayoutBounds().getWidth()) / 2) {
                    toReturn = temp.next;
                } else {
                    toReturn = temp;
                }
                break;

            }
            temp = temp.next;
        }
        if (found == false) {
            toReturn = fileNodes[fileNodes.length - 1];
        }
        fileList.cursor.next = toReturn;
        return toReturn;
    }

    public TextList.TextNode findClosestTextObjectWithoutCursor(double X, double Y) {
        int lineNum;
        if (Y == 0) {
            lineNum = 0;
        } else {
            lineNum = (int) (Math.ceil(Y / standardHeight)) - 1;
        }
        double roundedY = lineNum * standardHeight;
        boolean found = false;
        TextList.TextNode toReturn;
        TextList.TextNode temp = characterMappings.get(roundedY);
        toReturn = temp;
        while (temp.next != null) {
            double spanOfChar = temp.ch.getX() + Math.round(temp.ch.getLayoutBounds().getWidth());
            if (spanOfChar > X) {
                found = true;
                if (Math.abs(X - temp.ch.getX()) >= Math.round(temp.ch.getLayoutBounds().getWidth()) / 2) {
                    toReturn = temp.next;
                } else {
                    toReturn = temp;
                }
                break;

            }
            temp = temp.next;
        }
        if (found == false) {
            toReturn = fileNodes[fileNodes.length - 1];
        }
        return toReturn;
    }
    public double[] cursorPosition(double X, double Y) {
        TextList.TextNode closest = findClosestTextObject(X, Y);
        double[] toReturn = new double[2];
        toReturn[0] = closest.prev.ch.getX() + closest.prev.ch.getLayoutBounds().getWidth();
        toReturn[1] = closest.ch.getY();
        return toReturn;
    }

    public double[] cursorPositionWithoutCursor(double X, double Y) {
        TextList.TextNode closest = findClosestTextObjectWithoutCursor(X, Y);
        double[] toReturn = new double[2];
        toReturn[0] = closest.prev.ch.getX() + closest.prev.ch.getLayoutBounds().getWidth();
        toReturn[1] = closest.ch.getY();
        return toReturn;
    }


    public void setWindowWidth(int width){
        this.windowWidth = width;
    }

    public void setWindowHeight(int height){
        this.windowHeight = height;
    }

    public void setFontSize(int size) {
        this.fontSize = size;
    }

    public void wordWrapping(int endIndex){
        fileContents[startOfWord].setX(LEFT_MARGIN);
        fileContents[startOfWord].setY(fileContents[startOfWord].getY() + standardHeight);
        addThisTextNodeToMap(fileNodes[startOfWord], fileContents[startOfWord].getY());
        for(int i  = startOfWord + 1; i <= endIndex; i++){
            fileContents[i].setX(fileContents[i-1].getX() + fileContents[i-1].getLayoutBounds().getWidth());
            fileContents[i].setY(fileContents[i-1].getY());
        }
    }
}

