package remote.gen;

import js.data.AbstractData;
import js.json.JSMap;

public class RemoteEntry implements AbstractData {

  public String name() {
    return mName;
  }

  public String url() {
    return mUrl;
  }

  public JSMap hostInfo() {
    return mHostInfo;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "name";
  protected static final String _1 = "url";
  protected static final String _2 = "host_info";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mName);
    m.putUnsafe(_1, mUrl);
    m.putUnsafe(_2, mHostInfo);
    return m;
  }

  @Override
  public RemoteEntry build() {
    return this;
  }

  @Override
  public RemoteEntry parse(Object obj) {
    return new RemoteEntry((JSMap) obj);
  }

  private RemoteEntry(JSMap m) {
    mName = m.opt(_0, "");
    mUrl = m.opt(_1, "");
    {
      mHostInfo = JSMap.DEFAULT_INSTANCE;
      JSMap x = m.optJSMap(_2);
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
    if (object == null || !(object instanceof RemoteEntry))
      return false;
    RemoteEntry other = (RemoteEntry) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mName.equals(other.mName)))
      return false;
    if (!(mUrl.equals(other.mUrl)))
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
      r = r * 37 + mUrl.hashCode();
      r = r * 37 + mHostInfo.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected String mName;
  protected String mUrl;
  protected JSMap mHostInfo;
  protected int m__hashcode;

  public static final class Builder extends RemoteEntry {

    private Builder(RemoteEntry m) {
      mName = m.mName;
      mUrl = m.mUrl;
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
    public RemoteEntry build() {
      RemoteEntry r = new RemoteEntry();
      r.mName = mName;
      r.mUrl = mUrl;
      r.mHostInfo = mHostInfo;
      return r;
    }

    public Builder name(String x) {
      mName = (x == null) ? "" : x;
      return this;
    }

    public Builder url(String x) {
      mUrl = (x == null) ? "" : x;
      return this;
    }

    public Builder hostInfo(JSMap x) {
      mHostInfo = (x == null) ? JSMap.DEFAULT_INSTANCE : x;
      return this;
    }

  }

  public static final RemoteEntry DEFAULT_INSTANCE = new RemoteEntry();

  private RemoteEntry() {
    mName = "";
    mUrl = "";
    mHostInfo = JSMap.DEFAULT_INSTANCE;
  }

}
