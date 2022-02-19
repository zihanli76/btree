/*
 * CS7280 Special Topics in Database Management
 * Project 1: B-tree implementation.
 *
 * You need to code for the following functions in this program
 *   1. Lookup(int value) -> nodeLookup(int value, int node)
 *   2. Insert(int value) -> nodeInsert(int value, int node)
 *   3. Display(int node)
 *
 */

final class Btree {

  /* Size of Node. */
  private static final int NODESIZE = 2;

  /* Node array, initialized with length = 1. i.e. root node */
  private Node[] nodes = new Node[1];

  /* Number of currently used nodes. */
  private int cntNodes;

  /* Pointer to the root node. */
  private int root;

  /* Number of currently used values. */
  private int cntValues;

  /* Middle value passed to parent. */
  private Integer midValue;

  /*
   * B+ tree Constructor.
   */
  public Btree() {
    cntNodes = 0;
    cntValues = 0;
    root = initNode();

    //nodes[root].children[0] = createLeaf();
  }

  /*********** B tree functions for Public ******************/

  /*
   * Lookup(int value)
   *   - True if the value was found.
   */
  public boolean Lookup(int value) {
    return nodeLookup(value, root);
  }

  /*
   * Insert(int value)
   *    - If -1 is returned, the value is inserted and increase cntValues.
   *    - If -2 is returned, the value already exists.
   */
  public void Insert(int value) {
    //if(nodeInsert(value, root) == -1) cntValues++;
    int res = nodeInsert(value, root);
    System.out.println("return: " + res + "\n");
    if(res == -1) cntValues++;
  }


  /*
   * CntValues()
   *    - Returns the number of used values.
   */
  public int cntValues() {
    return cntValues;
  }

  /*********** B-tree functions for Internal  ******************/

  /*
   * nodeLookup(int value, int pointer)
   *    - True if the value was found in the specified node.
   *
   */
  private boolean nodeLookup(int value, int pointer) {
//    for (int i = 0; i < nodes.length; i++) {
//      System.out.print(nodes[i] + " ");
//    }
//    System.out.println();
    System.out.println();
    System.out.println("finding " + value + " in node " + pointer); // =======================================================
    if (nodes[pointer].children == null) {

      return findVal(value, pointer) != -1;
    } else {
      if (findVal(value, pointer) != -1) return true;
      int child = findChild(value, pointer);
      System.out.println("Child found is " + child); // =======================================================
      return nodeLookup(value, child);
    }
  }

  /*
   * nodeInsert(int value, int pointer)
   *    - -2 if the value already exists in the specified node
   *    - -1 if the value is inserted into the node or
   *            something else if the parent node has to be restructured
   */
  private int nodeInsert(int value, int pointer) {
    Node curr = nodes[pointer];
    System.out.println("pointer: " + pointer + "  val: " + value); // =======================================================
    if (!isLeaf(curr)) { // The node is no leaf node
      System.out.println("no leaf"); // =======================================================
      int valPos = findVal(value, pointer);
      if (valPos != -1)
        return -2;
      int child = findChild(value, pointer);
      int newChild = nodeInsert(value, child);

      if (newChild == -2 || newChild == -1) {
        return pointer;
      } else if (nodes[pointer].size < NODESIZE) { //some space is left
        // insert middle value to the current node
        insertVal(midValue, pointer);

        // insert child pointer to the current node
        insertChild(newChild, pointer);

        System.out.print("updated children for node " + pointer + ": "); // =======================================================
        for (int i = 0; i < nodes[pointer].children.length; i++ )System.out.print(nodes[pointer].children[i]);
        System.out.println();

        return -1;
      } else { //no space is left
        // insert middle value to the current node
        int rightChild = insertValAndSplit(midValue, pointer);

        insertToChildArrayAndSplit(newChild, pointer);

        // see if the current node is the root
        if (pointer == root) {
          // create a new root
          Node newRoot = new Node();
          newRoot.size = 1;
          checkSize();
          root = cntNodes; // reset root pointer
          System.out.println("new root ptr: " + cntNodes);
          nodes[cntNodes++] = newRoot;
          newRoot.values = new int[NODESIZE];
          newRoot.values[0] = midValue;
          newRoot.children = new int[NODESIZE + 1];
          newRoot.children[0] = pointer;
          newRoot.children[1] = rightChild;
        } else {
          return rightChild;
        }

        return -1;
      }
    } else { // The current node is a leaf node
      System.out.println("leaf"); // =======================================================
      int valPos = findVal(value, pointer);
      if (valPos != -1) { // the value is found in the leaf node
        System.out.println("value is found"); // =======================================================
        return -2;
      } else if (nodes[pointer].size < NODESIZE) { // some space is left
        System.out.println("some space is left, insert"); // =======================================================
        insertVal(value, pointer);
        printArray(pointer); // =======================================================
        return -1;
      } else { // no space is left
        System.out.println("split");
        int rightChild = insertValAndSplit(value, pointer);
        if (pointer == root) {
          // create a new root
          Node newRoot = new Node();
          newRoot.size = 1;
          checkSize();
          root = cntNodes; // reset root pointer
          System.out.println("new root ptr: " + cntNodes);
          nodes[cntNodes++] = newRoot;
          newRoot.values = new int[NODESIZE];
          newRoot.values[0] = midValue;
          newRoot.children = new int[NODESIZE + 1];
          newRoot.children[0] = pointer;
          newRoot.children[1] = rightChild;
          return -1;
        } else {
          return rightChild;
        }
      }
    }
  }


