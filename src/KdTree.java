import java.util.TreeSet;

/** Class: KdTree.java
 *  @author Yury Park
 *  @version 1.0 <p>
 *
 *  This Class - KdTree class.
 *  This is a 2-D tree (a.k.a. K-D tree where K = 2) implementation to represent points on a 2-D plane
 *  to allow for efficient range search (all points that lie in a given query rectangle) and nearest neighbor search
 *  (point nearest to a given query location).
 *
 *  The algorithms for searching and inserting points are similar to those for BSTs, but at the root
 *  we use the x-coordinate (if the point to be inserted has a smaller x-coordinate than the point at the root, go left; otherwise go right);
 *  then at the next level, we use the y-coordinate (if the point to be inserted has a smaller y-coordinate than the point in the node,
 *  go left; otherwise go right); then at the next level the x-coordinate, and so forth.
 *
 *  The prime advantage of a 2d-tree over a BST is that it supports efficient implementation of range search and nearest neighbor search.
 *  Each node corresponds to an axis-aligned rectangle in the unit square, which encloses all of the points in its subtree.
 *  The root corresponds to the unit square; the left and right children of the root corresponds to the two rectangles split
 *  by the x-coordinate of the point at the root; and so forth.
 *
 *  Range search. To find all points contained in a given query rectangle, start at the root and recursively search for points
 *  in both subtrees using the following pruning rule: if the query rectangle does not intersect the rectangle corresponding
 *  to a node, there is no need to explore that node (or its subtrees). A subtree is searched only if it might contain a point
 *  contained in the query rectangle.
 *
 *  Nearest neighbor search. To find a closest point to a given query point, start at the root and recursively search in both
 *  subtrees using the following pruning rule: if the closest point discovered so far is closer than the distance between
 *  the query point and the rectangle corresponding to a node, there is no need to explore that node (or its subtrees).
 *  That is, a node is searched only if it might contain a point that is closer than the best one found so far.
 *  The effectiveness of the pruning rule depends on quickly finding a nearby point. To do this, organize your recursive
 *  method so that when there are two possible subtrees to go down, you always choose the subtree that is on the same
 *  side of the splitting line as the query point as the first subtree to explore—the closest point found while exploring
 *  the first subtree may enable pruning of the second subtree.
 */
public class KdTree {

	private Node root;             // root of BST
	private int size;
	private RectHV CANVAS = new RectHV(0, 0, 1, 1);	//We will assume this canvas size
	private Point2D currentBestPoint;
	private double currentBestDist;

	/**
	 * class: Node
	 *        A private inner (nested) class.
	 */
    private class Node {
    	private Point2D p;
    	private RectHV rect;	//every point has a "rectangular area" it encompasses.
        private boolean hasVerticalLineDivider; //every point has either a vertical or horizontal line divider.
        private Node leftChild, rightChild;  // left and right subtrees of the node

        private Node(Point2D p, RectHV rect, boolean isVertical) {
        	this.p = p;
        	this.rect = rect;
        	this.hasVerticalLineDivider = isVertical;
        	leftChild = null;
        	rightChild = null;
        }
    }

	public KdTree()                               // construct an empty set of points
	{
		root = null;
		size = 0;
	}

	public boolean isEmpty()                      // is the set empty?
	{
		return size == 0;
	}

	public int size()                         // number of points in the set
	{
		return size;
	}

	public void insert(Point2D p)              // add the point to the set (if it is not already in the set)
	{
		checkIfNull(p);
		//invoke helper (recursive) method
		root = insert(root, p, true, CANVAS.xmin(), CANVAS.ymin(), CANVAS.xmax(), CANVAS.ymax());
	}

