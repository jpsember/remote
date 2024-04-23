package remote;

import static js.base.Tools.*;

import java.io.File;
import java.util.Map;

import js.webtools.RemoteManager;
import js.webtools.gen.RemoteEntityInfo;

public class UserHandler extends RemoteHandler {

  @Override
  protected final String supplyName() {
    return "user";
  }

  @Override
  public void entityUpdate(String label) {
    var ent = getUserEntity(label, true);
    var rec = ent.toBuilder();
    update(rec);
  }

  private void update(RemoteEntityInfo.Builder rec) {
    todo("we need to document the addtional args port, user, url, etc");
    var c = RemoteOper.SHARED_INSTANCE.cmdLineArgs();
    alert("my prefixes for alerts are not being parsed as expected");
    todo("!the semantics with cmd line args parsing is confusing...");
    if (c.hasNextArg()) {
      rec.port(c.nextArgIf("port", rec.port()));
      rec.user(c.nextArgIf("user", rec.user()));
      rec.url(c.nextArgIf("url", rec.url()));
      rec.projectDir(c.nextArgIf("project_dir", rec.projectDir()));
    }
    var b = rec.build();
    pr(b);
    var mgr = RemoteManager.SHARED_INSTANCE;
    mgr.infoEdit().userEntities().put(rec.label(), b);
  }

  @Override
  public void entityCreate(String label, String imageLabel) {
    checkArgument(nullOrEmpty(imageLabel), "images not supported");
    var ent = getUserEntity(label, false);

    if (ent != null)
      badState("entity already exists:", label, INDENT, ent);

    var b = RemoteEntityInfo.DEFAULT_INSTANCE.toBuilder();
    b.label(label).host(name()).port(22).projectDir(new File("/home/" + label));
    update(b);
  }

  @Override
  public Map<String, RemoteEntityInfo> entityList() {
    var mgr = RemoteManager.SHARED_INSTANCE;
    return mgr.info().userEntities();
  }

  @Override
  public void entityDelete(String label) {
    var ent = getUserEntity(label, false);
    if (ent == null) {
      return;
    }
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
