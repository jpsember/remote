package remote;

import java.util.List;
import java.util.Map;

import js.base.BaseObject;
import js.json.JSList;
import js.webtools.gen.RemoteEntityInfo;
import remote.gen.KeyPairEntry;
import static js.base.Tools.*;

public abstract class RemoteHandler extends BaseObject {

  /**
   * The name of the handler (e.g. aws, linode)
   */
  @Override
  protected String supplyName() {
    throw notSupported();
  }

  /**
   * Create a remote entity
   * 
   * @param entityName
   *          a unique name to distinguish this entity
   * @param imageName
   *          if non empty, the name of an image to create it with
   */
  public void entityCreate(String entityName, String imageName) {
    throw notSupported();
  }

  public Map<String, RemoteEntityInfo> entityList() {
    throw notSupported();
  }

  public void entityDelete(String name) {
    throw notSupported();
  }

  public RemoteEntityInfo entitySelect(String name) {
    throw notSupported();
  }

  public void imageCreate(String name) {
    throw notSupported();
  }

  public void imageDelete(String name) {
    throw notSupported();
  }

  public JSList imageList() {
    throw notSupported();
  }

  public List<KeyPairEntry> keyPairList() {
    throw notSupported();
  }

  public void importKeyPair(String name, String key) {
    throw notSupported();
  }

}