	private Node insert(Node subroot, Point2D p, boolean isVerticalLine,
						double xmin, double ymin, double xmax, double ymax) {
		if (subroot == null) {	//base case. If subroot is null, then just create a new node with the given point here.
			subroot = new Node(p, new RectHV(xmin, ymin, xmax, ymax), isVerticalLine);
			size++;
			return subroot;
		}
		//end if (subroot == null)

		//another base case. If we have a duplicate point, do nothing and just return the subroot itself
		if (subroot.p.equals(p)) return subroot;

		//Go down to the left or right subchild depending on whether the subroot's line divider is vertical or not,
		//and whether the given point's x or y coordinate is "less than" subroot point's x or y coordinate.
		//(remember, in terms of x-coordinate, "less" means to the left, and vice versa.
		//in terms of y-coordinate, "less" means to the south, and vice versa.
		if (subroot.hasVerticalLineDivider == true){
			if (p.x() < subroot.p.x()) {	//given point's x-coord is "less"
				subroot.leftChild = insert(subroot.leftChild,
										   p,
										   !isVerticalLine,
										   xmin, ymin, subroot.p.x(), ymax);
			}
			else {
				subroot.rightChild = insert(subroot.rightChild,
										    p,
										    !isVerticalLine,
										    subroot.p.x(), ymin, xmax, ymax);
			}
		}
		else {		//else if (subroot.hasVerticalLineDivider == false)
			if (p.y() > subroot.p.y()) {	//given point's y-coord is "less" (despite the > sign) since it lies to the south.
				subroot.leftChild = insert(subroot.leftChild,
										   p,
										   !isVerticalLine,
										   xmin, subroot.p.y(), xmax, ymax);

			}
			else {
				subroot.rightChild = insert(subroot.rightChild,
											p,
											!isVerticalLine,
											xmin, ymin, xmax, subroot.p.y());
			}
		}
		//end if (subroot.hasVerticalLineDivider == true) / else
		return subroot;
	}
	//end private void insert

	public boolean contains(Point2D p)            // does the set contain point p?
	{
		checkIfNull(p);
		return contains(root, p);
	}

	private boolean contains(Node subroot, Point2D p) {
		if (subroot == null) return false;
		if (subroot.p.equals(p)) return true;

		//Choose which subtree (left or right) to go down to continue the search
		if (subroot.hasVerticalLineDivider  && p.x() < subroot.p.x() ||
			!subroot.hasVerticalLineDivider && p.y() > subroot.p.y()) {
			return contains(subroot.leftChild, p);
		} else if (subroot.hasVerticalLineDivider  && p.x() > subroot.p.x() ||
				   !subroot.hasVerticalLineDivider && p.y() < subroot.p.y()) {
			return contains(subroot.rightChild, p);
		} else {	//In this case, subroot has vertical   divider and p.x() == subroot.p.x()
					//or            subroot has horizontal divider and p.y() == subroot.p.y().
					//So we'll need to search both subtrees.
			return contains(subroot.leftChild, p) || contains(subroot.rightChild, p);
		}
	}

	public void draw()                         // draw all points and divider lines to standard draw
	{
		draw(root);
	}

	private void draw(Node subroot) {
		if (subroot == null) {
			return;
		}

		//first draw the point itself
		StdDraw.setPenRadius(.01);	//use fine point tip pen
		StdDraw.setPenColor(StdDraw.BLACK);
		subroot.p.draw();

		//draw the splitting line
		StdDraw.setPenRadius();	//reset pen tip size
		if (subroot.hasVerticalLineDivider) {
			StdDraw.setPenColor(StdDraw.RED);
//			System.out.println("drawing vertical line...");
			StdDraw.line(subroot.p.x(), subroot.rect.ymin(), subroot.p.x(), subroot.rect.ymax());
		} else {
			StdDraw.setPenColor(StdDraw.BLUE);
//			System.out.println("drawing horizontal line...");
			StdDraw.line(subroot.rect.xmin(), subroot.p.y(), subroot.rect.xmax(), subroot.p.y());
		}

		//Finally recursively draw the left and right child
		draw(subroot.leftChild);
		draw(subroot.rightChild);
	}

	public Iterable<Point2D> range(RectHV rect)             // all points that are inside the query rectangle
	{
		this.checkIfNull(rect);
		TreeSet<Point2D> list = new TreeSet<>();
		range(this.root, rect, list);	//invoke helper recursive method to fill up the list
		return list;					//return the list containing 0 or more points.
	}

