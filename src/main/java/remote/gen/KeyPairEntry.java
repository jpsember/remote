package remote.gen;

import js.data.AbstractData;
import js.json.JSMap;

public class KeyPairEntry implements AbstractData {

  public String name() {
    return mName;
  }

  public JSMap hostInfo() {
    return mHostInfo;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "name";
  protected static final String _1 = "host_info";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mName);
    m.putUnsafe(_1, mHostInfo);
    return m;
  }

  @Override
  public KeyPairEntry build() {
    return this;
  }

  @Override
  public KeyPairEntry parse(Object obj) {
    return new KeyPairEntry((JSMap) obj);
  }

  private KeyPairEntry(JSMap m) {
    mName = m.opt(_0, "");
    {
      mHostInfo = JSMap.DEFAULT_INSTANCE;
      JSMap x = m.optJSMap(_1);
      if (x != null) {
        mHostInfo = x.lock();
      }
    }
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof KeyPairEntry))
      return false;
    KeyPairEntry other = (KeyPairEntry) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mName.equals(other.mName)))
      return false;
    if (!(mHostInfo.equals(other.mHostInfo)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mName.hashCode();
      r = r * 37 + mHostInfo.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected String mName;
  protected JSMap mHostInfo;
  protected int m__hashcode;

  public static final class Builder extends KeyPairEntry {

    private Builder(KeyPairEntry m) {
      mName = m.mName;
      mHostInfo = m.mHostInfo;
    }

    @Override
    public Builder toBuilder() {
      return this;
    }

    @Override
    public int hashCode() {
      m__hashcode = 0;
      return super.hashCode();
    }

    @Override
    public KeyPairEntry build() {
      KeyPairEntry r = new KeyPairEntry();
      r.mName = mName;
      r.mHostInfo = mHostInfo;
      return r;
    }

    public Builder name(String x) {
      mName = (x == null) ? "" : x;
      return this;
    }

    public Builder hostInfo(JSMap x) {
      mHostInfo = (x == null) ? JSMap.DEFAULT_INSTANCE : x;
      return this;
    }

  }

  public static final KeyPairEntry DEFAULT_INSTANCE = new KeyPairEntry();

  private KeyPairEntry() {
    mName = "";
    mHostInfo = JSMap.DEFAULT_INSTANCE;
  }

}
