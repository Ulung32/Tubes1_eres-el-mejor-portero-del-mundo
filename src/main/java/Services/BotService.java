package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class BotService {
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;

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
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.FOOD || item.getGameObjectType() == ObjectTypes.SUPERFOOD)
                .sorted(Comparator.comparing(item -> UtilityFunctions.getTrueDistance(bot, item)))
                .collect(Collectors.toList());
            var smallerPlayer = gameState.getPlayerGameObjects()
                    .stream().filter(item -> item.getSize() < bot.getSize())
                    .sorted(Comparator.comparing(item -> UtilityFunctions.getTrueDistance(bot, item)))
                    .collect(Collectors.toList());
            var biggerPlayer = gameState.getPlayerGameObjects()
                    .stream().filter(item -> item.getSize() > bot.getSize());
            var obstacleList = gameState.getGameObjects()
                    .stream().filter(item -> item.getGameObjectType() == ObjectTypes.ASTEROIDFIELD);
            var objectsToAvoid = Stream.concat(biggerPlayer, obstacleList)
                    .sorted(Comparator.comparing(item -> UtilityFunctions.getTrueDistance(bot, item)))
                    .collect(Collectors.toList());

            if (UtilityFunctions.nearEdge(bot, gameState)) {
                int enemiesNear = UtilityFunctions.countEnemyNear(bot, objectsToAvoid), resultantHeading, sidingHeading;
                sidingHeading = UtilityFunctions.getHeadingToCenterPoint(bot, gameState) % 360;
                if (enemiesNear > 0) {
                    resultantHeading = UtilityFunctions.findResultant(bot, objectsToAvoid, enemiesNear);
                    resultantHeading = ((resultantHeading + sidingHeading) / 2) % 360;
                    playerAction.heading = resultantHeading;
                    if (UtilityFunctions.getTrueDistance(objectsToAvoid.get(0), bot) < 50 && bot.getSize() > 100) {
                        playerAction.action = PlayerActions.FORWARD;
                    } else if (bot.getSize() < 100) {
                        playerAction.action = PlayerActions.FORWARD;
                    } else {
                        playerAction.action = PlayerActions.FORWARD;
                    }
                } else {
                    resultantHeading = sidingHeading % 360;
                    playerAction.heading = resultantHeading;
                    playerAction.action = PlayerActions.FORWARD;
                }
            } else if (objectsToAvoid.size() > 0 && UtilityFunctions.getTrueDistance(objectsToAvoid.get(0), bot) < 50) {
                int enemiesNear = UtilityFunctions.countEnemyNear(bot, objectsToAvoid), resultantHeading;
                resultantHeading = UtilityFunctions.findResultant(bot, objectsToAvoid, enemiesNear);
                playerAction.heading = resultantHeading;
                if (UtilityFunctions.getTrueDistance(objectsToAvoid.get(0), bot) < 50 && bot.getSize() > 100) {
                    playerAction.action = PlayerActions.FORWARD;
                } else {
                    playerAction.action = PlayerActions.FORWARD;
                }
            } else {
                // GameObject target = null;
                // int targetDensity = 0;
                // for (int i = 0; i < edibleList.size(); i++) {
                //     GameObject currentTarget = edibleList.get(i);
                //     if (UtilityFunctions.getDensity(currentTarget, edibleList) > targetDensity) {
                //         if (UtilityFunctions.isSave(bot, objectsToAvoid, currentTarget)) {
                //             targetDensity = UtilityFunctions.getDensity(currentTarget, edibleList) / ((int) UtilityFunctions.getTrueDistance(bot, currentTarget)+1);
                //             target = currentTarget;
                //         }
                //     }
                // }
                // playerAction.heading = getHeadingBetween(target);
                // playerAction.action = PlayerActions.FORWARD;
                GameObject target;
                if (smallerPlayer.size() > 0) {
                    target = smallerPlayer.get(0);
                } else {
                    target = foodList.get(0);
                }
                playerAction.heading = getHeadingBetween(target);
                playerAction.action = PlayerActions.FORWARD;
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
