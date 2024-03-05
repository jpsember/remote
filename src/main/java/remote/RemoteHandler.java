package remote;

import js.json.JSList;
import js.json.JSMap;
import js.webtools.gen.RemoteEntityInfo;

public interface RemoteHandler {

  /**
   * The name of the handler (e.g. aws, linode)
   */
  String name();

  /**
   * Create a remote entity
   * 
   * @param entityName
   *          a unique name to distinguish this entity
   * @param imageName
   *          if non empty, the name of an image to create it with
   */
  void entityCreate(String entityName, String imageName);

  
  JSMap entityList();

  JSMap entityListDetailed();

  void entityDelete(String name);

  RemoteEntityInfo entitySelect(String name);

  void imageCreate(String name);

  void imageDelete(String name);

  JSList imageList();

}
