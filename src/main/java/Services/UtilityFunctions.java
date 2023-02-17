package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class UtilityFunctions {

    public static int searchRadius = 200;

    /**
     Fungsi untuk mengkonversi sudut dari radian ke derajat
    */
    public static int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }
    
    /**
    Fungsi untuk mendapatkan jarak asli antara dua objek yaitu jarak yang mempertimbangkan radius objek tersebut.
    */
    public static double getTrueDistance(GameObject object1, GameObject object2) {
        var triangleX = Math.abs(object1.getPosition().x - object2.getPosition().x);
        var triangleY = Math.abs(object1.getPosition().y - object2.getPosition().y);
        return (Math.sqrt(triangleX * triangleX + triangleY * triangleY) - object1.getSize() - object2.getSize());
    }
    
    /**
    Fungsi yang mengembalikan integer berupa selisih heading antar dua objek dari bot.
    */
    public static int getAngle(GameObject bot, GameObject target, GameObject bigger){
        var direction1 = toDegrees(Math.atan2(target.getPosition().y - bot.getPosition().y,
        target.getPosition().x - bot.getPosition().x));
        direction1 = (direction1 + 360) % 360;
        var direction2 = toDegrees(Math.atan2(bigger.getPosition().y - bot.getPosition().y,
        bigger.getPosition().x - bot.getPosition().x));
        direction2 = (direction2 + 360) % 360;
        if (direction2 > direction1) {
            return direction2 - direction1;
        } else {
            return direction1 - direction2;
        }
    }   
    
    /**
    Fungsi yang mengembalikan densitas di sekitar sebuah food dengan radius pencarian 200
    */
    public static int getDensity (GameObject target, List<GameObject> foodList){
        var val = 0;
        for(int i = 0; i<foodList.size(); i++){
            if (getTrueDistance(target, foodList.get(i)) < 200){
                val += foodList.get(i).getSize();
            }
        }
        return val;
    }
    
    /**
    Fungsi yang akan mengembalikan apakah sebuah makanan aman dituju atau tidak
    */
    public static boolean isSave(GameObject bot, List<GameObject> obstacleList, GameObject target) {
        for (int i = 0; i < obstacleList.size(); i++) {
            if (obstacleList.get(i).getGameObjectType() == ObjectTypes.PLAYER) {
                if (getAngle(target, bot, obstacleList.get(i)) < 90 && (getTrueDistance(obstacleList.get(i), target) < getTrueDistance(bot, target))) {
                    return false;
                } else if (getAngle(target, bot, obstacleList.get(i)) > 90) {
                    if((getTrueDistance(bot, target)/2) > getTrueDistance(target, obstacleList.get(i))){
                        return false;
                    }
                }
            } else {
                int saveAngle = toDegrees(Math.asin((obstacleList.get(i).getSize()+bot.getSize())/getDistance(bot, obstacleList.get(i))));
                if(Math.abs(getHeadingBetween(bot, obstacleList.get(i)) - getHeadingBetween(bot, target)) > saveAngle){
                    if(getTrueDistance(bot, target) > getTrueDistance(bot, obstacleList.get(i))){
                        return false;
                    }
                }   
            }
        }
        return true;
    }
    
    /**
    Menghitung semua enemy dalam radius searchRadius dari bot
    */
    public static int countEnemyNear(GameObject bot, List<GameObject> enemies, double searchRadius) {
        int nearEnemyCount = 0;
        for (int i = 0; i < enemies.size(); i++) {
            if (getTrueDistance(enemies.get(i), bot) < searchRadius) {
                nearEnemyCount += 1;
            }
        }
        return nearEnemyCount;
    }
    
    /**
    Fungsi akan mengembalikan jumlah obstacles dalam radius searchRadius
    */
    public static int countObstacleNear(GameObject bot, List<GameObject> obstacles, double searchRadius) {
        int nearEnemyCount = 0;
        for (int i = 0; i < obstacles.size(); i++) {
            if (getTrueDistance(obstacles.get(i), bot) < searchRadius) {
                nearEnemyCount += 1;
            }
        }
        return nearEnemyCount;
    }
    
    /**
    Fungsi akan menghitung jarak bot dari border atau edge
    */
    public static double distanceFromEdge(GameObject bot, GameState gameState) {
        return ((double)(gameState.getWorld().getRadius()) - 100) - distanceFromCenterPoint(bot, gameState);
    }
    
    /**
    Fungsi akan menghitung resulant heading dari semua musuh yang dekat
    */
    public static int findResultant(GameObject bot, List<GameObject> enemies, int enemyCount) {
        if (enemies.size() == 0) {
            return 0;
        }
        int result = (getHeadingBetween(bot, enemies.get(0)) + 180) % 360;
        for (int i = 1; i < enemyCount; i++) {
            result += ((getHeadingBetween(bot, enemies.get(i)) + 180) % 360);
            result /= 2;
            result %= 360;
        }
        return result;
    }
    
    /**
    Fungsi akan mengembalikan boolean yang akan menyatakan apakah kita perlu menghindari sebuah teleporter atau tidak.
    */
    public static boolean avoidTeleporter(GameObject bot, GameObject nearestTeleporter) {
        int saveAngle = toDegrees(Math.asin((bot.getSize())/getDistance(nearestTeleporter, bot)));
        int relativeHeading = getHeadingBetween(nearestTeleporter, bot) - nearestTeleporter.getCurrentHeading();
        if (getTrueDistance(nearestTeleporter, bot) <= 100){
            if (relativeHeading < 0){
                if (relativeHeading > -1*saveAngle){
                    return true;
                }
            } else {
                if (relativeHeading < saveAngle){
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
    Fungsi ini akan mengembalikan boolean yang akan menyatakan apakah sebuah target aman sebagai target teleporter atau tidak.
    */
    public static boolean targetIsSaveToTeleport(GameObject bot, GameObject target, GameState gameState) {
        var biggerPlayerAroundTarget = gameState.getPlayerGameObjects()
        .stream().filter(item -> (item.getSize() >= bot.getSize() && UtilityFunctions.getDistance(bot, item) != 0 && UtilityFunctions.getTrueDistance(target, item) <= bot.getSize()))
            .sorted(Comparator.comparing(item -> UtilityFunctions.getTrueDistance(bot, item)))
            .collect(Collectors.toList());
        return biggerPlayerAroundTarget.size() == 0 && target.getSize() < bot.getSize() - 30 && bot.getSize() > 50;
    }
    
    /**
    Fungsi yang mengembalikan boolean yang menyatakan apakah bot sedang berada dekat edge atau tidak.
    */
    public static boolean nearEdge(GameObject bot, GameState gameState) {
        return (distanceFromCenterPoint(bot, gameState)) > (double)(gameState.getWorld().getRadius() - 30);
    }
    
    /**
    Fungsi akan mengemblikan integer berupa heading dari bot menuju otherObject
    */
    public static int getHeadingBetween(GameObject bot, GameObject otherObject){
        var direction = toDegrees(Math.atan2(otherObject.getPosition().y - bot.getPosition().y,
        otherObject.getPosition().x - bot.getPosition().x));
        return (direction + 360) % 360;
    }
    
    /**
    Fungsi akan mengembalikan relative heading dari bot ke otherObject, hasil positif menandakan searah jarum jam, dan hasil negatif menandakan berlawanan arah jarum jam.
    */
    public static int getRelativeHeading(GameObject bot, GameObject otherObject){
        return getHeadingBetween(bot, otherObject) - bot.getCurrentHeading();
    }
    
    /**
    Fungsi ini akan mengembalikan integer berupa heading bot menuju titik (0, 0)
    */
    public static int getHeadingToCenterPoint(GameObject bot, GameState gameState) {
        var direction = toDegrees(Math.atan2(gameState.getWorld().getCenterPoint().y - bot.getPosition().y,
        gameState.getWorld().getCenterPoint().x - bot.getPosition().x));
        return direction;
    }
    
    /**
    Mengembalikan jarak dari bot ke titik 0, 0
    */
    public static double distanceFromCenterPoint(GameObject object, GameState gameState) {
        int triangleX = Math.abs(object.getPosition().x);
        int triangleY = Math.abs(object.getPosition().y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY) + object.getSize();
    }
    
    /**
    Fungsi akan mengembalikan list berisi player dengan ukuran lebih kecil dari bot
    */
    public static List<GameObject> listSmaller(GameObject bot, GameState gameState){
        var playerList = gameState.getGameObjects()
        .stream().filter(item -> item.getGameObjectType() == ObjectTypes.PLAYER && item.getSize() < bot.getSize())
        .sorted(Comparator.comparing(GameObject::getSize))
        .collect(Collectors.toList());
        return playerList;
    }
    
    /**
    Fungsi akan mengembalikan pemain yang memiliki ukuran lebih kecil dari bot yang paling besar.
    */
    public static GameObject bigestSmaller(GameObject bot, GameState gameState){
        var playerList = listSmaller(bot, gameState);
        return playerList.get(playerList.size() - 1);
    }
    
    /**
    Fungsi akan mengembalikan heading yang aman dari obstacles.
    */
    public static int getSaveHeading(GameObject bot, List<GameObject> obstacles) {
        List<Integer> notSave = new ArrayList<Integer>();
        for (int i = 0; i < obstacles.size(); i++) {
            if (getDistance(obstacles.get(i), bot) < 200) {
                int alpha = (int) Math.ceil(Math.asin(obstacles.get(i).getSize()/getDistance(obstacles.get(i), bot)));
                int heading = getHeadingBetween(bot, obstacles.get(i));
                int bottom = (heading - alpha) % 360;
                int top = (heading + alpha) % 360;
                if (bottom > top) {
                    top += 360;
                    for (int j = bottom; j < top; j++) {
                        notSave.add(j % 360);
                    }
                } else {
                    for (int j = top; j < bottom; j++) {
                        notSave.add(j % 360);
                    }
                }
            }
        }
        int maxSave = notSave.get(0) + 360 - notSave.get(notSave.size() - 1), index = notSave.size() - 1;
        for (int i = 0; i < notSave.size() - 1; i++) {
            int temp = notSave.get(i + 1) - notSave.get(i);
            if (temp >= maxSave) {
                maxSave = temp;
                index = i;
            }
        }
        int saveHeading;
        if (index == notSave.size() - 1) {
            saveHeading = (notSave.get(0) + 360 + notSave.get(notSave.size() - 1)) / 2;
        } else {
            saveHeading = (notSave.get(index + 1) + notSave.get(index)) / 2;
        }
        return saveHeading;
    }
    
    /**
    Fungsi akan mengembalikan heading yang aman dari obstacles.
    */
    public static List<GameObject> getTeleporterList(GameState gameState, GameObject bot) {
        var teleList = gameState.getGameObjects()
        .stream().filter(item -> item.getGameObjectType() == ObjectTypes.TELEPORTER)
        .sorted(Comparator.comparing(item -> getDistance(bot, item)))
        .collect(Collectors.toList());
        return teleList;
    }
    
    /**
    Fungsi akan mengembalikan jarak dari bot ke target dari pusat objek
    */
    public static double getDistance(GameObject bot, GameObject target) {
        var triangleX = Math.abs(bot.getPosition().x - target.getPosition().x);
        var triangleY = Math.abs(bot.getPosition().y - target.getPosition().y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }
    
    /**
    Fungsi akan mengembalikan jarak object ke titik (0, 0)
    */
    public static double getDistanceFromCenterPoint(GameObject object, GameState gameState) {
        var triangleX = Math.abs(object.getPosition().x - gameState.getWorld().getCenterPoint().x);
        var triangleY = Math.abs(object.getPosition().y - gameState.getWorld().getCenterPoint().y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY) + object.getSize() + 2;
    }
    
    /**
    Fungsi akan mengembalikan jarak object ke titik (0, 0)
    */
    public static boolean outOfBounds(GameObject object, GameState gameState) {
        if (getDistanceFromCenterPoint(object, gameState) > (double)gameState.getWorld().getRadius()) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
    Fungsi akan mengembalikan apakah objek dengan UUID objectID akan outOfBounds
    */
    public static boolean outOfBoundsWithId(UUID objectID, GameState gameState) {
        var Teleporter = gameState.getGameObjects()
        .stream().filter(item -> item.getGameObjectType() == ObjectTypes.TELEPORTER && item.getId() == objectID)
        .collect(Collectors.toList());
        GameObject myTeleporter = Teleporter.get(0);
        return outOfBounds(myTeleporter, gameState);
    }
    
    /**
    Fungsi akan mengembalikan heading untuk menghindari gas cloud
    */
    public static int avoidGasCloud(GameObject bot, GameObject gas, int prevHeading){
        int saveAngle = toDegrees(Math.asin((gas.getSize()+bot.getSize())/getDistance(bot, gas)));
        int relativeHeading = getHeadingBetween(bot, gas) - prevHeading;
        if (getTrueDistance(bot, gas) <= 70){
            if (relativeHeading < 0){
                if (relativeHeading > -1*saveAngle){
                    return getHeadingBetween(bot, gas)+saveAngle;
                }
            } else {
                if (relativeHeading < saveAngle){
                    return getHeadingBetween(bot, gas)-saveAngle;
                }
            }
        }
        return prevHeading;
    }
    
    /**
    Fungsi akan mengembalikan boolean apakah diperlukan aktivasi shield atau tidak.
    */
    public static boolean activateShield(GameObject bot, GameObject torpedo){
        int saveAngle = toDegrees(Math.asin((bot.getSize()+torpedo.getSize())/getDistance(torpedo, bot)));
        int relativeHeading = getHeadingBetween(torpedo, bot) - torpedo.getCurrentHeading();
        if (getTrueDistance(torpedo, bot) <=100){
            if (relativeHeading < 0){
                if (relativeHeading > -1*saveAngle){
                    return true;
                }
            } else {
                if (relativeHeading < saveAngle){
                    return true;
                }
            }
        }
        return false;
    }    
    
    /**
    Fungsi akan mengembalikan boolean apakah kita perlu menyalakan afterburner atau tidak.
    */
    public static boolean activateAfterBurner(GameObject bot, GameObject torpedo){
        int saveAngle = toDegrees(Math.asin((bot.getSize()+torpedo.getSize())/getDistance(torpedo, bot)));
        int relativeHeading = getHeadingBetween(torpedo, bot) - torpedo.getCurrentHeading();
        if (getTrueDistance(torpedo, bot) <= 250 && getTrueDistance(torpedo, bot) > 100){
            if (relativeHeading < 0){
                if (relativeHeading < 0){
                    if (relativeHeading > -1*saveAngle){
                        return true;
                    }
                } else {
                    if (relativeHeading < saveAngle){
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
