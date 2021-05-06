import processing.core.PApplet;
import processing.data.Table;
import processing.data.TableRow;
import processing.event.MouseEvent;

public class Main extends PApplet {
	int originX;  // TESTING
	int originY; // TESTING

	float[][] T = new Tinit(3).matrix;;

	Table table3d;
	Table table2d;
	boolean translate = false;
	boolean scale = false;
	float transformX, transformY;
	int countClicks = 0;

	float d = 376;
	float vx = 0.5f, vy = 1f, vz = 3;
	float alpha1, alpha2;

	final static class Tinit {
		float[][] matrix;

		Tinit() {
			this.matrix = new float[3][3];
			for (int i = 0; i < 3; i++)
				this.matrix[i][i] = 1;
		}

		public Tinit(int n) {
			this.matrix = new float[n][n];
			for (int i = 0; i < n; i++)
				this.matrix[i][i] = 1;
		}
	}

	class Axonometric {
		float[][] matrix = new float[2][3];

		public Axonometric(float c1, float c2, float c3, float a1, float a2) {
			this.matrix[0][0] = -c1 * cos(radians(a1));
			this.matrix[0][1] = c2 * cos(radians(a2));
			this.matrix[0][2] = 0;
			this.matrix[1][0] = -c1 * sin(radians(a1));
			this.matrix[1][1] = -c2 * sin(radians(a2));
			this.matrix[1][2] = c3;
		}
	}

	class Vector {
		float x, y, z;

		public Vector(float x, float y, float z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}

	class MinMax {
		float minX, minY, maxX, maxY;

		public MinMax(float minX, float minY, float maxX, float maxY) {
			this.minX = minX;
			this.minY = minY;
			this.maxX = maxX;
			this.maxY = maxY;
		}

		public void find() {
			float x1, y1, x2, y2;
			for (TableRow row : table2d.rows()) {
				x1 = row.getFloat("x1");
				y1 = row.getFloat("y1");
				x2 = row.getFloat("x2");
				y2 = row.getFloat("y2");

				if (x1 < minX) minX = x1;
				if (y1 < minY) minY = y1;
				if (x1 > maxX) maxX = x1;
				if (y1 > maxY) maxY = y1;
			}
		}
	}

	class Pixel {
		float x, y;

		public Pixel(float x, float y) {
			this.x = x;
			this.y = y;
		}
	}

	class BoundingBox {
		float x1, y1, x2, y2;

		public BoundingBox() {
		}

		public void fit() {
			x1 = table2d.getRow(0).getFloat("x1");
			y1 = table2d.getRow(0).getFloat("y1");
			x2 = table2d.getRow(0).getFloat("x2");
			y2 = table2d.getRow(0).getFloat("y2");

			for (TableRow row : table2d.rows()) {
				if (row.getFloat("x1") < x1) x1 = row.getFloat("x1");
				if (row.getFloat("y1") < y1) y1 = row.getFloat("y1");
				if (row.getFloat("x2") > x2) x2 = row.getFloat("x2");
				if (row.getFloat("y2") > y2) y2 = row.getFloat("y2");
			}
		}

		public Pixel center() {
			return new Pixel((x1+x2)/2, (y1+y2)/2);
		}

		@Override
		public String toString() {
			return "BoundingBox{" +
					"x1=" + x1 +
					", y1=" + y1 +
					", x2=" + x2 +
					", y2=" + y2 +
					'}';
		}
	}

	public void setup() {
		size(640, 480);
		originX = 0 / 2;  // TESTING
		originY = 0 / 2; // TESTING

		table3d = new Table();
		table3d.addColumn("x1");
		table3d.addColumn("y1");
		table3d.addColumn("z1");
		table3d.addColumn("x2");
		table3d.addColumn("y2");
		table3d.addColumn("z2");

		table2d = new Table();
		table2d.addColumn("x1");
		table2d.addColumn("y1");
		table2d.addColumn("x2");
		table2d.addColumn("y2");

		try {
			table3d = loadTable("model.csv", "header");
			if (table3d == null) throw new Exception("Nem lehet olvasni a modell-leíró állományt!");
		} catch (Exception e) {
			println(e.getMessage());
			System.exit(1);
		}
	}

