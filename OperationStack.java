package editor;

/**
 * Created by We on 05-03-2016.
 */
public class OperationStack {
    public myStack undoStack;
    public myStack redoStack;
    public boolean canRedo;
    public OperationStack() {
        undoStack = new myStack();
        redoStack = new myStack();
        canRedo = false;
    }

    public StackNode undo(){
        StackNode temp = undoStack.pop();
        redoStack.push(temp);
        return temp;
    }

    public StackNode redo(){
        StackNode temp = redoStack.pop();
        undoStack.push(temp);
        return temp;
    }


    public class StackNode {
        public double[] cursorPos;
        public String character;
        public String type;

        public StackNode() {
            this.character = "";
            this.type = "sentinel";
        }

        public StackNode(String ch, String t, double[] cursorPos) {
            this.character = ch;
            this.type = t;
            this.cursorPos = cursorPos;
        }


    }

    public class myStack {
        private int head;
        private int bottom;
        private int size;
        private StackNode[] items;

        public myStack() {
            head = -1;
            bottom = -1;
            size = 0;
            items = new StackNode[100];
        }

        private int modulo(int a, int b) {
            // Returns a modulo b, but if remainder is negative, adjusts for this fact//
            int output = a % b;
            if (output < 0) {
                return output + b;
            } else {
                return output;
            }
        }

        public void push(String c, String t, double[] temp) {
            StackNode i = new StackNode(c, t, temp);
            if(isEmpty()) {
                head = 0;
                bottom = 0;
                items[0] = i;
                size++;
            }
            else {
                if(isFull()) {
                    popThirdBardo();
                }
                int newindex = modulo(head- 1, items.length);
                items[newindex] = i;
                head = newindex;
                size++;
            }
        }

        public void push(StackNode i){
            if(isEmpty()) {
                head = 0;
                bottom = 0;
                items[0] = i;
                size++;
            }
            else {
                if(isFull()) {
                    popThirdBardo();
                }
                int newindex = modulo(head- 1, items.length);
                items[newindex] = i;
                head = newindex;
                size++;
            }
        }

        public boolean isFull() {
            return (size == 100);
        }

        public boolean isEmpty() {
            return size == 0;
        }

        public StackNode pop() {
            if (isEmpty()) {
                return null;
            } else {
                StackNode removed = items[head];
                items[head] = null;
                head = modulo(head + 1, items.length);
                size--;
                if (size == 0) {
                    head = -1;
                    bottom = -1;
                }
                return removed;

            }
        }


        public StackNode popThirdBardo() {
            if (isEmpty()) {
                return null;
            } else {
                StackNode removed = items[bottom];
                items[bottom] = null;
                bottom = modulo(bottom - 1, items.length);
                size--;
                if (size == 0) {
                    head = -1;
                    bottom = -1;
                }
                return removed;
            }
        }
    }
}
