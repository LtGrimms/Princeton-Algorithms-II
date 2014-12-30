import java.awt.Color;


public class SeamCarver {
	private Picture picture;
	private double[][] energy;
	private int[][] parent;
	private static final double MAX_E = 195075.0;
	
	public SeamCarver(Picture picture) {
		this.picture = picture;
		energy = new double[width()][height()];
		parent = new int[width()][height()];
		for (int row = 0; row < height(); row++)
			for (int col = 0; col < width(); col++) {
				energy[col][row] = energy(col, row);
			}
	}
	
	public Picture picture() {
		return picture;
	}
	
	public int width() {
		return picture.width();
	}
	
	public int height() {
		return picture.height();
	}
	
	public double energy(int x, int y) {
		if (x < 0 || x >= width() || y < 0 || y >= height()) 
			throw new IndexOutOfBoundsException("Energy (" + x + "," + y + ") is outside of bounds " + width() + ", " + height() );
		
		if (x == width()-1 || x == 0 || y == height()-1 || y == 0)
			return MAX_E;
		
		double xDiff = diff(picture.get(x-1, y), picture.get(x+1, y));
		double yDiff = diff(picture.get(x, y-1), picture.get(x, y+1));
		return xDiff + yDiff;
	}
	
	public int[] findHorizontalSeam() {
		
		return findSeam(false);
		
//		transpose();
//		int[] seam = findVerticalSeam();
//		transpose();
//		return seam;
	}
	
	public int[] findVerticalSeam() {
		
		return findSeam(true);
		
//		int[] seam = new int[height()];
//		double[] distTo = new double[width()];
//		double[] oldDistTo = new double[width()];
//		
//		for (int y = 0; y < height() - 1; y++) {
//			for (int x = 0; x < width(); x++) {
//				relax(x, y, distTo, oldDistTo);
//			}
//			System.arraycopy(distTo, 0, oldDistTo, 0, width());
//		}
//		
//		double min = distTo[0];
//		int seamIndex = 0;
//		for (int i = 1; i < distTo.length; i++) {
//			if (distTo[i] < min) {
//				min = distTo[i];
//				seamIndex = i;
//			}
//		}
//		
//		seam[seam.length - 1] = seamIndex;
//		for (int row = seam.length - 2; row >= 0; row--) {
//			seam[row] = parent[seam[row+1]][row+1];
//		}
//		
//		return seam;
	}
	
