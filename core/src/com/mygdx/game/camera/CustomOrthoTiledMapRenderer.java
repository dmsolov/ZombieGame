package com.mygdx.game.camera;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.mygdx.game.Zombie;
import com.mygdx.game.ZombieActor;

import java.util.ArrayList;
import java.util.List;

public class CustomOrthoTiledMapRenderer extends OrthogonalTiledMapRenderer {

    private final List<Zombie> zombies;

    public CustomOrthoTiledMapRenderer(TiledMap map){
        super(map);
        //list of all zombies on map
        zombies = new ArrayList<>();
    }

    //добавляет зомбей в лист
    public void addZombie(Zombie sprite){
        zombies.add(sprite);
    }

    @Override
    public void render(){
        beginRender();
        for(MapLayer layer : map.getLayers()){
            if(layer.isVisible()){
                if(layer instanceof TiledMapTileLayer){
                    renderTileLayer((TiledMapTileLayer) layer);
                }
                //отображает объекты на карте
                for(MapObject object : layer.getObjects()){
                    renderObject(object);
                }
            }
        }
        //отображает зомби на нужном слое
        for(Zombie zombie : zombies){
            zombie.setLayer(map.getLayers().get(zombie.getLayerIndex()));
        }
        endRender();

    }

    @Override
    public void renderObject(MapObject object){
        Object actor = object.getProperties().get("actor");
        if(actor instanceof ZombieActor){
            ZombieActor zombie = (ZombieActor) actor;
            //изменяет сцену анимации
            zombie.act(Gdx.graphics.getDeltaTime());
            zombie.draw(batch, 0);
        } else if(object instanceof TextureMapObject){
            TextureMapObject mapObject = (TextureMapObject) object;
            if(mapObject.isVisible()){
                //рисует объекты окружения
                batch.draw(mapObject.getTextureRegion(), mapObject.getX(), mapObject.getY());
            }
        }

    }
}
