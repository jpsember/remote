package remote;

import static js.base.Tools.*;

import java.util.Map;

import js.app.AppOper;
import js.app.CmdLineArgs;
import js.base.BaseObject;
import js.base.BasePrinter;
import js.webtools.RemoteManager;

public class RemoteOper extends AppOper {

  public static final RemoteOper SHARED_INSTANCE = new RemoteOper();

  private RemoteOper() {
  }

  @Override
  public String userCommand() {
    return "";
  }

  @Override
  public String getHelpDescription() {
    var b = new BasePrinter();
    b.pr("manage remote entities, e.g. linode or AWS");
    b.pr();
    b.pr("list                   -- list entities");
    b.pr("details                -- list entities with full detail");
    b.pr("select <label>         -- select entity to be 'active'");
    b.pr("create <label>\n" + "   [image <imglabel>]  -- create entity");
    b.pr("delete <label>         -- delete entity");
    b.pr("createimage <imglabel> -- create image from current entity");
    b.pr("images                 -- list images");
    return b.toString();
  }

  @Override
  public void perform() {
    var mgr = RemoteManager.SHARED_INSTANCE;

    if (false && alert("experiment")) {

      todo("Support selecting an active remote");

      setHandler("aws");
      var h = handler();

      var instId = "camel";

      if (true) {
        pr("creating", instId);
        h.entityCreate(instId, "ami-08f7912c15ca96832");
      }

      if (false) {
        pr("deleting", instId);
        h.entityDelete(instId);
      }

      if (false) {
        var pubKey = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDIdpbQF+FOu0s6OBT0IivYKyjszCPSN9is5IVpBQ6CHX4ZVYeYowhVxLsnwB4RWj/t8sEsQTGqD9V0NscdGongOST6344RcVmAuRYPaOUY9LqqKQnojrYtCWGfMDAmjadtUJqfpxhs2GwFgSS4u9CsATjAhoso5gpk4RdBTJghck1qLGMFeEg0pTUpOJ6Rq8NEjmlLrLVHi1obgLhuZANgqJcNhrfWiUPKQXoMXNXWJDkqMdONxphJe7Fv/y6GRI2tYktElKS68XQ7QVA+/PpNDUcW5KNHS9uf1Az7jb8PuWYzjn6rPpF4O8fnbdxfsK2X1HXN9vn+I8XNS0Mkq/cL Jeff's primary public RSA key";
        h.importKeyPair("jeff", pubKey);
      }

      pr("entities:");
      for (var ent : h.entityList().entrySet()) {
        pr(ent.getKey(), ":", INDENT, ent.getValue());
      }

      //  pr("key pairs:", INDENT, h.keyPairList());
      return;
    }

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
        mgr.flush();
      }
        break;
      case "select": {
        var label = parseLabel(a);
        var info = handler().entitySelect(label);
        mgr.infoEdit().activeEntity(info);
        mgr.flush();
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
    var b = 
    mgr.infoEdit();
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

  private static Map<String, RemoteHandler> sHandlerMap = hashMap();

  public static void registerHandler(RemoteHandler handler) {
    sHandlerMap.put(handler.name(), handler);
  }

  static {
    RemoteOper.registerHandler(new LinodeHandler());
    RemoteOper.registerHandler(new AWSHandler());
  }

}
