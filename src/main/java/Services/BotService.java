package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class BotService {
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;
    private GameObject teleporter;
    private boolean fireTeleporter = false;
    private boolean teleporterLocked = false;
    private boolean supernovaFired = false;

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
        String botOutput = "Random Heading";
        playerAction.action = PlayerActions.FORWARD;
        playerAction.heading = new Random().nextInt(360);
        

        if (!gameState.getGameObjects().isEmpty()) {
            var foodList = gameState.getGameObjects()
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.FOOD || item.getGameObjectType() == ObjectTypes.SUPERFOOD || item.getGameObjectType() == ObjectTypes.SUPERNOVAPICKUP);
            var smallerPlayer = gameState.getPlayerGameObjects()
                    .stream().filter(item -> item.getSize() < bot.getSize());
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
            var torpedoList = gameState.getGameObjects()
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.TORPEDOSALVO)
                    .sorted(Comparator.comparing(item -> UtilityFunctions.getTrueDistance(bot, item)))
                    .collect(Collectors.toList());
            var teleporterList = gameState.getGameObjects()
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.TELEPORTER)
                    .sorted(Comparator.comparing(item -> UtilityFunctions.getTrueDistance(bot, item)))
                    .collect(Collectors.toList());
            int avoidEnemy;
            int tempHeading = 0;
            if (UtilityFunctions.nearEdge(bot, gameState)) {
                botOutput = "Going to center";
                int centerHeading = UtilityFunctions.getHeadingToCenterPoint(bot, gameState);
                int enemiesNear = UtilityFunctions.countEnemyNear(bot, obstacleList, UtilityFunctions.distanceFromEdge(bot, gameState));
                int obstaclesNear = UtilityFunctions.countObstacleNear(bot, obstacleList, UtilityFunctions.distanceFromEdge(bot, gameState));
                playerAction.action = PlayerActions.FORWARD;
                playerAction.heading = centerHeading;
                if (enemiesNear > 0) {
                    avoidEnemy = UtilityFunctions.findResultant(bot, biggerPlayer, enemiesNear);
                    int finalHeading;
                    finalHeading = ((avoidEnemy + centerHeading) / 2) % 360;
                    if (obstaclesNear > 0) {
                        finalHeading = UtilityFunctions.avoidGasCloud(bot, obstacleList.get(0), finalHeading);
                    }
                    playerAction.heading = finalHeading;
                } else if (obstaclesNear > 0) {
                    botOutput = "Avoiding gasCloud";
                    int foodHeading = bot.getCurrentHeading();
                    if(foods.size() > 0){
                        if (gameState.getWorld().getCurrentTick() % 2 == 0){
                            foodHeading = getHeadingBetween(foods.get(0));
                            tempHeading = foodHeading;
                        } else {
                            foodHeading = bot.getCurrentHeading();
                            tempHeading = foodHeading;
                        }
                        botOutput = "eating than avoid gas";
                    }
                    playerAction.heading = UtilityFunctions.avoidGasCloud(bot, obstacleList.get(0), foodHeading); 
                } else if (bot.getTorpedoSalvoCount() > 0 && bot.getSize() > 100){
                    if (biggerPlayer.size() > 0) {
                        int heading = getHeadingBetween(biggerPlayer.get(0));
                        if(heading > centerHeading-60 && heading <centerHeading+60){
                            playerAction.action = PlayerActions.FIRETORPEDOES;
                            playerAction.heading = heading;
                            botOutput = "Firing torpedoes near border";
                        }
                        else{
                            botOutput = "Avoiding border";
                            playerAction.heading = centerHeading;
                        }
                        
                    } else if (smallerPlayerList.size() > 0) {
                        int heading = getHeadingBetween(smallerPlayerList.get(0));
                        if (heading > centerHeading - 60 && heading < centerHeading + 60){
                            playerAction.action = PlayerActions.FIRETORPEDOES;
                            playerAction.heading = heading;
                            botOutput = "Firing torpedoes to smaller players";
                        } else {
                            botOutput = "Avoiding border";
                            playerAction.heading = centerHeading;
                        }
                    }
                }
            } else if (bot.getTorpedoSalvoCount() > 0 && bot.getSize() > 100 || bot.getSupernovaAvailable() == 1 || supernovaFired) {
                var supernovaBomb = gameState.getGameObjects()
                    .stream().filter(item -> (item.getGameObjectType() == ObjectTypes.SUPERNOVABOMB))
                    .sorted(Comparator.comparing(item -> UtilityFunctions.getTrueDistance(bot, item)))
                    .collect(Collectors.toList());
                if (bot.getSupernovaAvailable() == 1) {
                    playerAction.action = PlayerActions.FIRESUPERNOVA;
                    supernovaFired = true;
                    if (biggerPlayer.size() > 0) {
                        botOutput = "firing supernove ke bigger";
                        playerAction.heading = getHeadingBetween(biggerPlayer.get(0));
                    } else if (smallerPlayerList.size() > 0) {
                        botOutput = "firing supernove ke smaller";
                        playerAction.heading = getHeadingBetween(smallerPlayerList.get(0));
                    }
                } else if (supernovaFired && getDistanceBetween(bot, supernovaBomb.get(0)) >= 400) {
                    playerAction.action = PlayerActions.DETONATESUPERNOVA;
                } else {
                    playerAction.action = PlayerActions.FIRETORPEDOES;
                    if (biggerPlayer.size() > 0) {
                        botOutput = "firing torpedo ke bigger";
                        playerAction.heading = getHeadingBetween(biggerPlayer.get(0));
                    } else if (smallerPlayerList.size() > 0) {
                        botOutput = "firing torpedo ke smaller";
                        playerAction.heading = getHeadingBetween(smallerPlayerList.get(0));
                    }
                }
            } else {
                GameObject target = null;
                botOutput = "Eating";
                playerAction.action = PlayerActions.FORWARD;
                if (foods.size() != 0) {
                    if (foods.size() > 1) {
                        if (Math.abs((int) Math.round(UtilityFunctions.getTrueDistance(bot, foods.get(0)) - UtilityFunctions.getTrueDistance(bot, foods.get(1)))) < 5){
                            if (foods.size() > 2 && Math.abs((int) Math.round(UtilityFunctions.getTrueDistance(bot, foods.get(2)) - UtilityFunctions.getTrueDistance(bot, foods.get(1)))) < 5) {
                                target = foods.get(3);
                                playerAction.heading = getHeadingBetween(foods.get(3));
                            } else {
                                target = foods.get(2);
                                playerAction.heading = getHeadingBetween(foods.get(2));
                            }
                        } else {
                            target = foods.get(0);
                            playerAction.heading = getHeadingBetween(foods.get(0));
                        }
                    } else {
                        target = foods.get(0);
                        playerAction.heading = getHeadingBetween(foods.get(0));
                    }
                }
                int obstaclesNear, enemiesNear;
                if (target == null) {
                    obstaclesNear = UtilityFunctions.countObstacleNear(bot, obstacleList, 75);
                    enemiesNear = UtilityFunctions.countEnemyNear(bot, biggerPlayer, 100);
                } else {
                    obstaclesNear = UtilityFunctions.countObstacleNear(bot, obstacleList, UtilityFunctions.getDistance(bot, target));
                    enemiesNear = UtilityFunctions.countEnemyNear(bot, biggerPlayer, UtilityFunctions.getDistance(bot, target));
                }
                if (enemiesNear > 0) {
                    avoidEnemy = UtilityFunctions.findResultant(bot, biggerPlayer, enemiesNear);
                    int finalHeading;
                    if (obstaclesNear > 0) {
                        botOutput = "avoid enemy dan gasCloud";
                        finalHeading = UtilityFunctions.avoidGasCloud(bot, obstacleList.get(0), avoidEnemy);
                    } else {
                        botOutput = "avoid enemy";
                        finalHeading = avoidEnemy;
                    }
                    playerAction.heading = finalHeading;
                    playerAction.action = PlayerActions.FORWARD;
                } else if (obstaclesNear > 0) {
                    botOutput = "Avoiding gasCloud";
                    int foodHeading = new Random().nextInt(360);
                    if (foods.size() != 0) {
                        target = foods.get(0);
                        if (gameState.getWorld().getCurrentTick() % 2 ==0){
                            foodHeading = getHeadingBetween(target);
                        } else {
                            foodHeading = bot.getCurrentHeading();
                        }
                        tempHeading = foodHeading;
                    }
                    playerAction.heading = UtilityFunctions.avoidGasCloud(bot, obstacleList.get(0), foodHeading);
                }

                if (teleporterList.size() > 0) {
                    for (int i = 0; i < teleporterList.size(); i++) {
                        if (UtilityFunctions.avoidTeleporter(bot, teleporterList.get(i))) {
                            if (fireTeleporter) {
                                if (teleporterLocked) {
                                    botOutput = "Teleporting";
                                    if (UtilityFunctions.getDistance(teleporter, bot) <= 20 || UtilityFunctions.getDistance(teleporterList.get(i), bot) <= 40) {
                                        playerAction.action = PlayerActions.TELEPORT;
                                        fireTeleporter = false;
                                        teleporterLocked = false;
                                    }
                                } else {
                                    botOutput = "Locking teleporter";
                                    teleporter = teleporterList.get(0);
                                }
                            } else {
                                if (bot.getTeleporterCount() > 0) {
                                    if (smallerPlayerList.size() > 0) {
                                        for (int j = smallerPlayerList.size() - 1; j >= 0; j--) {
                                            if (UtilityFunctions.targetIsSaveToTeleport(bot, smallerPlayerList.get(j), gameState)) {
                                                botOutput = "Teleporting to smaller player";
                                                playerAction.heading = getHeadingBetween(smallerPlayerList.get(j));
                                                playerAction.action = PlayerActions.FIRETELEPORT;
                                                fireTeleporter = true;
                                                break;
                                            }
                                        }
                                    } else if (foods.size() > 0) {
                                        for (int j = foods.size() - 1; j >= 0; j--) {
                                            if (UtilityFunctions.targetIsSaveToTeleport(bot, foods.get(j), gameState)) {
                                                botOutput = "Teleporting to ideal food";
                                                playerAction.heading = getHeadingBetween(foods.get(j));
                                                playerAction.action = PlayerActions.FIRETELEPORT;
                                                fireTeleporter = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                String aktifga = "ga aktif";
                if (torpedoList.size()>0){
                    if (UtilityFunctions.activateShield(bot, torpedoList.get(0)) && bot.getSize()>50){
                        playerAction.action = PlayerActions.ACTIVATESHIELD;
                        aktifga = "aktif";
                    }
                }

                System.out.println("Game Tick : " + gameState.getWorld().getCurrentTick());
                if (obstacleList.size()>0){
                    System.out.println("Jarak gas cloud terdekat :" + UtilityFunctions.getTrueDistance(bot, obstacleList.get(0)));
                }
                if (biggerPlayer.size()>0){
                    System.out.println("Jarak enemy terdekat :" + UtilityFunctions.getTrueDistance(bot, biggerPlayer.get(0)));
                }
                if (foods.size()>0){
                    System.out.println("Jarak Makanan terdekat :" + UtilityFunctions.getTrueDistance(bot, foods.get(0)) + ", " + foods.get(0));
                }
                if (foods.size()>1){
                    System.out.println("Makanan kedua terdekat :"+ UtilityFunctions.getTrueDistance(bot, foods.get(1)));
                }
                System.out.println("Heading : " + botOutput + " " + aktifga);
                System.out.println("tempHeading : " + tempHeading);
                System.out.println("current Heading :" + bot.getCurrentHeading());
                System.out.println("Size :" + bot.getSize());
                System.out.println("Jarak ke edge :" + UtilityFunctions.distanceFromEdge(bot, gameState) + 100);
                System.out.println("\n");
            }
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