  /*********** Functions for accessing node  ******************/

  /*
   * isLeaf(Node node)
   *    - True if the specified node is a leaf node.
   *         (Leaf node -> a missing children)
   */
  boolean isLeaf(Node node) {
    return node.children == null;
  }

  /*
   * initNode(): Initialize a new node and returns the pointer.
   *    - return node pointer
   */
  int initNode() {
    Node node = new Node();
    node.values = new int[NODESIZE];
    // node.children =  new int[NODESIZE + 1];

    checkSize();
    nodes[cntNodes] = node;
    return cntNodes++;
  }

  /*
   * checkSize(): Resizes the node array if necessary.
   */
  private void checkSize() {
    if(cntNodes == nodes.length) {
      Node[] tmp = new Node[cntNodes << 1];
      System.arraycopy(nodes, 0, tmp, 0, cntNodes);
      nodes = tmp;
    }
  }

  private int findVal(int value, int pointer) {
    System.out.print("Node " + pointer + " value: ");
    printArray(pointer);
    if (nodes[pointer].children != null) {
      System.out.print("Node " + pointer + " children: ");
      printChildren(pointer);
    }

    for (int i = 0; i < NODESIZE; i++) {
      if (nodes[pointer].values[i] == value)
        return i;
    }
    return -1;
  }

  private void insertVal(int value, int pointer) {
    int[] newValues = insertToArray(value, pointer);
    System.arraycopy(newValues, 0, nodes[pointer].values, 0, NODESIZE);
    nodes[pointer].size++;
  }

  private int insertValAndSplit(int value, int pointer) {
    int[] left = new int[NODESIZE];
    int[] right = new int[NODESIZE];
    int[] newValues = insertToArray(value, pointer);

    //======
    for (int i = 0; i < newValues.length; i++) {
      System.out.print(newValues[i] + " ");
    }
    System.out.println();
    //======


    int leftsize = (NODESIZE + 1) / 2;
    for (int i = 0; i < leftsize; i++) {
      left[i] = newValues[i];
    }

    // upload the middle value
    midValue = newValues[leftsize];


    for (int i = 0; i < NODESIZE - leftsize; i++) {
      right[i] = newValues[leftsize + i + 1];
    }
    // reset values and size to original node
    nodes[pointer].values = left;
    nodes[pointer].size = leftsize;

    // create a new node for right
    Node newNode = new Node();
    newNode.values = right;
    newNode.size = NODESIZE - leftsize;
    checkSize();
    System.out.println("new node ptr: " + cntNodes);
    nodes[cntNodes] = newNode;
    cntNodes++;
    return cntNodes - 1;
  }

