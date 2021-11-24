package com.mygdx.game;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.MapObjects.Tree;

import java.util.*;

    public class Zombie extends SpecialSprite{

        private static final float HEIGHT_OFFSET = 7f;

        protected final Wave whiteWave;
        protected final ZombieActor zombieActor;

        protected Vector2 currentTargetPoint;
        protected Vector2 target;
        protected Vector2 returnPoint;

        protected Tree treeTarget;

        protected boolean isCreatingPath = false;
        protected boolean hasTimber = false; //"true" если зомби идет с бревном
        protected boolean cutDown = false; //"true" если идет рубить дерево
        protected boolean returnToStart = false; //"true" возвращается в исходную точку(с бревном)
        protected boolean goDown = true; //"true" when mob goes from top to bottom of frame

        protected final List<Obstruction> obstructions;
        protected final List<Vector2> points;
        protected final Deque<AbstractMap.SimpleEntry<Float, Integer>> layers; //layers of mapobjects

        protected int pointIndex = 0; //index of current target point
        protected int layerIndex = 0; //index of current layerIndex

        protected MapLayer layer;

        protected final MapObject mapObject;



        public Zombie(Vector2 location, ZombieActor actor, Wave point, List<Obstruction> obstructions){
            super(location);
            zombieActor = actor;
            this.whiteWave = point;
            setCenter();
            currentTargetPoint = ZombieActor.INITIAL_POINT.cpy();
            target = ZombieActor.INITIAL_POINT.cpy();
            points = new ArrayList<>();
            this.obstructions = obstructions;
            layers = new ArrayDeque<>();
            mapObject = new MapObject();
            mapObject.getProperties().put("actor", zombieActor);
            layer = new MapLayer();
            setCenter();
            setLayerIndex();
        }

        public void update(){

            //запрещает обновление, если путь не создан
            if(isCreatingPath) return;

            //не дает управлять зомби еогда тот возвращается с бревном
            if(returnToStart){
                if(location.dst2(returnPoint) < 100f){
                    returnToStart = false;
                    hasTimber = false;
                }
            }

            if(location.dst2(currentTargetPoint) < 100f){
                // останавливаем моба, если он уже в целевой точке
                if(location.dst2(target) < 100f){
                    points.clear();
                }
                stop();
            } else {
                if(location.y > currentTargetPoint.y){
                    if(location.x <= currentTargetPoint.x){
                        zombieActor.setFlip(true);
                    } else {
                        zombieActor.setFlip(false);
                    }
                    if(hasTimber) zombieActor.setZombieMode(ZombieActor.WALKWOOD_UP);
                    else zombieActor.setZombieMode(ZombieActor.WALK_UP);
                } else {
                    if(location.x <= currentTargetPoint.x){
                        zombieActor.setFlip(true);

                    } else {
                        zombieActor.setFlip(false);
                    }
                    if(hasTimber) zombieActor.setZombieMode(ZombieActor.WALKWOOD_DOWN);
                    else zombieActor.setZombieMode(ZombieActor.WALK_DOWN);
                }
            }

            // меняем слой в зависимости от ближайшего препятствия
            if(!layers.isEmpty()){
                if(goDown){
                    if(location.y >= layers.getFirst().getKey()){
                        layerIndex = layers.getFirst().getValue() + 1;
                        layers.pop();
                    }
                } else {
                    if(location.y <= layers.getFirst().getKey()){
                        layerIndex = layers.getFirst().getValue();
                        layers.pop();
                    }
                }
            }

            super.update(currentTargetPoint);
        }

        private void stop(){
            pointIndex++;
            if(pointIndex < points.size()){
                currentTargetPoint = points.get(pointIndex);
            } else {
                whiteWave.stop();
            }
            //проигрывает анимацию рубки дерева, когда зомби подходит к дереву
            if(cutDown && location.dst2(treeTarget.getCutPosition()) < 100f){
                zombieActor.setFlip(true);
                treeTarget.chopDown(this);
            } else {
                //зомби дошел до точки
                zombieActor.setZombieMode(ZombieActor.STAND);
            }
        }


        public Tree getTreeTarget(){
            return treeTarget;
        }

        public int getZombieMode(){
            return zombieActor.getAnimationMode();
        }

        public int getLayerIndex(){
            return layerIndex;
        }

        /**
         * идет в изначатьную точку с бревном после рубки дерева
         */
        public void pickTree(){
            cutDown = false;
            hasTimber = true;
            returnToStart = true;
            follow(returnPoint);
        }

        /**
         * просчитывает путь к цели
         */
        public void follow(Vector2 target){
            //start creating a path
            isCreatingPath = true;

            if(returnToStart) target = returnPoint;

            pointIndex = 0;
            points.clear();
            layers.clear();

            List<List<Vector2>> paths = new ArrayList<>();
            List<AbstractMap.SimpleEntry<Float, Integer>> layersList = new ArrayList<>();

            //true if mob moves from top to bottom
            goDown = location.y < target.y;

            //get collections of bypass points
            for(Obstruction obstruction : obstructions){
                //if obstruction is between location and target
                //mob need to change the layerIndex when passes near
                if(Math.abs(obstruction.getCenter().y - location.y) +
                        Math.abs(obstruction.getCenter().y - target.y) <=
                        Math.abs(location.y - target.y)){
                    //add location.y of and layer of obstruction in special list (read below about sorting of this list)
                    layersList.add(new AbstractMap.SimpleEntry<>(obstruction.getCenter().y, obstruction.getLayer()));
                }

                //find bypass
                List<Vector2> bypass = obstruction.getBypass(location, target);
                if(bypass != null){
                    paths.add(bypass);
                }

            }

            //sort collections of bypass point from closest to farthest
            //collection sorts by first bypass point
            paths.sort(Comparator.comparingDouble(c -> location.dst2(c.get(0))));

            //sort special list from farthest to closest location.y of obstruction to mob.location.y
            //mob alternately passes all of obstructions in special list
            //and change his layer respectively
            layersList.sort(Comparator.comparingDouble(c -> Math.abs(location.y - c.getKey())));
            layers.addAll(layersList);

            //add points
            for(List<Vector2> path : paths){
                points.addAll(path);
            }

            //add target as final point
            points.add(target);
            this.target = target;

            currentTargetPoint = points.get(pointIndex);
            whiteWave.start(target.x, target.y);

            //path is created
            isCreatingPath = false;
        }

        public void setTreeTarget(Tree tree){
            //запрешает рубить деревье если зомби несет бревно
            if(returnToStart) return;

            returnPoint = getLocation().cpy();
            cutDown = true;
            treeTarget = tree;
        }

        public ZombieActor getActor(){
            return zombieActor;
        }

        /**
         * set mob on the new layer
         */
        public void setLayer(MapLayer layer){
            removeLayer();
            this.layer = layer;
            layer.getObjects().add(mapObject);
        }

        /**
         * set initial layer when mob created
         */
        private void setLayerIndex(){
            //find closest obstruction
            //Obstruction obstruction = obstructions.get(1);
            Obstruction obstruction = Collections.min(obstructions, Comparator.comparingDouble(c->Math.abs(location.y - c.getCenter().y)));
            //if obstruction is located higher than mob
            if(obstruction.getCenter().y <= location.y){
                layerIndex = obstruction.getLayer() + 1;
            } else {
                //if lower
                layerIndex = obstruction.getLayer();
            }
        }

        /**
         * remove mob from the old layer
         */
        private void removeLayer(){
            layer.getObjects().remove(mapObject);
        }

        /**
         * used to correct displaying if mob moving
         */
        private void setCenter(){
            this.centerX = zombieActor.getWidth() / 2;
            this.centerY = zombieActor.getHeight() / 2 + HEIGHT_OFFSET;
        }

        @Override
        protected void display(){
            zombieActor.setPosition(location.x - centerX, location.y - centerY);
        }
    }