package remote;

import static js.base.Tools.*;

import js.app.App;

public class Remote extends App {

  public static void main(String[] args) {
    loadTools();
    Remote app = new Remote();
    app.startApplication(args);
    app.exitWithReturnCode();
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  protected void registerOperations() {
    registerOper(new RemoteOper());
  }

}
