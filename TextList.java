package editor;

import javafx.geometry.VPos;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Iterator;

public class TextList implements Iterable<Text> {
    private TextNode frontSentinel;
    private TextNode backSentinel;
    private int size;
    public Cursor cursor;

    public TextList() {
        size = 0;
        frontSentinel = new TextNode(new Text(""), null, null);
        backSentinel = new TextNode(new Text(""), null, null);
        cursor = new Cursor();
    }

    public class Cursor {
        public TextNode next;

        public Cursor() {
            next = backSentinel;
        }
    }

    public class TextNode {
        public Text ch;
        public TextNode next;
        public TextNode prev;

        public TextNode(Text t, TextNode n, TextNode p) {
            ch = t;
            next = n;
            prev = p;
        }
    }

    public void addAtCursor(Text t) {
        size++;
        TextNode toAdd = new TextNode(t, cursor.next, cursor.next.prev);
        cursor.next.prev.next = toAdd;
        cursor.next.prev = toAdd;
    }

    public TextNode backspace() {
        TextNode toDelete = cursor.next.prev;
        cursor.next.prev = cursor.next.prev.prev;
        cursor.next.prev.next = cursor.next;
        size--;
        return toDelete;
    }

    public void addFirst(Text t) {
        size++;
        TextNode toAdd = new TextNode(t, null, frontSentinel);
        cursor.next.prev = toAdd;
        frontSentinel.next = toAdd;
        backSentinel.prev = toAdd;
    }

    public TextNode removeLast() {
        size--;
        TextNode toDelete = cursor.next.prev;
        cursor.next = backSentinel;
        frontSentinel.next = null;
        frontSentinel.prev = null;
        backSentinel.next = null;
        backSentinel.prev = null;
        return toDelete;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public Iterator<Text> iterator() {
        return new CheshireCat();
    }

    private class CheshireCat implements Iterator<Text> {
        private TextNode knowWhichMushroom;

        //private int numChecked;
        public CheshireCat() {
            knowWhichMushroom = frontSentinel.next;
            //numChecked = 0;
        }

        public boolean hasNext() {
            return (knowWhichMushroom != null);
        }

        public Text next() {
            Text currentChar = knowWhichMushroom.ch;
            knowWhichMushroom = knowWhichMushroom.next;
            return currentChar;
        }


    }

    public Text[] makeArray() {
        Text[] toReturn = new Text[size];
        Iterator<Text> iterate = iterator();
        for (int i = 0; i < size; i++) {
            Text temp = iterate.next();
            if (temp != null && temp.getText() != "") {
                toReturn[i] = temp;
            }
        }
        return toReturn;
    }

    public TextNode[] makeArrayofNodes() {
        TextNode[] toReturn = new TextNode[size];
        TextNode temp = frontSentinel.next;
        for (int i = 0; i < size; i++) {
            toReturn[i] = temp;
            if (temp.next != null) {
                temp = temp.next;
            }
        }
        return toReturn;
    }

}