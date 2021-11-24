package com.mygdx.game.camera;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mygdx.game.MapObjects.GamingBorders;
import com.mygdx.game.MapObjects.Tree;
import com.mygdx.game.Zombie;

import java.util.List;

/**
 * позваляет управлять картой и зомби при помощи мыши
 */

public class OrthoCamController extends Stage {

    private final OrthographicCamera camera;

    private final Vector3 curr = new Vector3();
    private final Vector3 last = new Vector3(-1, -1, -1);

    private final int mapHeight;
    private final int mapWidth;

    private final Zombie zombie;

    private final List<Tree> trees;

    private final GamingBorders gamingBorders;

    public OrthoCamController (OrthographicCamera camera, int mapHeight, int mapWidth, Zombie zombie, List<Tree> trees, GamingBorders gamingBorders) {
        this.camera = camera;
        this.mapHeight = mapHeight;
        this.mapWidth = mapWidth;
        this.zombie = zombie;
        this.trees = trees;
        this.gamingBorders = gamingBorders;
    }

    @Override
    public boolean touchDragged (int x, int y, int pointer) {
        Vector3 delta = new Vector3();
        camera.unproject(curr.set(x, y, 0));

        if (!(last.x == -1 && last.y == -1 && last.z == -1) && Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {//drag map with left button
            camera.unproject(delta.set(last.x, last.y, 0));
            delta.sub(curr);

            float viewSizeY = camera.viewportHeight/2.0f;
            float viewSizeX = camera.viewportWidth/2.0f;
            float camX = camera.position.x + delta.x;
            float camY = camera.position.y + delta.y;
            boolean availableX = camX < mapWidth - viewSizeX && camX > viewSizeX;
            boolean availableY = camY < mapHeight - viewSizeY*3.0f  && camY > viewSizeY;
            if(!availableX){
                delta.x = 0;
            }
            if(!availableY){
                delta.y = 0;
            }

            camera.position.add(delta.x, delta.y, 0);
        }

        last.set(x, y, 0);
        return false;
    }

    @Override
    public boolean touchUp (int x, int y, int pointer, int button) {
        last.set(-1, -1, -1);

        //ставит цель на карте
        Vector3 clickCoordinates = new Vector3(x, y, 0); //get coordinates
        Vector3 position = camera.unproject(clickCoordinates);
        Vector2 target = new Vector2(position.x, position.y); //set target location
        gamingBorders.checkGamingZone(zombie.getLocation(), target); //проверяет в игровой зоне ли цель
        if(button == Input.Buttons.RIGHT){
            zombie.follow(target); //just follow
        }
        if(button == Input.Buttons.LEFT){ //зомби идет рубить дерево
            for(Tree tree : trees){
                if(tree.contains(target.x, target.y)){
                    zombie.setTreeTarget(tree);
                    target.set(tree.getCutPosition().x, tree.getCutPosition().y);
                    zombie.follow(target);
                    break;
                }
            }
        }
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        if (amount < 0) {
            camera.zoom -= 0.25f;
            if (camera.zoom < 0.25) {
                camera.zoom = 0.25f;
            }
        } else {
            camera.zoom += 0.25;
            if (camera.zoom > 2) {
                camera.zoom = 2;
            }
        }

        return false;
    }
}