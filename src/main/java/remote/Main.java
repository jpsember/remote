package remote;

import static js.base.Tools.*;

import js.app.App;

public class Main extends App {

  public static void main(String[] args) {
    loadTools();
    if (false && alert("using experimental args"))
      args = "details".split(" ");
    Main app = new Main();
    app.startApplication(args);
    app.exitWithReturnCode();
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  protected void registerOperations() {
    registerOper(RemoteOper.SHARED_INSTANCE);
  }

}
