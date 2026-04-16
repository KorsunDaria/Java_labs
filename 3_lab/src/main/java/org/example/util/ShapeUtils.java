package org.example.util;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

public class ShapeUtils {

    public static Shape createShape(String type, double x, double y, double w, double h) {
        switch (type.toLowerCase()) {
            case "ellipse":
                return new Ellipse2D.Double(x, y, w, h);
            case "triangle":
                Path2D.Double triangle = new Path2D.Double();
                triangle.moveTo(x + w / 2, y);
                triangle.lineTo(x, y + h);
                triangle.lineTo(x + w, y + h);
                triangle.closePath();
                return triangle;
            default:
                return new Rectangle2D.Double(x, y, w, h);
        }
    }

    public static Shape rotateShape(Shape shape, double angle, double centerX, double centerY) {
        if (angle == 0) return shape;
        AffineTransform tx = AffineTransform.getRotateInstance(angle, centerX, centerY);
        return tx.createTransformedShape(shape);
    }
}