	private void range(Node subroot, RectHV rect, TreeSet<Point2D> list) {
		if (subroot == null) return; //base case
//		System.out.printf("Now checking %s...\n", subroot.p.toString());
		if (rect.contains(subroot.p)) {		//rect.contains is a built-in method in the RectHV class
			list.add(subroot.p);
		}

//		System.out.printf("Now checking the position of rectangle to the dividing line of %s...\n", subroot.p);
		switch (positionOfRectToDividingLine(rect, subroot)) {	//invoke helper method
		case -1: //rect is either to the left of dividing line (if line is vertical) or is below the dividing line (if horizontal)
//			System.out.printf("Rectangle lies to the left or south of %s. Recursing left child...\n", subroot.p);
			range(subroot.leftChild, rect, list);	//so go down left subtree and recurse.
			break;
		case 0:
//			System.out.printf("Rectangle intersects %s's dividing line! recursing both children...\n", subroot.p);
			range(subroot.leftChild, rect, list);
			range(subroot.rightChild, rect, list);
			break;
		default:
//			System.out.printf("Rectangle lies to the right or north of %s. Recursing right child...\n", subroot.p);
			range(subroot.rightChild, rect, list);
			break;
		}
	}

	/**
	 * Method: positionOfRectToDividingLine
	 * @param rect given rectangle
	 * @param n given node
	 * @return -1 if rect is positioned "less than" the dividing line
	 *            (meaning that rect is positioned to the left of line (if line is vertical), or below line if horizontal),
	 *          1 if "greater than", and
	 *          0 if rect intersects line.
	 */
	private int positionOfRectToDividingLine(RectHV rect, Node n) {
		if (n.hasVerticalLineDivider) {				//if line divider is vertical...
			if (rect.xmax() < n.p.x()) return -1;	//rect is to the left of dividing line
			if (rect.xmin() > n.p.x()) return 1;	//rect is to the right of dividing line
			return 0;								//rect intersects dividing line
		} else {									//if line divider is horizontal....
			if (rect.ymin() > n.p.y()) return -1;	//rect is below dividing line
			if (rect.ymax() < n.p.y()) return 1;	//rect is above dividing line
			return 0;								//rect intersects dividing line
		}
	}

	public Point2D nearest(Point2D givenP)             // a nearest neighbor in the set to point p; null if the set is empty
	{
		checkIfNull(givenP);
		this.currentBestDist = Double.POSITIVE_INFINITY;	//initialize nearest distance found so far
		this.currentBestPoint = null;						//initialize the nearest point to return
		nearest(this.root, givenP);							//run recursive helper method
		return this.currentBestPoint;
	}

	private void nearest(Node subtree, Point2D givenP) {
		if (subtree == null) return;

		//use distanceSquared for greater efficiency (no need to use square roots)
		double currentDist = subtree.p.distanceSquaredTo(givenP);
		if (currentDist < currentBestDist) {
			currentBestDist = currentDist;
			currentBestPoint = subtree.p;
		}

		boolean searchLeftSubtreeFirst = false;
		switch (positionOfGivenPointToDividingLine(givenP, subtree)) {	//helper method
		case -1:
			searchLeftSubtreeFirst = true;
			nearest(subtree.leftChild, givenP);
			break;
		case 1:
			searchLeftSubtreeFirst = false;
			nearest(subtree.rightChild, givenP);
			break;
		}

		//Prune the search tree (might not need to explore both subtrees) if certain conditions are met..
		if (searchLeftSubtreeFirst) {	//If we just finished searching the left subtree..
			//If this condition is met, we don't need to search right subtree
			if (subtree.rightChild != null &&
					this.currentBestDist <= subtree.rightChild.rect.distanceSquaredTo(givenP)) {
				//do nothing
			} else {
				nearest(subtree.rightChild, givenP);
			}
		}
		else {
			//Else, if we just finished searching the right subtree...we don't need to search left subtree
			//if this condition is met.
			if (subtree.leftChild != null &&
					this.currentBestDist <= subtree.leftChild.rect.distanceSquaredTo(givenP)) {}
			else nearest(subtree.leftChild, givenP);
		}
	}

	private int positionOfGivenPointToDividingLine(Point2D givenP, Node n) {
		if (n.hasVerticalLineDivider) {				//if line divider is vertical...
			if (givenP.x() <= n.p.x()) return -1;	//return -1 if given point is to the left of (or lies on) dividing line
			else return 1;							//return  1 if given point is to the right of dividing line
		} else {									//if line divider is horizontal....
			if (givenP.y() >= n.p.y()) return -1;	//return -1 if given point is below (or lies on) dividing line
			else return 1;							//return  1 if given point is above dividing line
		}
	}

	private void checkIfNull(Object o) {
		if (o == null)
			throw new java.lang.NullPointerException("A null argument was passed!");
	}
}