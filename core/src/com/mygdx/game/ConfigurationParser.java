package com.mygdx.game;

import com.mygdx.game.MapObjects.MapSettings;
import org.jdom2.DataConversionException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ConfigurationParser {

    public static MapSettings readConfigurationFromXml(String filename) {
        MapSettings tileMap = new MapSettings();
        SAXBuilder saxBuilder = new SAXBuilder();
        File inputFile = new File(filename);
        try {
            Document document = saxBuilder.build(inputFile);
            Element mapElement = document.getRootElement();
            readMap(tileMap, mapElement);
            List<Element> tiles = mapElement.getChild("items").getChild("list").getChildren();
            for (Element tile : tiles) {
                tileMap.addTile(readTile(tile));
            }
            Element offset = mapElement.getChild("offset");
            Element pointElement = offset.getChild("Point");
            Point point = new Point();
            int x = pointElement.getAttribute("x").getIntValue();
            int y = pointElement.getAttribute("y").getIntValue();
            point.setLocation(x, y);
            tileMap.setOffset(point);

        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
        return tileMap;
    }

    private static void readMap(MapSettings mapSettings, Element classElement) throws DataConversionException {
        mapSettings.setDefaultScale(classElement.getAttribute("defaultScale").getDoubleValue());
        mapSettings.setImage(classElement.getAttribute("image").getValue());
        mapSettings.setMaxScale(classElement.getAttribute("maxScale").getDoubleValue());
        mapSettings.setMinScale(classElement.getAttribute("minScale").getDoubleValue());
        mapSettings.setTileBorderSize(classElement.getAttribute("tileBorderSize").getIntValue());
        mapSettings.setTileHeight(classElement.getAttribute("tileHeight").getIntValue());
        mapSettings.setTileMapHeight(classElement.getAttribute("tileMapHeight").getIntValue());
        mapSettings.setTileMapWidth(classElement.getAttribute("tileMapWidth").getIntValue());
        mapSettings.setTileWidth(classElement.getAttribute("tileWidth").getIntValue());
        mapSettings.setTilesPerAtlasColumn(classElement.getAttribute("tilesPerAtlasColumn").getIntValue());
        mapSettings.setTilesPerAtlasRow(classElement.getAttribute("tilesPerAtlasRow").getIntValue());
    }

    private static TileHolder readTile(Element classElement) throws DataConversionException {
        TileHolder tile = new TileHolder();
        tile.setFlipHorizontal(classElement.getAttribute("flipHorizontal").getBooleanValue());
        tile.setFlipVertical(classElement.getAttribute("flipVertical").getBooleanValue());
        tile.setHeight(classElement.getAttribute("height").getIntValue());
        tile.setWidth(classElement.getAttribute("width").getIntValue());
        tile.setIndex(classElement.getAttribute("index").getIntValue());
        tile.setX(classElement.getAttribute("x").getIntValue());
        tile.setY(classElement.getAttribute("y").getIntValue());
        return tile;
    }


}
