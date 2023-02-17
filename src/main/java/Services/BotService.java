package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class BotService {
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;
    private boolean useTele;
    private int teleHeading;

    public BotService() {
        this.playerAction = new PlayerAction();
        this.gameState = new GameState();
    }


    public GameObject getBot() {
        return this.bot;
    }

    public void setBot(GameObject bot) {
        this.bot = bot;
    }

    public PlayerAction getPlayerAction() {
        return this.playerAction;
    }

    public void setPlayerAction(PlayerAction playerAction) {
        this.playerAction = playerAction;
    }

    public void computeNextPlayerAction(PlayerAction playerAction) {
        playerAction.action = PlayerActions.FORWARD;
        playerAction.heading = new Random().nextInt(360);

        if (!gameState.getGameObjects().isEmpty()) {
            var foodList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.FOOD || item.getGameObjectType() == ObjectTypes.SUPERFOOD);
                // .sorted(Comparator.comparing(item -> UtilityFunctions.getTrueDistance(bot, item)))
                // .collect(Collectors.toList());
            var smallerPlayer = gameState.getPlayerGameObjects()
                    .stream().filter(item -> item.getSize() < bot.getSize());
                    // .sorted(Comparator.comparing(item -> UtilityFunctions.getTrueDistance(bot, item)))
                    // .collect(Collectors.toList());
            var smallerPlayerList = gameState.getPlayerGameObjects()
                    .stream().filter(item -> item.getSize() < bot.getSize())
                    .sorted(Comparator.comparing(item -> UtilityFunctions.getTrueDistance(bot, item)))
                    .collect(Collectors.toList());
            var foods = Stream.concat(foodList, smallerPlayer)
                .sorted(Comparator.comparing(item -> UtilityFunctions.getTrueDistance(bot, item)))
                .collect(Collectors.toList());
            var biggerPlayer = gameState.getPlayerGameObjects()
                    .stream().filter(item -> (item.getSize() >= bot.getSize() && UtilityFunctions.getDistance(bot, item) != 0))
                    .sorted(Comparator.comparing(item -> UtilityFunctions.getTrueDistance(bot, item)))
                    .collect(Collectors.toList());
            var obstacleList = gameState.getGameObjects()
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.GASCLOUD || item.getGameObjectType() == ObjectTypes.WORMHOLE)
                    .sorted(Comparator.comparing(item -> UtilityFunctions.getTrueDistance(bot, item)))
                    .collect(Collectors.toList());
            int avoidEnemy, avoidObstacle;
            // var objectsToAvoid = Stream.concat(biggerPlayer, obstacleList)
            //         .sorted(Comparator.comparing(item -> UtilityFunctions.getTrueDistance(bot, item)))
            //         .collect(Collectors.toList());

            if (UtilityFunctions.nearEdge(bot, gameState)) {
                String botOutput = "Going to center";
                int centerHeading = UtilityFunctions.getHeadingToCenterPoint(bot, gameState);
                int enemiesNear = UtilityFunctions.countEnemyNear(bot, obstacleList, UtilityFunctions.distanceFromEdge(bot, gameState));
                int obstaclesNear = UtilityFunctions.countObstacleNear(bot, obstacleList, UtilityFunctions.distanceFromEdge(bot, gameState));
                playerAction.action = PlayerActions.FORWARD;
                playerAction.heading = centerHeading;
                if (enemiesNear > 0) {
                    botOutput = "Running";
                    avoidEnemy = UtilityFunctions.findResultant(bot, biggerPlayer, enemiesNear);
                    int finalHeading;
                    if (obstaclesNear > 0) {
                        avoidObstacle = UtilityFunctions.findResultant(bot, obstacleList, obstaclesNear);
                        finalHeading = ((avoidEnemy + avoidObstacle) / 2) % 360;
                    } else {
                        finalHeading = avoidEnemy;
                    }
                    finalHeading = ((finalHeading + centerHeading) / 2) % 360;
                    playerAction.heading = finalHeading;
                } else if (obstaclesNear > 0) {
                    botOutput = "Avoiding obstacle";
                    avoidObstacle = UtilityFunctions.findResultant(bot, obstacleList, obstaclesNear);
                    playerAction.heading = ((avoidObstacle + centerHeading) / 2) % 360;
                    if(foods.size()>0 && UtilityFunctions.getTrueDistance(bot, foods.get(0)) < UtilityFunctions.getTrueDistance(bot, obstacleList.get(0)) ){
                        playerAction.heading = getHeadingBetween(foods.get(0));
                    }
                } else if(bot.getTorpedoSalvoCount() > 0 && bot.getSize() > 100){
                    if (biggerPlayer.size() > 0) {
                        int heading = getHeadingBetween(biggerPlayer.get(0));
                        if(heading > centerHeading-60 && heading <centerHeading+60){
                            playerAction.action = PlayerActions.FIRETORPEDOES;
                            playerAction.heading = heading;
                            botOutput = "Firing torpedoes and going to center";
                        }
                        else{
                            playerAction.heading = centerHeading;
                        }
                        
                    } else if (smallerPlayerList.size() > 0) {
                        int heading = getHeadingBetween(smallerPlayerList.get(0));
                        if(heading > centerHeading-60 && heading <centerHeading+60){
                            playerAction.action = PlayerActions.FIRETORPEDOES;
                            playerAction.heading = heading;
                            botOutput = "Firing torpedoes and going to center";
                        }
                        else{
                            playerAction.heading = centerHeading;
                        }
                    }
                }
                System.out.println(botOutput);
            } else if (bot.getTorpedoSalvoCount() > 0 && bot.getSize() > 100) {
                String botOutput = "Firing torpedoes";
                playerAction.action = PlayerActions.FIRETORPEDOES;
                if (biggerPlayer.size() > 0) {
                    playerAction.heading = getHeadingBetween(biggerPlayer.get(0));
                } else if (smallerPlayerList.size() > 0) {
                    playerAction.heading = getHeadingBetween(smallerPlayerList.get(0));
                }
                System.out.println(botOutput);
            } else {
                GameObject target = null;
                String botOutput = "Eating";
                playerAction.action = PlayerActions.FORWARD;
                if (foods.size() != 0) {
                    target = foods.get(0);
                    playerAction.heading = getHeadingBetween(target);
                }
                int obstaclesNear, enemiesNear;
                if (target == null) {
                    obstaclesNear = UtilityFunctions.countObstacleNear(bot, obstacleList, 75);
                    enemiesNear = UtilityFunctions.countEnemyNear(bot, biggerPlayer, 100);
                } else {
                    obstaclesNear = UtilityFunctions.countObstacleNear(bot, obstacleList, UtilityFunctions.getDistance(bot, target));
                    enemiesNear = UtilityFunctions.countEnemyNear(bot, biggerPlayer, UtilityFunctions.getDistance(bot, target) );
                }
                if (enemiesNear > 0) {
                    botOutput = "Running";
                    avoidEnemy = UtilityFunctions.findResultant(bot, biggerPlayer, enemiesNear);
                    int finalHeading;
                    if (obstaclesNear > 0) {
                        avoidObstacle = UtilityFunctions.findResultant(bot, obstacleList, obstaclesNear);
                        finalHeading = ((avoidEnemy + avoidObstacle) / 2) % 360;
                    } else {
                        finalHeading = avoidEnemy;
                    }
                    playerAction.heading = finalHeading;
                    playerAction.action = PlayerActions.FORWARD;
                    
                    // if (bot.getTorpedoSalvoCount() > 0) {
                    //     botOutput = "Firing torpedoes";
                    //     playerAction.action = PlayerActions.FIRETORPEDOES;
                    //     playerAction.heading = getHeadingBetween(biggerPlayer.get(0));
                    // }
                } else if (obstaclesNear > 0) {
                    botOutput = "Avoiding obstacle";
                    avoidObstacle = UtilityFunctions.findResultant(bot, obstacleList, obstaclesNear);
                    playerAction.heading = avoidObstacle;
                    playerAction.action = PlayerActions.FORWARD;
                }
                // else {
                //     obstaclesNear = UtilityFunctions.countObstacleNear(bot, obstacleList, 75);
                //     enemiesNear = UtilityFunctions.countEnemyNear(bot, biggerPlayer, 100);
                // }
                
                // int headingPadding = Math.abs(toDegrees(Math.asin((biggerPlayer.get(0).getSize() + bot.getSize()) / getDistanceBetween(bot, biggerPlayer.get(0))))) + 5;
                System.out.println(botOutput);
                // if (smallerPlayer.size() > 0 && bot.getTeleporterCount() > 0 && !useTele){
                //     playerAction.heading = UtilityFunctions.getHeadingBetween(bot, smallerPlayer.get(smallerPlayer.size()-1));
                //     playerAction.action = PlayerActions.FIRETELEPORT;
                //     useTele = true;
                //     teleHeading = playerAction.heading;
                // } else {
                    // if (useTele) {
                    //     var teleList = UtilityFunctions.getTeleporterList(gameState, bot);
                    //     GameObject myTele = null;
                    //     for (int i = 0; i < teleList.size(); i++){
                    //         if(teleList.get(i).getHeading() == teleHeading){
                    //             myTele = teleList.get(i);
                    //             break;
                    //         }
                    //     }
                    //     for(int i = 0; i < smallerPlayer.size(); i++){
                    //         if(UtilityFunctions.getTrueDistance(smallerPlayer.get(i), myTele) < bot.getSize()){
                    //             playerAction.action = PlayerActions.TELEPORT;
                    //             break;
                    //         }
                    //     }
                    // }
                    // else
                // }
                // if (gasList.size() >0){
                //     this.playerAction.heading = UtilityFunctions.avoidGasCloud(bot, gasList.get(0));
                // }
            }

        //     if (UtilityFunctions.nearEdge(bot, gameState)) {
        //         int enemiesNear = UtilityFunctions.countEnemyNear(bot, objectsToAvoid), resultantHeading, sidingHeading;
        //         sidingHeading = UtilityFunctions.getHeadingToCenterPoint(bot, gameState) % 360;
        //         if (enemiesNear > 0) {
        //             resultantHeading = UtilityFunctions.findResultant(bot, objectsToAvoid, enemiesNear);
        //             resultantHeading = ((resultantHeading + sidingHeading) / 2) % 360;
        //             playerAction.heading = resultantHeading;
        //             if (UtilityFunctions.getTrueDistance(objectsToAvoid.get(0), bot) < 50 && bot.getSize() > 100) {
        //                 playerAction.action = PlayerActions.FORWARD;
        //             } else if (bot.getSize() < 100) {
        //                 playerAction.action = PlayerActions.FORWARD;
        //             } else {
        //                 playerAction.action = PlayerActions.FORWARD;
        //             }
        //         } else {
        //             resultantHeading = sidingHeading % 360;
        //             playerAction.heading = resultantHeading;
        //             playerAction.action = PlayerActions.FORWARD;
        //         }
        //     } else if (objectsToAvoid.size() > 0 && UtilityFunctions.getTrueDistance(objectsToAvoid.get(0), bot) < 100) {
        //         int enemiesNear = UtilityFunctions.countEnemyNear(bot, objectsToAvoid), resultantHeading;
        //         resultantHeading = UtilityFunctions.findResultant(bot, objectsToAvoid, enemiesNear);
        //         playerAction.heading = resultantHeading;
        //         if (UtilityFunctions.getTrueDistance(objectsToAvoid.get(0), bot) < 50 && bot.getSize() > 100) {
        //             playerAction.action = PlayerActions.FORWARD;
        //         } else {
        //             playerAction.action = PlayerActions.FORWARD;
        //         }
        //     } else {
        //         // GameObject target = null;
        //         // int targetDensity = 0;
        //         // for (int i = 0; i < edibleList.size(); i++) {
        //         //     GameObject currentTarget = edibleList.get(i);
        //         //     if (UtilityFunctions.getDensity(currentTarget, edibleList) > targetDensity) {
        //         //         if (UtilityFunctions.isSave(bot, objectsToAvoid, currentTarget)) {
        //         //             targetDensity = UtilityFunctions.getDensity(currentTarget, edibleList) / ((int) UtilityFunctions.getTrueDistance(bot, currentTarget)+1);
        //         //             target = currentTarget;
        //         //         }
        //         //     }
        //         // }
        //         // playerAction.heading = getHeadingBetween(target);
        //         // playerAction.action = PlayerActions.FORWARD;
        //         GameObject target;
        //         if (smallerPlayer.size() > 0) {
        //             target = smallerPlayer.get(0);
        //         } else {
        //             target = foodList.get(0);
        //         }
        //         playerAction.heading = getHeadingBetween(target);
        //         playerAction.action = PlayerActions.FORWARD;
        //     }
        }

        
        

        this.playerAction = playerAction;
    }

    public GameState getGameState() {
        return this.gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        updateSelfState();
    }

    private void updateSelfState() {
        Optional<GameObject> optionalBot = gameState.getPlayerGameObjects().stream().filter(gameObject -> gameObject.id.equals(bot.id)).findAny();
        optionalBot.ifPresent(bot -> this.bot = bot);
    }

    private double getDistanceBetween(GameObject object1, GameObject object2) {
        var triangleX = Math.abs(object1.getPosition().x - object2.getPosition().x);
        var triangleY = Math.abs(object1.getPosition().y - object2.getPosition().y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    private int getHeadingBetween(GameObject otherObject) {
        var direction = toDegrees(Math.atan2(otherObject.getPosition().y - bot.getPosition().y,
                otherObject.getPosition().x - bot.getPosition().x));
        return (direction + 360) % 360;
    }

    private int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }


}
