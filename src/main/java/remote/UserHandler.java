package remote;

import static js.base.Tools.*;

import java.util.Map;

import js.webtools.RemoteManager;
import js.webtools.gen.RemoteEntityInfo;

public class UserHandler extends RemoteHandler {

  @Override
  protected final String supplyName() {
    return "user";
  }

  @Override
  public void entityCreate(String label, String imageLabel) {
    checkArgument(nullOrEmpty(imageLabel), "images not supported");
    var ent = getUserEntity(label, false);

    // We allow create to be called repeatedly on an existing one to modify its parameters
    //    if (ent != null)
    //      badState("entity already exists:", label, INDENT, ent);
    if (ent == null) {
      ent = RemoteEntityInfo.DEFAULT_INSTANCE.toBuilder().label(label).host(name()).port(22);
    }
    var rec = ent.toBuilder();

    //    rec.label(label);
    //    rec.host(name());

    //rec.port(22);

    var c = RemoteOper.SHARED_INSTANCE.cmdLineArgs();
    while (c.hasNextArg()) {
      pr("next arg:", c.peekNextArg(), "rec:", INDENT, rec);
      if (c.nextArgIf("port")) {
        rec.port(Integer.parseInt(c.nextArg()));
        continue;
      }
      if (c.nextArgIf("user")) {
        rec.user(c.nextArg());
        continue;
      }
      if (c.nextArgIf("url")) {
        rec.url(c.nextArg());
        continue;
      }
      break;
    }
    var b = rec.build();
    pr(b);

    var mgr = RemoteManager.SHARED_INSTANCE;
    mgr.infoEdit().userEntities().put(label, b);
    mgr.flush();
  }

  @Override
  public Map<String, RemoteEntityInfo> entityList() {
    var mgr = RemoteManager.SHARED_INSTANCE;
    return mgr.info().userEntities();
  }

  @Override
  public void entityDelete(String label) {
    var ent = getUserEntity(label, true);
    log("deleting:", label, INDENT, ent);
    var mgr = RemoteManager.SHARED_INSTANCE;
    mgr.infoEdit().userEntities().remove(label);
    mgr.flush();
  }

  @Override
  public RemoteEntityInfo entitySelect(String label) {
    var ent = getUserEntity(label, true);
    var mgr = RemoteManager.SHARED_INSTANCE;
    mgr.infoEdit().activeEntity(ent);
    mgr.createSSHScript();
    return ent;
  }

  private RemoteEntityInfo getUserEntity(String label, boolean mustExist) {
    var info = entityList().get(label);
    if (mustExist && info == null)
      badArg("No user entity found with label:", label);
    return info;
  }

}
