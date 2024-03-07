package remote;

import static js.base.Tools.*;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import js.base.DateTimeTools;
import js.base.SystemCall;
import js.data.DataUtil;
import js.json.JSMap;
import js.webtools.RemoteManager;
import js.webtools.gen.RemoteEntityInfo;
import remote.gen.KeyPairEntry;

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

  @Override
  public void entityCreate(String entityName, String imageLabel) {
    {
      var ent = entityWithName(entityName);
      if (ent != null)
        throw badArg("attempt to create entity with name that already exists:", entityName, INDENT, ent);
    }
    // https://awscli.amazonaws.com/v2/documentation/api/latest/reference/ec2/run-instances.html
    ec2();
    arg("run-instances");
    arg("--image-id", imageLabel);
    arg("--instance-type", "t2.micro");
    arg("--count", 1);
    todo("ability to specify key name");
    arg("--key-name", "jeff");
    //if (!alert("NOT storing user data"))
    arg("--user-data", entityName);
    //sc.arg("--dry-run");

    pr(scOut());
    String instanceId = "";
    var instList = scOut().getList("Instances");
    for (var m : instList.asMaps()) {
      instanceId = m.opt("InstanceId", "");
      if (nonEmpty(instanceId))
        break;
    }
    checkState(nonEmpty(instanceId), "no InstanceId returned");
    var startTime = 0L;
    //var prevState = "";
    while (true) {
      long curr = System.currentTimeMillis();
      if (startTime == 0)
        startTime = curr;
      else {
        if (curr - startTime > DateTimeTools.MINUTES(5))
          throw badState("instance is not 'ready' even after a few minutes!");
        DateTimeTools.sleepForRealMs(15000);
      }
      var mp = getNewIdToEntryMap();
      var ent = mp.get(instanceId);
      String state = "";
      if (ent != null) {
        state = entityState(ent);
      } else
        throw badState("no entry for instance:", instanceId, "found in map");
      // if (!state.equals(prevState))
      pr("...elapsed time:", DateTimeTools.humanDuration(curr - startTime), "state:", state);
      if (state.equals("running"))
        break;
    }
  }

  @Override
  public Map<String, RemoteEntityInfo> entityList() {
    Map<String, RemoteEntityInfo> out = new TreeMap<String, RemoteEntityInfo>();
    for (var val : entityIdToNameMap().values()) {
      // Entities that have deleted may hang around for an hour or so; their states
      // will be "terminated".  Omit these from the results.  Also omit any that
      // have an empty state (which is unexpected).
      //
      var s = entityState(val);
      if (nullOrEmpty(s) || s.equals("terminated"))
        continue;
      out.put(val.label(), val);
    }
    return out;
  }

  @Override
  public void entityDelete(String name) {
    var entry = entityWithName(name);
    if (entry == null) {
      throw badArg("no entity found with name:", name);
    }

    var instanceId = instanceId(entry);
    // https://awscli.amazonaws.com/v2/documentation/api/latest/reference/ec2/terminate-instances.html
    ec2();
    arg("terminate-instances");
    arg("--instance-ids", instanceId);
    pr(scOut());
    entityIdToNameMap().remove(instanceId);
  }

  @Override
  public RemoteEntityInfo entitySelect(String name) {
    var ent = entityWithName(name);
    checkState(ent != null, "no entity found with name:", name);

    todo("Is RemoteEntityInfo extraneous?  Vs RemoteInfo?");
    var b = RemoteEntityInfo.newBuilder();
    b.label(name) //
        .url(ent.url()) //
        .user("root") //
        .projectDir(new File("/root"));
    ;
    var mgr = RemoteManager.SHARED_INSTANCE;
    mgr.infoEdit().activeEntity(b);
    mgr.createSSHScript();
    return b;
  }

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

  private String entityState(RemoteEntityInfo entry) {
    return entry.hostInfo().optJSMapOrEmpty("State").opt("Name", "");
  }

  /**
   * Determine which, if any, entity has a particular name
   */
  private RemoteEntityInfo entityWithName(String name) {
    for (var ent : entityIdToNameMap().values()) {
      if (ent.label().equals(name))
        return ent;
    }
    return null;
  }

  /**
   * Determine the InstanceId for an instance
   */
  private String instanceId(RemoteEntityInfo ent) {
    var id = ent.hostInfo().opt("InstanceId", "");
    if (nullOrEmpty(id))
      badArg("entry has no InstanceId:", INDENT, ent);
    return id;
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

  private Map<String, RemoteEntityInfo> entityIdToNameMap() {
    if (mIdToEntityMap == null)
      mIdToEntityMap = getNewIdToEntryMap();
    return mIdToEntityMap;
  }

  private Map<String, RemoteEntityInfo> getNewIdToEntryMap() {
    // https://awscli.amazonaws.com/v2/documentation/api/latest/reference/ec2/describe-instances.html
    ec2();
    arg("describe-instances");

    Map<String, RemoteEntityInfo> idToEntMap = hashMap();
    var res = scOut().getList("Reservations");
    for (var resElem : res.asMaps()) {
      for (var m : resElem.getList("Instances").asMaps()) {
        var instanceId = m.get("InstanceId");
        var entityName = getUserData(instanceId);
        if (nullOrEmpty(entityName)) {
          alert("instance has no userData:", instanceId);
          continue;
        }
        var b = RemoteEntityInfo.newBuilder();
        b.label(entityName);
        b.hostInfo(m);
        var state = entityState(b);
        if (nullOrEmpty(state))
          continue;
        var ipAddr = m.opt("PublicIpAddress", "");
        b.url(ipAddr);
        idToEntMap.put(instanceId, b.build());
      }
    }
    return idToEntMap;
  }

  /**
   * Prepare to make a SystemCall to "aws ec2"
   */
  private SystemCall ec2() {
    mSystemCall = null;
    mSystemCallJsonResults = null;
    var sc = new SystemCall();
    sc.setVerbose(verbose());
    mSystemCall = sc;
    sc().arg("/usr/local/bin/aws", "ec2");
    return sc();
  }

  private SystemCall sc() {
    if (mSystemCall == null)
      badState("no call to ec2!");
    return mSystemCall;
  }

  private SystemCall arg(Object... argumentObjects) {
    return sc().arg(argumentObjects);
  }

  private JSMap scOut() {
    if (mSystemCallJsonResults == null) {
      checkState(mSystemCall != null);
      var sc = mSystemCall;
      if (sc.exitCode() != 0) {
        alert("got exit code", sc.exitCode(), INDENT, sc.systemErr());
      }
      sc.assertSuccess();
      mSystemCallJsonResults = new JSMap(sc.systemOut());
      mSystemCall = null;
    }
    return mSystemCallJsonResults;
  }

  private Map<String, RemoteEntityInfo> mIdToEntityMap;
  private SystemCall mSystemCall;
  private JSMap mSystemCallJsonResults;
}
