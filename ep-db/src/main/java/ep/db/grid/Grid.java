/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ep.db.grid;

import java.util.ArrayList;
import java.util.List;

import ep.db.quadtree.Bounds;
import ep.db.quadtree.Vec2;

/**
 *
 * @author Márcio Peres
 */
public class Grid {

    private float data[];
    private Vec2 points[];
    private int width;
    private int height;

    public Grid(int width, int height) {
        this.width = width;
        this.height = height;
        data = new float[width * height];
        points = new Vec2[width * height];
    }

    public float[] getData() {
        return data;
    }

    public Vec2[] getPoints() {
        return points;
    }
    
    public void printData(){
        System.out.println("\nData:");
        for (int i = 0; i < data.length; i++) {
            if ( i % width == 0)
            	System.out.println();
            String d = String.format("%.4f", data[i]).replace(',', '.');
            System.out.printf("%s, ", d);                
        }
    }

    public void evaluate(List<Vec2> values, Bounds bounds, Kernel k) {

        //Para o calculo dos pontos será considerado os tamanhos em X e em Y
        float bandwidthX = bounds.size().x / (width - 1);
        float bandwidthY = bounds.size().y / (height - 1);

        List<Vec2> valuesGrid[][] = new ArrayList[height - 1][width - 1];
        for (int i = 0; i < valuesGrid.length; i++) {
            for (int j = 0; j < valuesGrid[i].length; j++) {
                valuesGrid[i][j] = new ArrayList<>();
            }            
        }
        
        //Canto superior esquerdo
        Vec2 p0 = new Vec2(bounds.getP1().x, bounds.getP2().y);
        for (Vec2 value : values) {
            int j = (int) ((value.x - p0.x) / bandwidthX);
            int i = (int) ((p0.y - value.y) / bandwidthY); //invert for positives results
            if (i == height-1) --i;
            if (j == width-1) --j;
            valuesGrid[i][j].add(value);
        }

        Vec2 p = new Vec2(p0);
        for (int i = 0; i < height-1; i++) {
            for (int j = 0; j < width-1; j++) {
                points[i*width + j] = new Vec2(p);
                data[i*width + j] = evaluatePointListGrid(valuesGrid, i, j, 20, k, p);
                p.x += bandwidthX;
            }
            p.y -= bandwidthY;
            p.x = p0.x;
        }
    }

    private float evaluatePointListGrid(List<Vec2> valuesGrid[][], int i, int j, float bandwidth, Kernel k, Vec2 p) {

        //Select values from list "valuesCell", adding in list "values" those at least bandwidth from "p"
        //Their distances are added in list "distantes"
        List<Float> distances = new ArrayList<>();
        if (i > 0 && j > 0) { //top left cell
            selectPointListCell(valuesGrid[i - 1][j - 1], bandwidth, p, distances);
        }
        if (i < (width-1) && j > 0) { //botton left cell
            selectPointListCell(valuesGrid[i][j - 1], bandwidth, p, distances);
        }
        if (i > 0 && j < (height-1)) { //top right cell
            selectPointListCell(valuesGrid[i - 1][j], bandwidth, p, distances);
        }
        if (i < (width-1) && j < (height-1)) { //botton right cell
            selectPointListCell(valuesGrid[i][j], bandwidth, p, distances);
        }

        if(distances.isEmpty())
            return 0;
        
        //compute the value at point p, use the pré-computed distances
        float valueAtP = 0;
        for (float distance : distances) {
            valueAtP += k.eval(distance / bandwidth);
        }
        return valueAtP;// / (distances.size() * bandwidth);
    }

    //Select values from list "valuesCell", adding in list "values" those at least bandwidth from "p"
    private void selectPointListCell(List<Vec2> valuesCell, float bandwidth, Vec2 p, List<Float> distances) {
        for (Vec2 value : valuesCell) {
            float distance = (float) Vec2.distance(p, value);
            if (distance < bandwidth) {
                distances.add(distance);
            }
        }
    }
}
