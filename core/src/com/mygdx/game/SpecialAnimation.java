package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class SpecialAnimation{

    protected final Animation<TextureRegion> animation;

    protected final float WIDTH;
    protected final float HEIGHT;


    public SpecialAnimation(String imagePath, String xmlPath){
        animation = getAnimationStages(imagePath, xmlPath);
        WIDTH = animation.getKeyFrames()[0].getRegionWidth();
        HEIGHT = animation.getKeyFrames()[0].getRegionHeight();
    }
    protected Animation<TextureRegion> getAnimationStages(String imagePath, String xmlPath){
        TextureRegion[] animation = null;
        try{
            Texture texture = new Texture(Gdx.files.internal(imagePath));
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringElementContentWhitespace(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(xmlPath));
            NodeList nodeList = document.getElementsByTagName("Frame");
            animation = new TextureRegion[nodeList.getLength()];
            for(int i = 0; i < nodeList.getLength(); i++){
                Node node = nodeList.item(i);
                if(node.getNodeType() == Node.ELEMENT_NODE){
                    Element element = (Element) node;
                    int
                            x = Integer.parseInt(element.getAttribute("x")),
                            width = Integer.parseInt(element.getAttribute("width")),
                            height = Integer.parseInt(element.getAttribute("height"));
                    animation[i] = new TextureRegion(texture, x, 0, width, height);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return new Animation<>(0.03f, animation);
    }

    public TextureRegion getAnimationStage(int i){
        return animation.getKeyFrames()[i];
    }

    public Animation<TextureRegion> getAnimation(){
        return animation;
    }

    public int getLength(){
        return animation.getKeyFrames().length;
    }

    public float getWidth(){
        return WIDTH;
    }

    public float getHeight(){
        return HEIGHT;
    }
}