	private int[] findSeam(boolean vertical) {
		int height, width;
		
		if (vertical) {
			height = height();
			width = width();
		} else {
			height = width();
			width = height();
		}
		
		int[] seam = new int[height];
		double[] distTo = new double[width];
		double[] oldDistTo = new double[width];
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				relax(x, y, distTo, oldDistTo, vertical);
				//else 		  relax(y, x, distTo, oldDistTo);  cannot do this since this would break the order
			}
			System.arraycopy(distTo, 0, oldDistTo, 0, width);
		}
		
		double min = distTo[0];
		int seamIndex = 0;
		for (int i = 1; i < distTo.length; i++) {
			if (distTo[i] < min) {
				min = distTo[i];
				seamIndex = i;
			}
		}
		
		seam[seam.length - 1] = seamIndex;
		for (int row = seam.length - 2; row >= 0; row--) {
			if (vertical) seam[row] = parent[seam[row+1]][row+1];
			else 		  seam[row] = parent[row+1][seam[row+1]];
		}
		
		return seam;	
	}
	
	public void removeHorizontalSeam(int[] seam) {

		removeSeam(seam, false);
		
//		transpose();
//		removeVerticalSeam(seam);
//		transpose();
	}
	
	public void removeVerticalSeam(int[] seam) {
		
		removeSeam(seam, true);
		
//		Picture replacement = new Picture(width() - 1, height());
//		double[][] repEnergy = new double[width() - 1][height()];
//		
//		for (int row = 0; row < replacement.height(); row++) {
//			int count = 0;
//			for (int col = 0; col < replacement.width(); col++) {
//				if (col == seam[row]) count++;
//				replacement.set(col, row, picture.get(col+count, row));
//			}
//		}
//		
//		picture = replacement;
//		
//		for (int row = 0; row < height(); row++)
//			for (int col = 0; col < width(); col++) {
//				if (col < seam[row] - 1)
//					repEnergy[col][row] = energy[col][row];
//				else
//					repEnergy[col][row] = energy(col, row);
//			}
//		
//		energy = repEnergy;
//		parent = new int[width()][height()];
	}
	
	private void removeSeam(int[] seam, boolean vertical) {
		int height, width;
		if (vertical) {
			height = height();
			width = width() -1;
		} else {
			height = height() - 1;
			width = width();
		}
		
		
		Picture replacement = new Picture(width, height);
		double[][] repE = new double[width][height];
		
		int count = 0;
		if (vertical) {
			for (int row = 0; row < height; row++) {
				count = 0;
				for (int col = 0; col < width; col++) {
					if (col == seam[row]) count++;
					replacement.set(col, row, picture.get(col+count, row));
				}
			}
		} else {
			for (int col = 0; col < width; col++) {
				count = 0;
				for (int row = 0; row < height; row++) {
					if (row == seam[col]) count++;
					replacement.set(col, row, picture.get(col, row+count));
				}
			}
		}
		
		picture = replacement;
		
		for (int row = 0; row < height; row++) 
			for (int col = 0; col < width; col++) {
				if      (vertical && col > seam[row] - 2)  repE[col][row] = energy(col, row);
				else if (!vertical && row > seam[col] - 2) repE[col][row] = energy(col, row);
				else 									   repE[col][row] = energy[col][row];
				
			}
		
	}
	
	private void relax (int x, int y, double[] distTo, double[] oldDistTo, boolean vertical) {
		int row, col;
		
		if (vertical) {
			col = x;
			row = y;
		} else {
			col = y;
			row = x;
		}
		
		//first row case
		if (y == 0) {
			distTo[x] = MAX_E;
			parent[col][row] = -1;
			return;
		}
		
		
		//left node case
		if (x == 0) {
			double a = oldDistTo[x];
			double b = oldDistTo[x+1];
			double min = Math.min(a, b);
			distTo[x] = min + energy(col,row);
			//since lower energy will always come from nodes inside bounderies it is okay to set parent without conditions
			parent[col][row] = 1;
			return;
		}
		
		//right node case
		if (x == distTo.length - 1) {
			double a = oldDistTo[x];
			double b = oldDistTo[x-1];
			double min = Math.min(a, b);
			distTo[x] = min + energy(col,row);
			parent[col][row] = x - 1;
			return;
		}
		
		double a = oldDistTo[x-1];
		double b = oldDistTo[x];
		double c = oldDistTo[x+1];
		double min = Math.min(a, b);
		min = Math.min(min, c);
		
		distTo[x] = min + energy(col,row);
		if (min == a)
			parent[col][row] = x - 1;
		else if (min == b)
			parent[col][row] = x;
		else
			parent[col][row] = x + 1;
	}
	
	private void transpose() {
		Picture replacement = new Picture(height(), width());
		double[][] repEnergy = new double[height()][width()];
		int[][] repParent = new int[height()][width()];
		
		for (int row = 0; row < height(); row++)
			for (int col = 0; col < width(); col++) {
				replacement.set(row, col, picture.get(col, row));
				repEnergy[row][col] = energy[col][row];
			}
		
		energy = repEnergy;
		picture = replacement;
		parent = repParent;
	}
	
	private double diff(Color a, Color b) {
		double redDif = Math.pow(a.getRed() - b.getRed(), 2);
		double bluDif = Math.pow(a.getBlue() - b.getBlue(), 2);
		double grnDif = Math.pow(a.getGreen() - b.getGreen(), 2);
		return redDif + bluDif + grnDif;
	}
}
