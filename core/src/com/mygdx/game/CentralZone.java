package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;

import java.util.List;

public class CentralZone implements Obstruction{
    protected final List<Vector2> cornerPoints;


    public CentralZone(List<Vector2> cornerPoints){
        this.cornerPoints = cornerPoints;
    }

    @Override
    public List<Vector2> getCornerPoints(){
        return cornerPoints;
    }

    @Override
    public Vector2 getCenter(){
        return new Vector2(0, 0);
    }

    @Override
    public void setLayer(int layer){

    }

    @Override
    public int getLayer(){
        return 0;
    }
}
