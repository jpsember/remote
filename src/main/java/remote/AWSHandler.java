package remote;

import static js.base.Tools.*;

import java.util.List;
import java.util.Map;

import js.base.SystemCall;
import js.data.DataUtil;
import js.json.JSList;
import js.json.JSMap;
import js.webtools.gen.RemoteEntityInfo;
import remote.gen.KeyPairEntry;
import remote.gen.RemoteEntry;

/**
 * RemoteHandler that wraps the aws cli tool (/usr/local/bin/aws)
 */
public class AWSHandler extends RemoteHandler {

  // Viewing instances (in us-west-2a region):  
  //  https://us-west-2.console.aws.amazon.com/ec2/home?region=us-west-2#Instances:

  // The handler should use the aws ec2 CLI, invoked via "aws ec2"

  // https://docs.aws.amazon.com/AWSEC2/latest/APIReference/Using_Endpoints.html

  @Override
  protected final String supplyName() {
    return "aws";
  }

  private SystemCall ec2() {
    mSc = null;
    scMap = null;
    var sc = new SystemCall();
    sc.setVerbose(verbose());
    mSc = sc;
    sc().arg("/usr/local/bin/aws", "ec2");
    return sc();
  }

  private SystemCall sc() {
    if (mSc == null) {
      badState("no call to ec2!");
    }
    return mSc;
  }

  private SystemCall arg(Object... argumentObjects) {
    return sc().arg(argumentObjects);
  }

  private JSMap scOut() {
    if (scMap == null) {
      checkState(mSc != null);
      var sc = mSc;
      if (sc.exitCode() != 0) {
        alert("got exit code", sc.exitCode(), INDENT, sc.systemErr());
      }
      sc.assertSuccess();
      scMap = new JSMap(sc.systemOut());
      mSc = null;
    }
    return scMap;
  }

  @Override
  public void entityCreate(String entityLabel, String imageLabel) {
    // https://awscli.amazonaws.com/v2/documentation/api/latest/reference/ec2/run-instances.html
    ec2();
    arg("run-instances");
    arg("--image-id", imageLabel);
    arg("--instance-type", "t2.micro");
    arg("--count", 1);
    todo("ability to specify key name");
    arg("--key-name", "jeff");
    if (!alert("NOT storing user data"))
      arg("--user-data", entityLabel);
    //sc.arg("--dry-run");

    pr(scOut());
    todo("wait until instance has started");
    // Call entityList, and look for one with State:Name:"running"
  }

  @Override
  public Map<String, RemoteEntry> entityList() {

    if (mEntityMap == null) {

      // https://awscli.amazonaws.com/v2/documentation/api/latest/reference/ec2/describe-instances.html
      ec2();
      arg("describe-instances");

      Map<String, RemoteEntry> mp = hashMap();

      var inst = list();
      var res = scOut().getList("Reservations");
      for (var resElem : res.asMaps()) {
        inst = resElem.getList("Instances");

        for (var m : inst.asMaps()) {
          var ent = RemoteEntry.newBuilder();
          if (!alert("NOT storing hostInfo"))
            ent.hostInfo(m);

          var instanceId = m.get("InstanceId");
          var userData = getUserData(instanceId);
          ent.name(userData);
          if (nullOrEmpty(userData)) {
            alert("entity has no userData:", instanceId);
            continue;
          }
          ent.url(m.get("PublicIpAddress"));
          mp.put(ent.name(), ent.build());
        }
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

    // https://awscli.amazonaws.com/v2/documentation/api/latest/reference/ec2/describe-instance-status.html
    // https://awscli.amazonaws.com/v2/documentation/api/latest/reference/ec2/run-instances.html
    ec2();
    arg("describe-instance-status");
    var m = scOut();
    var lst = m.getList("InstanceStatuses");
    pr(lst);
    return lst;
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
  //
  //  private boolean verifyOk() {
  //    var errors = mErrors;
  //    if (!errors.isEmpty())
  //      badState("Errors in system call!", INDENT, errors);
  //    return true;
  //  }
  //
  //  private JSMap output() {
  //    return mSysCallOutput;
  //  }
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

  @Override
  public List<KeyPairEntry> keyPairList() {

    // https://docs.aws.amazon.com/cli/latest/reference/ec2/describe-key-pairs.html
    ec2();
    arg("describe-key-pairs");

    var jsonList = scOut().getList("KeyPairs");
    List<KeyPairEntry> lst = arrayList();
    for (var m : jsonList.asMaps()) {
      var ent = KeyPairEntry.newBuilder();
      ent.name(m.get("KeyName"));
      ent.hostInfo(m);
      lst.add(ent.build());
    }
    return lst;
  }

  @Override
  public void importKeyPair(String name, String key) {
    // https://awscli.amazonaws.com/v2/documentation/api/latest/reference/ec2/import-key-pair.html
    ec2();
    arg("import-key-pair");
    arg("--key-name", name);
    var keyBase64 = DataUtil.encodeBase64(key.getBytes());
    arg("--public-key-material", keyBase64);
    var result = scOut();
    pr(result);
  }

  private String getUserData(String instanceId) {
    // See https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/user-data.html#user-data-api-cli
    ec2();
    arg("describe-instance-attribute");
    arg("--instance-id", instanceId);
    arg("--attribute", "userData");
    var m = scOut().getMap("UserData");
    var result = m.opt("Value", "");
    if (nonEmpty(result))
      result = new String(DataUtil.parseBase64(result));
    return result;
  }

  private Map<String, String> entityIdToNameMap() {
    if (mEntityIdToNameMap == null) {

      // https://awscli.amazonaws.com/v2/documentation/api/latest/reference/ec2/describe-instances.html
      ec2();
      arg("describe-instances");

      Map<String, String> idToNameMap = hashMap();
      var res = scOut().getList("Reservations");
      for (var resElem : res.asMaps()) {
        for (var m : resElem.getList("Instances").asMaps()) {
          var instanceId = m.get("InstanceId");
          var entityName = getUserData(instanceId);
          if (nullOrEmpty(entityName)) {
            alert("instance has no userData:", instanceId);
            continue;
          }
          idToNameMap.put(instanceId, entityName);
        }
      }
      mEntityIdToNameMap = idToNameMap;
    }
    return mEntityIdToNameMap;
  }

  private Map<String, String> mEntityIdToNameMap;
  private Map<String, RemoteEntry> mEntityMap;
  private SystemCall mSc;
  private JSMap scMap;
}
