final static class Tinit {
        float[][] matrix;

        public Tinit(int n) {
            this.matrix = new float[n][n];
            for (int i = 0; i < n; i++)
                this.matrix[i][i] = 1;
        }
    }

    static class Axonometric {
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

    static class Vector {
        float x, y, z;

        public Vector(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    static class Pixel {
        float x, y;

        public Pixel() {
        }
    }

    class BoundingBox {
        float x1, y1, x2, y2, width, height;
        Pixel center = new Pixel();

        public BoundingBox() {
        }

        public void fit() {
            x1 = table2d.getRow(0).getFloat("x1");
            y1 = table2d.getRow(0).getFloat("y1");
            x2 = table2d.getRow(0).getFloat("x2");
            y2 = table2d.getRow(0).getFloat("y2");

            for (TableRow row : table2d.rows()) {
                x1 = Math.min(x1, row.getFloat("x1"));
                y1 = Math.min(y1, row.getFloat("y1"));
                x2 = Math.max(x2, row.getFloat("x2"));
                y2 = Math.max(y2, row.getFloat("y2"));
            }

            width = x2 - x1;
            height = y2 - y1;
            center.x = (x1 + x2) / 2;
            center.y = (y1 + y2) / 2;
        }

        public void draw() {
            rect(x1, y1, width, height);
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

    enum Method {
        central, centralis,
        parallel, parhuzamos,
        axonometric, axonometrikus,
        isometric, izometrikus,
        frontal, frontalis,
        dimetric, dimetrikus
    }
    Method method;

    boolean recalcProjection = true;
    boolean firstRun = true;

    BoundingBox boundingBox = new BoundingBox();
    Pixel projectionCenter = new Pixel();

    float[][] T = new Tinit(3).matrix;

    Table table3d;
    Table table2d;
    boolean translate = false;
    boolean scale = false;
    float translateX, translateY;
    float scaleX, scaleY;
    int countClicks = 0;

    float d = 376;
    float vx = 0.5f, vy = 1f, vz = 3;
    float alpha1, alpha2;


    public void setup() {
        size(640, 480);

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
            if (table3d == null) throw new Exception("Nem lehet olvasni a modell-le??r?? ??llom??nyt!");
        } catch (Exception e) {
            println(e.getMessage());
            System.exit(1);
        }

        method = Method.frontal;
    }

    public void draw() {
        background(204);
        project(method);
        drawProjection();
    }

    void drawLine(float x1, float y1, float x2, float y2) {
        float m;
        float i, j;

        if (x2 != x1) { // nem f??gg??leges
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
        } else {    // f??gg??leges
            for (j = Math.min(y1, y2); j < (Math.max(y1, y2)); j++) {
                point(x1, j);
            }
        }
    }

    void drawProjection() {
        if (firstRun) {
            BoundingBox boundingBox = new BoundingBox();
            boundingBox.fit();
            projectionCenter.x = width / 2f - boundingBox.center.x;
            projectionCenter.y = height / 2f - boundingBox.center.y;
            translate(projectionCenter.x, projectionCenter.y);
            firstRun = false;
        } else {
            if (countClicks == 0) {
                if (scale) {
                    if (checkOverflow())
                        translate(translateX, translateY);
                    if (checkOverflow())
                        scale2d(scaleX, scaleY);
                }
                if (checkOverflow())
                    translate(translateX, translateY);
            }

            boundingBox.draw();
            for (TableRow row : table2d.rows()) {
                drawLine(row.getFloat("x1"), row.getFloat("y1"), row.getFloat("x2"), row.getFloat("y2"));
            }
        }
    }

    void calculateProjection(float[][] T3d) {
        if (recalcProjection) {
            table2d.clearRows();
            float[] p;
            int i = 0;
            for (TableRow row : table3d.rows()) {
                p = new float[]{0, 0, 0, 1};

                p[0] = row.getFloat("x1");
                p[1] = row.getFloat("y1");
                p[2] = row.getFloat("z1");
                p = matrixMultiplication(T3d, p);
                float x1 = p[0];
                float y1 = p[1];
                table2d.getRow(i).setFloat("x1", x1);
                table2d.getRow(i).setFloat("y1", y1);

                p = new float[]{0, 0, 0, 1};
                p[0] = row.getFloat("x2");
                p[1] = row.getFloat("y2");
                p[2] = row.getFloat("z2");
                p = matrixMultiplication(T3d, p);
                float x2 = p[0];
                float y2 = p[1];
                table2d.getRow(i).setFloat("x2", x2);
                table2d.getRow(i).setFloat("y2", y2);

                i++;
            }

            //checkOverflow();
            translate(projectionCenter.x + translateX, projectionCenter.y + translateY);
            recalcProjection = false;
        }
    }

    void project(Method method) {
        if (method == Method.central || method == Method.centralis) {
            centralProjection();
        }
        if (method == Method.parallel || method == Method.parhuzamos) {
            parallelProjection();
        }
        if (method == Method.axonometric || method == Method.axonometrikus) {
            axonometricProjection();
        }
        if (method == Method.isometric || method == Method.izometrikus) {
            isometricAxonometricProjection();
        }
        if (method == Method.frontal || method == Method.frontalis) {
            frontalAxonometricProjection();
        }
        if (method == Method.dimetric || method == Method.dimetrikus) {
            dimetricAxonometricProjection(1, 1, 1);
        }
    }

    void centralProjection() {
        centralProjection(d);
    }

    void centralProjection(float d) {
        float[][] T3d = new Tinit(4).matrix;
        T3d[2][2] = 0;
        T3d[3][2] = -1 / d;

        calculateProjection(T3d);
    }

    void parallelProjection() {
        parallelProjection(new Vector(vx, vy, vz));
    }

    void parallelProjection(Vector v) {
        float[][] T3d = new Tinit(4).matrix;
        T3d[2][2] = 0;
        T3d[0][2] = -v.x / v.z;
        T3d[1][2] = -v.y / v.z;

        calculateProjection(T3d);
    }

    void axonometricProjection() {
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

        calculateProjection(axonometric.matrix);
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
        recalcProjection = true;
    }

    boolean checkOverflow() {
        boolean overflow = false;

        if (scale) {
            if (boundingBox.width > width || boundingBox.height > height) {
                float ratio = Math.min(width / boundingBox.width, height / boundingBox.height);
                scaleX = ratio;
                scaleY = ratio;
                overflow = true;
            } else {
                if (boundingBox.x1 < 0) {
                    translateX = abs(boundingBox.x1);
                    overflow = true;
                }
                if (boundingBox.y1 < 0) {
                    translateY = abs(boundingBox.y1);
                    overflow = true;
                }
                if (boundingBox.x2 > width) {
                    translateX = -abs(width - boundingBox.x2);
                    overflow = true;
                }
                if (boundingBox.y2 > height) {
                    translateY = -abs(height - boundingBox.y2);
                    overflow = true;
                }
                projectionCenter.x += translateX;
                projectionCenter.y += translateY;
            }
        } else {
            if (boundingBox.x1 + translateX < 0) {
                translateX = -boundingBox.x1;
                overflow = true;
            }
            if (boundingBox.y1 + translateY < 0) {
                translateY = -boundingBox.y1;
                overflow = true;
            }
            if (boundingBox.x2 + translateX > width) {
                translateX = (width - boundingBox.x2);
                overflow = true;
            }
            if (boundingBox.y2 + translateY > height) {
                translateY = (height - boundingBox.y2);
                overflow = true;
            }
            projectionCenter.x += translateX;
            projectionCenter.y += translateY;
        }

        return overflow;
    }

    void transform() {
        float[] p;

        for (TableRow row : table2d.rows()) {
            p = new float[]{0, 0, 1};
            p[0] = row.getFloat("x1");
            p[1] = row.getFloat("y1");
            p = matrixMultiplication(T, p);
            row.setFloat("x1", p[0]);
            row.setFloat("y1", p[1]);

            p = new float[]{0, 0, 1};
            p[0] = row.getFloat("x2");
            p[1] = row.getFloat("y2");
            p = matrixMultiplication(T, p);
            row.setFloat("x2", p[0]);
            row.setFloat("y2", p[1]);
        }

        boundingBox.fit();
        translateX = 0;
        translateY = 0;
    }

    void translate() {
        countClicks++;

        if (countClicks % 2 == 0) {
            translateX = mouseX - translateX;
            translateY = mouseY - translateY;
            countClicks = 0;
            checkOverflow();
            translate(translateX, translateY);
        } else {
            translateX = mouseX;
            translateY = mouseY;
        }
    }

    public void translate(float translateX, float translateY) {
        T = new Tinit(3).matrix;
        T[0][2] = translateX;
        T[1][2] = translateY;

        transform();
    }

    void scale() {
        countClicks++;

        if (countClicks % 2 == 0) {
            translateX = mouseX - translateX;
            translateY = mouseY - translateY;
            countClicks = 0;
            scale2d(translateX, translateY);
        } else {
            translateX = mouseX;
            translateY = mouseY;
        }
    }

    void scale2d(float transformX, float transformY) {
        T = new Tinit(3).matrix;
        T[0][0] = transformX;
        T[1][1] = transformY;

        transform();
    }

    public void mousePressed() {
        if (translate || scale) {
            if (translate) {
                translate();
            }
            if (scale) {
                scale();
            }
        }
    }

    public void keyPressed() {
        if (table3d.getRowCount() % 2 == 0) {
            switch (key) {
                case 'x': {
                    translate = false;
                    scale = false;
                    rotate3d(1f);
                    break;
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
    }
