package remote;

import static js.base.Tools.*;

import java.util.Map;

import js.base.BaseObject;
import js.base.SystemCall;
import js.json.JSList;
import js.json.JSMap;
import js.webtools.gen.RemoteEntityInfo;
import remote.gen.RemoteEntry;

public class AWSHandler extends BaseObject implements RemoteHandler {

  // The handler should use the aws ec2 CLI, invoked via "aws ec2"

  // https://docs.aws.amazon.com/AWSEC2/latest/APIReference/Using_Endpoints.html

  @Override
  protected String supplyName() {
    return "aws";
  }

  @Override
  public void entityCreate(String entityLabel, String imageLabel) {

    var sc = new SystemCall();
    sc.setVerbose(verbose());
    sc.arg("/usr/local/bin/aws", "ec2");

    // https://awscli.amazonaws.com/v2/documentation/api/latest/reference/ec2/run-instances.html
    sc.arg("run-instances");
    sc.arg("--image-id", "ami-08f7912c15ca96832");
    sc.arg("--instance-type", "t2.micro");
    sc.arg("--count", 1);
    todo("we need to be able to define a key pair");
    sc.arg("--key-name", "jeff");
    sc.arg("--user-data", entityLabel);

    sc.arg("--dry-run");

    pr(sc.systemOut());
    if (sc.exitCode() != 0) {
      alert("got exit code", sc.exitCode(), INDENT, sc.systemErr());
    }

    throw notFinished();
  }

  public JSList describeKeyPairs() {
    // https://docs.aws.amazon.com/cli/latest/reference/ec2/describe-key-pairs.html
    var sc = systemCall();
    sc.arg("/usr/local/bin/aws", "ec2");
    sc.arg("describe-key-pairs");
    sc.assertSuccess();
    return new JSMap(sc.systemOut()).getList("KeyPairs");
  }

  private SystemCall systemCall() {
    var sc = new SystemCall();
    sc.setVerbose(verbose());
    return sc;
  }

  @Override
  public Map<String, RemoteEntry> entityList() {

    if (mEntityMap == null) {

      // https://awscli.amazonaws.com/v2/documentation/api/latest/reference/ec2/describe-instances.html
      var sc = systemCall();
      sc.arg("/usr/local/bin/aws", "ec2");
      sc.arg("describe-instances");

      // sc.arg("--dry-run");

      pr(sc.systemOut());
      if (sc.exitCode() != 0) {
        alert("got exit code", sc.exitCode(), INDENT, sc.systemErr());
      }
      var res = new JSMap(sc.systemOut()).getList("Reservations");
      checkArgument(res.size() == 1);
      var inst = res.getMap(0).getList("Instances");

      Map<String, RemoteEntry> mp = hashMap();
      for (var m : inst.asMaps()) {
        var ent = RemoteEntry.newBuilder();
        ent.hostInfo(m);

        // Look for user data
        {
          var um = m.optJSMap("UserData");

        }
        if (ent.name().isEmpty()) {
          alert("no name found for instance:", INDENT, m);
          continue;
        }
        mp.put(ent.name(), ent.build());
      }
      mEntityMap = mp;
    }
    return mEntityMap;
  }

  @Override
  public void entityDelete(String label) {
    throw notFinished();
  }

  @Override
  public RemoteEntityInfo entitySelect(String label) {
    throw notFinished();
  }

  @Override
  public void imageCreate(String imageLabel) {
    throw notFinished();
  }

  @Override
  public void imageDelete(String name) {
    throw notFinished();
  }

  @Override
  public JSList imageList() {
    throw notFinished();
  }
  //
  //  private JSMap callLinode(String action, String endpoint) {
  //    return callAWS(action, endpoint, null);
  //  }
  //
  //  private JSMap callAWS(String action, String endpoint, JSMap m) {
  //
  //    mErrors = null;
  //    mSysCallOutput = null;
  //
  //    var sc = new SystemCall();
  //    sc.setVerbose(verbose());
  //
  //    sc.arg("curl", //
  //        "-s", // suppress progress bar
  //        "-H", "Content-Type: application/json", //
  //        "-H", "Authorization: Bearer " + config().accessToken());
  //    sc.arg("-X", action);
  //    if (m != null)
  //      sc.arg("-d", m);
  //
  //    sc.arg("https://api.linode.com/v4/" + endpoint);
  //
  //    if (sc.exitCode() != 0) {
  //      alert("got exit code", sc.exitCode(), INDENT, sc.systemErr());
  //    }
  //
  //    sc.assertSuccess();
  //
  //    var out = new JSMap(sc.systemOut());
  //    mSysCallOutput = out;
  //    log("system call output:", INDENT, out);
  //
  //    mErrors = out.optJSListOrEmpty("errors");
  //    if (!mErrors.isEmpty())
  //      log("Errors occurred in the system call:", INDENT, out);
  //    return output();
  //  }

  private boolean verifyOk() {
    var errors = mErrors;
    if (!errors.isEmpty())
      badState("Errors in system call!", INDENT, errors);
    return true;
  }

  private JSMap output() {
    return mSysCallOutput;
  }
  //
  //  private RemoteEntry getLinodeInfo(String label, boolean mustExist) {
  //    var m = getLinode(label);
  //    if (m == null) {
  //      if (mustExist)
  //        badArg("label not found:", label);
  //      return null;
  //    }
  //    return m;
  //  }

  //  private RemoteEntry getLinode(String label) {
  //    return labelToIdMap().get(label);
  //  }

  //  private Map<String, RemoteEntry> labelToIdMap() {
  //    if (mEntityMap == null) {
  //      // https://www.linode.com/docs/api/linode-instances/#linodes-list
  //      var mout = callLinode("GET", "linode/instances");
  //      verifyOk();
  //      var mp = new HashMap<String, RemoteEntry>();
  //      var nodes = mout.getList("data");
  //      for (var m2 : nodes.asMaps()) {
  //        var ent = RemoteEntry.newBuilder();
  //        ent.hostInfo(m2);
  //        var ipv4 = m2.getList("ipv4");
  //        if (ipv4.size() != 1)
  //          badArg("unexpected ipv4:", ipv4, INDENT, m2);
  //        ent.name(m2.get("label")) //
  //            .url(ipv4.getString(0)) //
  //        ;
  //        mp.put(ent.name(), ent.build());
  //      }
  //      mEntityMap = mp;
  //    }
  //    return mEntityMap;
  //  }

  private Map<String, RemoteEntry> mEntityMap;
  private JSList mErrors;
  private JSMap mSysCallOutput;

}