	public void draw() {
		background(204);
		stroke(160);
		line(0, originY, width, originY);  // TESTING
		line(originX, 0, originX, height); // TESTING
		stroke(0);

		//d+=0.5;
		//rotate3d(0.5f);
		//centralProjection();
		parallelProjection();
		//axonometricProjection();
		//isometricAxonometricProjection();
		//frontalAxonometricProjection();
		//dimetricAxonometricProjection(1, 1, 1);
	}

	void drawLine(float x1, float y1, float x2, float y2) {
		float m;
		float i, j;

		if (x2 != x1) { // nem függőleges
			m = (y2 - y1) / (x2 - x1);

			if (abs(m) <= 1) {
				j = (x1 < x2) ? y1 : y2;
				for (i = Math.min(x1, x2); i < (Math.max(x1, x2)); i++) {
					point(i, j);
					j += m;
				}
			} else {
				i = (y1 < y2) ? x1 : x2;
				for (j = Math.min(y1, y2); j < (Math.max(y1, y2)); j++) {
					point(i, j);
					i += 1 / m;
				}
			}
		} else {    // függőleges
			for (j = Math.min(y1, y2); j < (Math.max(y1, y2)); j++) {
				point(x1, j);
			}
		}
	}

	void drawProjection() {
		BoundingBox boundingBox = new BoundingBox();
		boundingBox.fit();
		translate(width/2 - boundingBox.center().x, height/2 - boundingBox.center().y);

		for (TableRow row : table2d.rows()) {
			drawLine(row.getFloat("x1"), row.getFloat("y1"), row.getFloat("x2"), row.getFloat("y2"));
		}
	}

	void centralProjection() {
		centralProjection(d);
	}

	void centralProjection(float d) {
		float[][] T = new Tinit(4).matrix;
		T[2][2] = 0;
		T[3][2] = -1 / d;

		float[] p;
		for (TableRow row : table3d.rows()) {
			p = new float[]{0, 0, 0, 1};
			p[0] = row.getFloat("x1")/* - originX*/;
			p[1] = row.getFloat("y1")/* - originY*/;
			p[2] = row.getFloat("z1");
			p = matrixMultiplication(T, p);
			//row.setFloat("x1", p[0] + originX);
			//row.setFloat("y1", p[1] + originY);
			float x1 = p[0] + originX;
			float y1 = p[1] + originY;

			p = new float[]{0, 0, 0, 1};
			p[0] = row.getFloat("x2")/* - originX*/;
			p[1] = row.getFloat("y2")/* - originY*/;
			p[2] = row.getFloat("z2");
			p = matrixMultiplication(T, p);
			//row.setFloat("x2", p[0] + originX);
			//row.setFloat("y2", p[1] + originY);
			float x2 = p[0] + originX;
			float y2 = p[1] + originY;

			drawLine(x1, y1, x2, y2);
		}
	}

	void parallelProjection() {
		parallelProjection(new Vector(vx, vy, vz));
	}

	void parallelProjection(Vector v) {
		float[][] T3d = new Tinit(4).matrix;
		T3d[2][2] = 0;
		T3d[0][2] = -v.x / v.z;
		T3d[1][2] = -v.y / v.z;

		table2d.clearRows();
		float[] p;
		int i = 0;
		for (TableRow row : table3d.rows()) {
			p = new float[]{0, 0, 0, 1};
			table2d.getRow(i);
			//TableRow newRow2d = table2d.addRow();

			p[0] = row.getFloat("x1")/* - originX*/;
			p[1] = row.getFloat("y1")/* - originY*/;
			p[2] = row.getFloat("z1");
			p = matrixMultiplication(T3d, p);
			//row.setFloat("x1", p[0] + originX);
			//row.setFloat("y1", p[1] + originY);
			float x1 = p[0] /*+ originX*/;
			float y1 = p[1] /*+ originY*/;
			table2d.getRow(i).setFloat("x1", x1);
			table2d.getRow(i).setFloat("y1", y1);

			p = new float[]{0, 0, 0, 1};
			p[0] = row.getFloat("x2")/* - originX*/;
			p[1] = row.getFloat("y2")/* - originY*/;
			p[2] = row.getFloat("z2");
			p = matrixMultiplication(T3d, p);
			//row.setFloat("x2", p[0] + originX);
			//row.setFloat("y2", p[1] + originY);
			float x2 = p[0] /*+ originX*/;
			float y2 = p[1] /*+ originY*/;

			table2d.getRow(i).setFloat("x2", x2);
			table2d.getRow(i).setFloat("y2", y2);

			//drawLine(x1, y1, x2, y2);
			i++;
		}
		drawProjection();
	}

