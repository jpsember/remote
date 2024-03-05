package remote;

import java.util.List;

import js.base.BaseObject;
import remote.gen.KeyPairEntry;
import static js.base.Tools.*;

public abstract class AbstractRemoteHandler extends BaseObject implements RemoteHandler {

  @Override
  public List<KeyPairEntry> keyPairList() {
    throw notSupported();
  }

  @Override
  public void importKeyPair(String name, String key) {
    throw notSupported();
  }

}
