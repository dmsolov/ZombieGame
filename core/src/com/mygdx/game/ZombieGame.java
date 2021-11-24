package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Timer;
import com.mygdx.game.MapObjects.Building;
import com.mygdx.game.MapObjects.GamingBorders;
import com.mygdx.game.MapObjects.MapSettings;
import com.mygdx.game.MapObjects.Tree;
import com.mygdx.game.camera.CustomOrthoTiledMapRenderer;
import com.mygdx.game.camera.OrthoCamController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class ZombieGame extends ApplicationAdapter {
	SpriteBatch batch;

	Zombie zombie;
	protected List<Zombie> zombies;

	protected TiledMap map;

	protected OrthogonalTiledMapRenderer renderer;
	protected OrthographicCamera camera;
	protected OrthoCamController cameraController;
	protected Vector3 cameraPosition;

	protected GamingBorders gamingBorders;

	protected List<Tree> trees;
	protected List<Building> buildings;

	protected List<Body> cloths;
	protected List<Had> hats;

	protected List<Obstruction> obstructions;//коллекция объектов генерируемых на карте
	
	@Override
	public void create ()
	{
		createMap();

		setGamingBorders();

		createTrees();
		createBuildings();
		setObstructions();

		createMainZombie();
		createMobZombies();
		createZombiesUpdater();

		InputMultiplexer processor = new InputMultiplexer(cameraController);
		Gdx.input.setInputProcessor(processor);

	}

	/** рисует карту, задает настройки камеры, создает renderer, batch */
	public void createMap(){
		MapSettings mapSettings = ConfigurationParser.readConfigurationFromXml(Path.MAIN_ISLAND_XML);

		int mapHeight = mapSettings.getTileMapHeight();
		int mapWidth = mapSettings.getTileMapWidth();
		int tileHeight = mapSettings.getTileHeight();
		int tileWidth = mapSettings.getTileWidth();

		camera = new OrthographicCamera();
		camera.setToOrtho(true, 900, 700);
		camera.update();

		batch = new SpriteBatch();

		map = new TiledMap();
		MapLayers layers = map.getLayers();
		Texture wholeImage = new Texture(Path.MAIN_ISLAND);

		int columns = mapSettings.getTilesPerAtlasColumn();
		int rows = mapSettings.getTilesPerAtlasRow();
		int xOffset = (int) mapSettings.getOffset().getX();
		int yOffset = (int) mapSettings.getOffset().getY();


		TiledMapTileLayer layer = new TiledMapTileLayer(mapWidth, mapHeight, tileWidth, tileHeight);
		List<TileHolder> tiles = mapSettings.getTiles();
		for(TileHolder tile : tiles){
			int i = (tile.getIndex() - 1) / columns;
			int j = (tile.getIndex() - 1) % rows;

			TextureRegion region = new TextureRegion(wholeImage, j * 104, i * 104, tileWidth + 4, tileHeight + 4);
			TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
			region.flip(tile.isFlipHorizontal(), !tile.isFlipVertical());
			cell.setTile(new StaticTiledMapTile(region));
			layer.setCell((tile.getX() - xOffset) / tileWidth, (tile.getY() - yOffset) / tileHeight, cell);
		}
		layers.add(layer);

		renderer = new CustomOrthoTiledMapRenderer(map);
		cameraPosition = new Vector3(camera.position);
	}

	/**
	 * добавляет объекты из масива(деревья, здания) на карту
	 * задаент непроходимое возвыгение
	 * определяет слои для объектов
	 */
	public void setObstructions(){
		obstructions = new ArrayList<>();
		obstructions.addAll(trees);
		obstructions.addAll(buildings);
		obstructions.sort(Comparator.comparingDouble(o -> o.getCenter().y));

		map.getLayers().add(new MapLayer());

		for(int i = 0; i < obstructions.size(); i++){
			MapLayer mapLayer = new MapLayer();
			Obstruction obstruction = obstructions.get(i);
			mapLayer.getObjects().add((TextureMapObject) obstruction);
			obstruction.setLayer(i+1);
			map.getLayers().add(mapLayer);
		}

		//недоступное возвышение в средине карты
		List<Vector2> points = new ArrayList<>();
		points.add(new Vector2(638, 780));
		points.add(new Vector2(1300, 1105));
		points.add(new Vector2(2375, 585));
		points.add(new Vector2(1700, 80));

		CentralZone elevation = new CentralZone(points);
		obstructions.add(elevation);
	}

	/** устанавливает границы игровой зоны(где могут передвигаться мобы) */
	public void setGamingBorders(){
		List<Vector2> points = new ArrayList<>();
		points.add(new Vector2(150, 1050));
		points.add(new Vector2(1710, 1850));
		points.add(new Vector2(3270, 1060));
		points.add(new Vector2(1710, 260));

		gamingBorders = new GamingBorders(points);

	}

	/** добавляет в массив объектов деревья */
	public void createTrees(){
		trees = new ArrayList<>();
		int i = 0;
		while(i < 3){
			float y = 500 + i * 150;
			float x = (float) (200 + Math.random() * 2500);
			if(gamingBorders.isInZone(new Vector2(x,y))) {
				Tree tree = new Tree(
						Path.TROPIC_PALM,
						Path.TROPIC_PALM_XML,
						Path.TROPIC_PALM_SHADOW,
						Path.TROPIC_PALM_SHADOW_XML,
						Path.TROPIC_PALM_STUMP,
						Path.TROPIC_PALM_STUMP_XML,
						x,
						y);
				trees.add(tree);
				i++;
			}
		}
	}

	/** добавляет в массив объектов башни */
	public void createBuildings(){
		buildings = new ArrayList<>();
		for(int i = 0; i < 3; i++){
			float y = 700 + i * 170;
			float x = (float) (300 + Math.random() * 500);
			if(gamingBorders.isInZone(new Vector2(x,y))) {
				Building building = new Building(
						Path.TOWER,
						Path.TOWER_XML,
						x, y);
				buildings.add(building);
			}

		}
	}

	/**
	 * создает главного зомби, "witeWawe" для него
	 * и cameraController
	 */
	public void createMainZombie() {
		MapLayer whiteWaveLayer = new MapLayer();
		Wave whiteWave = new Wave();
		whiteWaveLayer.getObjects().add(whiteWave);
		whiteWave.setVisible(false);
		map.getLayers().add(whiteWaveLayer);

		createClothes();

		ZombieActor zombieActor = new ZombieActor();
		zombieActor.setCloth(cloths.get(1));
		zombieActor.setHat(hats.get(1));
		zombieActor.setPosition(ZombieActor.INITIAL_POINT.x, ZombieActor.INITIAL_POINT.y);
		zombie = new Zombie(ZombieActor.INITIAL_POINT.cpy(), zombieActor, whiteWave, obstructions);

		((CustomOrthoTiledMapRenderer) renderer).addZombie(zombie);

		cameraController = new OrthoCamController(camera, 2700, 3400, zombie, trees, gamingBorders);
		Gdx.input.setInputProcessor(cameraController);
	}

	public void createMobZombies(){
		zombies = new ArrayList<>();
		Wave wave = new Wave();
		for(int i = 0; i < 5; i++){
			ZombieActor zombieActor = new ZombieActor();
			zombieActor.setPosition(ZombieActor.INITIAL_POINT.x, zombieActor.INITIAL_POINT.y);
			Zombie zombie = new Zombie(zombieActor.INITIAL_POINT.cpy(), zombieActor, wave, obstructions);
			zombies.add(zombie);
			((CustomOrthoTiledMapRenderer)renderer).addZombie(zombie);
		}

		//рандомно управлет зомби-мобами
		Random random = new Random();
		Timer timer = new Timer();
		timer.scheduleTask(new Timer.Task(){
			@Override
			public void run(){
				for(Zombie mob : zombies){
					Vector2 target = new Vector2(random.nextInt(3400), random.nextInt(2700));
					gamingBorders.checkGamingZone(mob.getLocation(), target);
					mob.follow(target);
					for(Tree tree : trees){
						if(tree.contains(target.x, target.y)){
							mob.setTreeTarget(tree);
							target.set(tree.getCutPosition().x, tree.getCutPosition().y);
							mob.follow(target);
							break;
						}
					}
				}
			}
		}, 0, 3f);
		timer.start();
	}

	@Override
	public void render ()
	{
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		renderer.setView(camera);
		renderer.render();
		batch.setProjectionMatrix(camera.combined);
		cameraController.act(Gdx.graphics.getDeltaTime());
		cameraController.draw();
		camera.update();
	}

	public void createClothes(){
		cloths = new ArrayList<>();
		cloths.add(new Body(
				Path.CLOTH_STAND,
				Path.CLOTH_STAND_XML,
				Path.CLOTH_WALK_DOWN,
				Path.CLOTH_WALK_DOWN_XML,
				Path.CLOTH_WALK_UP,
				Path.CLOTH_WALK_UP_XML,
				Path.CLOTH_WALKWOOD_DOWN,
				Path.CLOTH_WALKWOOD_DOWN_XML,
				Path.CLOTH_WALKWOOD_UP,
				Path.CLOTH_WALKWOOD_UP_XML,
				Path.CLOTH_WOODCUT,
				Path.CLOTH_WOODCUT_XML));
		cloths.add(new Body(
				Path.DOUBLE_CLOTH_STAND,
				Path.DOUBLE_CLOTH_STAND_XML,
				Path.DOUBLE_CLOTH_WALK_DOWN,
				Path.DOUBLE_CLOTH_WALK_DOWN_XML,
				Path.DOUBLE_CLOTH_WALK_UP,
				Path.DOUBLE_CLOTH_WALK_UP_XML,
				Path.DOUBLE_CLOTH_WALKWOOD_DOWN,
				Path.DOUBLE_CLOTH_WALKWOOD_DOWN_XML,
				Path.DOUBLE_CLOTH_WALKWOOD_UP,
				Path.DOUBLE_CLOTH_WALKWOOD_UP_XML,
				Path.DOUBLE_CLOTH_WOODCUT,
				Path.DOUBLE_CLOTH_WOODCUT_XML));

		hats = new ArrayList<>();
		hats.add(new Had(
				Path.HAT_STAND,
				Path.HAT_STAND_XML,
				Path.HAT_WALK_DOWN,
				Path.HAT_WALK_DOWN_XML,
				Path.HAT_WALK_UP,
				Path.HAT_WALK_UP_XML,
				Path.HAT_WALKWOOD_DOWN,
				Path.HAT_WALKWOOD_DOWN_XML,
				Path.HAT_WALKWOOD_UP,
				Path.HAT_WALKWOOD_UP_XML,
				Path.HAT_WOODCUT,
				Path.HAT_WOODCUT_XML));
		hats.add(new Had(
				Path.DOUBLE_HAT_STAND,
				Path.DOUBLE_HAT_STAND_XML,
				Path.DOUBLE_HAT_WALK_DOWN,
				Path.DOUBLE_HAT_WALK_DOWN_XML,
				Path.DOUBLE_HAT_WALK_UP,
				Path.DOUBLE_HAT_WALK_UP_XML,
				Path.DOUBLE_HAT_WALKWOOD_DOWN,
				Path.DOUBLE_HAT_WALKWOOD_DOWN_XML,
				Path.DOUBLE_HAT_WALKWOOD_UP,
				Path.DOUBLE_HAT_WALKWOOD_UP_XML,
				Path.DOUBLE_HAT_WOODCUT,
				Path.DOUBLE_HAT_WOODCUT_XML));
	}

	/**
	 * обновляет отображение всех мобов
	 */
	public void createZombiesUpdater(){
		Timer timer = new Timer();
		timer.scheduleTask(new Timer.Task(){
			@Override
			public void run(){
				zombie.update();
				for(Zombie mob : zombies){
					mob.update();
				}
			}
		}, 0, 0.01f);
		timer.start();
	}

	@Override
	public void dispose () {
		batch.dispose();
		map.dispose();
		cameraController.dispose();
	}
}