	void axonometricProjection() {
		//alpha1 += 1;
		//alpha2 += 0.3;
		axonometricProjection(1, 1, 1, alpha1, alpha2);
	}

	void isometricAxonometricProjection() {
		final float c = 1;
		final float alpha = 30;
		axonometricProjection(c, c, c, alpha, alpha);
	}

	void frontalAxonometricProjection() {
		alpha1 = 30;
		alpha2 = 0;
		final float c1 = 0.5f, c2 = 1, c3 = 1;
		axonometricProjection(c1, c2, c3, alpha1, alpha2);
	}

	void dimetricAxonometricProjection(float c1, float c2, float c3) {
		alpha1 = degrees(atan(7f / 8));
		alpha2 = degrees(atan(1f / 8));
		axonometricProjection(c1, c2, c3, alpha1, alpha2);
	}

	void axonometricProjection(float c1, float c2, float c3, float alpha1, float alpha2) {
		Axonometric axonometric = new Axonometric(c1, c2, c3, alpha1, alpha2);

		float[] p;
		for (TableRow row : table3d.rows()) {
			p = new float[]{0, 0, 0};
			p[0] = row.getFloat("x1")/* - originX*/;
			p[1] = row.getFloat("y1")/* - originY*/;
			p[2] = row.getFloat("z1");
			p = matrixMultiplication(axonometric.matrix, p);
			//row.setFloat("x1", p[0] + originX);
			//row.setFloat("y1", p[1] + originY);
			float x1 = p[0] + originX;
			float y1 = p[1] + originY;

			p = new float[]{0, 0, 0, 1};
			p[0] = row.getFloat("x2")/* - originX*/;
			p[1] = row.getFloat("y2")/* - originY*/;
			p[2] = row.getFloat("z2");
			p = matrixMultiplication(axonometric.matrix, p);
			//row.setFloat("x2", p[0] + originX);
			//row.setFloat("y2", p[1] + originY);
			float x2 = p[0] + originX;
			float y2 = p[1] + originY;

			drawLine(x1, y1, x2, y2);
		}
	}

	float[] matrixMultiplication(float[][] t, float[] p) {
		float[] transformed;
		if (p.length == 4) {
			transformed = new float[]{0, 0, 0, 1};
		} else {
			transformed = new float[]{0, 0, 1};
		}

		for (int i = 0; i < t.length; i++) {
			float sum = 0;
			for (int j = 0; j < t[i].length; j++) {
				sum += t[i][j] * p[j];
			}
			transformed[i] = sum;
		}

		try {
			if (t.length == 4 && transformed[3] != 1) {
				if (transformed[3] == 0) {
					throw new ArithmeticException("Div null!!!");
				}
				for (int i = 0; i < t.length; i++) {
					transformed[i] = transformed[i] / transformed[t.length - 1];
				}
			}
		} catch (ArithmeticException ae) {
			println(ae);
		}
		return transformed;
	}

	void rotate3d(float alpha) {
		float[][] T = new Tinit(4).matrix;
		T[1][1] = cos(radians(alpha));
		T[1][2] = -sin(radians(alpha));
		T[2][1] = sin(radians(alpha));
		T[2][2] = cos(radians(alpha));

		float[] p;
		for (TableRow row : table3d.rows()) {
			p = new float[]{0, 0, 0, 1};
			p[0] = row.getFloat("x1");
			p[1] = row.getFloat("y1");
			p[2] = row.getFloat("z1");
			p = matrixMultiplication(T, p);
			row.setFloat("x1", p[0]);
			row.setFloat("y1", p[1]);
			row.setFloat("z1", p[2]);

			p = new float[]{0, 0, 0, 1};
			p[0] = row.getFloat("x2");
			p[1] = row.getFloat("y2");
			p[2] = row.getFloat("z2");
			p = matrixMultiplication(T, p);
			row.setFloat("x2", p[0]);
			row.setFloat("y2", p[1]);
			row.setFloat("z2", p[2]);
		}
	}

