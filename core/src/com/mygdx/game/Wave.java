package com.mygdx.game;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.utils.Timer;

public class Wave extends TextureMapObject{

    protected final SpecialAnimation point;

    protected float
            posX,
            posY;
    protected final float
            WIDTH,
            HEIGHT;
    protected int stage;


    public Wave(){
        point = new SpecialAnimation(Path.WHITE_WAVE, Path.WHITE_WAVE_XML);
        WIDTH = point.getWidth();
        HEIGHT = point.getHeight();
        stage = 0;
        Timer timer = new Timer();
        timer.scheduleTask(new Timer.Task(){
            @Override
            public void run(){
                stage++;
                if(stage >= point.getLength()){
                    stage = 0;
                }
            }
        }, 0, 0.1f);
        timer.start();
        setVisible(false);
    }

    @Override
    public TextureRegion getTextureRegion(){
        return point.getAnimationStage(stage);
    }

    public void start(float x, float y){
        posX = x - WIDTH/2;
        posY = y - HEIGHT/2;
        setX(posX);
        setY(posY);
        setVisible(true);
    }

    public void stop(){
        setVisible(false);
    }
}
