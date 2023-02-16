package Models;

import Enums.*;
import java.util.*;

public class GameObject {
  public UUID id;
  public Integer size;
  public Integer speed;
  public Integer currentHeading;
  public Position position;
  public ObjectTypes gameObjectType;
  public Integer effects;
  public Integer torpedoSalvoCount;
  public Integer supernovaAvailable;
  public Integer teleporterCount;
  public Integer shieldCount;

  public GameObject(UUID id, Integer size, Integer speed, Integer currentHeading, Position position, ObjectTypes gameObjectType, Integer effects, Integer torpedoSalvoCount, Integer supernovaAvailable, Integer teleporterCount, Integer shieldCount) {
    this.id = id;
    this.size = size;
    this.speed = speed;
    this.currentHeading = currentHeading;
    this.position = position;
    this.gameObjectType = gameObjectType;
    this.effects = effects;
    this.torpedoSalvoCount = torpedoSalvoCount;
    this.supernovaAvailable = supernovaAvailable;
    this.teleporterCount = teleporterCount;
    this.shieldCount = shieldCount;
  }

  public UUID getId() {
    return id;
  }

  public int getHeading(){
    return this.currentHeading;
  }
  
  public void setId(UUID id) {
    this.id = id;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public int getSpeed() {
    return speed;
  }

  public void setSpeed(int speed) {
    this.speed = speed;
  }

  public int getCurrentHeading() {
    return currentHeading;
  }

  public void setHeading(int Angle){
    this.currentHeading = Angle;
  }

  public Position getPosition() {
    return position;
  }

  public void setPosition(Position position) {
    this.position = position;
  }

  public ObjectTypes getGameObjectType() {
    return gameObjectType;
  }
  
  public void setGameObjectType(ObjectTypes gameObjectType) {
    this.gameObjectType = gameObjectType;
  }
  
  public Integer getTorpedoSalvoCount() {
    return this.torpedoSalvoCount;
  }

  public void setTorpedoSalvoCount(Integer torpedoSalvoCount) {
    this.torpedoSalvoCount = torpedoSalvoCount;
  }
  
  public Integer getSupernovaAvailable() {
    return this.supernovaAvailable;
  }
  
  public void setSupernovaAvailable(Integer supernovaAvailable) {
    this.supernovaAvailable = supernovaAvailable;
  }
  
  public Integer getTeleporterCount() {
    return this.teleporterCount;
  }

  public void setTeleporterCount(Integer teleporterCount) {
    this.teleporterCount = teleporterCount;
  }
  
  public Integer getShieldCount() {
    return this.shieldCount;
  }
  
  public void getShieldCount(Integer shieldCount) {
    this.shieldCount = shieldCount;
  }

  public static GameObject FromStateList(UUID id, List<Integer> stateList)
  {
    Position position = new Position(stateList.get(4), stateList.get(5));
    if (stateList.get(3) == 1) {
      return new GameObject(id, stateList.get(0), stateList.get(1), stateList.get(2), position, ObjectTypes.valueOf(stateList.get(3)), stateList.get(6), stateList.get(7), stateList.get(8), stateList.get(9), stateList.get(10));
    } else {
      return new GameObject(id, stateList.get(0), stateList.get(1), stateList.get(2), position, ObjectTypes.valueOf(stateList.get(3)), 0, 0, 0, 0, 0);
    }
  }
}