  private int[] insertToArray(int value, int pointer) {
    int[] newValues = new int[NODESIZE + 1];
    int[] values = nodes[pointer].values;
    int idx = 0;
    int i = 0;

    for (; i < nodes[pointer].size; i++) {
      if (values[i] <= value) {
        newValues[idx++] = values[i];
      } else {
        break;
      }
    }

    newValues[idx++] = value;

    while (i < NODESIZE)
      newValues[idx++] = values[i++];

    return newValues;
  }

  private int findChild(int value, int pointer) {
    int i;
    for (i = 0; i < nodes[pointer].size; i++) {
      if (nodes[pointer].values[i] > value) {
        break;
      }
    }

    return nodes[pointer].children[i];
  }

  private void insertChild(int newChild, int pointer) {
    int[] newChildren = insertToChildArray(newChild, pointer, false);
    System.arraycopy(newChildren, 0, nodes[pointer].children, 0, NODESIZE + 1);
  }


  private int[] insertToChildArray(int newChild, int pointer, boolean split) {
    int newChildFirst = nodes[newChild].values[0];
    int i, idx = 0;
    int[] newChildren = new int[NODESIZE + 2];

    //======
    System.out.print("new child first is " + nodes[newChild].values[0] + " old children first is : ");
    for (int j = 0; j < nodes[pointer].children.length; j++) {
      System.out.print(nodes[nodes[pointer].children[j]].values[0] + " ");
    }
    System.out.println();
    //======

    //======
    System.out.print("new child is " + newChild + " old children is : ");
    for (int j = 0; j < nodes[pointer].children.length; j++) {
      System.out.print(nodes[pointer].children[j] + " ");
    }
    System.out.println();
    //======

    System.out.println("inserting child, size is " + nodes[pointer].size); // =========

    int len = split? NODESIZE + 1 : nodes[pointer].size;
    for (i = 0; i < len; i++) { // size is already added 1
      int child = nodes[pointer].children[i];
      int first = nodes[child].values[0];
      if (first < newChildFirst) {
        newChildren[idx++] = nodes[pointer].children[i];
      } else {
        break;
      }
    }

    newChildren[idx++] = newChild;

    while (i <= NODESIZE)
      newChildren[idx++] = nodes[pointer].children[i++];

    //======
    System.out.print("new children is : ");
    for (int j = 0; j < newChildren.length; j++) {
      System.out.print(newChildren[j] + " ");
    }
    System.out.println();
    //======

    return newChildren;
  }

  private void insertToChildArrayAndSplit(int newChild, int pointer) {
    int[] left = new int[NODESIZE + 1];
    int[] right = new int[NODESIZE + 1];
    int[] newChildren = insertToChildArray(newChild, pointer, true);
    int leftsize = (NODESIZE + 2) / 2;
    for (int i = 0; i < leftsize; i++) {
      left[i] = newChildren[i];
    }
    for (int i = 0; i < NODESIZE + 1 - leftsize; i++) {
      right[i] = newChildren[leftsize + i];
    }

//    //======
//    System.out.print("new children for split is : ");
//    for (int i = 0; i < newChildren.length; i++) {
//      System.out.print(newChildren[i] + " ");
//    }
//    System.out.println();
//    //======

    // reset children of original node to left
    nodes[pointer].children = left;

    // reset children of new node to right 
    nodes[cntNodes - 1].children = right;
  }

  private void printArray(int pointer) {
    for (int i = 0; i < nodes[pointer].size; i++) {
      System.out.print(nodes[pointer].values[i] + " ");
    }
    System.out.println();
  }

  private void printChildren(int pointer) {
    for (int i = 0; i < nodes[pointer].children.length; i++) {
      System.out.print(nodes[pointer].children[i] + " ");
    }
    System.out.println();
  }

}

/*
 * Node data structure.
 *   - This is the simplest structure for nodes used in B-tree
 *   - This will be used for both internal and leaf nodes.
 */
final class Node {
  /* Node Values (Leaf Values / Key Values for the children nodes).  */
  int[] values;

  /* Node Array, pointing to the children nodes.
   * This array is not initialized for leaf nodes.
   */
  int[] children;

  /* Number of entries
   * (Rule in B Trees:  d <= size <= 2 * d).
   */
  int size;
}
