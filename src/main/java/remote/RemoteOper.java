package remote;

import static js.base.Tools.*;

import java.util.Map;

import js.app.AppOper;
import js.app.CmdLineArgs;
import js.base.BaseObject;
import js.base.BasePrinter;
import js.webtools.RemoteManager;
import remote.gen.RemoteConfig;

public class RemoteOper extends AppOper {

  public static final RemoteOper SHARED_INSTANCE = new RemoteOper();

  private RemoteOper() {
  }

  @Override
  public String userCommand() {
    return "";
  }

  @Override
  protected String shortHelp() {
    return "manage remote entities, e.g. linode, aws, or Raspberry Pi";
  }

  @Override
  protected void longHelp(BasePrinter b) {
    b.pr("remote [user | aws | linode] <cmds>*");
    b.br();
    b.pr("cmds is one or more of:").br();
    b.pr("list                   -- list entities");
    b.pr("details                -- list entities with full detail");
    b.pr("select <label>         -- select entity to be 'active'");
    b.pr("create <label>\n" + "   [image <imglabel>]  -- create entity");
    b.pr("update <label>         -- update entity");
    b.pr("delete <label>         -- delete entity");
    b.pr("createimage <imglabel> -- create image from current entity");
    b.pr("images                 -- list images");

  }

  @Override
  public RemoteConfig defaultArgs() {
    return RemoteConfig.DEFAULT_INSTANCE;
  }

  @Override
  public RemoteConfig config() {
    if (mConfig == null) {
      mConfig = (RemoteConfig) super.config();
    }
    return mConfig;
  }

  @Override
  public void perform() {
    var mgr = RemoteManager.SHARED_INSTANCE;

    var a = cmdLineArgs();
    while (a.hasNextArg()) {
      var cmd = a.nextArg();

      var c = sHandlerMap.get(cmd);
      if (c != null) {
        setHandler(cmd);
        continue;
      }

      switch (cmd) {
      default:
        throw setError("Unrecognized command:", cmd);
      case "create": {
        var entityLabel = parseLabel(a);
        String imageLabel = null;
        if (a.nextArgIf("image")) {
          imageLabel = parseLabel(a);
        }
        if (handler().entityList().containsKey(entityLabel))
          setError("entity already exists:", entityLabel);
        handler().entityCreate(entityLabel, imageLabel);
        handler().entitySelect(entityLabel);
      }
        break;
      case "update":
        handler().entityUpdate(parseLabel(a));
        break;
      case "list":
        showList(false);
        break;
      case "details":
        showList(true);
        break;
      case "delete": {
        var label = parseLabel(a);
        handler().entityDelete(label);
        if (mgr.info().activeEntity() != null && mgr.info().activeEntity().label().equals(label))
          mgr.infoEdit().activeEntity(null);
      }
        break;
      case "select": {
        var label = parseLabel(a);
        var info = handler().entitySelect(label);
        mgr.infoEdit().activeEntity(info);
      }
        break;
      case "createimage": {
        var imageLabel = parseLabel(a);
        handler().imageCreate(imageLabel);
      }
        break;
      case "images":
        pr(handler().imageList());
        break;
      }
    }
    mgr.flush();
  }

  private void showList(boolean detailed) {
    var mp = handler().entityList();
    var jsonOut = map();
    for (var ent : mp.values()) {
      var entJson = ent.toJson();
      if (!detailed)
        entJson.remove("host_info");
      jsonOut.put(ent.label(), entJson);
    }
    pr(jsonOut);
  }

  private String parseLabel(CmdLineArgs a) {
    var label = a.nextArg("");
    if (label.isEmpty())
      setError("please provide a label");
    return label;
  }

  private void setHandler(String name) {
    var h = sHandlerMap.get(name);
    if (h == null)
      setError("no registered handler:", name);
    var mgr = RemoteManager.SHARED_INSTANCE;
    var b = mgr.infoEdit();
    b.activeHandlerName(name);
    // If active entity belongs to a different handler, remove it
    if (!mgr.activeEntity().host().equals(name))
      b.activeEntity(null);
    mgr.flush();
  }

  private RemoteHandler handler() {
    var mgr = RemoteManager.SHARED_INSTANCE;
    var name = mgr.info().activeHandlerName();
    if (name.isEmpty())
      setError("no active remote handler defined");
    var h = sHandlerMap.get(name);
    if (h == null)
      setError("no handler found for name:", name);
    if (verbose() && h instanceof BaseObject) {
      ((BaseObject) h).setVerbose();
    }
    return h;
  }

  private RemoteConfig mConfig;
  private static Map<String, RemoteHandler> sHandlerMap = hashMap();

  public static void registerHandler(RemoteHandler handler) {
    sHandlerMap.put(handler.name(), handler);
  }

  static {
    RemoteOper.registerHandler(new UserHandler());
    RemoteOper.registerHandler(new LinodeHandler());
    RemoteOper.registerHandler(new AWSHandler());
  }

}
