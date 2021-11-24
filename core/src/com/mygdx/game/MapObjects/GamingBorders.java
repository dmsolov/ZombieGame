package com.mygdx.game.MapObjects;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Obstruction;

import java.util.List;

public class GamingBorders {

    protected final List<Vector2> cornerPoints;

    boolean inZone = true;

    public GamingBorders(List<Vector2> cornerPoints){
        this.cornerPoints = cornerPoints;
    }

    public void checkGamingZone(Vector2 location, Vector2 target){

        if(!Obstruction.isPointInPolygon(target, cornerPoints)){
            List<Vector2> list = Obstruction.getIntersectionPoints(location, target, cornerPoints);
            if(!list.isEmpty()){
                //ставит точку пересечения как цель, если цель за пределаси игровой зоны
                Vector2 v = list.get(0);
                target.set(v.x, v.y);
            }
        }
    }

    public boolean isInZone(Vector2 target) {
        return Obstruction.isPointInPolygon(target, cornerPoints);
    }
}
