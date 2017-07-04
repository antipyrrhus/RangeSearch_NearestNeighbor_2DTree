import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/** Class: PointSET.java
 *  @author Yury Park
 *  @version 1.0 <p>
 *
 *
 *  This Class - PointSET
 *  Purpose - See Week05 - Programming Assignment 5 - Kd-Trees.pdf under src/ directory.
 */
public class PointSET {
	private TreeSet<Point2D> setOfPoints;
	public         PointSET()                     // construct an empty set of points
	{
		setOfPoints = new TreeSet<>();
	}
	public           boolean isEmpty()                      // is the set empty?
	{
		return setOfPoints.isEmpty();
	}
	public               int size()                         // number of points in the set
	{
		return setOfPoints.size();
	}
	public              void insert(Point2D p)              // add the point to the set (if it is not already in the set)
	{
		checkIfNull(p);
		setOfPoints.add(p);
	}
	public           boolean contains(Point2D p)            // does the set contain point p?
	{
		checkIfNull(p);
		return setOfPoints.contains(p);
	}
	public              void draw()                         // draw all points to standard draw
	{
		StdDraw.setPenRadius(.01);	//use fine point tip pen
		StdDraw.setPenColor(StdDraw.BLACK);
		for (Point2D p : setOfPoints) p.draw();
	}

	public Iterable<Point2D> range(RectHV rect)             // all points that are inside the rectangle
	{
		checkIfNull(rect);
		List<Point2D> pointsInsideRect = new ArrayList<>();

		//brute force search: go thru all points in the set and see if their location lies inside rectangle
		for (Point2D p : setOfPoints) {
			if (p.x() >= rect.xmin() && p.x() <= rect.xmax() &&
				p.y() >= rect.ymin() && p.y() <= rect.ymax()) {
				pointsInsideRect.add(p);
			}
		}
		return pointsInsideRect;
	}

	public           Point2D nearest(Point2D givenP)             // a nearest neighbor in the set to point p; null if the set is empty
	{
		checkIfNull(givenP);
		double nearestSoFar = Double.POSITIVE_INFINITY;
		Point2D nearestP = null;

		//brute force: go thru all points and save whichever one is nearest the given point.
		for (Point2D p : setOfPoints) {
			double distance = p.distanceSquaredTo(givenP);
			if (distance < nearestSoFar) {
				nearestP = p;
				nearestSoFar = distance;
			}
		}
		//end for
		return nearestP;
	}

	private void checkIfNull(Object o) {
		if (o == null)
			throw new java.lang.NullPointerException("A null argument was passed!");
	}

	public static void main(String[] args)                  // unit testing of the methods (optional)
	{

	}
}
