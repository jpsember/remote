package remote;

import static js.base.Tools.*;

import java.io.File;

import js.file.Files;
import remote.gen.RemoteEntry;

public final class RemoteUtils {

  public static void createSSHScript(RemoteEntry ent) {
    checkArgument(nonEmpty(ent.user()), "no user:", INDENT, ent);
    StringBuilder sb = new StringBuilder();
    sb.append("#!/usr/bin/env bash\n");
    sb.append("echo \"Connecting to: ");
    sb.append(ent.name());
    sb.append("\"\n");
    sb.append("ssh ");
    sb.append(ent.user());
    sb.append("@");
    sb.append(ent.url());
    sb.append(" -oStrictHostKeyChecking=no");
    sb.append(" $@");
    sb.append('\n');
    File f = new File(Files.binDirectory(), "sshe");
    var fl = Files.S;
    fl.writeString(f, sb.toString());
    fl.chmod(f, 755);
  }

}
