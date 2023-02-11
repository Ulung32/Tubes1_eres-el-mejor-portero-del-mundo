package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class UtilityFunctions {
    public boolean anyOpponentInRadius(GameObject bot, GameState gameState) {
        var playerList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.PLAYER && getDistance(bot, item) <= 200).collect(Collectors.toList());
        return playerList.size() != 0;
    }
    public int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }
    public int getAngle(GameObject bot, GameObject target, GameObject bigger){
        var direction1 = toDegrees(Math.atan2(target.getPosition().y - bot.getPosition().y,
                target.getPosition().x - bot.getPosition().x));
        direction1 = (direction1 + 360) % 360;
        var direction2 = toDegrees(Math.atan2(bigger.getPosition().y - bot.getPosition().y,
                bigger.getPosition().x - bot.getPosition().x));
        direction2 = (direction2 + 360) % 360;
        if(direction2 > direction1){
            return direction2 - direction1;
        }
        else{
            return direction1 - direction2;
        }
    }
    public GameObject getTarget(GameObject bot, GameState gameState) {
        var playerList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.PLAYER && getDistance(bot, item) <= 200)
                .sorted(Comparator.comparing(GameObject::getSize))
                .collect(Collectors.toList());
        int index = 0;
        while (playerList.get(index).getSize() < bot.getSize()) {
            index += 1;
        }
        GameObject target = null;
        for(int i = 0; i<index; i++){
            for(int j = index; j < playerList.size(); i++ ){
                if(getAngle(bot, playerList.get(i), playerList.get(j)) > 45){
                    target = playerList.get(i);
                }
            }
        }
        return target;
    }

//    public int eat(GameObject bot, GameState gamestate) {

//    }

    public double getDistance(GameObject bot, GameObject target) {
        var triangleX = Math.abs(bot.getPosition().x - target.getPosition().x);
        var triangleY = Math.abs(bot.getPosition().y - target.getPosition().y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }
}