	void transform(float originX, float originY, boolean checkOverflow, String method) {
		float[] p;

		for (TableRow row : table2d.rows()) {
			p = new float[]{0, 0, 1};
			p[0] = row.getFloat("x1")/* - originX*/;
			p[1] = row.getFloat("y1")/* - originY*/;
			p = matrixMultiplication(T, p);
			row.setFloat("x1", p[0]/* + originX*/);
			row.setFloat("y1", p[1]/* + originY*/);

			p = new float[]{0, 0, 1};
			p[0] = row.getFloat("x2")/* - originX*/;
			p[1] = row.getFloat("y2")/* - originY*/;
			p = matrixMultiplication(T, p);
			row.setFloat("x2", p[0]/* + originX*/);
			row.setFloat("y2", p[1]/* + originY*/);
		}
		/*
		for (TableRow row : table2d.rows()) {
			drawLine(row.getFloat("x1"), row.getFloat("y1"), row.getFloat("x2"), row.getFloat("y2"));
		}
		 */

		if (checkOverflow)
			checkOverflow(method);
	}

	void translate(boolean checkOverflow) {
		countClicks++;

		if (countClicks % 2 == 0) {
			transformX = mouseX - transformX;
			transformY = mouseY - transformY;
			countClicks = 0;

			translate(transformX, transformY, checkOverflow);
		} else {
			transformX = mouseX;
			transformY = mouseY;
		}
	}

	public void translate(float transformX, float transformY, boolean checkOverflow) {
		T = new Tinit(3).matrix;
		T[0][2] = transformX;
		T[1][2] = transformY;

		transform(originX, originY, checkOverflow, "translate");
	}

	void scale(boolean checkOverflow) {
		countClicks++;

		if (countClicks % 2 == 0) {
			transformX = mouseX - transformX;
			transformY = mouseY - transformY;
			countClicks = 0;

			// TESTING
			if (transformX > 0) {
				transformX = 2;
				transformY = 2;
			} else {
				transformX = 0.5F;
				transformY = 0.5F;
			}
			// TESTING END

			scale(transformX, transformY, checkOverflow);
		} else {
			transformX = mouseX;
			transformY = mouseY;
		}
	}

	void scale(float transformX, float transformY, boolean checkOverflow) {
		T = new Tinit().matrix;
		T[0][0] = transformX;
		T[1][1] = transformY;

		transform(originX, originY, checkOverflow, "scale");
	}

	public void checkOverflow(String method) {
		float deltaX = 0, deltaY = 0;

		switch (method) {
			case "translate": {
				MinMax mm = new MinMax(originX, originY, width, height);
				mm.find();
				if (originX - mm.minX > 0) deltaX = originX - mm.minX;
				if (width - mm.maxX < 0) deltaX = width - mm.maxX;
				if (originY - mm.minY > 0) deltaY = originY - mm.minY;
				if (height - mm.maxY < 0) deltaY = height - mm.maxY;

				if (deltaX != 0 || deltaY != 0) translate(deltaX, deltaY, false);
				break;
			}
			case "scale": {
				TableRow row0 = table2d.getRow(0);

				MinMax mm = new MinMax(row0.getFloat("x1"), row0.getFloat("y1"), row0.getFloat("x2"), row0.getFloat("y2"));
				mm.find();

				deltaX = 1;
				deltaY = 1;

				if (mm.maxX - mm.minX > width - originX) {
					deltaX = (width - originX) / (mm.maxX - mm.minX);
				}
				if (mm.maxY - mm.minY > height - originY) {
					deltaY = (height - originY) / (mm.maxY - mm.minY);
				}

				scale(deltaX, deltaY, false);
				checkOverflow("translate");
				break;
			}
		}
	}

	public void mousePressed() {
		if (translate || scale) {
			if (translate) {
				translate(false);
			}
			if (scale) {
				scale(true);
			}
		} else {
			println(mouseX, mouseY); // DEBUG
		}
	}

	public void keyPressed() {
		if (table3d.getRowCount() % 2 == 0)   // Megkezdett modell-elem esetén a transzformációk nem kapcsolhatók be
			switch (key) {                  // A három funkció közül egyszerre csak az egyik működjön
				case 'x': {
					rotate3d(1f);
				}
				case 't': {
					translate = !translate;
					scale = false;
					break;
				}
				case 's': {
					scale = !scale;
					translate = false;
					break;
				}
			}
	}


	// TESTING
	public void mouseWheel(MouseEvent event) {
		float e = event.getCount();
		if (e < 0)
			scale(2, 2, true);
		else
			scale(0.5F, 0.5F, true);
	}
	// TESTING END

	public void settings() {
		setup();
	}

	static public void main(String[] passedArgs) {
		PApplet.main("Main");
	}
}